#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float time;

in vec2 texCoord;
out vec4 fragColor;

// Edited https://www.shadertoy.com/view/flSGDK
float hash(float x) { return fract(x + 1.3215 * 1.8152); }

float hash3(vec3 a) { return fract((hash(a.z * 42.8883) + hash(a.y * 36.9125) + hash(a.x * 65.4321)) * 291.1257); }

vec3 rehash3(float x) { return vec3(hash(((x + 0.5283) * 59.3829) * 274.3487), hash(((x + 0.8192) * 83.6621) * 345.3871), hash(((x + 0.2157) * 36.6521) * 458.3971)); }

float sqr(float x) { return x * x; }
float fastdist(vec3 a, vec3 b) { return sqr(b.x - a.x) + sqr(b.y - a.y) + sqr(b.z - a.z); }

vec4 eval(float x, float y, float z) {
    vec4 p[27];
    vec3 pos = vec3(x, y, z);
    vec3 cellFloor = floor(pos);

    for (int _x = -1; _x < 2; _x++) {
        for (int _y = -1; _y < 2; _y++) {
            for (int _z = -1; _z < 2; _z++) {
                vec3 _p = cellFloor + vec3(_x, _y, _z);
                float h = hash3(_p);
                p[(_x + 1) + ((_y + 1) * 3) + ((_z + 1) * 9)] = vec4(rehash3(h) + _p, h);
            }
        }
    }

    float m = 9999.9999;
    float w = 0.0;
    vec2 cellCenter = vec2(0.0);

    for (int i = 0; i < 27; i++) {
        float d = fastdist(pos, p[i].xyz);
        if (d < m) {
            m = d;
            w = p[i].w;
            cellCenter = p[i].xy;
        }
    }

    vec2 dirToCenter = cellCenter - vec2(x, y);
    return vec4(m, w, dirToCenter);
}

void main() {
    vec2 centerOffset = (texCoord - vec2(0.5)) * vec2(InSize.x / InSize.y, 1.0);
    float distFromCenter = length(centerOffset);

    float centerMask = smoothstep(0.05, 0.45, distFromCenter);

    vec2 uv = (texCoord * InSize) / InSize.y * 4.0;

    vec4 voronoi = eval(uv.x, uv.y, time * 0.4);
    float dist = sqrt(voronoi.x);
    float cellHash = voronoi.y;
    vec2 dirToCenter = voronoi.zw;

    float randomAngle = cellHash * 6.2831853;
    vec2 randomDir = vec2(cos(randomAngle), sin(randomAngle));

    float cellWeight = 1.0 - smoothstep(0.0, 1.0, dist);

    float distortionStrength = 0.06 * centerMask;

    vec2 offset = (randomDir + dirToCenter) * cellWeight * distortionStrength;

    vec4 color = texture(DiffuseSampler, texCoord + offset);

    fragColor = color*vec4(1, (1 - centerMask)/6, (1 - centerMask)/6, 1);
}