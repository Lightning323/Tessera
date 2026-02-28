/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.blocks.type;

import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockType;
import com.tessera.engine.server.block.construction.BlockTypeModel.BlockModel;
import com.tessera.engine.server.block.construction.BlockTypeModel.BlockModelLoader;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;

import java.io.IOException;

/**
 * @author zipCoder933
 */
public class PillarRenderer extends BlockType {

    BlockModel pillar;

    public PillarRenderer() throws IOException {
        // ObjToBlockModel.parseFile(null, false, 1.6f,
        //         ResourceUtils.resource("block types\\pillar.obj"));

        pillar = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/pillar.blockType"), renderSide_subBlock);
        initializationCallback = (b) -> {
            b.opaque = false;
            b.solid = true;
        };
    }

    @Override
    public boolean constructBlock(VertexSet buffers, Block block, BlockData data,
                                  Block[] neighbors, BlockData[] neighborData, byte[] light, Chunk chunk, int chunkX, int chunkY, int chunkZ, boolean isUsingGreedyMesher) {
        pillar.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);

        return false;
    }

    @Override
    public void getCursorBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        getCollisionBoxes(consumer, box, block, data, x, y, z);
    }

    private final float sixteenthConstant = 0.0625f;
    private final float width = 1 - (sixteenthConstant * 4);

    @Override
    public void getCollisionBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        box.setPosAndSize(x + (sixteenthConstant * 2), y, z + (sixteenthConstant * 2), width, 1, width);
        consumer.accept(box);
    }
}
