package com.tessera.engine.common.world.gen;

import com.tessera.engine.common.math.FastNoise;
import com.tessera.engine.common.packets.BlockUpdatePacket;
import com.tessera.engine.common.world.ServerWorld;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.chunk.FutureChunk;
import com.tessera.engine.common.world.chunk.ServerChunk;
import com.tessera.engine.common.world.wcc.WCCi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Server-side chunk-generation context. Replaces {@code Terrain.GenSession}.
 *
 * <p>All coordinates are WORLD coordinates and every operation targets the
 * SERVER world being generated. In particular this context never touches
 * {@code Main.getClient()}: the old session wrote spillover blocks into the
 * client world (and read solidity from it), which broke dedicated servers and
 * silently dropped cross-chunk content on the floor.
 *
 * <p>Cross-chunk writes (trees, plants, ores spilling over a chunk border)
 * are handled two ways so trees never get cut off mid-chunk, regardless of
 * the order chunks generate in:
 * <ul>
 *   <li>neighbor chunk already exists — write its voxels directly (with the
 *       same overwrite policy as below);</li>
 *   <li>neighbor chunk does not exist yet — stage the block in that chunk's
 *       {@link FutureChunk}, applied with policy once its base terrain is
 *       filled.</li>
 * </ul>
 *
 * <p>Writes into chunks that were already sent to clients are recorded and can
 * be flushed as {@link BlockUpdatePacket}s via {@link #drainSpilloverUpdates},
 * so late decorations patch connected clients instead of diverging from them.
 *
 * <p>Threading: one context is used by exactly one generation thread. Writes
 * into the home chunk are lock-free; every cross-chunk access synchronizes on
 * the {@link ServerWorld} instance (single lock — no lock ordering issues).
 */
public final class GenContext {

    /** Deterministic per-chunk RNG. Same seed + chunk = same decorations. */
    public final Random random = new Random();
    public final ServerWorld world;
    public final ServerChunk home;

    /** Reused coordinate splitter. Single-threaded use only. */
    private final WCCi wcc = new WCCi();

    private List<BlockUpdatePacket> spilloverUpdates;

    public GenContext(ServerWorld world, ServerChunk home, int worldSeed) {
        this.world = world;
        this.home = home;
        random.setSeed(FastNoise.Hash3D(worldSeed, home.position.x, home.position.y, home.position.z));
    }

    // <editor-fold defaultstate="collapsed" desc="random helpers">

    public int randomInt(int lowerBound, int upperBound) {
        return random.nextInt(upperBound - lowerBound) + lowerBound;
    }

    public float randomFloat(float lowerBound, float upperBound) {
        return (random.nextFloat() * upperBound - lowerBound) + lowerBound;
    }

    public double randomDouble(double lowerBound, double upperBound) {
        return (random.nextDouble() * upperBound - lowerBound) + lowerBound;
    }

    /**
     * Generates a random boolean with the specified probability.
     *
     * @param probability The probability of returning true (0.0 to 1.0).
     */
    public boolean randBoolWithProbability(float probability) {
        return random.nextFloat() < probability;
    }
    // </editor-fold>

    /**
     * Reads a block in world coordinates from already-generated server chunks.
     * Returns air (0) for chunks that do not exist yet — decorations must treat
     * unknown space as empty, never as solid.
     */
    public short getBlockWorld(int x, int y, int z) {
        wcc.set(x, y, z);
        Chunk chunk = world.getChunk(wcc.chunk);
        if (chunk == null) return 0;
        return chunk.voxels.getBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
    }

    /** Unconditional write in world coordinates (trunks, ground, ores...). */
    public void setBlockWorld(int x, int y, int z, short block) {
        writeWorld(x, y, z, block, false);
    }

    /**
     * Write in world coordinates, but only into air (leaves, plants, branches).
     * This is what keeps a late decoration from eating terrain — or an
     * earlier tree — that is already there.
     */
    public void setBlockWorldOnlyIfAir(int x, int y, int z, short block) {
        writeWorld(x, y, z, block, true);
    }

    private void writeWorld(int x, int y, int z, short block, boolean onlyIfAir) {
        wcc.set(x, y, z);
        Chunk chunk = world.getChunk(wcc.chunk);
        if (chunk == home) {
            int lx = wcc.chunkVoxel.x, ly = wcc.chunkVoxel.y, lz = wcc.chunkVoxel.z;
            if (!onlyIfAir || home.voxels.getBlock(lx, ly, lz) == 0) {
                home.voxels.setBlock(lx, ly, lz, block);
            }
            return;
        }
        synchronized (world) {
            Chunk target = world.getChunk(wcc.chunk);
            int lx = wcc.chunkVoxel.x, ly = wcc.chunkVoxel.y, lz = wcc.chunkVoxel.z;
            if (target != null) {
                if (!onlyIfAir || target.voxels.getBlock(lx, ly, lz) == 0) {
                    target.voxels.setBlock(lx, ly, lz, block);
                    if (target instanceof ServerChunk serverChunk && serverChunk.isSentToClients()) {
                        if (spilloverUpdates == null) spilloverUpdates = new ArrayList<>();
                        spilloverUpdates.add(new BlockUpdatePacket(x, y, z, block, null));
                    }
                }
            } else {
                // newFutureChunk copies the position vector internally.
                world.newFutureChunk(wcc.chunk).addBlock(block, lx, ly, lz, onlyIfAir);
            }
        }
    }

    /**
     * Applies blocks staged by earlier-generated neighbors into the home chunk.
     * Must run after this chunk's base terrain is filled (staged trunks punch
     * through unconditionally; staged leaves only settle into air).
     */
    public void drainPendingFutures() {
        FutureChunk attached = home.takeFutureChunk();
        if (attached != null) attached.applyTo(home);
        FutureChunk staged;
        synchronized (world) {
            staged = world.takeFutureChunk(home.position);
        }
        if (staged != null) staged.applyTo(home);
    }

    /** Takes (and clears) the spillover updates recorded for sent chunks. */
    public List<BlockUpdatePacket> drainSpilloverUpdates() {
        if (spilloverUpdates == null) return List.of();
        List<BlockUpdatePacket> out = spilloverUpdates;
        spilloverUpdates = null;
        return out;
    }
}
