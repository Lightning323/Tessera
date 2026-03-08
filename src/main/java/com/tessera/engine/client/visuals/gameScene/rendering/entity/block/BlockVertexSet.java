/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene.rendering.entity.block;

import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityMesh_ArrayTexture;
import org.joml.Vector3f;

import java.util.ArrayList;

/**
 * @author zipCoder933
 */
public class BlockVertexSet extends VertexSet<EntityMesh_ArrayTexture> {
    final ArrayList<Vector3f> positions = new ArrayList<Vector3f>();
    //    ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    final ArrayList<Vector3f> uvs = new ArrayList<Vector3f>();
    //    ArrayList<Integer> light = new ArrayList<Integer>();
    float[] positionsArray, uvsArray;

    public BlockVertexSet() {
    }

    public int size() {
        return positions.size();
    }

    /**
     * Free any buffers and make it ready for the next round
     */
    public void reset() {
        positions.clear();
        uvs.clear();
        positionsArray = null;
        uvsArray = null;
    }

    @Override
    public void makeVertexSet() {
        //Convert arraylists to arrays
        positionsArray = new float[positions.size() * 3];
        for (int i = 0; i < positions.size(); i++) {
            positionsArray[i * 3] = positions.get(i).x;
            positionsArray[i * 3 + 1] = positions.get(i).y;
            positionsArray[i * 3 + 2] = positions.get(i).z;
        }
        uvsArray = new float[uvs.size() * 3];
        for (int i = 0; i < uvs.size(); i++) {
            uvsArray[i * 3] = uvs.get(i).x;
            uvsArray[i * 3 + 1] = uvs.get(i).y;
            uvsArray[i * 3 + 2] = uvs.get(i).z;
        }
    }

    public void sendToMesh(EntityMesh_ArrayTexture mesh) {
        if (positionsArray == null || positionsArray.length == 0) {
            return;
        }
        mesh.sendBuffersToGPU(positionsArray, uvsArray);
        reset();
    }


    public void vertex(float x, float y, float z,
                       float uvX, float uvY, byte normal,
                       BlockTexture.FaceTexture texture, byte light) {
        positions.add(new Vector3f(x, y, z));
        uvs.add(new Vector3f(uvX, uvY, texture.zLayer));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY, int normal,
                       BlockTexture.FaceTexture texture, byte light) {
        positions.add(new Vector3f(x, y, z));
        uvs.add(new Vector3f(uvX, uvY, texture.zLayer));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY,
                       BlockTexture.FaceTexture texture, byte light) {
        positions.add(new Vector3f(x, y, z));
        uvs.add(new Vector3f(uvX, uvY, texture.zLayer));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY, byte normal,
                       int texture, byte animationLength, byte light) {
        positions.add(new Vector3f(x, y, z));
        uvs.add(new Vector3f(uvX, uvY, texture));
    }

}
