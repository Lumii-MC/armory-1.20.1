#version 150

uniform sampler2D DiffuseSampler;
uniform float time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    // This is a fragment shader, it runs for every pixel on the screen. There are also vertex shaders, compute shaders, and even geometry shaders
    // Lodestone only allows fragment shaders in the post processing pipeline so this one is obviously a fragment
    // texCoord is the input coordinates of the current pixel from 0, 0 to 1, 1 respectively
    // fragColor expects a vec4 in the RGBA format
    // this code outputs the input color as if the shader isn't there
    // it samples the color of the current pixel from the DiffuseSampler which is basically a 2d texture of the rendered image the shader is running on top of
    // this one is already registered so you can fuck around with GLSL, toggle the shader with /markedToggle
    // try looking at the other shaders i wrote here to get the idea of how viewspace post shaders work
    // i highly recommend looking at these as they explain the core concepts of shaders realy well
    // https://www.youtube.com/watch?v=xnZfMRTmfJY https://www.youtube.com/watch?v=3mfvZ-mdtZQ
    // the glsl syntax is pretty similar to java as they are both based on C, though here you won't be using any numerical data types except floats
    // vec2,vec3,vec4 are the basic vector/color data holders, they can be created through vecN(float1, float2...)
    // to get data from them you just do either vecN.xyzw or vecN.rgba. you can get any number of values as long as they don't surpass 4 values
    // vecN.r outputs a float, vecN.rg gives a vec2, vecNrgb gives a vec3 and so on, you can also swap them around like vecN.bgr, vecN.xyx, vecN.zyx etc.
    // glsl has most of java's Math. functions, you just write them without the Math. for example clamp(floatA, floatB, floatC);
    // texture(sampler2d, vec2) gets the vec4 color currently at the vec2 position in the sampler
    // one funny thing you could do is offset the coordinates you get the color from based on noise or a sine wave to create basic distortions
    // uniforms are the values passed to the shader from java code, here you have the time and the base image
    // everythig from here is pure math and functions you write to get the image to do what you need

    // TODO: Lumi learn shaders
    // TODO: Lumi learn shaders
    // TODO: Lumi learn shaders
    // TODO: Lumi learn shaders
    // TODO: Lumi learn shaders
    // TODO: Lumi learn shaders
    fragColor = texture(DiffuseSampler, texCoord);
}