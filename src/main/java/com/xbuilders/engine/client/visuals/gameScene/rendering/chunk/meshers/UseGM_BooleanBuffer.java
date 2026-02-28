package com.xbuilders.engine.client.visuals.gameScene.rendering.chunk.meshers;

import com.xbuilders.utils.BooleanBuffer;
import com.xbuilders.engine.common.world.chunk.Chunk;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;

/**
 * A buffer to store boolean values about if the Naive mesher will allow
 * the greedy mesher to use a specific voxel position in its mesh
 */
class UseGM_BooleanBuffer {
    public BooleanBuffer buffer;

    //We change coordinates to support negative indexes for out of bounds voxels
    public final static Vector3i size = new Vector3i(Chunk.WIDTH + 2, Chunk.HEIGHT + 2, Chunk.WIDTH + 2);

    public UseGM_BooleanBuffer(MemoryStack stack) {
        buffer = new BooleanBuffer(size.x * size.y * size.z, stack);
    }

    public static int getIndexOfCoords(final int x, final int y, final int z) {
        return (x + 1) + size.x * ((y + 1) + size.y * (z + 1));
    }

    public boolean get(int x, int y, int z) {
        return buffer.get(getIndexOfCoords(x, y, z));
    }

    public void put(int x, int y, int z, boolean value) {
        buffer.put(getIndexOfCoords(x, y, z), value);
    }
}
