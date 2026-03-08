// 
// Decompiled by Procyon v0.5.36
// 
package com.tessera.engine.common.world.wcc;

import com.tessera.utils.MiscUtils;
import static com.tessera.engine.common.math.MathUtils.positiveMod;
import static com.tessera.engine.common.world.wcc.WCCi.chunkDiv;
import com.tessera.engine.common.world.chunk.Chunk;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;

public class WCCf {

    public final Vector3i chunk;
    public final Vector3f chunkVoxel;

    public WCCf() {
        this.chunk = new Vector3i();
        this.chunkVoxel = new Vector3f();
    }

    public WCCf(IntBuffer buff1, FloatBuffer buff2) {
        this.chunk = new Vector3i(buff1);
        this.chunkVoxel = new Vector3f(buff2);
    }

    public WCCf(MemoryStack stack) {
        this.chunk = new Vector3i(stack.mallocInt(3));
        this.chunkVoxel = new Vector3f(stack.mallocFloat(3));
    }

    /**
     * Assign the values to your specified vectors
     *
     * @param chunk
     * @param block
     */
    public WCCf(final Vector3i chunk, final Vector3f block) {
        this.chunk = chunk;
        this.chunkVoxel = block;
    }

    public WCCf set(Vector3f vec) {
        return set(vec.x, vec.y, vec.z);
    }

    public WCCf set(final float worldX, final float worldY, final float worldZ) {
        float blockX = positiveMod(worldX, Chunk.WIDTH);
        float blockY = positiveMod(worldY, Chunk.WIDTH);
        float blockZ = positiveMod(worldZ, Chunk.WIDTH);

        this.chunkVoxel.set(blockX, blockY, blockZ);

        int chunkX = chunkDiv((int) worldX);
        int chunkY = chunkDiv((int) worldY);
        int chunkZ = chunkDiv((int) worldZ);

        this.chunk.set(chunkX, chunkY, chunkZ);
        return this;
    }

    @Override
    public String toString() {
        return "WCC{chunk=" + MiscUtils.printVec(chunk)
                + ", voxel=" + MiscUtils.printVec(chunkVoxel) + '}';
    }
}
