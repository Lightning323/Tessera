/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.client.visuals.gameScene.rendering.wireframeBox;

import com.xbuilders.window.render.Shader;
import java.io.IOException;

import org.joml.Vector4f;

/**
 *
 * @author zipCoder933
 */
public class SolidColorShader extends Shader {

    public int colorUnifrom, mvpUniform;

    public SolidColorShader() {
        super();
        try {
            String vertexShader = """
                                              #version 330 core
                                              layout (location = 0) in vec3 inPosition;
                                              uniform mat4 MVP;
                                              uniform vec4 color;
                                                                             void main() {
                                                  gl_Position = MVP * vec4(inPosition, 1.0);
                                              }
                                              """;
            String fragmentShader = """
                                            #version 330 core
                                            out vec4 FragColor;
                                            uniform vec4 color;
                                                                         void main() {
                                                FragColor = vec4(color);
                                            }
                                            """;
            init(vertexShader, fragmentShader);
            colorUnifrom = getUniformLocation("color");
            mvpUniform = getUniformLocation("MVP");
        } catch (IOException ex) {
            com.xbuilders.Main.LOGGER.error("Unhandled exception in SolidColorShader", ex);
        }
    }

    public void setColor(Vector4f color) {
        loadVec4f(colorUnifrom, color);
    }

    @Override
    public void bindAttributes() {
    }
}
