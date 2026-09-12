package com.tessera.engine.common.world.chunk;

import org.joml.Vector3i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Blocks staged for a chunk that does not exist yet — typically decorations
 * (tree trunks, canopies) spilling over from an already-generating neighbor.
 *
 * <p>Each staged block carries an overwrite policy: trunks punch through
 * whatever base terrain is filled later, while leaves only settle into air, so
 * applying staged blocks after the base fill converges no matter which chunk
 * generated first.
 */
public class FutureChunk {

    /** A staged block plus its overwrite policy. */
    public record FutureBlock(short id, boolean onlyIfAir) {
    }

    public final Vector3i position;
    public final Map<Vector3i, FutureBlock> futureBlocks;
    public final HashSet<Vector3i> futureSun;
    public final HashSet<Vector3i> futureTorch;

    public FutureChunk(Vector3i position) {
        this.position = position;
        this.futureSun = new HashSet<>();
        this.futureTorch = new HashSet<>();
        this.futureBlocks = new HashMap<>();
    }

    /** Stage a block that overwrites whatever the base fill produced. */
    public void addBlock(short id, int x, int y, int z) {
        addBlock(id, x, y, z, false);
    }

    /**
     * Stage a block.
     *
     * @param onlyIfAir when true the block is only applied into air, leaving
     *                  already-generated terrain (or earlier decorations) alone.
     */
    public void addBlock(short id, int x, int y, int z, boolean onlyIfAir) {
        Vector3i key = new Vector3i(x, y, z);
        FutureBlock staged = new FutureBlock(id, onlyIfAir);
        // Merge, don't overwrite: an unconditional write (trunk, ground)
        // always dominates an only-if-air one (leaves) for the same cell, no
        // matter which was staged first. Otherwise the second write would
        // silently eat the first and trees would lose their tips.
        futureBlocks.merge(key, staged, (old, cur) ->
                (cur.onlyIfAir() && old.onlyIfAir()) ? cur : (cur.onlyIfAir() ? old : cur));
    }

    /**
     * Legacy bulk apply: writes every staged block unconditionally.
     * Preserved for non-generation callers; generation uses {@link #applyTo}.
     */
    public void setBlocksInChunk(Chunk chunk) {
        futureBlocks.forEach((position, block) -> {
            chunk.voxels.setBlock(position.x, position.y, position.z, block.id());
        });
    }

    /** Policy-aware apply, used once the chunk's base terrain is filled. */
    public void applyTo(Chunk chunk) {
        futureBlocks.forEach((position, block) -> {
            if (!block.onlyIfAir()
                    || chunk.voxels.getBlock(position.x, position.y, position.z) == 0) {
                chunk.voxels.setBlock(position.x, position.y, position.z, block.id());
            }
        });
    }
}
