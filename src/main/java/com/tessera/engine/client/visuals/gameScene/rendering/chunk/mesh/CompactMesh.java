/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene.rendering.chunk.mesh;

import java.nio.IntBuffer;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.gameScene.rendering.Mesh;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.ChunkShader;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.ARBOcclusionQuery.glGenQueriesARB;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.glPolygonMode;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

/**
 * @author zipCoder933
 */
public class CompactMesh extends Mesh {

    private int vao, vbo, textureID, vertLength;
    final static int VALUES_PER_VERTEX = 3;

    public boolean isEmpty() {
        return vertLength == 0;
    }

    public void makeEmpty() {
        vertLength = 0;
    }

    public CompactMesh() {
        vao = GL30.glGenVertexArrays();//Every chunk gets its own VAO
        vbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glVertexAttribPointer(//Specifies how the data in the buffer is to be interpreted. In this case, it configures attribute 0 to expect 3 floats.
                0, // attribute id
                VALUES_PER_VERTEX, // size
                GL11.GL_FLOAT, // typeReference
                false, 0, 0
        );
        GL20.glEnableVertexAttribArray(0); //Enables the vertex attribute array at index 0.
        GL30.glBindVertexArray(0);
    }

    /**
     * @param textureID the textureID to set
     */
    public void setTextureID(int textureID) {
        this.textureID = textureID;
    }


    public void sendBuffersToGPU(IntBuffer g_vertex_buffer_data) {
        vertLength = g_vertex_buffer_data.capacity() / VALUES_PER_VERTEX;


        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, g_vertex_buffer_data, GL15.GL_STATIC_DRAW); //send data to the GPU
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void sendBuffersToGPU(int[] g_vertex_buffer_data, int size) {
        vertLength = size / VALUES_PER_VERTEX;


        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, g_vertex_buffer_data, GL15.GL_STATIC_DRAW); //send data to the GPU
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void sendBuffersToGPU(IntBuffer g_vertex_buffer_data, int size) {
        vertLength = size / VALUES_PER_VERTEX;


        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, g_vertex_buffer_data, GL15.GL_STATIC_DRAW); //send data to the GPU
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        GL30.glDeleteVertexArrays(vao);
        GL30.glDeleteBuffers(vbo);
    }

    public void draw(boolean wireframe) {
        if (isEmpty()) {
            return;
        }
        GL30.glBindVertexArray(vao);
        if (wireframe) {
            ClientWindow.printDebugsEnabled(false);
            GL11.glLineWidth(2); //Set the line width
            ClientWindow.printDebugsEnabled(true);
            GL11.glBindTexture(GL33.GL_TEXTURE_2D_ARRAY, 0);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // Enable wireframe mode
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertLength);//We can specify what vertex to start at and how many verticies to draw
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // Disable wireframe mode
        }

        GL11.glBindTexture(GL33.GL_TEXTURE_2D_ARRAY, textureID);//required to assign texture to mesh
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertLength); //We have to specify how many verticies we want}
    }

    public void draw(ChunkShader shader, boolean wireframe) {
        shader.bind();
        if (wireframe) {
            shader.setColorMode(1, 1, 1);
            draw(true);
            shader.setTextureMode();
        }
        draw(false);
        shader.unbind();
    }

    @Override
    public String toString() {
        return "CompactMesh{" +
                "vao=" + vao +
                ", vbo=" + vbo +
                ", texture=" + textureID +
                ", vertLength=" + vertLength +
                '}';
    }
}
