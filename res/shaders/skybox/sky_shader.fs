#version 330 core
 
in vec2 UV;
in float normals;
out vec4 color;
uniform sampler2D tex;
 
void main(){
   vec4 val = texture( tex, UV );
   // if(val.a == 0.0){//ditch transparent fragments
   //    discard;
   // }
   color = val;
   // color.rgb *= max(sun,torch);
   // color = vec4(UV.x,UV.y,0.0,1.0);
   // color = vec4(1.0, 0.0, 0.0, 1.0);
}