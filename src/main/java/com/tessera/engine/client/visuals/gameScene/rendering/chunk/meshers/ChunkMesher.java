package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers;

import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.common.world.chunk.ChunkVoxels;
import org.joml.Vector3i;

public abstract class ChunkMesher<T extends VertexSet> {

    public ChunkMesher(ChunkVoxels voxels, Vector3i chunkPositionOffset) {
        this.data = voxels;
        this.chunkPosition = chunkPositionOffset;
    }

   UseGM_BooleanBuffer buffer_shouldUseGreedyMesher;
    public final ChunkVoxels data;
    public final Vector3i chunkPosition;
}
