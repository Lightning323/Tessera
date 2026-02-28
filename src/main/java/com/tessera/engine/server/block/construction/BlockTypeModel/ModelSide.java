package com.tessera.engine.server.block.construction.BlockTypeModel;

import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;

public class ModelSide {

    public final ModelVertex[] vertices;
    public int textureSide = 0;

    public ModelSide(int vertexCount) {
        vertices = new ModelVertex[vertexCount];
    }

    public void render(int x, int y, int z, VertexSet buff, BlockTexture.FaceTexture texture,
                       byte[] light) {
        
        for (int i = 0; i < vertices.length; i++) {
            ModelVertex vertex = vertices[i];
            buff.vertex(
                    vertex.position.x + x,
                    vertex.position.y + y,
                    vertex.position.z + z,
                    vertex.u, vertex.v, vertex.normal,
                    texture, light[0]);
        }
    }
}
