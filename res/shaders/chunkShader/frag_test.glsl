#version 400 core
// #ifdef GL_ES
// precision mediump float;
// precision mediump int;
// #endif

// METHODS =============================================================================
//=====================================================================================
float map(float value, float inMin, float inMax, float outMin, float outMax) {
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

vec2 map(vec2 value, vec2 inMin, vec2 inMax, vec2 outMin, vec2 outMax) {
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

vec3 map(vec3 value, vec3 inMin, vec3 inMax, vec3 outMin, vec3 outMax) {
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

vec4 map(vec4 value, vec4 inMin, vec4 inMax, vec4 outMin, vec4 outMax) {
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

in vec3 UV;
in float normal;
in vec3 position;

uniform sampler2DArray textureArray;
uniform int textureLayerCount;
uniform vec4 solidColor;

out vec4 color;



void main() {
    vec4 val = texture(textureArray, UV);
    // if (val.a == 0.0) {  // ditch transparent fragments
    //     discard;
    // }

    color = solidColor;

    // float l = UV.z / textureLayerCount;
    // color = vec4(l, l, l, 1.0);


    // if(normal == 0.0) {
    //     color = vec4(0.0, 0.0, 0.0, 1.0);
    // } else if(normal == 1.0) {
    //     color = vec4(1.0, 1.0, 1.0, 1.0);
    // } else if(normal == 2.0) {
    //     color = vec4(1.0,0.0,0.0,1.0);
    // } else if(normal == 3.0) {
    //     color = vec4(0.0,1.0,0.0,1.0);
    // } else if(normal == 4.0) {
    //     color = vec4(0.0,0.0,1.0,1.0);
    // } else if(normal == 5.0) {
    //     color = vec4(1.0,1.0,0.0,1.0);
    // } else {
    //     color = vec4(0.0,0.0,0.0,1.0);
    // }
        
    
}