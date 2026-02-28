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
public class StairsRenderer extends BlockType {

    BlockModel[] side = new BlockModel[4];
    BlockModel[] floor = new BlockModel[4];
    BlockModel[] ceiling = new BlockModel[4];

    public StairsRenderer() throws IOException {
        initializationCallback = (b) -> {

            b.initialBlockData = (existingData) -> {
                BlockData data = new BlockData(2);
                Main.getClient().userPlayer.camera.simplifiedPanTiltAsBlockData(data);
                if (data.get(1) == (byte) 0
                        && (Math.abs(Main.getClient().userPlayer.camera.cursorRay.getHitNormalAsInt().x) != 0
                        || Math.abs(Main.getClient().userPlayer.camera.cursorRay.getHitNormalAsInt().z) != 0)) {
                    data.set(1, (byte) 3);
                }
                return data;
            };

            b.easierMiningTool_tag = "pickaxe";
            b.opaque = false;
            b.solid = true;
        };
//        ObjToBlockModel.parseFileWithYRotations(false, 1.6f,
//                ResourceUtils.resource("block types\\stairs\\side.obj"));
//        ObjToBlockModel.parseFileWithYRotations(false, 1.6f,
//                ResourceUtils.resource("block types\\stairs\\floor.obj"));
//        ObjToBlockModel.parseFileWithYRotations(false, 1.6f,
//                ResourceUtils.resource("block types\\stairs\\ceiling.obj"));

        BlockModel.ShouldRenderSide renderSide = new BlockModel.ShouldRenderSide() {
            @Override
            public boolean shouldRenderSide(Block thisBlock, Block neighbor) {
                return shouldRenderFace_subBlock(thisBlock, neighbor);
//                return neighbor.isSolid();
            }
        };

        for (int i = 0; i < 4; i++)
            side[i] = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/stairs\\side" + i + ".blockType"), renderSide);

        for (int i = 0; i < 4; i++)
            floor[i] = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/stairs\\floor" + i + ".blockType"), renderSide);

        for (int i = 0; i < 4; i++)
            ceiling[i] = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/stairs\\ceiling" + i + ".blockType"), renderSide);
    }

    public void rotateBlockData(BlockData data, boolean clockwise) {
      super.rotateBlockData(data, clockwise);
    }


    @Override
    public boolean constructBlock(VertexSet buffers, Block block, BlockData data,
                                  Block[] neighbors, BlockData[] neighborData, byte[] light, Chunk chunk, int chunkX, int chunkY, int chunkZ, boolean isUsingGreedyMesher) {

        try {
            if (data.get(1) == 3) {
                side[data.get(0)].render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
            } else if (data.get(1) >= 0) {
                floor[data.get(0)].render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
            } else {
                ceiling[data.get(0)].render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
            }
        } catch (Exception e) {
            floor[0].render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        }
        return false;
    }

    @Override
    public void getCursorBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        getCollisionBoxes(consumer, box, block, data, x, y, z);
    }


    @Override
    public void getCollisionBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        if (data != null) {
            if (data.get(1) == 3) {
                if (data.get(0) == 1) {
                    box.setPosAndSize(x + 0.5f, y, z + 0.5f, 0.5f, 1f, 0.5f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z, 1f, 1f, 0.5f);
                    consumer.accept(box);
                } else if (data.get(0) == 2) {
                    box.setPosAndSize(x + 0.5f, y, z, 0.5f, 1f, 0.5f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z + 0.5f, 1f, 1f, 0.5f);
                    consumer.accept(box);
                } else if (data.get(0) == 3) {
                    box.setPosAndSize(x, y, z, 0.5f, 1f, 0.5f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z + 0.5f, 1f, 1f, 0.5f);
                    consumer.accept(box);
                } else {
                    box.setPosAndSize(x, y, z + 0.5f, 0.5f, 1f, 0.5f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z, 1f, 1f, 0.5f);
                    consumer.accept(box);
                }
            } else if (data.get(1) >= 0) {
                if (data.get(0) == 1) {
                    box.setPosAndSize(x, y + 0.5f, z, 0.5f, 0.5f, 1f);
                    consumer.accept(box);
                    box.setPosAndSize(x + 0.5f, y, z, 0.5f, 1f, 1);
                    consumer.accept(box);
                } else if (data.get(0) == 2) {
                    box.setPosAndSize(x, y + 0.5f, z, 1f, 0.5f, 0.5f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z + 0.5f, 1f, 1f, 0.5f);
                    consumer.accept(box);
                } else if (data.get(0) == 3) {
                    box.setPosAndSize(x + 0.5f, y + 0.5f, z, 0.5f, 0.5f, 1f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z, 0.5f, 1f, 1f);
                    consumer.accept(box);
                } else {
                    box.setPosAndSize(x, y + 0.5f, z + 0.5f, 1f, 0.5f, 0.5f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z, 1f, 1f, 0.5f);
                    consumer.accept(box);
                }
            } else {
                if (data.get(0) == 1) {
                    box.setPosAndSize(x, y, z, 0.5f, 0.5f, 1f);
                    consumer.accept(box);
                    box.setPosAndSize(x + 0.5f, y, z, 0.5f, 1f, 1);
                    consumer.accept(box);
                } else if (data.get(0) == 2) {
                    box.setPosAndSize(x, y, z, 1f, 0.5f, 0.5f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z + 0.5f, 1f, 1f, 0.5f);
                    consumer.accept(box);
                } else if (data.get(0) == 3) {
                    box.setPosAndSize(x + 0.5f, y, z, 0.5f, 0.5f, 1f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z, 0.5f, 1f, 1f);
                    consumer.accept(box);
                } else {
                    box.setPosAndSize(x, y, z + 0.5f, 1f, 0.5f, 0.5f);
                    consumer.accept(box);
                    box.setPosAndSize(x, y, z, 1f, 1f, 0.5f);
                    consumer.accept(box);
                }
            }
        }
    }
}
