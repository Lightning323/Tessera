/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals;

import com.tessera.window.render.Shader;
import com.tessera.window.utils.vbo.IndexVBO;
import com.tessera.window.utils.vbo.VBO;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.io.IOException;

/**
 * @author zipCoder933
 */
public class RectOverlay {

    static class RectShader extends Shader {

        static final String vertShader = """
                #version 330 core
                layout(location = 0) in vec3 vertexPosition_modelspace;
                layout(location = 1) in vec2 vertexUV;
                 
                out vec2 UV;
                 
                void main(){
                    gl_Position =  vec4(vertexPosition_modelspace,1);
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
                    //color = vec4(1,0,0,1);
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

        public RectShader() throws IOException {
            super(vertShader, fragShader);
            mvpUniform = getUniformLocation("MVP");
            colorUniform = getUniformLocation("colorToSet");
            useColorUniform = getUniformLocation("useColor");
        }
    }


    // Vertex Buffer
    static final float[] centeredVertices = {
            -1f, -1f, 0, // Top-left
            -1f, 1f, 0, // Bottom-left
            1f, 1f, 0,// Bottom-right
            1f, -1f, 0,// Top-right
    };

    // UV Buffer (assuming full texture coverage)
    static final float[] uv = {
            0.0f, 1.0f, // Top-left
            0.0f, 0.0f, // Bottom-left
            1.0f, 0.0f, // Bottom-right
            1.0f, 1.0f // Top-right
    };

    static final int[] indicies = {0, 1, 2, 2, 3, 0};


    VBO vertBuffer;
    VBO uvBuffer;
    IndexVBO indiciesBuffer;
    public final int vao;
    private int vertLength;
    public RectShader shader;
    private int textureID;
    private Vector4f color = new Vector4f(0, 0, 0, 0);
    private boolean useColor = false;

    public RectOverlay() {
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao); //Every mesh should have its own VAO

        indiciesBuffer = new IndexVBO();
        vertBuffer = new VBO(0);
        uvBuffer = new VBO(1);

        vertBuffer.bind();
        vertBuffer.specifyAttributes(3, GL11.GL_FLOAT);
        vertBuffer.enable();
        uvBuffer.bind();
        uvBuffer.specifyAttributes(2, GL11.GL_FLOAT);
        uvBuffer.enable();
        try {
            shader = new RectShader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sendBuffersToGPU(centeredVertices, uv, indicies);
    }

    private void sendBuffersToGPU(float[] g_vertex_buffer_data, float[] g_uv_buffer_data, int[] indicies) {
        vertBuffer.bind();
        vertBuffer.sendToGPU(g_vertex_buffer_data);
        uvBuffer.bind();
        uvBuffer.sendToGPU(g_uv_buffer_data);
        indiciesBuffer.bind();
        indiciesBuffer.sendToGPU(indicies);
        vertLength = indicies.length;
    }

    public void setTextureID(int textureID) {
        this.textureID = textureID;
        shader.useTexture();
        this.useColor = false;
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        shader.useColor(color);
        this.useColor = true;
    }

    private void innerDraw(int textureID) {
        GL30.glBindVertexArray(vao);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);//required to assign texture to mesh
        //and to draw the mesh, simply replace glDrawArrays by this...
        // Draw the triangles !
        GL15.glDrawElements(
                GL11.GL_TRIANGLES, // mode
                vertLength, // count
                GL11.GL_UNSIGNED_INT, // typeReference
                0L // element array buffer offset (long 0)
        );
        GL30.glBindVertexArray(0);
    }

    public void draw() {
        if (useColor && color.w == 0) return;
        shader.bind();
//        if (wireframe) {
//            GL11.glLineWidth(2); //Set the line width
//            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // Enable wireframe mode
//            innerDraw(0);
//            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // Disable wireframe mode
//        }
        innerDraw(textureID);
        shader.unbind();
    }
}
