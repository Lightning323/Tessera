#version 330 core

layout(location = 0) in vec3 vertexPosition_modelspace;
layout(location = 1) in vec3 uv_coords;

out vec3 UV;
out float normals;
uniform int textureLayerCount;
uniform mat4 projViewMatrix;
uniform mat4 modelMatrix;
 
void main(){
    gl_Position =  projViewMatrix * modelMatrix * vec4(vertexPosition_modelspace,1);


    float type = floor(uv_coords.z - 0.5f); //-1+0.5
    float textureLayer = float(max(0, min(textureLayerCount, type)));
    UV = vec3(uv_coords.x,uv_coords.y,textureLayer);
}