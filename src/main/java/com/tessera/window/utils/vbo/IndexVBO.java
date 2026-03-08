/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.vbo;

import org.lwjgl.opengl.GL15;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;

public class IndexVBO {

    public final int bufferID;

    public IndexVBO() {
        bufferID = GL15.glGenBuffers();
    }

    public void bind() {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, bufferID);
    }

    public void unbind() {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void sendToGPU(float[] data) {
        bind();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
    }

    public void sendToGPU(int[] data) {
        bind();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW); //send data to the GPU
    }

    public void delete() {
        glDeleteBuffers(bufferID);
    }
}
