package com.tessera.engine.common.world.chunk;

import com.tessera.engine.common.world.ServerWorld;
import com.tessera.engine.common.world.WorldData;
import com.tessera.engine.common.world.gen.GenContext;
import org.joml.Vector3i;

public class ServerChunk extends Chunk {

    public static final int GEN_TERRAIN_GENERATED = 1;
    public static final int GEN_SUN_GENERATED = 2;

    /**
     * Set once this chunk's data packet has been handed to the network layer.
     * Late cross-chunk decorations landing here afterwards must be re-broadcast
     * as block updates, or clients would keep the pre-decoration snapshot.
     */
    private volatile boolean sentToClients = false;

    public boolean isSentToClients() {
        return sentToClients;
    }

    public void markSentToClients() {
        sentToClients = true;
    }

    public ServerChunk(Vector3i position, FutureChunk futureChunk, ServerWorld world) {
        super(position, futureChunk, world);

    }

    public ServerChunk(Chunk other, Vector3i position, FutureChunk futureChunk, ServerWorld world) {
        super(other, position, futureChunk, world);
    }


    public void addNeighbors() {
        //Make all neighbor chunks
        for (int i = 0; i < NeighborInformation.NEIGHBOR_VECTORS.length; i++) {
            Vector3i offset = NeighborInformation.NEIGHBOR_VECTORS[i];
            Vector3i neighborPos = new Vector3i(position).add(offset);
            if (!world.hasChunk(neighborPos)) {
                world.addChunk(neighborPos);
            }
        }
        neghbors.cacheNeighbors();
    }


    /**
     * Runs base terrain + decorations exactly once. Must only be called by the
     * generation pipeline (this method is synchronized as a backstop against
     * double submission).
     *
     * @return the generation context, carrying spillover bookkeeping for
     * already-sent neighbor chunks.
     */
    public synchronized GenContext generateTerrain() {
        ServerWorld serverWorld = (ServerWorld) world;
        GenContext ctx = serverWorld.terrain.generate(this);
        ctx.drainPendingFutures();
        progressGenState(GEN_TERRAIN_GENERATED);
        return ctx;
    }

    public synchronized void generateLight() {
        if (getGenState() == GEN_TERRAIN_GENERATED) {
            try {
                for (int x = 0; x < voxels.size.x; x++) {
                    for (int y = 0; y < voxels.size.y; y++) {
                        for (int z = 0; z < voxels.size.z; z++) {
                            voxels.setSun(x, y, z, 15);
                        }
                    }
                }
            } finally {
                progressGenState(GEN_SUN_GENERATED);
            }
        }
    }


    Object saveLock = new Object();

    /**
     * Only saves the chunk if it is owned by the user and has changed since it
     * was last saved.
     *
     * @param info
     * @return if the chunk was really saved
     */
    public boolean save(WorldData info) {
        return false;
    }
}
