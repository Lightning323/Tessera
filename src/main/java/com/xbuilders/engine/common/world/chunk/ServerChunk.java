package com.xbuilders.engine.common.world.chunk;

import com.xbuilders.utils.MiscUtils;
import com.xbuilders.engine.common.world.ServerWorld;
import com.xbuilders.engine.common.world.Terrain;
import com.xbuilders.engine.common.world.WorldData;
import org.joml.Vector3i;

public class ServerChunk extends Chunk {

    public static final int GEN_TERRAIN_GENERATED = 1;
    public static final int GEN_SUN_GENERATED = 2;

    public ServerChunk(Vector3i position, FutureChunk futureChunk, ServerWorld world) {
        super(position, futureChunk, world);

    }

    public ServerChunk(Chunk other, Vector3i position, FutureChunk futureChunk, ServerWorld world) {
        super(other, position, futureChunk, world);
    }


    public void addNeighbors() {
        //Make all neighbor chunks
        for (int i = 0; i < NeighborInformation.NEIGHBOR_VECTORS.length; i++) {
            Vector3i position = NeighborInformation.NEIGHBOR_VECTORS[i];
            if (!world.hasChunk(position)) {
                world.addChunk(position);
            }
        }
        neghbors.cacheNeighbors();
    }


    /**
     * This method of this chunk can only be run by one thread at a time
     *
     * @param data
     * @param terrain
     * @param future
     */
    public synchronized void generateTerrain(WorldData data, Terrain terrain, FutureChunk future) {
        Terrain.GenSession session = terrain.createTerrainOnChunk(this);
        if (future != null) {
            future.setBlocksInChunk(this);
        }
        System.out.println("Generated terrain at " + MiscUtils.printVec(this.position) + " Is empty: " + voxels.blocksAreEmpty);
        progressGenState(GEN_TERRAIN_GENERATED);
    }

    public synchronized void generateLight(WorldData worldData, Terrain terrain, FutureChunk future) {
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
//        if (isOwnedByUser() && needsToBeSaved) {
//            synchronized (saveLock) {
//                needsToBeSaved = false;
//                return ChunkSavingLoadingUtils.writeChunkToFile(this, info.getChunkFile(position));
//            }
//        }
        return false;
    }
}
