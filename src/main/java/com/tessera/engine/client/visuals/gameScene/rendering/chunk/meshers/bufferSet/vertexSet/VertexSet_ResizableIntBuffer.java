package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet;

import com.tessera.engine.client.visuals.gameScene.rendering.chunk.mesh.CompactMesh;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.ResizableIntBuffer;

public class VertexSet_ResizableIntBuffer extends CompactVertexSet {


    private final ResizableIntBuffer verts = new ResizableIntBuffer(6000);

    public int size() {
        return verts.size();
    }

    public void makeVertexSet() {     //The main contributor to the memory usage is the IntBuffer that gets created here

    }


    public void reset() {
        verts.clear();
    }

    @Override
    public void sendToMesh(CompactMesh mesh) {
        if(verts.size() == 0) {
            return;
        }
        verts.getBuffer().clear();
        mesh.sendBuffersToGPU(verts.getBuffer(), verts.size());
        reset();
    }
    //</editor-fold>

    public void vertex(int layer, int a, int b, int c) {
        verts.add(a);
        verts.add(b);
        verts.add(c);
    }

}
