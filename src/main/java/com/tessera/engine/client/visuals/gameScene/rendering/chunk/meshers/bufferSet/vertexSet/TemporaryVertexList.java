/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

/**
 * @author zipCoder933
 */
class TemporaryVertexList {

    private List<IntBuffer> verts1 = new ArrayList<>();

    public void clear() {
        verts1.clear();
    }

    public int size() {
        return verts1.size();
    }

    public IntBuffer getVert(int index) {
        return verts1.get(index);
    }

    public void addVert(int firstInt, int secondInt, int thridInt) {  //The arraylist is a minor contributor to the memory usage.
        IntBuffer vert = MemoryUtil.memAllocInt(CompactVertexSet.VECTOR_ELEMENTS);
        vert.put(0, firstInt);
        vert.put(1, secondInt);
        vert.put(2, thridInt);
        verts1.add(vert);
    }
}
