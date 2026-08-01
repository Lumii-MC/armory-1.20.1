#version 150

#moj_import <lodestone:common_math.glsl>

uniform sampler2D DiffuseSampler;
uniform sampler2D MainDepthSampler;

uniform samplerBuffer DataBuffer;
uniform int InstanceCount;
uniform float time;

// stuff for world-space calculation evil magic
uniform mat4 invProjMat;
uniform mat4 invViewMat;
uniform mat4 ViewMat;
uniform mat4 ProjMat;
uniform vec3 cameraPos;
uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;

#define STEPS 20
#define SURFACE_FADE 6.75
#define FOG_DENSITY 1.95
#define VOLUME_STEP_SIZE 0.25
#define MAX_LIFETIME 5.5

// Once again, Inigo Quilez is the goat
// https://iquilezles.org/articles/smin/
float smin( float a, float b, float k )
{
    k *= 1.0;
    float r = exp2(-a/k) + exp2(-b/k);
    return -k*log2(r);
}

// https://iquilezles.org/articles/sdfxor/
float opXor(float a, float b) {
    return max(min(a, b), -max(a, b));
}

float opSub(float a, float b) {
    return max(-b, a);
}

float sdCylinder( vec3 p, float r, float h )
{
    vec2 d = abs(vec2(length(p.xz),p.y)) - vec2(r,h);
    return min(max(d.x,d.y),0.0) + length(max(d,0.0));
}

float backIn(float t, float s) {
    return t * t * ((s + 1.0) * t - s);
}

float backOut(float t, float s) {
    t -= 1.0;
    return t * t * ((s + 1.0) * t + s) + 1.0;
}

float backInOut(float t, float s) {
    if (t < 0.5)
    return 0.5 * backIn(2.0 * t, s);
    else
    return 0.5 * (backOut(2.0 * t - 1.0, s) + 1.0);
}

float cubicBezierEase(float x, vec2 p1, vec2 p2) {
    float t = x;
    for (int i = 0; i < 5; i++) {
        float mt  = 1.0 - t;
        float mt2 = mt * mt;
        float t2  = t * t;
        float t3  = t2 * t;

        float xVal = 3.0 * mt2 * t * p1.x
        + 3.0 * mt * t2 * p2.x
        + t3;

        float dxdt = 3.0 * p1.x * mt * (1.0 - 3.0 * t)
        + 3.0 * p2.x * t * (2.0 - 3.0 * t)
        + 3.0 * t2;

        if (abs(dxdt) < 1e-6) break;

        t -= (xVal - x) / dxdt;
        t  = clamp(t, 0.0, 1.0);
    }

    float mt  = 1.0 - t;
    float mt2 = mt * mt;
    float t2  = t * t;
    return 3.0 * mt2 * t * p1.y
    + 3.0 * mt * t2 * p2.y
    + t2 * t;
}

float overshootInExpoOutEase(float x) {
    vec2 p1 = vec2(0.25, -0.3);
    vec2 p2 = vec2(1.0,  1.0);
    return cubicBezierEase(x, p1, p2);
}

float envelope(float time, float amplitude, float riseEnd, float holdEnd, float descentEnd) {
    if (time < 0.0)
    return 0.0;

    if (time < riseEnd) {
        float t = clamp(time / riseEnd, 0.0, 1.0);
        return amplitude * backInOut(t, 1.70158);
    }

    if (time < holdEnd)
    return amplitude;

    if (time < descentEnd) {
        float t = clamp((time - holdEnd) / (descentEnd - holdEnd), 0.0, 1.0);
        float g = overshootInExpoOutEase(t);
        return amplitude * (1.0 - g);
    }

    return 0.0;
}

