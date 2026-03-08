/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities.vehicle;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.common.players.PositionLock;
import com.tessera.engine.common.math.MathUtils;

import java.io.IOException;

/**
 * @author zipCoder933
 */
public class Boat extends Vehicle {

    static Vehicle_staticData staticData;
    private final String textureFile;
    public int textureID;

    public Boat(ClientWindow window, long uniqueIdentifier, String textureFile) {
        super(window, uniqueIdentifier);
        this.textureFile = textureFile;
        frustumSphereRadius = (1.5f);
        aabb.setOffsetAndSize(1.5f, 1f, 1.5f, true);
    }

    public boolean isInWater() {
        int wx = (int) worldPosition.x;
        int wy = (int) worldPosition.y;
        int wz = (int) worldPosition.z;
        boolean belowBLockLiquid = Client.world.getBlock(wx, wy + 1, wz).isLiquid()
                && worldPosition.y > wy + 0.85f;//We dont have to stand by strict block coordinates

        isInWater = Client.world.getBlock(wx, wy, wz).isLiquid()
                || Client.world.getBlock(wx, wy - 1, wz).isLiquid()
                || belowBLockLiquid;

        return isInWater;
    }

    boolean isInWater;

    @Override
    public void vehicle_draw() {
        modelMatrix.rotateY((float) (getRotationYDeg() * (Math.PI / 180)));
        modelMatrix.update();
        modelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);
        staticData.body.draw(false, textureID);
    }

    boolean rise;

    @Override
    public boolean vehicle_move() {
        int wx = (int) worldPosition.x;
        int wy = (int) worldPosition.y;
        int wz = (int) worldPosition.z;
        isInWater();

        if (playerIsRidingThis()) {
            if (isInWater) {
                if (Client.world.getBlock(wx, wy - 1, wz).isLiquid()) {
                    rise = true;
                }
            } else {
                worldPosition.y -= 0.03;
                rise = false;
            }

            float rotateSpeed = 0.5f;
            float targetSpeed = 0;
            if (getPlayer().forwardKeyPressed()) {
                if (isInWater) {
                    targetSpeed = 0.15f;
                    rotateSpeed = 2.0f;
                } else {
                    rotateSpeed = 0.5f;
                    targetSpeed = 0.03f;
                    speedCurve = targetSpeed;
                }
            } else if (getPlayer().backwardKeyPressed()) {
                if (isInWater) {
                    targetSpeed = -0.08f;
                    rotateSpeed = 1.0f;
                } else {
                    rotateSpeed = 0.5f;
                    targetSpeed = -0.01f;
                    speedCurve = targetSpeed;
                }
            }

            if (rise) {
                worldPosition.y -= 0.01;
            }

            if (getPlayer().leftKeyPressed()) {
                float rotationY1 = getRotationYDeg() + rotateSpeed;
                this.setRotationYDeg(rotationY1);
            } else if (getPlayer().rightKeyPressed()) {
                float rotationY1 = getRotationYDeg() - rotateSpeed;
                this.setRotationYDeg(rotationY1);
            }
            speedCurve = (float) MathUtils.curve(speedCurve, targetSpeed, 0.03f);
            goForward(speedCurve);
        } else if (isInWater) {
            speedCurve = (float) MathUtils.curve(speedCurve, 0, 0.03f);
            goForward(speedCurve / 2);
        }
        return get3DDistToPlayer() < 50;
    }

    @Override
    public void vehicle_entityMoveEvent() {
        if (posHandler != null) posHandler.setGravityEnabled(isInWater() ? false : true);
    }


    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);//Always call super!

        if (staticData == null) {//Only called once
            staticData = new Vehicle_staticData(
                    "assets/tessera/entities/boat/boat.obj",
                    "assets/tessera/entities/boat/textures");
        }

        posHandler.setGravityEnabled(isInWater() ? false : true);

        if (textureFile != null) {
            textureID = staticData.textures.get(textureFile);
        }
    }


    float speedCurve;


    @Override
    public boolean run_ClickEvent() {
        Client.userPlayer.positionLock = new PositionLock(this, 0);
        return true;
    }


}

