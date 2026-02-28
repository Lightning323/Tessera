/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.obj.buffers;

import com.tessera.window.utils.obj.OBJ;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 *
 * @author zipCoder933
 */
public class OBJBufferSet {

    ArrayList<Vector3f> verts = new ArrayList<>();
    ArrayList<Vector3f> normals = new ArrayList<>();
    ArrayList<Vector2f> uvs = new ArrayList<>();

    public float[] vertBuffer, uvBuffer, normalsBuffer;

    public OBJBufferSet(OBJ obj) {
        verts = new ArrayList<>();
        normals = new ArrayList<>();
        uvs = new ArrayList<>();

        for (OBJ.Face f : obj.getFaces()) {
            int v1 = f.getVertexCoordinates()[0];
            int v2 = f.getVertexCoordinates()[1];
            int v3 = f.getVertexCoordinates()[2];

            verts.add(obj.getVertexCoordinates().get(v1 - 1));
            verts.add(obj.getVertexCoordinates().get(v2 - 1));
            verts.add(obj.getVertexCoordinates().get(v3 - 1));

            if (f.getNormals() != null) {
                int n1 = f.getNormals()[0];
                int n2 = f.getNormals()[1];
                int n3 = f.getNormals()[2];

                normals.add(obj.getNormals().get(n1 - 1));
                normals.add(obj.getNormals().get(n2 - 1));
                normals.add(obj.getNormals().get(n3 - 1));
            }
            int uv1 = f.getTextureCoords()[0];
            int uv2 = f.getTextureCoords()[1];
            int uv3 = f.getTextureCoords()[2];

            uvs.add(obj.getTextureCoordinates().get(uv1 - 1));
            uvs.add(obj.getTextureCoordinates().get(uv2 - 1));
            uvs.add(obj.getTextureCoordinates().get(uv3 - 1));
        }

    }

    public void makeBuffers() {
        vertBuffer = new float[verts.size() * 3];
        uvBuffer = new float[verts.size() * 2];
        normalsBuffer = new float[verts.size() * 3];

        int vertIndex = 0;

        for (int i = 0; i < verts.size(); i++) {
            vertBuffer[vertIndex] = verts.get(i).x;
            vertIndex++;
            vertBuffer[vertIndex] = verts.get(i).y;
            vertIndex++;
            vertBuffer[vertIndex] = verts.get(i).z;
            vertIndex++;
        }
        vertIndex = 0;

        for (int i = 0; i < normals.size(); i++) {
            normalsBuffer[vertIndex] = normals.get(i).x;
            vertIndex++;
            normalsBuffer[vertIndex] = normals.get(i).y;
            vertIndex++;
            normalsBuffer[vertIndex] = normals.get(i).z;
            vertIndex++;
        }

        vertIndex = 0;
        for (int i = 0; i < uvs.size(); i++) {
            uvBuffer[vertIndex] = uvs.get(i).x;
            vertIndex++;
            uvBuffer[vertIndex] = -uvs.get(i).y;
            vertIndex++;
        }
    }
}
