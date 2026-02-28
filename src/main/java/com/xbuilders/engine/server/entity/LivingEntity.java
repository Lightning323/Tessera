/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.server.entity;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.xbuilders.Main;
import com.xbuilders.content.vanilla.Blocks;
import com.xbuilders.content.vanilla.entities.animal.mobile.AnimalRandom;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.client.ClientWindow;
import com.xbuilders.engine.server.item.ItemStack;
import com.xbuilders.engine.common.players.Player;
import com.xbuilders.engine.common.math.MathUtils;
import com.xbuilders.engine.common.worldInteraction.collision.PositionHandler;

import com.xbuilders.content.vanilla.Items;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class LivingEntity extends Entity {

    public Limb[] limbs;
    public final PositionHandler pos;
    public final ClientWindow window;
    public final Player player;
    public Consumer<Float> goForwardCallback;
    public boolean freezeMode = false;
    public boolean tamed = false;
    public int lifetime = 0;
    public float despawnLikelihood = 0.01f;
    public static final int OLD_LIFETIME = 100;
    public boolean isHostile = false;

    //Health
    public int maxHealth = 100;
    public float health = maxHealth;
    public float healthRegeneration = 0.1f;

    public boolean isHostile() {
        return isHostile && !tamed;
    }

    public void damage(float attackDamage) {
        health -= attackDamage;
    }

    public boolean isOld() {
        return lifetime >= OLD_LIFETIME;
    }

    public static final String JSON_SPECIES = "a_s";
    public static final String JSON_TAMED = "a_t";
    public static final String JSON_LIFETIME = "a_l";

    public boolean allowVoluntaryMovement() {
        return false;
    }

    public long lastTimeFed;
    private float rotationYDeg;
    public final AnimalRandom random;
    public int animalEatCooldownMS = 60 * 1000; //1 per minute


    public boolean isPendingDestruction() {
        return false;
    }

    public void facePlayer() {
        setRotationYDeg((float) Math.toDegrees(getYDirectionToPlayer()));
    }


    public void serializeStateData(Output output, Kryo kryo) {
        kryo.writeObject(output, rotationYDeg);
        random.writeState(output, kryo);
    }

    public void loadStateData(Input input, Kryo kryo) {
        rotationYDeg = kryo.readObject(input, float.class);
        random.readState(input, kryo);
    }


    public boolean playerHasAnimalFeed() {
        ItemStack heldItem = Client.userPlayer.getSelectedItem();
        return heldItem != null && heldItem.item.equals(Items.TOOL_ANIMAL_FEED);
    }

    public void eatAnimalFeed() {
    }

    @Override
    public void initSupplier(EntitySupplier entitySupplier) {
        super.initSupplier(entitySupplier);
        entitySupplier.spawnCondition = (x, y, z) -> {
            if (Main.getClient().world.getBlockID(x, y, z) == Blocks.BLOCK_WATER) return true;
            return false;
        };
        entitySupplier.despawnCondition = (e) -> {
            if (e instanceof LivingEntity) {
                LivingEntity a = (LivingEntity) e;
                if (a.tamed) return false;
            }
            return true;
        };
        entitySupplier.isAutonomous = true;
    }

    public LivingEntity(long uniqueId, ClientWindow window) {
        super(uniqueId);
        this.window = window;
        random = new AnimalRandom();
        this.player = Client.userPlayer;
        this.pos = new PositionHandler(window, Main.getClient().world, aabb, player.aabb);
        pos.setGravityEnabled(true);
        random.setSeed((int) getUniqueIdentifier());
        health = maxHealth;
    }

    public abstract void animal_move();

    public abstract void animal_drawBody();

    public void server_update() {
        if (allowVoluntaryMovement() && !freezeMode) animal_move();
        if (ClientWindow.frameCount % 10 == 0) {
            lifetime++;

            //Update health
            health += healthRegeneration;
            health = Math.min(health, maxHealth);

            if (
                    distToPlayer > 10
                            && !inFrustum
                            && Math.random() < despawnLikelihood &&
                            (supplier.despawnCondition == null || supplier.despawnCondition.despawn(this))
            ) {
                destroy();
            }
        }
    }

    public final void client_draw() {
        if (inFrustum || playerIsRidingThis()) {
            //Model matrix is our parent (body) matrix
            modelMatrix.rotateY((float) Math.toRadians(getRotationYDeg()));
            animal_drawBody();

            //Draw our limbs
            if (limbs != null) for (int i = 0; i < limbs.length; i++) limbs[i].inner_draw_limb(modelMatrix);
        }
        if (updatePosHandler || inFrustum || playerIsRidingThis()) {
            updatePosHandler = false;
            pos.update(); //Update if 1) it's been moved 2) we're in frustum 3) we're the player's animal
        }
    }

    private boolean updatePosHandler = false;

    public final void entityMoveEvent() {//Called after update position
        updatePosHandler = true;
    }

    public float getRotationYDeg() {
        return rotationYDeg;
    }

    public void setRotationYDeg(float rotationYDeg) {
        this.rotationYDeg = rotationYDeg;
    }


    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        if (hasData && node.has(JSON_TAMED)) tamed = node.get(JSON_TAMED).asBoolean();
        else tamed = !spawnedNaturally;

        if (hasData && node.has(JSON_LIFETIME)) lifetime = node.get(JSON_LIFETIME).asInt();
        else lifetime = random.nextBoolean() ? 0 : OLD_LIFETIME;
    }

    public void serializeDefinitionData(JsonGenerator generator) throws IOException {
        generator.writeBooleanField(JSON_TAMED, tamed);
        generator.writeNumberField(JSON_LIFETIME, lifetime);
    }


    /**
     * @return the angle in radians
     */
    public float getYDirectionToPlayer(Player player) {
        return (float) (-MathUtils.calcRotationAngle(worldPosition.x, worldPosition.z, player.worldPosition.x, player.worldPosition.z) + MathUtils.HALF_PI);
    }

    public float getYDirectionToPlayer() {
        return getYDirectionToPlayer(Client.userPlayer);
    }


    public void goForward(float amount, boolean jump) {
        amount *= window.smoothFrameDeltaSec * 50;
        if (freezeMode) return;
        Vector2f vec = MathUtils.getCircumferencePoint(-getRotationYDeg(), amount);
        worldPosition.add(vec.x, 0, vec.y);
        if (goForwardCallback != null) goForwardCallback.accept(amount);
        if (jump) jumpIfColliding(400, false);
    }

    private long lastJumpTime;

    private void jumpIfColliding(int interval /*ms*/, boolean jumpOverEntities) {
        Vector3f pen = pos.collisionHandler.collisionData.block_penPerAxes;
        if (jumpOverEntities) pen = pos.collisionHandler.collisionData.totalPenPerAxes;

        if (Math.abs(pen.x) > 0.02 || Math.abs(pen.z) > 0.02) {
            if (System.currentTimeMillis() - lastJumpTime > interval) {
                lastJumpTime = System.currentTimeMillis();
                pos.jump();
            }
        }
    }

    public String toString() {
        return "Animal: " + (tamed ? "Tamed" : "Wild") + " uid: " + getUniqueIdentifier();
    }

    public boolean tryToConsume(ItemStack itemStack) {
        if (System.currentTimeMillis() - lastTimeFed > animalEatCooldownMS) {
            lastTimeFed = System.currentTimeMillis();
            return true;
        }
        return false;
    }

}
