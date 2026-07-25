#version 150

uniform sampler2D DiffuseSampler;
uniform float time;

in vec2 texCoord;
out vec4 fragColor;

vec3 hash33(vec3 p3) {
    p3 = fract(p3 * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yxz + 33.33);
    return fract((p3.xxy + p3.yxx) * p3.zyx) * 2.0 - 1.0;
}

vec3 fade(vec3 t) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

float gradientNoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);

    vec3 g000 = hash33(i + vec3(0.0, 0.0, 0.0));
    vec3 g100 = hash33(i + vec3(1.0, 0.0, 0.0));
    vec3 g010 = hash33(i + vec3(0.0, 1.0, 0.0));
    vec3 g110 = hash33(i + vec3(1.0, 1.0, 0.0));
    vec3 g001 = hash33(i + vec3(0.0, 0.0, 1.0));
    vec3 g101 = hash33(i + vec3(1.0, 0.0, 1.0));
    vec3 g011 = hash33(i + vec3(0.0, 1.0, 1.0));
    vec3 g111 = hash33(i + vec3(1.0, 1.0, 1.0));

    float n000 = dot(g000, f - vec3(0.0, 0.0, 0.0));
    float n100 = dot(g100, f - vec3(1.0, 0.0, 0.0));
    float n010 = dot(g010, f - vec3(0.0, 1.0, 0.0));
    float n110 = dot(g110, f - vec3(1.0, 1.0, 0.0));
    float n001 = dot(g001, f - vec3(0.0, 0.0, 1.0));
    float n101 = dot(g101, f - vec3(1.0, 0.0, 1.0));
    float n011 = dot(g011, f - vec3(0.0, 1.0, 1.0));
    float n111 = dot(g111, f - vec3(1.0, 1.0, 1.0));

    vec3 u = fade(f);

    float nx00 = mix(n000, n100, u.x);
    float nx10 = mix(n010, n110, u.x);
    float nx01 = mix(n001, n101, u.x);
    float nx11 = mix(n011, n111, u.x);

    float nxy0 = mix(nx00, nx10, u.y);
    float nxy1 = mix(nx01, nx11, u.y);

    return mix(nxy0, nxy1, u.z);
}

float noise(in vec2 st, in float seed) {
    return gradientNoise(vec3(st, seed)) * 0.5 + 0.5;
}

#define OCTAVES 3
float fbm(in vec2 st, in float seed) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < OCTAVES; i++) {
        value += amplitude * noise(st, seed);
        st *= 1.5;
        seed *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

float warp(in vec2 pos, in float seed) {
    float f = fbm(pos, seed) - 0.5;

    vec2 posQuad = pos + vec2(21.5, 13.5) * f;

    return fbm(posQuad, seed);
}

void main() {
    vec2 uv = floor(texCoord * 1080.0) / 2900.0;
    float seed = time / 4.0;

    vec2 scaledUV = uv * 3.0;

    vec2 toCenter = texCoord - 0.5;
    float dist = length(toCenter);
    vec2 dir = dist > 0.0 ? toCenter / dist : vec2(0.0);

    float noiseMag = (warp(scaledUV, seed) - 0.5) * 0.5;

    float sine = max(sin(mod(time, 100.0)), 0.2);
    float vignetteMask = smoothstep(0.2, 0.6, dist - sine * 0.2) + 0.25;

    vec2 distortionOffset = dir * noiseMag * (vignetteMask * 0.5);

    vec3 tint = vec3(0.25, 0.0, 0.0) * vignetteMask;

    vec4 mainImageColor = texture(DiffuseSampler, texCoord + distortionOffset);

    fragColor = vec4(mainImageColor.rgb + tint, 1.0);
}