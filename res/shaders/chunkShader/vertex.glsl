#version 400 core

precision mediump float;
precision mediump int;

// Input vertex data, different for all executions of this shader.
layout(location = 0) in uvec3 vertex;

const int CHUNK_WIDTH = 32;

// Output data ; will be interpolated for each fragment.
out vec3 UV;
out float normal;
out float sun;
out float torch;
out vec3 position;
out vec3 chunkspace_position;
out vec3 worldspace_position;

// Values that stay constant for the whole mesh.
uniform mat4 MVP;
uniform int animationTime;
uniform vec3 chunkPosition;
uniform float maxMult12bits;
uniform float maxMult10bits;
uniform int textureLayerCount;

void main()
{

    // FIRST INTEGER
    uint packedInt = uint(vertex.x);
    // Extract the vertex X value (12 bits) by shifting right by 20 bits
    //The subtraction in the vertex positions are part of the unpacking process (see comment in vertexSet class in source code)
    float vertX = (float((packedInt >> 20u) & 0xFFFu) / maxMult12bits) - 1; 
    // Extract the vertex Y value (12 bits) by shifting right by 8 bits
    float vertY = (float((packedInt >> 8u) & 0xFFFu) / maxMult12bits) - 1;
    // Extract the normals value (3 bits) by shifting right by 5 bits
    normal = int((packedInt >> 5u) & 0x7u);
    // Extract the animation value (5 bits) by masking with 0b11111
    uint animationSize = packedInt & 0x1Fu;

    // SECOND INTEGER
    float vertZ = (float((uint(vertex.y) >> 20u) & 0xFFFu) / maxMult12bits) - 1;
    float u = float((uint(vertex.y) >> 10u) & 0x3FFu) / maxMult10bits;
    float v = float(uint(vertex.y) & 0x3FFu) / maxMult10bits;

    // THIRD INTEGER
    packedInt = uint(vertex.z);
    // Extract the texture value (16 bits) by shifting right by 16 bits
    uint textureID = (packedInt >> 16) & 0xFFFFu;

    // Extract the next 4 bits as sunlight and the rest of the bits as torchlight
    uint packedSun = (packedInt >> 4) & 0xFu;
    uint packedTorch = packedInt & 0xFu;
    sun = float(packedSun) / 15.0;
    torch = float(packedTorch) / 15.0;
    chunkspace_position = vec3(vertX, vertY, vertZ);
    worldspace_position = vec3(
            vertX + (chunkPosition.x*CHUNK_WIDTH),
            vertY + (chunkPosition.y*CHUNK_WIDTH),
            vertZ + (chunkPosition.z*CHUNK_WIDTH));

    //----------------------------------------
    if (animationSize > 1)
    {
        int maxTicks = int(animationTime / animationSize);
        textureID += animationTime - (animationSize * maxTicks);
    }

    float type = floor(textureID - 0.5f);
    float textureLayer = float(max(0, min(textureLayerCount, type)));

    // Output position of the vertex, in clip space : MVP * position
    gl_Position = MVP * vec4(vertX, vertY, vertZ, 1);

    position = gl_Position.xyz; //Position is just For fragment shader
    UV = vec3(u, v, textureLayer);
}