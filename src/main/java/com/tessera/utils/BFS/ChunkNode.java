// 
// Decompiled by Procyon v0.5.36
// 
package com.tessera.utils.BFS;

import com.tessera.engine.common.world.World;
import com.tessera.engine.common.world.chunk.Chunk;

import java.util.Objects;

import com.tessera.engine.common.world.wcc.WCCi;

public class ChunkNode {

    public Chunk chunk;
    public int x, y, z;


    public ChunkNode(WCCi coords, World world) {
        this.x = coords.chunkVoxel.x;
        this.y = coords.chunkVoxel.y;
        this.z = coords.chunkVoxel.z;
        this.chunk = coords.getChunk(world);
    }

    public ChunkNode(final Chunk chunk, final int x, final int y, final int z) {
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
        //(x ^ (x >> 32) ^ y ^ (y >> 32) ^ z ^ (z >> 32));//Generating efficient hashcode is CRUCIAL!
    }

    @Override
    public String toString() {
        return "ChunkNode{" + "chunk=" + chunk + ", coords=" + x + ", " + y + ", " + z + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkNode chunkNode = (ChunkNode) o;
        return x == chunkNode.x && y == chunkNode.y && z == chunkNode.z && Objects.equals(chunk, chunkNode.chunk);
    }
}
