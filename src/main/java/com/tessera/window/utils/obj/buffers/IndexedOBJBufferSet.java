/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.obj.buffers;

import com.tessera.window.utils.obj.IndexedOBJ;

public class IndexedOBJBufferSet {

    public float[] vertBuffer, uvBuffer, normalsBuffer;
    public int[] indicies;

    // Constructor
    public IndexedOBJBufferSet(IndexedOBJ obj) {
        // Convert lists to arrays if needed
        indicies = obj.indices;

        vertBuffer = new float[obj.indexedVertices.size() * 3];
        normalsBuffer = new float[obj.indexedVertices.size() * 3];
        uvBuffer = new float[obj.indexedVertices.size() * 2];

        int j = 0;
        for (int i = 0; i < obj.indexedVertices.size(); i++) {
            vertBuffer[j] = obj.indexedVertices.get(i).x;
            j++;
            vertBuffer[j] = obj.indexedVertices.get(i).y;
            j++;
            vertBuffer[j] = obj.indexedVertices.get(i).z;
            j++;
        }

        j = 0;
        for (int i = 0; i < obj.indexedTextureCoords.size(); i++) {
            uvBuffer[j] = obj.indexedTextureCoords.get(i).x;
            j++;
            uvBuffer[j] = -obj.indexedTextureCoords.get(i).y;
            j++;
        }

        j = 0;
        for (int i = 0; i < obj.indexedNormals.size(); i++) {
            normalsBuffer[j] = obj.indexedNormals.get(i).x;
            j++;
            normalsBuffer[j] = obj.indexedNormals.get(i).y;
            j++;
            normalsBuffer[j] = obj.indexedNormals.get(i).z;
            j++;
        }

        // Now you have the required indexed arrays for rendering
    }
}
