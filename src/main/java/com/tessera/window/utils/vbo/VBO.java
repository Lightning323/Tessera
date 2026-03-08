/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.vbo;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;

public class VBO {

    public final int bufferID, attributeID;

//VBO targets are:
//    GL15.GL_ARRAY_BUFFER
//    GL15.GL_ELEMENT_ARRAY_BUFFER

    public VBO(int attributeID) {
        this.attributeID = attributeID;
        bufferID = GL15.glGenBuffers();
    }

    public void bind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferID);
    }

    public void unbind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void sendToGPU(float[] data) {
        bind();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
    }

    public void sendToGPU(int[] data) {
        bind();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW); //send data to the GPU
    }

    public void specifyAttributes(
            final int size,
            final int type) {
        bind();
        GL20.glVertexAttribPointer( //Specifies how the data in the buffer is to be interpreted in the shader. 
                attributeID, // attribute 0. No particular reason for 0, but must match the layout in the shader.
                size, // size
                type, // typeReference
                false, // normalized?
                0, // stride
                0 // array buffer offset (no offset in this case)
        );
    }

    public void specifyAttributes(
            int size, int type, boolean normalized,
            int stride, long pointer) {
        bind();
        GL20.glVertexAttribPointer(
                attributeID, // attribute 0. No particular reason for 0, but must match the layout in the shader.
                size, // size
                type, // typeReference
                normalized, // normalized?
                stride, // stride
                pointer // array buffer offset (no offset in this case)
        );
    }

    public void enable() {
        bind();
        GL20.glEnableVertexAttribArray(attributeID);
    }

    public void disable() {
        bind();
        GL20.glDisableVertexAttribArray(attributeID);
    }

    public void delete() {
        glDeleteBuffers(bufferID);
    }
}
