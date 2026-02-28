package com.tessera.window.demos.shader;


import com.tessera.window.render.Shader;
import org.joml.Vector4f;
import java.io.IOException;

public class BasicShader extends Shader {

    static final String vertShader = """
            #version 330 core
            layout(location = 0) in vec3 vertexPosition_modelspace;
            layout(location = 1) in vec2 vertexUV;
             
            out vec2 UV;
            uniform mat4 MVP;
             
            void main(){
                gl_Position =  MVP * vec4(vertexPosition_modelspace,1);
                UV = vertexUV;
            }
            """;

    static final String fragShader = """
            #version 330 core
             
            in vec2 UV;
            uniform sampler2D myTextureSampler;
            uniform vec4 colorToSet;
            uniform float useColor;
            out vec4 color;
             
            void main(){
                //Mix the color and texture
                if(useColor == 1.0)color = colorToSet;
                else color = texture(myTextureSampler, UV);
            }
            """;


    public void useColor(Vector4f color) {
        loadVec4f(colorUniform, color);
        loadFloat(useColorUniform, 1f);
    }

    public void useTexture() {
        loadFloat(useColorUniform, 0f);
    }

    public final int mvpUniform;
    private final int colorUniform, useColorUniform;

    public BasicShader() {
        try {
            init(vertShader, fragShader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mvpUniform = getUniformLocation("MVP");
        colorUniform = getUniformLocation("colorToSet");
        useColorUniform = getUniformLocation("useColor");
    }

    @Override
    public void bindAttributes() {

    }
}
