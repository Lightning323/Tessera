/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.blocks.type;

import com.tessera.Main;
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
public class WallItemRenderer extends BlockType {

    BlockModel wall0, wall1, wall2, wall3;

    public WallItemRenderer() throws IOException {
        initializationCallback = (b) -> {

            b.initialBlockData = (existingData) -> {
                return Main.getClient().userPlayer.camera.simplifiedPanTiltAsBlockData(new BlockData(2));
            };

            b.opaque = false;
            b.solid = false;
            b.climbable = true;
            b.toughness = 0.1f;
        };
        generate3DIcon = false;
        wall0 = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/wall\\wall0.blockType"), renderSide_subBlock);
        wall1 = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/wall\\wall1.blockType"), renderSide_subBlock);
        wall2 = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/wall\\wall2.blockType"), renderSide_subBlock);
        wall3 = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/wall\\wall3.blockType"), renderSide_subBlock);
    }

    public boolean allowExistence(Block block, int worldX, int worldY, int worldZ) {
        return true;
//        Block testBlock;
//        if (data == null || data.get(0) == 3) {
//            testBlock = GameScene.world.getBlock(worldX - 1, worldY, worldZ);
//        } else if (data.get(0) == 0) {
//            testBlock = GameScene.world.getBlock(worldX, worldY, worldZ - 1);
//        } else if (data.get(0) == 1) {
//            testBlock = GameScene.world.getBlock(worldX + 1, worldY, worldZ);
//        } else {
//            testBlock = GameScene.world.getBlock(worldX, worldY, worldZ + 1);
//        }
//
//        BlockType itemType = ItemList.blocks.getBlockType(testBlock.typeReference);
//        if (itemType == null) {
//            return false;
//        }
//
//        return testBlock.typeReference != BlockList.LIQUID_BLOCK_TYPE_ID
//                && !testBlock.isAir() &&
//                (testBlock.solid || itemType.isCubeShape());
    }


    @Override
    public void getCollisionBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
    }

    final float ONE_SIXTEENTH = 1 / 16f;

    @Override
    public boolean constructBlock(VertexSet buffers, Block block, BlockData data, Block[] neighbors, BlockData[] neighborData, byte[] light, Chunk chunk, int chunkX, int chunkY, int chunkZ, boolean isUsingGreedyMesher) {
        if (data == null || data.size() < 1 || data.get(0) == 3) {
            wall3.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        } else if (data.get(0) == 0) {
            wall0.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        } else if (data.get(0) == 1) {
            wall1.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        } else {
            wall2.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        }

        return false;
    }

    @Override
    public void getCursorBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        if (data != null && data.size() > 0) {
            switch (data.get(0)) {
                case 1 -> box.setPosAndSize(x + ONE_SIXTEENTH * 14, y, z, ONE_SIXTEENTH * 2, 1, 1);
                case 2 -> box.setPosAndSize(x, y, z + ONE_SIXTEENTH * 14, 1, 1, ONE_SIXTEENTH * 2);
                case 3 -> box.setPosAndSize(x, y, z, ONE_SIXTEENTH * 2, 1, 1);
                default -> box.setPosAndSize(x, y, z, 1, 1, ONE_SIXTEENTH * 2);
            }
            consumer.accept(box);
        }
    }

}
