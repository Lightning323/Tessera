package com.tessera.content.vanilla;

import com.tessera.Main;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.common.world.chunk.Chunk;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import static com.tessera.engine.common.math.MathUtils.positiveMod;

public class EntityRemovalTool extends Item {
    Predicate<Entity> predicate;

    public EntityRemovalTool(Predicate<Entity> p, String id, String name) {
        super(id, name);
        setIcon("entity_removal_tool.png");
        maxStackSize = 1;
        tags.add("tool");
        predicate = p;
        destroyClickEvent = (ray, itemStack) -> {
            Vector3i pos = ray.getHitPos();
            deleteEntitiesAtPos(pos, 20);
            return true;
        };
        createClickEvent = (ray, itemStack) -> {
            Vector3i pos = ray.getHitPos();
            deleteEntitiesAtPos(pos, 50);
            return true;
        };
    }


    private void deleteEntitiesAtPos(Vector3i pos, int radius) {
        System.out.println("Removing all allEntities at " + pos);
        try {
            // Create a snapshot of the allEntities to avoid concurrency issues during iteration
            Collection<Entity> entitiesSnapshot = new ArrayList<>(Main.getClient().world.allEntities.values());
            entitiesSnapshot.forEach(entity -> {
                try {
                    if (entity != null && entity.worldPosition.distance(pos.x, pos.y, pos.z) < radius) {
                        System.out.println("Checking entity " + entity);
                        // Check if the entity meets the criteria and is within the radius
                        if (predicate.test(entity)) {
                            System.out.println("\tRemoving entity " + entity);
                            entity.destroy();
                        }
                    }
                } catch (Exception e) {
                    // Handle any issues that arise while processing a specific entity
                    Main.getClient().consoleOut("Error processing entity " + entity + ": " + e.getMessage());
                    //System.out.println("Error processing entity " + entity + ": " + e.getMessage());
                }
            });

            //Remove all allEntities in ALL chunks
            System.out.println("Removing all allEntities in the current chunk");
            for (Chunk chunk : Main.getClient().world.chunks.values()) {
                //Iterate over the entities backwards
                for (int i = chunk.entities.list.size() - 1; i >= 0; i--) {
                    Entity entity = chunk.entities.list.get(i);
                    if (entity == null) {
                        chunk.entities.list.remove(i);
                    } else if (entity.worldPosition.distance(pos.x, pos.y, pos.z) < radius) {
                        System.out.println("Checking entity " + entity);
                        if (predicate.test(entity)) {
                            System.out.println("\tRemoving entity " + entity);
                            entity.destroy();
                            chunk.entities.list.remove(i); //Remove from the entities
                        }
                    }
                }
            }

        } catch (Exception e) {
            Main.getClient().consoleOut("Error removing allEntities: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
