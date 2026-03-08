/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene.rendering.entity;

import com.tessera.engine.client.visuals.gameScene.rendering.Mesh;
import com.tessera.window.utils.obj.OBJ;
import com.tessera.window.utils.obj.OBJLoader;
import com.tessera.window.utils.obj.buffers.OBJBufferSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.lwjgl.opengl.GL11;

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
public class EntityMesh extends Mesh {

    protected int vao;
    protected int positionVBO;
    protected int uvVBO;
    protected int vertLength;

    public boolean isEmpty() {
        return vertLength == 0;
    }

    public void reset() {
        vertLength = 0;
    }

    public EntityMesh() {
        initVbos();
    }

    protected void initVbos() {
        vao = GL30.glGenVertexArrays();
        positionVBO = GL15.glGenBuffers();
        uvVBO = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionVBO);
        GL20.glVertexAttribPointer(0/*id*/, 3/*size*/, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvVBO);
        GL20.glVertexAttribPointer(1/*id*/, 2/*size*/, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    public void loadFromOBJ(File file) throws IOException {
        OBJ objModel = OBJLoader.loadModel(file);
        loadFromOBJ(objModel);
    }

    public void loadFromOBJ(InputStream stream) throws IOException {
        OBJ objModel = OBJLoader.loadModel(stream);
        loadFromOBJ(objModel);
    }

    public void loadFromOBJ(OBJ obj) {
        OBJBufferSet buffers = new OBJBufferSet(obj);

        buffers.makeBuffers();
        sendBuffersToGPU(buffers.vertBuffer, buffers.uvBuffer);
    }

    public void sendBuffersToGPU(float[] position, float[] uvs) {
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, position, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, uvVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, uvs, GL15.GL_STATIC_DRAW);

        vertLength = position.length / 3;
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        GL30.glDeleteVertexArrays(vao);
        GL30.glDeleteBuffers(positionVBO);
        GL30.glDeleteBuffers(uvVBO);
    }

    public void draw(boolean wireframe, int textureID) {
        GL30.glBindVertexArray(vao);

        if (wireframe) {
            GL11.glLineWidth(1); //Set the line width
            GL11.glBindTexture(GL33.GL_TEXTURE_2D, 0);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // Enable wireframe mode
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertLength); //We have to specify how many verticies we want
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); // Disable wireframe mode
        }

        GL11.glBindTexture(GL33.GL_TEXTURE_2D, textureID);//required to assign texture to mesh
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertLength); //We have to specify how many verticies we want
    }
}
