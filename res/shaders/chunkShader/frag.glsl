#version 400 core
// #ifdef GL_ES
// precision mediump float;
// precision mediump int;
// #endif

// METHODS =============================================================================
//=====================================================================================
float map(float value, float inMin, float inMax, float outMin, float outMax)
{
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

vec2 map(vec2 value, vec2 inMin, vec2 inMax, vec2 outMin, vec2 outMax)
{
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

vec3 map(vec3 value, vec3 inMin, vec3 inMax, vec3 outMin, vec3 outMax)
{
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

vec4 map(vec4 value, vec4 inMin, vec4 inMax, vec4 outMin, vec4 outMax)
{
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

// Interpolated values from the vertex shaders
in vec3 UV;
in float normal;
in float sun;
in float torch;
in vec3 position;
in vec3 chunkspace_position;
in vec3 worldspace_position;

// Ouput data
out vec4 color;

// Values that stay constant for the whole mesh.
uniform sampler2DArray textureArray;
uniform int viewDistance;
uniform vec3 skyColor;
uniform float flashlightDistance;
uniform vec4 solidColor;
uniform vec3 tint;
uniform vec3 fogColor;
uniform vec3 cursorMin;
uniform vec3 cursorMax;
uniform int textureLayerCount;
uniform float blockBreakPercentage;

const int BLOCK_BREAK_TEXTURE_COUNT = 10;

//This is the default chunk shader
// Light
//  POS_X = 0;
//  NEG_X = 1;
//  POS_Z = 2;
//  NEG_Z = 3;
//  NEG_Y = 4;
//  POS_Y = 5;

void main()
{
    if (solidColor.a > 0.0)
    {
        color = solidColor;
        return;
    }

    // get the color from the texture
    vec4 val = texture(textureArray, UV);
    //Cursor overlay
    if(worldspace_position.z >= cursorMin.z - 0.01 && worldspace_position.z <= cursorMax.z + 0.01
    && worldspace_position.x >= cursorMin.x - 0.01 && worldspace_position.x <= cursorMax.x + 0.01
    && worldspace_position.y >= cursorMin.y - 0.01 && worldspace_position.y <= cursorMax.y + 0.01
    ) {
        if(blockBreakPercentage > 0.0) {
           float textureID = blockBreakPercentage * BLOCK_BREAK_TEXTURE_COUNT;
           float type = floor(textureID - 0.5f);
           float textureLayer = float(max(0, min(textureLayerCount, type)));

           vec4 tex = texture(textureArray, vec3(UV.x, UV.y, textureLayer));
           val = mix(val, tex, tex.a);
        }


       // color = vec4(
       // worldspace_position.x - cursorMin.x,
       //  worldspace_position.y - cursorMin.y,
        //  worldspace_position.z - cursorMin.z, 1.0);
    }


    // calculate and apply Fog visiblity
    float fragDistance = length(position);
    float fogGradient = 16;                      // The length of the fog gradient
    float fogStart = viewDistance - fogGradient; // Start of fog
    float visibility = 1.0;
    if (fragDistance > fogStart)
    {
        visibility = map(fragDistance, fogStart, fogStart + fogGradient, 1.0, 0.0);
        visibility = clamp(visibility, 0.01, 1.0);
    }
    val.a *= visibility;

    if (val.a == 0.0)
    { // ditch transparent fragments
        discard;
    }

    float flashlight = 0;
    if (fragDistance < flashlightDistance)
    {
        flashlight = map(fragDistance, 0, flashlightDistance, 1, 0);
    }

    // Calculate the sun
    vec3 tintedSun = vec3(sun * tint.r, sun * tint.g, sun * tint.b);
    //Top = 4
    if (normal == 0.0f) //pos x
        tintedSun *= 0.9;
    else if (normal == 1.0f) //neg x
        tintedSun *= 0.8;
    else if (normal == 2.0f) //pos z
        tintedSun *= 0.85;
    else if (normal == 3.0f) //neg z
        tintedSun *= 0.85;
    else if (normal == 5.0f) //bottom
        tintedSun *= 0.7;

    // calculate the torch
    vec3 normTint = normalize(tint) * 1.5;
    vec3 tintedTorch = vec3(torch * normTint.r, torch * normTint.g, torch * normTint.b);

    val.rgb *= vec3(
        max(flashlight, max(tintedTorch.r, tintedSun.r)),
        max(flashlight, max(tintedTorch.g, tintedSun.g)),
        max(flashlight, max(tintedTorch.b, tintedSun.b)));


  //color = mix(vec4(fogColor, 1.0), val, visibility); //with colored fog
    color = vec4(val); //fog is just alpha channel

   // color = vec4(worldspace_position.x, worldspace_position.y, worldspace_position.z, 1.0);


    //      color = vec4(sun,sun,sun,1.0);
    // // X is red, Y is green, Z is blue
    // if (normal == 0.0f) color = vec4(1.0, 0.0, 0.0, 1.0);  // positive x
    // if (normal == 1.0f) color = vec4(0.5, 0.0, 0.0, 1.0);  // negative x

    // if (normal == 2.0f) color = vec4(0.0, 0.0, 1.0, 1.0);  // positive z
    // if (normal == 3.0f) color = vec4(0.0, 0.0, 0.5, 1.0);  // negative z

    // if (normal == 4.0f) color = vec4(0.0, 1.0, 0.0, 1.0);  // positive y
    // if (normal == 5.0f) color = vec4(0.0, 0.5, 0.0, 1.0);  // negative y
}