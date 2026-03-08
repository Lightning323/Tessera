/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.server.block.construction;

import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTypeModel.BlockModel;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.utils.resource.ResourceLoader;
import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.world.chunk.Chunk;

import java.util.function.Consumer;

/**
 * @author zipCoder933
 */
public abstract class BlockType {

    public static final int POS_X = 0;
    public static final int NEG_X = 1;
    public static final int POS_Z = 2;
    public static final int NEG_Z = 3;
    public static final int POS_Y = 4;
    public static final int NEG_Y = 5;

    public Consumer<Block> initializationCallback = null;
    public Consumer<Block> registrationCallback = null;
    public boolean generate3DIcon = true;
    public boolean replaceOnSet = false;
    public final static ResourceLoader resourceLoader = new ResourceLoader();

    //This is the first step in allowing a block to be permitted in the greedy mesher

    public final static int DENY_GM = 0;
    public final static int PERMIT_GM = 1;
    public final static int ALWAYS_USE_GM = 2;

    public int getGreedyMesherPermissions() {
        return DENY_GM;
    }

    public boolean allowExistence(Block block, int worldX, int worldY, int worldZ) {
        return true;
    }

    public final static BlockModel.ShouldRenderSide renderSide_subBlock = new BlockModel.ShouldRenderSide() {
        @Override
        public boolean shouldRenderSide(Block thisBlock, Block neighbor) {
            return shouldRenderFace_subBlock(thisBlock, neighbor);
            // return neighbor.isSolid();
        }
    };

    public final static BlockModel.ShouldRenderSide renderSide = new BlockModel.ShouldRenderSide() {
        @Override
        public boolean shouldRenderSide(Block thisBlock, Block neighbor) {
            return thisBlock.opaque ? !neighbor.opaque : neighbor.opaque;
        }
    };

    /**
     * @param buffers
     * @param block
     * @param data
     * @param neighbors
     * @param neighborData
     * @param lightValues
     * @param chunk               if chunk is null, that means we are rendering in raw block voxels instead
     * @param chunkX
     * @param chunkY
     * @param chunkZ
     * @param isUsingGreedyMesher if set to true, we can choose to return false if we want to use the greedy mesher
     * @return If we should use the greedy mesher
     */
    public abstract boolean constructBlock(VertexSet buffers,
                                           Block block, BlockData data,
                                           Block[] neighbors, BlockData[] neighborData, byte[] lightValues,
                                           Chunk chunk, int chunkX, int chunkY, int chunkZ,
                                           boolean isUsingGreedyMesher);

    public boolean determineIfUsingGreedyMesher(
            Block block, BlockData data,
            Block[] neighbors, BlockData[] neighborData, byte[] lightValues,
            Chunk chunk, int chunkX, int chunkY, int chunkZ) {
        return false;
    }

    // public Vector3f[] rotateYAxis(Block[] neighbors, Vector3f[] verts, int
    // rotation) {
    // Vector3f[] rotatedVerts = rotateVerticiesYAxis(verts, rotation);
    //
    // Block positiveX = neighbors[0];
    // Block negativeX = neighbors[1];
    // Block positiveZ = neighbors[2];
    // Block negativeZ = neighbors[3];
    //
    // switch (rotation) {
    // case 1:
    // neighbors[0] = positiveZ; // positiveX
    // neighbors[1] = negativeZ; // negativeX
    // neighbors[2] = negativeX; // positiveZ
    // neighbors[3] = positiveX; // negativeZ
    // break;
    // case 2:
    // neighbors[0] = negativeX; // positiveX
    // neighbors[1] = positiveX; // negativeX
    // neighbors[2] = negativeZ; // positiveZ
    // neighbors[3] = positiveZ; // negativeZ
    // break;
    // case 3:
    // neighbors[0] = negativeZ; // positiveX
    // neighbors[1] = positiveZ; // negativeX
    // neighbors[2] = positiveX; // positiveZ
    // neighbors[3] = negativeX; // negativeZ
    // break;
    // }
    // return rotatedVerts;
    // }

    public void getCursorBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        getCollisionBoxes(consumer, box, block, data, x, y, z);
    }

    @FunctionalInterface
    public interface BoxConsumer {
        void accept(AABB box);
    }

    public void getCollisionBoxes(BoxConsumer consumer, AABB box, Block block, BlockData data, int x, int y, int z) {
        consumer.accept(box.setPosAndSize(x, y, z, 1, 1, 1));
    }


    public void rotateBlockData(BlockData data, boolean clockwise) {
        if (data != null && data.size() == 2) {
            int d = data.get(0);
            d += (clockwise ? -1 : 1);
            data.set(0, (byte) (d % 4));
        }
    }

    // private Vector3f[] rotateVerticiesYAxis(Vector3f[] verts, int rotationXZ) {
    // double rot = (Math.PI / 2) * rotationXZ;
    //
    // if (rot == 0) {
    // return verts;
    // }
    //
    // final float center = 0.5f;
    // Vector3f[] verts2 = new Vector3f[verts.length];
    //
    // for (int i = 0; i < verts.length; i++) {
    // verts2[i] = new Vector3f(0, 0, 0);
    // verts2[i].y = verts[i].y;
    // double sin = Math.sin(rot);
    // double cos = Math.cos(rot);
    // verts2[i].x = (float) (center + (verts[i].x - center) * cos - (verts[i].z -
    // center) * sin);
    // verts2[i].z = (float) (center + (verts[i].x - center) * sin + (verts[i].z -
    // center) * cos);
    // }
    // return verts2;
    // }

    public final static boolean shouldRenderFace_subBlock(Block thisBlock, Block neighbor) {
        if (neighbor == null) {
            return true;
        } else {
            BlockType type = Registrys.blocks.getBlockType(neighbor.type);
            if (!neighbor.opaque || (type == null) || !type.isCubeShape()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return If the block typeReference is completely in the shape of a cube
     */
    public boolean isCubeShape() {
        return false;
    }

    public String toString() {
        return name == null ? "Unnamed" : name;
    }

    public String name;
}
