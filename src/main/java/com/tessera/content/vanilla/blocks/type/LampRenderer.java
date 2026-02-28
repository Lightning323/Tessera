/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.blocks.type;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockType;
import com.tessera.engine.server.block.construction.BlockTypeModel.BlockModel;
import com.tessera.engine.server.block.construction.BlockTypeModel.BlockModelLoader;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.content.vanilla.blocks.RenderType;

import java.io.IOException;

/**
 * @author zipCoder933
 */
public class LampRenderer extends BlockType {
    BlockModel lamp;
    BlockModel[] fenceSide, sideBlock;

    @Override
    public boolean allowExistence(Block block, int worldX, int worldY, int worldZ) {
        return sideIsSolid(Client.world.getBlock(worldX, worldY + 1, worldZ)) ||
                sideIsSolid(Client.world.getBlock(worldX + 1, worldY, worldZ)) ||
                sideIsSolid(Client.world.getBlock(worldX - 1, worldY, worldZ)) ||
                sideIsSolid(Client.world.getBlock(worldX, worldY, worldZ + 1)) ||
                sideIsSolid(Client.world.getBlock(worldX, worldY, worldZ - 1));
    }
    
    boolean sideIsSolid(Block block) {
        return block.solid;
    }

    void drawSide(int i, Block neighbor,
                  VertexSet buffers, Block block, Block[] neighbors, byte[] lightValues, int x, int y, int z) {

        if (neighbor.type == RenderType.FENCE) {
            fenceSide[i].render(buffers, block, neighbors, lightValues, x, y, z);
        } else {
            sideBlock[i].render(buffers, block, neighbors, lightValues, x, y, z);
        }
    }

    @Override
    public boolean constructBlock(VertexSet buffers, Block block, BlockData data, Block[] neighbors, BlockData[] neighborData, byte[] lightValues, Chunk chunk, int chunkX, int chunkY, int chunkZ, boolean isUsingGreedyMesher) {
        if (data != null && data.size() > 0) {
            int dataValue = data.get(0);
            if (sideIsSolid(neighbors[POS_Z]) && dataValue == 2) {
                drawSide(2, neighbors[POS_Z],
                        buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else if (sideIsSolid(neighbors[NEG_X]) && dataValue == 3) {
                drawSide(3, neighbors[NEG_X],
                        buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else if (sideIsSolid(neighbors[NEG_Z]) && dataValue == 0) {
                drawSide(0, neighbors[NEG_Z],
                        buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else if (sideIsSolid(neighbors[POS_X]) && dataValue == 1) {
                drawSide(1, neighbors[POS_X],
                        buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else {
                lamp.render(buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            }
        } else {
            if (sideIsSolid(neighbors[POS_Y])) {
                lamp.render(buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else if (sideIsSolid(neighbors[POS_Z])) {
                drawSide(2, neighbors[POS_Z],
                        buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else if (sideIsSolid(neighbors[NEG_X])) {
                drawSide(3, neighbors[NEG_X],
                        buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else if (sideIsSolid(neighbors[NEG_Z])) {
                drawSide(0, neighbors[NEG_Z],
                        buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else if (sideIsSolid(neighbors[POS_X])) {
                drawSide(1, neighbors[POS_X],
                        buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            } else {
                lamp.render(buffers, block, neighbors, lightValues, chunkX, chunkY, chunkZ);
            }
        }
        return false;
    }


    public LampRenderer() throws IOException {
        // ObjToBlockModel.parseFile(null, false,
        //         1.6f, ResourceUtils.resource("block types\\lamp\\lamp.obj"));
        // ObjToBlockModel.parseFileWithYRotations(false, 1.6f,
        //         ResourceUtils.resource("block types\\lamp\\side.obj"));
        // ObjToBlockModel.parseFileWithYRotations(false, 1.6f,
        //         ResourceUtils.resource("block types\\lamp\\side block.obj"));

        lamp = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/lamp\\lamp.blockType"),
                (t, n) -> shouldRenderFace_subBlock(t, n));
        fenceSide = new BlockModel[4];
        sideBlock = new BlockModel[4];
        for (int i = 0; i < 4; i++) {
            fenceSide[i] = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/lamp\\side" + i + ".blockType"),
                    (t, n) -> shouldRenderFace_subBlock(t, n));
            sideBlock[i] = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/lamp\\side block" + i + ".blockType"),
                    (t, n) -> shouldRenderFace_subBlock(t, n));
        }
        initializationCallback = (b) -> {
            b.opaque = false;
            b.solid = true;
        };
    }

    final float ONE_SIXTEENTH = 0.0625f;

    @Override
    public void getCursorBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        float a = ONE_SIXTEENTH * 3;
        float b = ONE_SIXTEENTH * 6;
        box.setPosAndSize(x + a, y + (ONE_SIXTEENTH * 2), z + a, 1 - b, 1 - (ONE_SIXTEENTH * 3), 1 - b);
        consumer.accept(box);
    }

}
