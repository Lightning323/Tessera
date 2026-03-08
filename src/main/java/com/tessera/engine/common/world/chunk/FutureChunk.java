package com.tessera.engine.common.world.chunk;

import org.joml.Vector3i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FutureChunk {

    public final Vector3i position;
    public final Map<Vector3i, Short> futureBlocks;
    public final HashSet<Vector3i> futureSun;
    public final HashSet<Vector3i> futureTorch;

    public FutureChunk(Vector3i position) {
        this.position = position;
        this.futureSun = new HashSet<>();
        this.futureTorch = new HashSet<>();
        this.futureBlocks = new HashMap<>();
    }

    public void addBlock(short id, int x, int y, int z) {
        futureBlocks.put(new Vector3i(x, y, z), id);
    }

    public void setBlocksInChunk(Chunk chunk) {
        futureBlocks.forEach((position, id) -> {
            chunk.voxels.setBlock(position.x, position.y, position.z, id);
        });
    }
}
