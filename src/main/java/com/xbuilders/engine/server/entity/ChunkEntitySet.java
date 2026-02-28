/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.server.entity;

import com.xbuilders.engine.client.ClientWindow;
import com.xbuilders.engine.common.players.localPlayer.camera.FrustumCullingTester;
import com.xbuilders.engine.client.visuals.gameScene.rendering.entity.EntityShader;
import com.xbuilders.engine.common.math.MathUtils;
import com.xbuilders.engine.common.world.World;
import com.xbuilders.engine.common.world.chunk.Chunk;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

/**
 * @author zipCoder933
 */
public class ChunkEntitySet {


    public boolean chunkUpdatedMesh;
    Chunk thisChunk;
    public final ArrayList<Entity> list;
    World world;


    public ChunkEntitySet(Chunk aThis, World world) {
        this.thisChunk = aThis;
        this.world = world;
        list = new ArrayList<>();
    }

    public void clear() {
        list.clear();
    }

    public Entity placeNew(EntitySupplier link, long identifier, float worldX, float worldY, float worldZ, byte[] bytes) {
        if (link != null) {
            Entity entity = link.get(identifier);
            entity.link = link;
            entity.chunk = thisChunk;


            entity.worldPosition.set(worldX, worldY, worldZ);
            entity.loadBytes = bytes;
            list.add(entity);
            return entity;
        }
        return null;
    }

    public Entity placeNew(Vector3i worldPos, EntitySupplier entity, byte[] data) {
        return placeNew(entity, 0, worldPos.x, worldPos.y, worldPos.z, data);
    }

    public Entity placeNew(Vector3f worldPos, EntitySupplier entity, byte[] data) {
        return placeNew(entity, 0, worldPos.x, worldPos.y, worldPos.z, data);
    }

    public Entity placeNew(Vector3f worldPos, long identifier, EntitySupplier entity, byte[] data) {
        return placeNew(entity, identifier, worldPos.x, worldPos.y, worldPos.z, data);

    }


    public static void startDraw(Matrix4f projection, Matrix4f view) {
        if (Entity.shader == null) {//Unless another entity uses a different shader, we only need to bind once
            Entity.shader = new EntityShader();
        }
        Entity.shader.bind();
        Entity.shader.updateProjectionViewMatrix(projection, view);
    }

    public void draw(FrustumCullingTester frustum, Vector3f playerPos) {


        for (int i = list.size() - 1; i >= 0; i--) {
            Entity e = list.get(i);
            if (e == null || e.isDestroyMode()) {
                System.out.println("Removing entity; " + (e == null ? "null" : "not null") + " destroyed: " + e.isDestroyMode());
//                Main.getServer().server.addEntityChange(e, GameServer.ENTITY_DELETED, true);
                list.remove(i);
            } else {
                if (e.needsInitialization) {//Initialize entity on the main thread
                    e.hidden_initializeEntity();
                }
                e.distToPlayer = e.worldPosition.distance(playerPos);

                if (e.distToPlayer < ClientWindow.settings.video_entityDistance.value) {
                    e.inFrustum = frustum.isSphereInside(e.worldPosition, e.frustumSphereRadius);//Sphere boundary checks are faster than AABB
                    e.hidden_drawEntity();
                    e.server_update();
                }

                if (chunkUpdatedMesh) {
                    e.hidden_entityOnChunkMeshChanged();
                }
                if (!e.chunkPosition.chunk.equals(e.chunk.position)) { //Switch chunks
                    Chunk toChunk = world.getChunk(e.chunkPosition.chunk);
                    if (toChunk != null) {
                        //If the chunk exists, AND it's generated, add the entity to the new chunk
//                        System.out.println("SWITCHING FROM " + MiscUtils.printVector(e.chunkPosition.chunk) + " TO " + toChunk);
//                        e.renderThisFrame = false;
                        e.chunk = toChunk;
                        toChunk.entities.list.add(e);
                        list.remove(i);
                    } else {
                        //Otherwise, clamp entity position to the existing chunk
                        e.worldPosition.set(
                                MathUtils.clamp(e.worldPosition.x, e.chunk.position.x, e.chunk.position.x + Chunk.WIDTH - 1),
                                MathUtils.clamp(e.worldPosition.y, e.chunk.position.y, e.chunk.position.y + Chunk.HEIGHT - 1),
                                MathUtils.clamp(e.worldPosition.z, e.chunk.position.z, e.chunk.position.z + Chunk.WIDTH - 1));
                        e.updatePosition();
                    }
                }
            }
        }
        chunkUpdatedMesh = false;
    }

}
