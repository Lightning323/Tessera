package com.tessera.engine.client.skin;

import com.tessera.engine.client.Client;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityShader;
import com.tessera.engine.common.players.Player;
import com.tessera.engine.common.worldInteraction.collision.EntityAABB;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.chunk.ChunkVoxels;
import com.tessera.engine.common.world.wcc.WCCf;
import com.tessera.engine.common.world.wcc.WCCi;
import com.tessera.window.render.MVP;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Skin {
    public final EntityAABB position;
    public static EntityShader shader;
    public final MVP modelMatrix;
    public final Player player;
    WCCf chunkPosition;
    Vector3f prevWorldPosition = new Vector3f();
    float sunValue, torchValue;
    public final String name;
    protected boolean initialized;

    public Skin(String name, Player player) {
        this.name = name;
        this.player = player;
        initialized = false;
        this.position = player.aabb;
        chunkPosition = new WCCf();
        modelMatrix = new MVP();
    }

    public abstract void init();

    public abstract void render();


    private void getLightForPosition() {
        Chunk chunk = Client.world.getChunk(chunkPosition.chunk);
        byte light = (byte) 0b11110000;

        if (chunk != null) {
            light = chunk.voxels.getPackedLight(
                    (int) Math.floor(chunkPosition.chunkVoxel.x),
                    (int) Math.floor(chunkPosition.chunkVoxel.y),
                    (int) Math.floor(chunkPosition.chunkVoxel.z));

            for (int i = 1; i < 3; i++) { //Go up, if the block is in an opaque block
                if (light == 0) {
                    WCCi wcc = new WCCi();
                    wcc.set((int) Math.floor(position.worldPosition.x),
                            (int) Math.floor(position.worldPosition.y - i),
                            (int) Math.floor(position.worldPosition.z));
                    chunk = Client.world.getChunk(wcc.chunk);
                    if (chunk != null) {
                        light = chunk.voxels.getPackedLight(
                                wcc.chunkVoxel.x,
                                wcc.chunkVoxel.y,
                                wcc.chunkVoxel.z);
                    }
                } else break;
            }
        }

        //Unpack light
        sunValue = (float) ChunkVoxels.getSun(light) / 15;
        torchValue = (float) ChunkVoxels.getTorch(light) / 15;
    }

    public final void super_render(Matrix4f projection, Matrix4f view) {
        if (!initialized) { //Initialize
            init();
            initialized = true;
        }

        if (shader == null) {
            shader = new EntityShader();
        }
        //Update position
        if (!position.worldPosition.equals(prevWorldPosition)) { //If the entity has moved
            chunkPosition.set(position.worldPosition);
            getLightForPosition();
            prevWorldPosition.set(position.worldPosition);
        }

        modelMatrix.identity().translate(position.worldPosition)
//                .translate(-position.offset.x, 0, -position.offset.z)
                .rotateY((float) (-player.pan - Math.PI / 2));

        modelMatrix.update();
        modelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);
        shader.loadFloat(shader.uniform_sun, sunValue);
        shader.loadFloat(shader.uniform_torch, torchValue);

        shader.bind();
        shader.updateProjectionViewMatrix(projection, view);

        //Actually render the skin
        render();
    }
}
