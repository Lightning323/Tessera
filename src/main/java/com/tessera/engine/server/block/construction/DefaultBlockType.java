/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.server.block.construction;

import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.world.chunk.Chunk;

/**
 * @author zipCoder933
 */
public class DefaultBlockType extends BlockType {

    public DefaultBlockType() {
        super();
        name = "default";
        initializationCallback = (b) -> {
            b.easierMiningTool_tag = "pickaxe";
        };
    }

    public int getGreedyMesherPermissions() {
        return ALWAYS_USE_GM;
    }

    @Override
    public boolean constructBlock(VertexSet buffer,
                                  Block block, BlockData data, Block[] neighbors, BlockData[] neighborData, byte[] light,
                                  Chunk chunk, int chunkX, int chunkY, int chunkZ, boolean isUsingGreedyMesher) {
        BlockTexture.FaceTexture texLayer;

        if (sideIsVisible(block, neighbors[NEG_X])) {
            texLayer = (block.texture.getNEG_X());
            //NEG_X FACE:
            buffer.vertex(chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, NEG_X, texLayer, light[NEG_X]);
            buffer.vertex(chunkX, chunkY, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_X, texLayer, light[NEG_X]);
            buffer.vertex(chunkX, chunkY, 1.0f + chunkZ, /* uvs */ 1.0f, 0.0f, NEG_X, texLayer, light[NEG_X]);

            buffer.vertex(chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, NEG_X, texLayer, light[NEG_X]);
            buffer.vertex(chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 0.0f, 1.0f, NEG_X, texLayer, light[NEG_X]);
            buffer.vertex(chunkX, chunkY, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_X, texLayer, light[NEG_X]);
        }

        if (sideIsVisible(block, neighbors[POS_X])) {
            texLayer = (block.texture.getPOS_X());
            //POS_X FACE:
            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 1.0f, 1.0f, POS_X, texLayer, light[POS_X]);
            buffer.vertex(1.0f + chunkX, chunkY, 1.0f + chunkZ, /* uvs */ 0.0f, 0.0f, POS_X, texLayer, light[POS_X]);
            buffer.vertex(1.0f + chunkX, chunkY, chunkZ, /* uvs */ 1.0f, 0.0f, POS_X, texLayer, light[POS_X]);

            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 1.0f, 1.0f, POS_X, texLayer, light[POS_X]);
            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 0.0f, 1.0f, POS_X, texLayer, light[POS_X]);
            buffer.vertex(1.0f + chunkX, chunkY, 1.0f + chunkZ, /* uvs */ 0.0f, 0.0f, POS_X, texLayer, light[POS_X]);
        }

        if (sideIsVisible(block, neighbors[POS_Y])) {
            texLayer = (block.texture.getPOS_Y());
            //POS_Y FACE:
            buffer.vertex(1.0f + chunkX, chunkY, chunkZ, /* uvs */ 1.0f, 0.0f, POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(chunkX, chunkY, 1.0f + chunkZ, /* uvs */ -0.0f, 1.0f, POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(chunkX, chunkY, chunkZ, /* uvs */ 0.0f, 0.0f, POS_Y, texLayer, light[POS_Y]);

            buffer.vertex(1.0f + chunkX, chunkY, chunkZ, /* uvs */ 1.0f, 0.0f, POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(1.0f + chunkX, chunkY, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, POS_Y, texLayer, light[POS_Y]);
            buffer.vertex(chunkX, chunkY, 1.0f + chunkZ, /* uvs */ -0.0f, 1.0f, POS_Y, texLayer, light[POS_Y]);
        }

        if (sideIsVisible(block, neighbors[NEG_Z])) {
            texLayer = (block.texture.getNEG_Z());
            //NEG_Z FACE:
            buffer.vertex(chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 1.0f, 1.0f, NEG_Z, texLayer, light[NEG_Z]);
            buffer.vertex(1.0f + chunkX, chunkY, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_Z, texLayer, light[NEG_Z]);
            buffer.vertex(chunkX, chunkY, chunkZ, /* uvs */ 1.0f, 0.0f, NEG_Z, texLayer, light[NEG_Z]);

            buffer.vertex(chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 1.0f, 1.0f, NEG_Z, texLayer, light[NEG_Z]);
            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 0.0f, 1.0f, NEG_Z, texLayer, light[NEG_Z]);
            buffer.vertex(1.0f + chunkX, chunkY, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_Z, texLayer, light[NEG_Z]);
        }

        if (sideIsVisible(block, neighbors[NEG_Y])) {
            texLayer = (block.texture.getNEG_Y());
            //NEG_Y FACE:
            buffer.vertex(chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 1.0f, 0.0f, NEG_Y, texLayer, light[NEG_Y]);
            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 0.0f, 1.0f, NEG_Y, texLayer, light[NEG_Y]);
            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 0.0f, 0.0f, NEG_Y, texLayer, light[NEG_Y]);

            buffer.vertex(chunkX, 1.0f + chunkY, chunkZ, /* uvs */ 1.0f, 0.0f, NEG_Y, texLayer, light[NEG_Y]);
            buffer.vertex(chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, NEG_Y, texLayer, light[NEG_Y]);
            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 0.0f, 1.0f, NEG_Y, texLayer, light[NEG_Y]);
        }

        if (sideIsVisible(block, neighbors[POS_Z])) {
            texLayer = (block.texture.getPOS_Z());
            //POS_Z FACE:
            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, POS_Z, texLayer, light[POS_Z]);
            buffer.vertex(chunkX, chunkY, 1.0f + chunkZ, /* uvs */ 0.0f, 0.0f, POS_Z, texLayer, light[POS_Z]);
            buffer.vertex(1.0f + chunkX, chunkY, 1.0f + chunkZ, /* uvs */ 1.0f, 0.0f, POS_Z, texLayer, light[POS_Z]);

            buffer.vertex(1.0f + chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 1.0f, 1.0f, POS_Z, texLayer, light[POS_Z]);
            buffer.vertex(chunkX, 1.0f + chunkY, 1.0f + chunkZ, /* uvs */ 0.0f, 1.0f, POS_Z, texLayer, light[POS_Z]);
            buffer.vertex(chunkX, chunkY, 1.0f + chunkZ, /* uvs */ 0.0f, 0.0f, POS_Z, texLayer, light[POS_Z]);
        }
        return isUsingGreedyMesher;
    }

    public boolean sideIsVisible(Block block, Block NEG_X) {
        return !NEG_X.opaque;
    }

    @Override
    public boolean isCubeShape() {
        return true;
    }


}
