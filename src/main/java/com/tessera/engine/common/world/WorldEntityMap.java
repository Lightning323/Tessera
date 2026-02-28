package com.tessera.engine.common.world;

import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.common.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldEntityMap extends HashMap<Long, Entity> {
    public void removeAllEntitiesFromChunk(Chunk chunk) {
        try {
            List<Entity> entitiesToRemove = new ArrayList<>();
            for (Entity e : new HashMap<>(this).values()) {
                if (e.chunkPosition.chunk.equals(chunk.position)) {
                    entitiesToRemove.add(e);
                }
            }
            for (Entity e : entitiesToRemove) {
                remove(e.getUniqueIdentifier());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addAllEntitiesFromChunk(Chunk chunk) {
        try {
            for (Entity e : new ArrayList<>(chunk.entities.list)) {
                put(e.getUniqueIdentifier(), e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
