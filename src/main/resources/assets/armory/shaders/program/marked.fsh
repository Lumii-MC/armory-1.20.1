#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    fragColor = vec4(
    color.r,
    color.g * 0,
    color.b * 0,
    color.a
    );
}