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
public class FenceRenderer extends BlockType {

    BlockModel post, boards0, boards1, boards2, boards3;

    public FenceRenderer(String path) throws IOException {
//        ObjToBlockModel.parseDirectory(null,
//                false, 1.6f,
//                ResourceUtils.resource("block types\\fence"));
        post = BlockModelLoader.load(resourceLoader.getResourceAsStream(path, "post.blockType"), renderSide_subBlock);
        boards0 = BlockModelLoader.load(resourceLoader.getResourceAsStream(path, "boards.blockType"), renderSide_subBlock);
        boards1 = BlockModelLoader.load(resourceLoader.getResourceAsStream(path, "boards1.blockType"), renderSide_subBlock);
        boards2 = BlockModelLoader.load(resourceLoader.getResourceAsStream(path, "boards2.blockType"), renderSide_subBlock);
        boards3 = BlockModelLoader.load(resourceLoader.getResourceAsStream(path, "boards3.blockType"), renderSide_subBlock);
        initializationCallback = (b) -> {
            b.opaque = false;
            b.solid = true;
        };
    }

    private boolean isSolid(Block block) {
        return block != null
                && block.solid
                && block.type != RenderType.FLOOR
                && block.type != RenderType.WALL_ITEM
                && block.type != RenderType.SPRITE;
    }

    @Override
    public boolean constructBlock(VertexSet buffers, Block block, BlockData data, Block[] neighbors, BlockData[] neighborData, byte[] light, Chunk chunk, int chunkX, int chunkY, int chunkZ, boolean isUsingGreedyMesher) {

        post.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);

        if (isSolid(neighbors[POS_Z])) {
            boards3.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        }
        if (isSolid(neighbors[NEG_X])) {
            boards0.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        }
        if (isSolid(neighbors[NEG_Z])) {
            boards1.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        }
        if (isSolid(neighbors[POS_X])) {
            boards2.render(buffers, block, neighbors, light, chunkX, chunkY, chunkZ);
        }
        return false;
    }

    float sixtheenth = 0.0625f;
    final float fenceHeight = 0.7f;//What to subtract regular fence top from

    @Override
    public void getCollisionBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        box.setPosAndSize(x + (sixtheenth * 6), y - fenceHeight, z + (sixtheenth * 6), (sixtheenth * 4), 1 + fenceHeight, (sixtheenth * 4));
        consumer.accept(box);

        if (isSolid(Client.world.getBlock(x + 1, y, z))) {
            box.setPosAndSize(x + (sixtheenth * 10), y - fenceHeight, z + (sixtheenth * 6), (sixtheenth * 6), 1 + fenceHeight, (sixtheenth * 4));
            consumer.accept(box);
        }
        if (isSolid(Client.world.getBlock(x - 1, y, z))) {
            box.setPosAndSize(x, y - fenceHeight, z + (sixtheenth * 6), (sixtheenth * 6), 1 + fenceHeight, (sixtheenth * 4));
            consumer.accept(box);
        }
        if (isSolid(Client.world.getBlock(x, y, z + 1))) {
            box.setPosAndSize(x + (sixtheenth * 6), y - fenceHeight, z + (sixtheenth * 10), (sixtheenth * 4), 1 + fenceHeight, (sixtheenth * 6));
            consumer.accept(box);
        }
        if (isSolid(Client.world.getBlock(x, y, z - 1))) {
            box.setPosAndSize(x + (sixtheenth * 6), y - fenceHeight, z, (sixtheenth * 4), 1 + fenceHeight, (sixtheenth * 6));
            consumer.accept(box);
        }
    }

    @Override
    public void getCursorBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        box.setPosAndSize(x + (sixtheenth * 6), y, z + (sixtheenth * 6), (sixtheenth * 4), 1f, (sixtheenth * 4));
        consumer.accept(box);

        if (isSolid(Client.world.getBlock(x + 1, y, z))) {
            box.setPosAndSize(x + (sixtheenth * 10), y, z + (sixtheenth * 6), (sixtheenth * 6), 1f, (sixtheenth * 4));
            consumer.accept(box);
        }
        if (isSolid(Client.world.getBlock(x - 1, y, z))) {
            box.setPosAndSize(x, y, z + (sixtheenth * 6), (sixtheenth * 6), 1f, (sixtheenth * 4));
            consumer.accept(box);
        }
        if (isSolid(Client.world.getBlock(x, y, z + 1))) {
            box.setPosAndSize(x + (sixtheenth * 6), y, z + (sixtheenth * 10), (sixtheenth * 4), 1f, (sixtheenth * 6));
            consumer.accept(box);
        }
        if (isSolid(Client.world.getBlock(x, y, z - 1))) {
            box.setPosAndSize(x + (sixtheenth * 6), y, z, (sixtheenth * 4), 1f, (sixtheenth * 6));
            consumer.accept(box);
        }
    }

}
