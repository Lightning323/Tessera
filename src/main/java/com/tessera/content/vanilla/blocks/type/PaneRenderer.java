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


public class PaneRenderer extends BlockType {

    private final BlockModel horizontal, vertical0, vertical1;

    public PaneRenderer() throws IOException {
        super();

        // ObjToBlockModel.parseFileWithYRotations( false,
        //         1.6f, ResourceUtils.resource("block types\\pane\\vertical.obj"));
        // ObjToBlockModel.parseFile(null, false, 1.6f, ResourceUtils.resource("block types\\pane\\horizontal.obj"));

        horizontal = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/pane\\horizontal.blockType"), renderSide_subBlock);
        vertical0 = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/pane\\vertical0.blockType"), renderSide_subBlock);
        vertical1 = BlockModelLoader.load(resourceLoader.getResourceAsStream("/assets/tessera/models/block/pane\\vertical1.blockType"), renderSide_subBlock);
        initializationCallback = (b) -> {
            b.initialBlockData = (existingData) -> {
                BlockData data = Main.getClient().userPlayer.camera.simplifiedPanTiltAsBlockData(new BlockData(2));
                return data;
            };

            b.opaque = false;
            b.solid = true;
        };
    }

    @Override
    public boolean constructBlock(VertexSet buffers, Block block, BlockData data, Block[] neighbors,
                                  BlockData[] neighborData, byte[] light, Chunk chunk,
                                  int chunkX, int chunkY, int chunkZ,
                                  boolean isUsingGreedyMesher) {
        if (data != null && data.size() == 2) {
            if (data.get(1) == 0) {
                if (data.get(0) == 1 || data.get(0) == 3) {
                    vertical1.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
                } else {
                    vertical0.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
                }
            } else {
                horizontal.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
            }
        } else {
            horizontal.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        }
        return false;
    }

    @Override
    public void getCursorBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        getCollisionBoxes(consumer, box, block, data, x, y, z);
    }


    final float ONE_SIXTEENTH = 1 / 16f;

    @Override
    public void getCollisionBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        if (data != null && data.size() == 2) {
            if (data.get(1) == 0) {
                if (data.get(0) == 1 || data.get(0) == 3) {
                    box.setPosAndSize(x + (ONE_SIXTEENTH * 7), y, z, ONE_SIXTEENTH * 2, 1, 1);
                    consumer.accept(box);
                } else {
                    box.setPosAndSize(x, y, z + (ONE_SIXTEENTH * 7), 1, 1, ONE_SIXTEENTH * 2);
                    consumer.accept(box);
                }
            } else {
                box.setPosAndSize(x, y + (ONE_SIXTEENTH * 7), z, 1, ONE_SIXTEENTH * 2, 1);
                consumer.accept(box);
            }
        } else {
            box.setPosAndSize(x + (ONE_SIXTEENTH * 7), y, z, ONE_SIXTEENTH * 2, 1, 1);
            consumer.accept(box);
        }
    }
}