package com.tessera.engine.client.visuals.gameScene.rendering.chunk.occlusionCulling;


import com.tessera.window.render.Shader;

import java.io.IOException;

public class EmptyShader extends Shader {

    static final String vertShader = """
            #version 330 core
            layout(location = 0) in vec3 vertexPosition_modelspace;
            uniform mat4 MVP;
            void main(){
                gl_Position =  MVP * vec4(vertexPosition_modelspace,1);
            }
            """;

    static final String fragShader = """
            #version 330 core
            out vec4 color;
            void main(){
                color = vec4(0.0, 0.0, 0.0, 1.0);
            }
            """;

    public final int mvpUniform;

    public EmptyShader() {
        try {
            init(vertShader, fragShader);
            mvpUniform = getUniformLocation("MVP");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bindAttributes() {

    }
}
