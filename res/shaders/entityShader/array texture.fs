#version 330 core
 
in vec3 UV;
in float normals;
out vec4 color;
uniform float sun;
uniform float torch;
uniform vec3 tint;
uniform sampler2DArray tex;
 
void main(){
   vec4 val = texture( tex, UV );
   if(val.a == 0.0){//ditch transparent fragments
      discard;
   }
   // if(normals == 1.0f) val *= 0.9;
   // if(normals == 2.0f) val *= 0.8;
   // if(normals == 3.0f) val *= 0.7;
   // if(normals == 4.0f) val *= 0.6;
   // if(normals == 5.0f) val *= 0.5;

   vec3 tintedSun = vec3(sun * tint.r, sun* tint.g, sun* tint.b);

   color = val;
   color.rgb *= vec3(max(tintedSun.r,torch),
                     max(tintedSun.g,torch),
                     max(tintedSun.b,torch));
   // color = vec4(UV.x,UV.y,0.0,1.0);
   // color = vec4(1.0, 1.0, 1.0, 1.0);
}