float map(vec3 p) {
    float d = 1e20;

    for (int instance = 0; instance < InstanceCount; instance++) {
        int index = instance * 5;
        vec3 center = fetch3(DataBuffer, index);
        float instanceTime = fetch(DataBuffer, index + 3);

        float instanceFade = 1.0 - clamp(instanceTime / MAX_LIFETIME, 0.0, 1.0);

        vec3 localP = p - center;

        float height = 1200;
        //float shape = sdCylinder(localP - vec3(0, height - 1, 0), 1+clamp((((6 * instanceFade) + sin(time*22)*clamp(1 - instanceFade, 0, 0.25))/10)*clamp(instanceTime/(MAX_LIFETIME/6), 0, 10), 0.0, 10)*3.5, height);
        float shape = sdCylinder(localP - vec3(0, height - 1, 0), envelope(instanceTime, 1, (MAX_LIFETIME/4), (MAX_LIFETIME/2), MAX_LIFETIME)*3+((sin(time*25)*0.35)*(1-instanceFade)), height);
        d = min(d, shape);
    }

    return d;
}

void main() {
    vec4 diffuseColor = texture(DiffuseSampler, texCoord);
    float sceneDepth = texture(MainDepthSampler, texCoord).r;
    vec3 worldPos = getWorldPos(MainDepthSampler, texCoord, invProjMat, invViewMat, cameraPos);

    bool hasSurface = sceneDepth < 1.0;
    float sceneDist = hasSurface ? length(worldPos - cameraPos) : 1e6;
    float sceneDepthView = hasSurface ? viewSpaceFromDepth(sceneDepth, texCoord, invProjMat).z : -1e6;

    vec3 rayOrigin = cameraPos;
    vec3 rayDir = normalize(worldPos - cameraPos);

    vec3 accumColor = vec3(0.0);
    float accumAlpha = 0.0;

    float t = 0.0;
    float maxDist = hasSurface ? sceneDist + SURFACE_FADE : 100.0;

    for (int i = 0; i < 64; i++) {
        float d = map(rayOrigin + rayDir * t);
        if (d < 0.01 || t > maxDist) break;
        t += d;
    }

    if (t < maxDist) {
        for (int i = 0; i < STEPS; i++) {
            if (t > maxDist) break;

            vec3 samplePos = rayOrigin + rayDir * t;
            float d = map(samplePos);

            if (d <= 0.0) {
                float density = clamp(-d, 0.0, 1.0);
                density *= density;

                if (hasSurface) {
                    float sampleDepthView = (ViewMat * vec4(samplePos - cameraPos, 1.0)).z;
                    density *= applyDepthFade(sceneDepthView, sampleDepthView, SURFACE_FADE);
                }

                float stepAlpha = clamp(density * FOG_DENSITY * VOLUME_STEP_SIZE, 0.0, 1.0);

                float lifetimeFade = 1.0 - clamp(time / MAX_LIFETIME, 0.0, 1.0);
                stepAlpha *= lifetimeFade;

                accumColor += (1.0 - accumAlpha) * stepAlpha;
                accumAlpha += (1.0 - accumAlpha) * stepAlpha;

                if (accumAlpha >= 0.995) break;

                t += VOLUME_STEP_SIZE;
            } else {
                t += max(d, VOLUME_STEP_SIZE);
            }
        }
    }

    float primaryInstanceTime = InstanceCount > 0 ? fetch(DataBuffer, 4) : 0.0;
    float globalFade = 1.0 - clamp(primaryInstanceTime / MAX_LIFETIME, 0.0, 1.0);

    accumAlpha *= globalFade;

    vec2 distToBorder = min(texCoord, 1.0 - texCoord);
    float minDistToEdge = min(distToBorder.x, distToBorder.y);

    float edgeFactor = smoothstep(0.0, 0.20, minDistToEdge);

    //float distAlpha = accumAlpha * edgeFactor;

    diffuseColor = texture(DiffuseSampler, texCoord);

    float diffuseRed = texture(DiffuseSampler, texCoord).r;
    float diffuseGreen = texture(DiffuseSampler, texCoord).g;
    float diffuseBlue = texture(DiffuseSampler, texCoord).b;

    diffuseRed += (accumAlpha * 0.5) * 1.2;
    diffuseGreen += (accumAlpha * 0.5) * 1.1;
    diffuseBlue += (accumAlpha * 0.5) * 0.3;

    fragColor = vec4(vec3(diffuseRed, diffuseGreen, diffuseBlue), diffuseColor.a);
}