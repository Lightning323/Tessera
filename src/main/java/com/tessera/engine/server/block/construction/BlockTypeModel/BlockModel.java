package com.tessera.engine.server.block.construction.BlockTypeModel;

import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;

public class BlockModel {

    public static final int SIDE_CENTER = 0;
    public static final int SIDE_POS_X = 1;
    public static final int SIDE_NEG_X = 2;
    public static final int SIDE_POS_Z = 3;
    public static final int SIDE_NEG_Z = 4;
    public static final int SIDE_POS_Y = 5;
    public static final int SIDE_NEG_Y = 6;

    final ModelSide[] sides = new ModelSide[7];
    ShouldRenderSide shouldRenderSide;

    public BlockModel(ShouldRenderSide shouldRenderSide) {
        this.shouldRenderSide = shouldRenderSide;
    }

    @FunctionalInterface
    public interface ShouldRenderSide {

        public boolean shouldRenderSide(Block thisBlock, Block neighbor);
    }

    public void render(VertexSet buff, Block block, Block[] neighbors, byte[] lightValues, int x, int y, int z) {
        BlockTexture.FaceTexture texture;
        //Side-1 is the side of the face that is touching the outside of the block (BlockType.side)
        for (int side = 0; side < sides.length; side++) {
            if (sides[side] != null
                    && (side == 0 || shouldRenderSide.shouldRenderSide(block, neighbors[side - 1]))) {
                switch (sides[side].textureSide) {
                    case SIDE_POS_X ->
                        texture = block.texture.getPOS_X();
                    case SIDE_NEG_X ->
                        texture = block.texture.getNEG_X();
                    case SIDE_POS_Z ->
                        texture = block.texture.getPOS_Z();
                    case SIDE_NEG_Z ->
                        texture = block.texture.getNEG_Z();
                    case SIDE_POS_Y ->
                        texture = block.texture.getPOS_Y();
                    case SIDE_NEG_Y ->
                        texture = block.texture.getNEG_Y();
                    default ->
                        texture = block.texture.getPOS_Y();
                }
                sides[side].render(x, y, z, buff, texture, lightValues);
            }
        }
    }
}
