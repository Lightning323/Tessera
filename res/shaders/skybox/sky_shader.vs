#version 330 core

layout(location = 0) in vec3 vertexPosition_modelspace;
layout(location = 1) in vec2 uv_coords;

out vec2 UV;
out float normals;
uniform mat4 projViewMatrix;
uniform float cycle_value;
 
void main(){
    gl_Position =  projViewMatrix * vec4(vertexPosition_modelspace,1);
    UV = vec2(cycle_value,uv_coords.y);
}