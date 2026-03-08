package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet;

import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.mesh.CompactMesh;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class TraditionalVertexSet extends CompactVertexSet {


    private TemporaryVertexList[] verts = {new TemporaryVertexList()};
    private IntBuffer buffer;
//    private final ResizableIntBuffer buffer = new ResizableIntBuffer(6000);

    public int size() {
        int size = 0;
        for (TemporaryVertexList vert : verts) {
            size += vert.size();
        }
        return size;
    }


    public void makeVertexSet() {     //The main contributor to the memory usage is the IntBuffer that gets created here
        int size = size();
        if (size == 0) { //This not only skips the creation of the buffer, but also prevents empty vertex sets from being created
            reset(); //Reset makes the buffer null, preventing it from being sent to the mesh
            return;
        }

        int vertIndex = 0;
//        buffer.resize(size() * BufferSet.VECTOR_ELEMENTS);
        buffer = MemoryUtil.memAllocInt(size * VECTOR_ELEMENTS);

        for (int buffIndex = 0; buffIndex < verts.length; buffIndex++) {
            for (int i = 0; i < verts[buffIndex].size(); i++) {
                IntBuffer vertex = verts[buffIndex].getVert(i);
                buffer.put(vertIndex, vertex.get(0));
                buffer.put(vertIndex + 1, vertex.get(1));
                buffer.put(vertIndex + 2, vertex.get(2));
                MemoryUtil.memFree(vertex);
                vertIndex += 3;
            }
        }
//        buffer.flip();
        for (TemporaryVertexList vert : verts) {
            vert.clear();
        }
    }

    public void sendToMesh(CompactMesh mesh) {
        mesh.makeEmpty();//This is crucial, so that empty meshes are declared as empty
        if (buffer != null) {//If the buffer is not null, send it to the mesh
            mesh.sendBuffersToGPU(buffer);
            reset();
        }
    }

    public void reset() {
        if (buffer != null) MemoryUtil.memFree(buffer);
        buffer = null;
    }
    //</editor-fold>

    public void vertex(int layer, int a, int b, int c) {
        verts[layer].addVert(a, b, c);
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY, byte normal,
                       BlockTexture.FaceTexture texture, byte light) {
        verts[0].addVert(packFirstInt(x, y, normal, texture.getAnimationFrames()),
                packSecondInt(z, uvX, uvY),
                packThirdInt(texture.zLayer, light));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY, int normal,
                       BlockTexture.FaceTexture texture, byte light) {
        verts[0].addVert(packFirstInt(x, y, (byte) normal, texture.getAnimationFrames()),
                packSecondInt(z, uvX, uvY),
                packThirdInt(texture.zLayer, light));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY,
                       BlockTexture.FaceTexture texture, byte light) {
        verts[0].addVert(packFirstInt(x, y, (byte) 0, texture.getAnimationFrames()),
                packSecondInt(z, uvX, uvY),
                packThirdInt(texture.zLayer, light));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY, byte normal,
                       int texture, byte animationLength, byte light) {
        verts[0].addVert(packFirstInt(x, y, normal, animationLength),
                packSecondInt(z, uvX, uvY),
                packThirdInt(texture, light));
    }
}
