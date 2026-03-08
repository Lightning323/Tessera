/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities.vehicle;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.common.players.localPlayer.LocalPlayer;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityMesh;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.utils.resource.ResourceLister;
import com.tessera.engine.common.worldInteraction.collision.PositionHandler;
import com.tessera.content.vanilla.Blocks;
import com.tessera.window.utils.texture.TextureUtils;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public abstract class Vehicle extends Entity {

    public static final String JSON_TEXTURE = "v_t";

    public static class Vehicle_staticData {
        public final EntityMesh body;
        public final HashMap<String, Integer> textures;

        public Vehicle_staticData(String bodyMesh, String texturesDir) throws IOException {
            body = new EntityMesh();

            body.loadFromOBJ(resourceLoader.getResourceAsStream(bodyMesh));

            //Generate textures
            String[] textureRes = ResourceLister.listSubResources(texturesDir);
            this.textures = new HashMap<>();
            for (String textureResource: textureRes) {
                String textureKey = resourceLoader.getName(textureResource).replace(".png", "");
                int textureID = Objects.requireNonNull(
                        TextureUtils.loadTextureFromResource(textureResource, false)).id;
                this.textures.put(textureKey, textureID);
            }
        }
    }

    /**
     * Resets the range of the degree back to 0-360
     */
    public float normalizeRotation(float rotationYDeg) {
        return rotationYDeg % 360;
    }

    /**
     * @return the client_distToPlayer
     */
    public float get3DDistToPlayer() {
        return distToPlayer;
    }

    public boolean isOnRoad() {
        int x = Math.round(worldPosition.x);
        int y = Math.round(worldPosition.y);
        int z = Math.round(worldPosition.z);
        short b1 = Client.world.getBlockID(x, y, z);
        short b2 = Client.world.getBlockID(x, y + 1, z);

        return b1 == (Blocks.BLOCK_MINECART_ROAD_BLOCK) || b1 == (Blocks.BLOCK_MINECART_ROAD_SLAB)
                || b2 == (Blocks.BLOCK_MINECART_ROAD_BLOCK) || b2 == (Blocks.BLOCK_MINECART_ROAD_SLAB);
    }

    private float rotationYDeg = 0;
    private boolean collisionEnabled = true;

    public boolean jumpWithSideCollision = false;
    public PositionHandler posHandler;
    ClientWindow window;
    LocalPlayer player;


    public Vehicle(ClientWindow window, long uniqueIdentifier) {
        super(uniqueIdentifier);
        this.window = window;
        this.player = Client.userPlayer;
    }

    /**
     * @return the collisionEnabled
     */
    public boolean isCollisionEnabled() {
        return collisionEnabled;
    }

    /**
     * @param collisionEnabled the collisionEnabled to setBlock
     */
    public void setCollisionEnabled(boolean collisionEnabled) {
        this.collisionEnabled = collisionEnabled;
    }

    public float getAngleToPlayer() {
        LocalPlayer userControlledPlayer = Client.userPlayer;
        return MathUtils.getAngleOfPoints(worldPosition.x, worldPosition.z,
                userControlledPlayer.worldPosition.x,
                userControlledPlayer.worldPosition.z);
    }

    public abstract void vehicle_draw();

    /**
     * @return the entity actually moved
     */
    public abstract boolean vehicle_move();

    private long lastJumpMS = 0;

    public void server_update() {
        aabb.update();
        if (vehicle_move()) {
            posHandler.update();
            if (
                    (Math.abs(posHandler.collisionHandler.collisionData.block_penPerAxes.x) > 0
                            || Math.abs(posHandler.collisionHandler.collisionData.block_penPerAxes.z) > 0)

                            && jumpWithSideCollision) {
                if (System.currentTimeMillis() - lastJumpMS > 200 && posHandler.onGround) {
                    posHandler.jump();
                    lastJumpMS = System.currentTimeMillis();
                }
            }
        }
    }

    @Override
    public final void client_draw() {
        if (inFrustum) {
            vehicle_draw();
        }
    }

    public void entityMoveEvent() {
        markAsModifiedByUser();
        vehicle_entityMoveEvent();
    }

    public abstract void vehicle_entityMoveEvent();

    public LocalPlayer getPlayer() {
        return Client.userPlayer;
    }


    public void goForward(float amount) {
        Vector2f vec = MathUtils.getCircumferencePoint(-getRotationYDeg(), amount);
        worldPosition.add(vec.x, 0, vec.y);
    }

    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        posHandler = new PositionHandler(window, Client.world, aabb, player.aabb);
    }


    public void serializeStateData(Output output, Kryo kryo) {
        kryo.writeObject(output, getRotationYDeg());
    }

    public void loadStateData(Input input, Kryo kryo) {
        rotationYDeg = kryo.readObject(input, Float.class);
    }

    public float getRotationYDeg() {
        return rotationYDeg;
    }

    public void setRotationYDeg(float rotationYDeg) {
        this.rotationYDeg = rotationYDeg;
    }
}
