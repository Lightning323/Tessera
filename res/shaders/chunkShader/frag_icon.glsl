#version 330 core
 
// Interpolated values from the vertex shaders
in vec3 UV;
in float normals;
 
// Ouput data
out vec4 color;
 
// Values that stay constant for the whole mesh.
uniform sampler2DArray textureArray;
 
void main(){
   vec4 val = texture( textureArray, UV );
   if(val.a == 0.0){//ditch transparent fragments
      discard;
   }
   // if(normals == 1.0f) val *= 0.9;
   // else if(normals == 2.0f) val *= 0.8;
   // else if(normals == 3.0f) val *= 0.7;
   // else if(normals == 4.0f) val *= 0.6;
   // else if(normals == 5.0f) val *= 0.5;

   color = val;
   // color = vec4(UV.x,UV.y,UV.z,1.0);
}