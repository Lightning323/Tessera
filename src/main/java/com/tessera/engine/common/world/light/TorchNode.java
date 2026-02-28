package com.tessera.engine.common.world.light;

import com.tessera.engine.common.world.World;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;

import java.util.Objects;

class TorchNode {
    public int lightVal = -1;
    public Chunk chunk;
    public int x, y, z;

    public TorchNode(WCCi coords, World world) {
        this.x = coords.chunkVoxel.x;
        this.y = coords.chunkVoxel.y;
        this.z = coords.chunkVoxel.z;
        this.chunk = coords.getChunk(world);
    }

    public TorchNode(final Chunk chunk, final int x, final int y, final int z, final int lightVal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.chunk = chunk;
        this.lightVal = lightVal;
    }

    public TorchNode(final Chunk chunk, final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.chunk = chunk;
    }

    public void set(Chunk chunk, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.chunk = chunk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunk, x, y, z);
    }

    @Override
    public String toString() {
        return "ChunkNode{" + "chunk=" + chunk + ", coords=" + x + ", " + y + ", " + z + '}';
    }

    @Override
    public boolean equals(Object o) {
        //We only care if the position is the same
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TorchNode chunkNode = (TorchNode) o;
        return x == chunkNode.x && y == chunkNode.y && z == chunkNode.z && Objects.equals(chunk, chunkNode.chunk);
    }
}