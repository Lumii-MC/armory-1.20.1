#version 150

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

// Shader by Homak on Modrinth! https://modrinth.com/user/Homak
// Licenced MIT https://opensource.org/license/mit
// Based on shock.fsh

// Really fast simplex/value noise https://www.shadertoy.com/view/XltXRH
float oct(vec3 p){
    return fract(4768.1232345456 * sin((p.x+p.y*43.0+p.z*137.0)));
}
float achnoise(vec3 x){
    vec3 p = floor(x);
    vec3 fr = smoothstep(0.0, 1.0, fract(x));
    vec3 LBZ = p + vec3(0.0, 0.0, 0.0);
    vec3 LTZ = p + vec3(0.0, 1.0, 0.0);
    vec3 RBZ = p + vec3(1.0, 0.0, 0.0);
    vec3 RTZ = p + vec3(1.0, 1.0, 0.0);

    vec3 LBF = p + vec3(0.0, 0.0, 1.0);
    vec3 LTF = p + vec3(0.0, 1.0, 1.0);
    vec3 RBF = p + vec3(1.0, 0.0, 1.0);
    vec3 RTF = p + vec3(1.0, 1.0, 1.0);

    float l0candidate1 = oct(LBZ);
    float l0candidate2 = oct(RBZ);
    float l0candidate3 = oct(LTZ);
    float l0candidate4 = oct(RTZ);

    float l0candidate5 = oct(LBF);
    float l0candidate6 = oct(RBF);
    float l0candidate7 = oct(LTF);
    float l0candidate8 = oct(RTF);

    float l1candidate1 = mix(l0candidate1, l0candidate2, fr[0]);
    float l1candidate2 = mix(l0candidate3, l0candidate4, fr[0]);
    float l1candidate3 = mix(l0candidate5, l0candidate6, fr[0]);
    float l1candidate4 = mix(l0candidate7, l0candidate8, fr[0]);


    float l2candidate1 = mix(l1candidate1, l1candidate2, fr[1]);
    float l2candidate2 = mix(l1candidate3, l1candidate4, fr[1]);


    float l3candidate1 = mix(l2candidate1, l2candidate2, fr[2]);

    return l3candidate1;
}

float supernoise3d(vec3 p){

    float a =  achnoise(p);
    float b =  achnoise(p + 120.5);
    return (a + b) * 0.5;
}

float fetch(samplerBuffer DataBuffer, int index) {
    return texelFetch(DataBuffer, index).r;
}

vec3 fetch3(samplerBuffer DataBuffer, int startIndex) {
    return vec3(fetch(DataBuffer, startIndex), fetch(DataBuffer, startIndex + 1), fetch(DataBuffer, startIndex + 2));
}

float getDepth(sampler2D DepthBuffer, vec2 uv) {
    return texture(DepthBuffer, uv).r;
}

vec3 getWorldPos(sampler2D DepthBuffer, vec2 texCoord, mat4 invProjMat, mat4 invViewMat, vec3 cameraPos) {
    float z = getDepth(DepthBuffer, texCoord) * 2.0 - 1.0;
    vec4 clipSpacePosition = vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePosition = invProjMat * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;
    vec4 localSpacePosition = invViewMat * viewSpacePosition;
    return cameraPos + localSpacePosition.xyz;
}

vec3 viewSpaceFromDepth(float depth, vec2 texCoord, mat4 invProjMat) {
    float z = depth * 2.0 - 1.0;
    vec4 clipSpacePosition = vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePosition = invProjMat * clipSpacePosition;
    return viewSpacePosition.xyz / viewSpacePosition.w;
}

float applyDepthFade(float sceneDepthView, float pixelDepthView, float intensity) {
    float spacing = pixelDepthView - sceneDepthView;
    return clamp(spacing / intensity, 0.0, 1.0);
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


float map(vec3 p, out float outTime) {
    float d = 1e10;
    outTime = 0.0;

    for (int instance = 0; instance < InstanceCount; instance++) {
        int index = instance * 4;
        vec3 center = fetch3(DataBuffer, index);
        float instanceTime = fetch(DataBuffer, index + 3);

        float instanceFade = 1.0 - clamp(instanceTime / 5.5, 0.0, 1.0);

        vec3 localP = p - center;

        float height = 1200.0;
        // this is some comically large line of code
        float shape = sdCylinder(localP - vec3(0.0, height - 1.0, 0.0), envelope(instanceTime, 1.0, (5.5/4.0), (5.5/2.0), 5.5)*3.0+((sin(time*25.0)*0.35)*(1.0-instanceFade)), height) - supernoise3d(p + vec3(0, instanceTime*instanceTime*instanceTime, 0));

        if (shape < d) {
            d = shape;
            outTime = instanceTime;
        }
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
    float maxDist = hasSurface ? sceneDist + 6.75 : 750.0;

    float dummyTime;
    for (int i = 0; i < 32; i++) {
        float d = map(rayOrigin + rayDir * t, dummyTime);
        if (d < 0.01 || t > maxDist) break;
        t += d;
    }

    if (t < maxDist) {
        for (int i = 0; i < 25; i++) {
            if (t > maxDist) break;

            vec3 samplePos = rayOrigin + rayDir * t;
            float hitTime;
            float d = map(samplePos, hitTime);

            if (d <= 0.0) {
                float density = clamp(-d, 0.0, 1.0);
                density *= density;

                if (hasSurface) {
                    float sampleDepthView = (ViewMat * vec4(samplePos - cameraPos, 1.0)).z;
                    density *= applyDepthFade(sceneDepthView, sampleDepthView, 6.75);
                }

                float stepAlpha = clamp(density * 1.95 * 0.25, 0.0, 1.0);

                float lifetimeFade = 1.0 - clamp(hitTime / 5.5, 0.0, 1.0);
                stepAlpha *= lifetimeFade;

                accumColor += (1.0 - accumAlpha) * stepAlpha;
                accumAlpha += (1.0 - accumAlpha) * stepAlpha;

                if (accumAlpha >= 0.995) break;

                t += 0.25;
            } else {
                t += max(d, 0.25);
            }
        }
    }

    vec2 distToBorder = min(texCoord, 1.0 - texCoord);
    float minDistToEdge = min(distToBorder.x, distToBorder.y);

    float edgeFactor = smoothstep(0.0, 0.20, minDistToEdge);

    diffuseColor = texture(DiffuseSampler, texCoord);

    float diffuseRed = texture(DiffuseSampler, texCoord).r;
    float diffuseGreen = texture(DiffuseSampler, texCoord).g;
    float diffuseBlue = texture(DiffuseSampler, texCoord).b;

    diffuseRed += (accumAlpha * 0.5) * 1.2;
    diffuseGreen += (accumAlpha * 0.5) * 1.1;
    diffuseBlue += (accumAlpha * 0.5) * 0.3;

    fragColor = vec4(vec3(diffuseRed, diffuseGreen, diffuseBlue), diffuseColor.a);
}