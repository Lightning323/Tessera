/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.blocks.type;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.server.block.construction.BlockType;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.world.chunk.BlockData;

import com.tessera.engine.common.world.chunk.Chunk;
import org.joml.Vector3f;

/**
 * @author zipCoder933
 */
public class SpriteRenderer extends BlockType {

    public SpriteRenderer() {
        super();
        replaceOnSet = true;
        generate3DIcon = false;
        initializationCallback = (b) -> {
            b.easierMiningTool_tag = "shovel";
            b.opaque = false;
            b.solid = false;
            b.toughness = 0f;
        };
    }

    static Vector3f[] vertices = {
            new Vector3f(0, 0, 0),//0
            new Vector3f(1, 0, 0),//1
            new Vector3f(1, 0, 1),//2
            new Vector3f(0, 0, 1),//3
            new Vector3f(0, 1, 0),//4
            new Vector3f(1, 1, 0),//5
            new Vector3f(1, 1, 1),//6
            new Vector3f(0, 1, 1)};//7
    static int[] indx = {
            0, 2, 6,
            0, 6, 4,
            3, 1, 5,
            3, 5, 7,
            2, 0, 4,
            2, 4, 6,
            1, 3, 7,
            1, 7, 5,};


    public boolean allowExistence(Block block, int worldX, int worldY, int worldZ) {
        Block belowBlock = Client.world.getBlock(worldX, worldY + 1, worldZ);
        return !belowBlock.isAir() && !belowBlock.isLiquid();
    }

    @Override
    public boolean constructBlock(VertexSet buffers,
                                  Block block, BlockData data, Block[] neighbors, BlockData[] neighborData, byte[] light,
                                  Chunk chunk, int chunkX, int chunkY, int chunkZ, boolean isUsingGreedyMesher) {

        BlockTexture.FaceTexture texLayer = block.texture.getNEG_X();

        buffers.vertex(vertices[indx[0]].x + chunkX, vertices[indx[0]].y + chunkY, vertices[indx[0]].z + chunkZ, 1.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[1]].x + chunkX, vertices[indx[1]].y + chunkY, vertices[indx[1]].z + chunkZ, 0.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[2]].x + chunkX, vertices[indx[2]].y + chunkY, vertices[indx[2]].z + chunkZ, 0.0f, 1.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[3]].x + chunkX, vertices[indx[3]].y + chunkY, vertices[indx[3]].z + chunkZ, 1.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[4]].x + chunkX, vertices[indx[4]].y + chunkY, vertices[indx[4]].z + chunkZ, 0.0f, 1.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[5]].x + chunkX, vertices[indx[5]].y + chunkY, vertices[indx[5]].z + chunkZ, 1.0f, 1.0f, texLayer, light[0]);

        buffers.vertex(vertices[indx[6]].x + chunkX, vertices[indx[6]].y + chunkY, vertices[indx[6]].z + chunkZ, 1.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[7]].x + chunkX, vertices[indx[7]].y + chunkY, vertices[indx[7]].z + chunkZ, 0.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[8]].x + chunkX, vertices[indx[8]].y + chunkY, vertices[indx[8]].z + chunkZ, 0.0f, 1.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[9]].x + chunkX, vertices[indx[9]].y + chunkY, vertices[indx[9]].z + chunkZ, 1.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[10]].x + chunkX, vertices[indx[10]].y + chunkY, vertices[indx[10]].z + chunkZ, 0.0f, 1.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[11]].x + chunkX, vertices[indx[11]].y + chunkY, vertices[indx[11]].z + chunkZ, 1.0f, 1.0f, texLayer, light[0]);

        buffers.vertex(vertices[indx[12]].x + chunkX, vertices[indx[12]].y + chunkY, vertices[indx[12]].z + chunkZ, 1.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[13]].x + chunkX, vertices[indx[13]].y + chunkY, vertices[indx[13]].z + chunkZ, 0.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[14]].x + chunkX, vertices[indx[14]].y + chunkY, vertices[indx[14]].z + chunkZ, 0.0f, 1.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[15]].x + chunkX, vertices[indx[15]].y + chunkY, vertices[indx[15]].z + chunkZ, 1.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[16]].x + chunkX, vertices[indx[16]].y + chunkY, vertices[indx[16]].z + chunkZ, 0.0f, 1.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[17]].x + chunkX, vertices[indx[17]].y + chunkY, vertices[indx[17]].z + chunkZ, 1.0f, 1.0f, texLayer, light[0]);

        buffers.vertex(vertices[indx[18]].x + chunkX, vertices[indx[18]].y + chunkY, vertices[indx[18]].z + chunkZ, 1.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[19]].x + chunkX, vertices[indx[19]].y + chunkY, vertices[indx[19]].z + chunkZ, 0.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[20]].x + chunkX, vertices[indx[20]].y + chunkY, vertices[indx[20]].z + chunkZ, 0.0f, 1.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[21]].x + chunkX, vertices[indx[21]].y + chunkY, vertices[indx[21]].z + chunkZ, 1.0f, 0.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[22]].x + chunkX, vertices[indx[22]].y + chunkY, vertices[indx[22]].z + chunkZ, 0.0f, 1.0f, texLayer, light[0]);
        buffers.vertex(vertices[indx[23]].x + chunkX, vertices[indx[23]].y + chunkY, vertices[indx[23]].z + chunkZ, 1.0f, 1.0f, texLayer, light[0]);
        return false;
    }
//

    public final float ONE_SIXTEENTH = (float) 1 / 16;

    @Override
    public void getCursorBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        float width = 1 - (ONE_SIXTEENTH * 2);
        box.setPosAndSize(x + (ONE_SIXTEENTH), y, z + (ONE_SIXTEENTH), width, 1, width);
        consumer.accept(box);
    }

    @Override
    public void getCollisionBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
    }

}
