/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene;

import com.tessera.engine.client.ClientWindow;
import com.tessera.window.render.Shader;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import org.lwjgl.opengl.GL30;

/**
 *
 * @author zipCoder933
 */
class Crosshair {

    // Define the coordinates of the line endpoints
    final int lineLength = 25;
    float[] vertices = {
        -lineLength, 0,
        lineLength, 0,
        0, -lineLength,
        0, lineLength
    };

    int colorUniform, windowUniform;
    int vao;
    Shader shader;

    public Crosshair(int width, int height) throws IOException {
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        // Create a VBO and bind it to the vertex shader
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        String vertexShaderSource = "#version 330 core\n"
                + "layout (location = 0) in vec2 aPos;\n"
                + "uniform vec2 WindowSize;\n"
                + "void main(){\n"
                + "   gl_Position = vec4(aPos.x / WindowSize.x, aPos.y / WindowSize.y, 0.0, 1.0);\n"
                + "}\n";

        // Create a simple fragment shader that outputs a constant color
        String fragmentShaderSource = "#version 330 core\n"
                + "out vec4 FragColor;\n"
                + "void main(){\n"
                + "   FragColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);\n"
                + "}\n";
        shader = new Shader(vertexShaderSource, fragmentShaderSource);
        windowUniform = glGetUniformLocation(shader.getID(), "WindowSize");
        glUseProgram(shader.getID());
        windowResizeEvent(width, height);
        GL30.glBindVertexArray(0);
    }

    public void windowResizeEvent(int width, int height) {
        glUseProgram(shader.getID());
        glUniform2f(windowUniform, width, height);
    }

    public void draw() {
        GL30.glBindVertexArray(vao);
        glUseProgram(shader.getID());
        ClientWindow.printDebugsEnabled(false);
        glLineWidth(3);
        glDrawArrays(GL_LINES, 0, 4);
        ClientWindow.printDebugsEnabled(true);
    }
}
