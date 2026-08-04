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

// Shader by Homak on Modrinth! https://modrinth.com/user/Homak
// Licenced MIT https://opensource.org/license/mit

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

float map(vec3 p, out float outTime) {
    float d = 1e20;
    outTime = 0.0;

    for (int instance = 0; instance < InstanceCount; instance++) {
        int index = instance * 5; // Stride is correctly 5 here
        vec3 center = fetch3(DataBuffer, index);
        float scale = fetch(DataBuffer, index + 3);
        float instanceTime = fetch(DataBuffer, index + 4);

        float instanceFade = 1.0 - clamp(instanceTime / 1.5, 0.0, 1.0);

        vec3 localP = p - center;

        float currentTick = instanceTime * 20.0;

        float innerRadius = scale + (currentTick * currentTick) / 80.0;

        float ringThickness = 5.0 * instanceFade;
        float outerRadius = innerRadius + ringThickness;
        float instanceD = opSub(length(localP) - outerRadius, length(localP) - innerRadius);

        if (instanceD < d) {
            d = instanceD;
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
    float maxDist = hasSurface ? sceneDist + 4.75 : 100.0;

    float dummyTime;
    for (int i = 0; i < 64; i++) {
        float d = map(rayOrigin + rayDir * t, dummyTime);
        if (d < 0.01 || t > maxDist) break;
        t += d;
    }

    if (t < maxDist) {
        for (int i = 0; i < 20; i++) {
            if (t > maxDist) break;

            vec3 samplePos = rayOrigin + rayDir * t;
            float hitTime;
            float d = map(samplePos, hitTime);

            if (d <= 0.0) {
                float density = clamp(-d, 0.0, 1.0);
                density *= density;

                if (hasSurface) {
                    float sampleDepthView = (ViewMat * vec4(samplePos - cameraPos, 1.0)).z;
                    density *= applyDepthFade(sceneDepthView, sampleDepthView, 4.75);
                }

                float instanceFade = 1.0 - clamp(hitTime / 1.5, 0.0, 1.0);
                float stepAlpha = clamp(density * 0.25 * 0.25, 0.0, 1.0) * instanceFade;

                accumColor += (1.0 - accumAlpha) * stepAlpha * vec3(1.0, 1.0, 1.0);
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

    float distAlpha = accumAlpha * edgeFactor;

    diffuseColor = texture(DiffuseSampler, texCoord + vec2(distAlpha) * 0.45);

    float diffuseRed = texture(DiffuseSampler, texCoord + (vec2(distAlpha) * 0.15 + (distAlpha / 2.0))).r;
    float diffuseGreen = texture(DiffuseSampler, texCoord + (vec2(distAlpha) * 0.15 + (distAlpha / 2.0) * 1.05)).g;
    float diffuseBlue = texture(DiffuseSampler, texCoord + (vec2(distAlpha) * 0.15 + (distAlpha / 2.0) * 1.1)).b;

    diffuseRed += (accumAlpha * 0.5) * 1.75;
    diffuseGreen += (accumAlpha * 0.5) * 1.55;
    diffuseBlue += (accumAlpha * 0.5) * 0.55;

    fragColor = vec4(vec3(diffuseRed, diffuseGreen, diffuseBlue), diffuseColor.a);
}