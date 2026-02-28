/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene.rendering;

import com.tessera.engine.server.block.construction.BlockTexture;

/**
 * @author zipCoder933
 */
public abstract class VertexSet<T extends Mesh> {
    public VertexSet() {
    }

    public abstract int size();

    /**
     * Free any buffers and make it ready for the next round
     */
    public abstract void reset();

    public abstract void makeVertexSet();

    public abstract void sendToMesh(T mesh);


    public abstract void vertex(float x, float y, float z,
                                float uvX, float uvY, byte normal,
                                BlockTexture.FaceTexture texture, byte light);

    public abstract void vertex(float x, float y, float z,
                                float uvX, float uvY, int normal,
                                BlockTexture.FaceTexture texture, byte light);

    public abstract void vertex(float x, float y, float z,
                                float uvX, float uvY,
                                BlockTexture.FaceTexture texture, byte light);

    public abstract void vertex(float x, float y, float z,
                                float uvX, float uvY, byte normal,
                                int texture, byte animationLength, byte light);

}
