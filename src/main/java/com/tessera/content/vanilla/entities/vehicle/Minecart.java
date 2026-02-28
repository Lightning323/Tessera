/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities.vehicle;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.common.players.localPlayer.LocalPlayer;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.players.PositionLock;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.content.vanilla.Blocks;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author zipCoder933
 */
public class Minecart extends Vehicle {

    public static final float FORWARD_SPEED = 0.19f;
    public static final float UP_DOWN_SPEED = (FORWARD_SPEED / 2) - 0.005f;

    static Vehicle_staticData staticData;
    final PositionLock positionLock;
    public Vector3f renderOffset = new Vector3f();

    private int textureID;
    final String texture;

    public Vector3i getFixedPosition() {
        //Keep fixed position untouched, we want this as close to real world position as possible
        fixedPosition.x = (int) Math.round(worldPosition.x);//Rounding is important
        fixedPosition.y = (int) Math.round(worldPosition.y);
        fixedPosition.z = (int) Math.round(worldPosition.z);
        return fixedPosition;
    }

    public Minecart(ClientWindow window, long uniqueIdentifier, String texture) {
        super(window, uniqueIdentifier);
        this.texture = texture;
        aabb.setOffsetAndSize(.9f, 1f, .9f, true);
        positionLock = new PositionLock(this, 0);


        //Instead of modifying the fixed position, we modify our offset and render offset around the world position
        aabb.offset.x += 0.5f;
        aabb.offset.z += 0.5f;
        renderOffset.x += 0.5f;
        renderOffset.z += 0.5f;
        aabb.offset.y += 1;
        renderOffset.y += 1;

        positionLock.setOffset(renderOffset.x, renderOffset.y, renderOffset.z);

        frustumSphereRadius = (3f);
    }


    @Override
    public boolean run_ClickEvent() {
        LocalPlayer userControlledPlayer = Client.userPlayer;
        if (userControlledPlayer.positionLock == null) {
            Client.userPlayer.positionLock = positionLock;
            forwardBackDir = 0;
            resetKeyEvent();
            onTrack = alignToNearestTrack();
            if (onTrack) {
                Main.getClient().consoleOut("Press the forward and backward keys to toggle minecart direction.");
            } else {
                Main.getClient().consoleOut("Use WASD or arrow keys to navigate on minecart roads");
            }
        }
        return true; //If it was consumed
    }

    float rotationYCurve;


    @Override
    public void vehicle_draw() {
        rotationYCurve = (float) MathUtils.curve(rotationYCurve, getRotationYDeg(), 0.25f);
        modelMatrix.translate(renderOffset).scale(0.7f);
        modelMatrix.rotateY((float) (rotationYCurve * (Math.PI / 180)));

        if (upOrDownState == -1) {
            if (forwardBackDir == -1) {
                modelMatrix.rotateX(-0.4f);
            } else {
                modelMatrix.rotateX(0.4f);
            }
        } else if (upOrDownState == 1) {
            if (forwardBackDir == -1) {
                modelMatrix.rotateX(0.4f);
            } else {
                modelMatrix.rotateX(-0.4f);
            }
        }

        modelMatrix.update();
        modelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);
        staticData.body.draw(false, textureID);
    }


    @Override
    public boolean vehicle_move() {
        drawRedBox(fixedPosition.x, fixedPosition.y, fixedPosition.z);
        if (playerIsRidingThis()) {
            if (onTrack) {
                posHandler.collisionsEnabled = false;
                moveWithTrack(getFixedPosition());
            } else {
                posHandler.collisionsEnabled = true;
                freeMove();
            }
            return true;
        } else posHandler.collisionsEnabled = true;
        return get3DDistToPlayer() < 50;
    }


    @Override
    public void vehicle_entityMoveEvent() {

    }

    //        static Box testBox;
    final Vector3i fixedPosition = new Vector3i(0, 0, 0);


    static void drawRedBox(int x, int y, int z) {
//            testBox.setColor(1, 0, 0, 1);
//            testBox.setPosition(x, y, z);
//            testBox.draw(GameScene.projection, GameScene.view);
    }

    static void drawGreenBox(int x, int y, int z) {
//            testBox.setColor(0, 1, 0, 1);
//            testBox.setPosition(x, y, z);
//            testBox.draw(GameScene.projection, GameScene.view);
    }


    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);//Always call super!
        if (staticData == null) {//Only called once
            staticData = new Vehicle_staticData(
                    "assets/tessera/entities/minecart\\minecart.obj",
                    "assets/tessera/entities/minecart/textures");
        }
        if (texture != null) {
            textureID = staticData.textures.get(texture);
        }
        alignToNearestTrack();
    }

    static int getOrientationModified(BlockData b) {
        if (b != null && b.size() > 0) {
            return (b.get(0) + 1) % 4;
        }
        return 0;
    }

    static int getOrientation(BlockData b) {
        return b.get(0);
    }

    private boolean alignToNearestTrack() {
        getFixedPosition();
        Vector3i currentTrackPiece = getNearestTrackPiece(fixedPosition.x, fixedPosition.y, fixedPosition.z);
        if (currentTrackPiece != null) {
            BlockData orientation = Client.world.getBlockData(currentTrackPiece.x, currentTrackPiece.y, currentTrackPiece.z);
            if (getOrientationModified(orientation) == 0 || getOrientationModified(orientation) == 2) {
                worldPosition.x = currentTrackPiece.x;
                this.setRotationYDeg((float) 0);
            } else {
                worldPosition.z = currentTrackPiece.z;
                this.setRotationYDeg((float) 90);
            }
            rotationYCurve = getRotationYDeg();
            return true;
        }
        return false;
    }


    private void freeMove() {
        upOrDownState = 0;
        posHandler.setGravityEnabled(true);
        float rotateSpeed = 0;
        float targetSpeed = 0;

        if (isOnRoad()) {
            rotateSpeed = 0.5f;
            if (getPlayer().forwardKeyPressed()) {
                targetSpeed = FORWARD_SPEED;
                rotateSpeed = 2.0f;
            } else if (getPlayer().backwardKeyPressed()) {
                targetSpeed = -0.08f;
                rotateSpeed = 1.0f;
            }
            speedCurve = (float) MathUtils.curve(speedCurve, targetSpeed, 0.03f);
        } else {
            rotateSpeed = 1.5f;
            if (getPlayer().forwardKeyPressed()) {
                targetSpeed = 0.015f;
            } else if (getPlayer().backwardKeyPressed()) {
                targetSpeed = -0.01f;
            }
            speedCurve = (float) MathUtils.curve(speedCurve, targetSpeed, 0.2f);
        }

        if (getPlayer().rightKeyPressed()) {
            float rotationY1 = getRotationYDeg() + rotateSpeed;
            this.setRotationYDeg(rotationY1);
        } else if (getPlayer().leftKeyPressed()) {
            float rotationY1 = getRotationYDeg() - rotateSpeed;
            this.setRotationYDeg(rotationY1);
        }
        setRotationYDeg(normalizeRotation(getRotationYDeg()));
        rotationYCurve = getRotationYDeg();
        goForward(speedCurve);
    }

    public float snapDegreeTo90(int degree) {
        float roundedDegree = Math.round(degree / 90.0) * 90; // this will give you 90
        // Return the snapped degree
        return roundedDegree;
    }


    float speedCurve;
    int forwardBackDir = 0;
    final Vector3i lastPos = new Vector3i(0, 0, 0);
    Vector3i rotPos;
    private boolean rotationEnabled = false;
    boolean onTrack = false;
    int upOrDownState = 0;
//        long riseInertaChangeMS = 0;

    /**
     * @param position the rotated to set
     */
    private void disableRotation(Vector3i position) {
        rotPos = position;
        this.rotationEnabled = false;
    }

    public void enableRotation() {
        this.rotationEnabled = true;
    }


    private void moveWithTrack(Vector3i pos) {
        this.forwardBackDir = assignForwardOrBackward(forwardBackDir);//0=stop,-1=back,1=go
        float speed = forwardBackDir > 0 ? FORWARD_SPEED : -FORWARD_SPEED;
        posHandler.setGravityEnabled(true);

        Block b = Client.world.getBlock(pos.x, pos.y, pos.z);
        Block bup = Client.world.getBlock(pos.x, pos.y - 1, pos.z);
        Block bdown = Client.world.getBlock(pos.x, pos.y + 1, pos.z);


        if (forwardBackDir == 0) {//If we are stopped
            if (b.id == Blocks.BLOCK_SWITCH_JUNCTION) {
                if (Client.userPlayer.leftKeyPressed()) {
                    if (switchJunctionKeyEvent) {
                        float rotationY1 = getRotationYDeg() + 90;
                        this.setRotationYDeg(rotationY1);
                        switchJunctionKeyEvent = false;
                    }
                } else if (Client.userPlayer.rightKeyPressed()) {
                    if (switchJunctionKeyEvent) {
                        float rotationY1 = getRotationYDeg() - 90;
                        this.setRotationYDeg(rotationY1);
                        switchJunctionKeyEvent = false;
                    }
                } else {
                    switchJunctionKeyEvent = true;
                }
            }
        } else {
            if (b.id == Blocks.BLOCK_SWITCH_JUNCTION) {
                stop(pos);
                goForward(speed);
            } else if (b.id == Blocks.BLOCK_TRACK_STOP) {
                stop(pos);
                goForward(speed);
            } else if (b.id == Blocks.BLOCK_CROSSTRACK) {
                enableRotation();
                goForward(speed);
            } else if (b.id == Blocks.BLOCK_CURVED_TRACK) {
                if (rotationEnabled) {
                    worldPosition.x = pos.x;
                    worldPosition.z = pos.z;

                    if (curvedTrackIsPointingLeft(lastPos, pos)) {
                        float rotationY1 = getRotationYDeg() - 90;
                        this.setRotationYDeg(rotationY1);
                    } else {
                        float rotationY1 = getRotationYDeg() + 90;
                        this.setRotationYDeg(rotationY1);
                    }
                    disableRotation(pos);
                }
                goForward(speed);
            } else if (b.id == Blocks.BLOCK_MERGE_TRACK) {
                if (rotationEnabled) {
                    worldPosition.x = pos.x;
                    worldPosition.z = pos.z;

                    if (mergeTrackShouldTurnLeft(lastPos, pos)) {
                        float rotationY1 = getRotationYDeg() - 90;
                        this.setRotationYDeg(rotationY1);
                    } else {
                        float rotationY1 = getRotationYDeg() + 90;
                        this.setRotationYDeg(rotationY1);
                    }
                    disableRotation(pos);
                }
                goForward(speed);
            } else {//Otherwise...
                enableRotation();

                //Raise or lower tracks
                if (
                        b.id == Blocks.BLOCK_RAISED_TRACK ||
                                bup.id == Blocks.BLOCK_RAISED_TRACK ||
                                bdown.id == Blocks.BLOCK_RAISED_TRACK
                ) {

                    boolean trackIsGoingUp = true;

                    int orientation = -1;
                    if (b.id == Blocks.BLOCK_RAISED_TRACK) {
                        orientation = getOrientation(Client.world.getBlockData(pos.x, pos.y, pos.z));
                    } else if (bdown.id == Blocks.BLOCK_RAISED_TRACK) {
                        orientation = getOrientation(Client.world.getBlockData(pos.x, pos.y + 1, pos.z));
                    } else if (bup.id == Blocks.BLOCK_RAISED_TRACK) {
                        orientation = getOrientation(Client.world.getBlockData(pos.x, pos.y - 1, pos.z));
                    }

                    trackIsGoingUp = true;
                    if (getLastStep() == null) {
                        //If there is a raised track above us, we are probbably going up
                        trackIsGoingUp = bup.id == Blocks.BLOCK_RAISED_TRACK;

                        //If there is a raised track below us, we are going down
                        if (bdown.id == Blocks.BLOCK_RAISED_TRACK) trackIsGoingUp = false;
                    } else {
                        switch (orientation) {
                            case 0 -> trackIsGoingUp = getLastStep().x > pos.x;
                            case 1 -> trackIsGoingUp = getLastStep().z > pos.z;
                            case 2 -> trackIsGoingUp = getLastStep().x < pos.x;
                            default -> trackIsGoingUp = getLastStep().z < pos.z;
                        }
//                            Vector3i netPos = new Vector3i(pos).sub(getLastStep());
//                            System.out.println("Raised Track Orientation: " + orientation + " netPos: " + MiscUtils.printVector(netPos) + " " + "   " + (trackIsGoingUp ? "Up" : "Down"));
                    }

                    if (trackIsGoingUp) {
                        posHandler.setGravityEnabled(false);
                        worldPosition.y -= UP_DOWN_SPEED;
                        goForward(UP_DOWN_SPEED * forwardBackDir);
                        upOrDownState = -1;
//                            riseInertaChangeMS = System.currentTimeMillis();
                    } else {
                        posHandler.setGravityEnabled(false);
                        worldPosition.y += UP_DOWN_SPEED;
                        goForward(UP_DOWN_SPEED * forwardBackDir);
                        upOrDownState = 1;
//                            riseInertaChangeMS = System.currentTimeMillis();
                    }
                }
                //Default track
                else if (onTrack) {
                    posHandler.setGravityEnabled(true);
                    upOrDownState = 0;
                    BlockData orientation = Client.world.getBlockData(pos.x, pos.y, pos.z);
                    if (orientation != null) {
                        if (getOrientationModified(orientation) == 0
                                || getOrientationModified(orientation) == 2) {
                            worldPosition.x = pos.x;
                        } else {
                            worldPosition.z = pos.z;
                        }
                    }
                    goForward(speed);

                    if (getNearestTrackPiece(pos.x, pos.y, pos.z) == null) {
                        onTrack = false;
                    }
                }
            }

            if (rotationEnabled == false && !pos.equals(rotPos)) {
                rotPos = pos;
                enableRotation();
            }
        }

        if (lastPos.x != pos.x || lastPos.z != pos.z) {
            steps.add(new Vector3i(pos));
            lastPos.set(pos);
            if (steps.size() > 2) {
                steps.remove(0);
            }
//                System.out.println("Steps: " + steps.toString() + " Last Pos: " + lastPos.toString());
        }
    }

    public Vector3i getLastStep() {
        if (steps.size() > 1) {
            return steps.get(steps.size() - 2);
        }
        return null;
    }

    ArrayList<Vector3i> steps = new ArrayList<>();


    private void stop(Vector3i position) {
        steps.clear();
        if (rotationEnabled) {
            forwardBackDir = 0;
            disableRotation(position);
        }
    }


    //Utils
    static boolean keyEvent = false;
    static boolean switchJunctionKeyEvent = false;

    static void resetKeyEvent() {
        keyEvent = true;
        switchJunctionKeyEvent = true;
    }


    public static int assignForwardOrBackward(int direction) {
        if (Client.userPlayer.forwardKeyPressed()) {
            if (keyEvent) {
                if (direction == 0) {
                    direction = 1;
                } else {
                    direction = 0;
                }
                keyEvent = false;
            }
        } else if (Client.userPlayer.backwardKeyPressed()) {
            if (keyEvent) {
                if (direction == 0) {
                    direction = -1;
                } else {
                    direction = 0;
                }
                keyEvent = false;
            }
        } else {
            keyEvent = true;
        }
        return direction;
    }

    public static boolean isTrack(int x, int y, int z) {
        Block block = Client.world.getBlock(x, y, z);
        return isTrack(block.id);

    }

    public static boolean mergeTrackShouldTurnLeft(Vector3i lastPos, Vector3i pos) {
        BlockData orientation = Client.world.getBlockData(pos.x, pos.y, pos.z);
        int o = getOrientation(orientation);

        boolean b = false;
        if (o == 0 || o == 1) {//We only need to chech 1 horizontal axis to see what direction the track is pointing
            b = pos.z < lastPos.z;
        } else {
            b = pos.z > lastPos.z;
        }
//            System.out.println("CURVE:" + o + "   " + (b ? "LEFT" : "RIGHT"));
        return b;
    }

    public static boolean curvedTrackIsPointingLeft(Vector3i lastPos, Vector3i pos) {
        BlockData orientation = Client.world.getBlockData(pos.x, pos.y, pos.z);
        boolean b = false;
        if (lastPos != null && orientation != null) {
            int o = getOrientation(orientation);
            switch (o) {
                /**
                 * We only have to check which axis is the same to determine the proper direction
                 */
                case 0 -> {
                    b = (pos.x != lastPos.x);
                }
                case 1 -> {
                    b = (pos.x == lastPos.x);
                }
                case 2 -> {
                    b = (pos.x != lastPos.x);
                }
                case 3 -> {
                    b = (pos.x == lastPos.x);
                }
            }
//                System.out.println("CURVE:" + o + "   " + (b ? "LEFT" : "RIGHT"));
            return b;
        }
        return false;
    }

    public static boolean isTrack(short block) {
        return block == Blocks.BLOCK_STRAIGHT_TRACK
                || block == Blocks.BLOCK_RAISED_TRACK
                || block == Blocks.BLOCK_CROSSTRACK
                || block == Blocks.BLOCK_CURVED_TRACK
                || block == Blocks.BLOCK_SWITCH_JUNCTION
                || block == Blocks.BLOCK_MERGE_TRACK
                || block == Blocks.BLOCK_TRACK_STOP;
    }

    public static Vector3i getNearestTrackPiece(int x, int y, int z) {
        if (isTrack(x, y, z)) {
            return new Vector3i(x, y, z);
        } else if (isTrack(x, y + 1, z)) {
            return new Vector3i(x, y + 1, z);
        } else if (isTrack(x + 1, y + 1, z)) {
            return new Vector3i(x + 1, y + 1, z);
        } else if (isTrack(x - 1, y + 1, z)) {
            return new Vector3i(x - 1, y + 1, z);
        } else if (isTrack(x, y + 1, z + 1)) {
            return new Vector3i(x, y + 1, z + 1);
        } else if (isTrack(x, y + 1, z - 1)) {
            return new Vector3i(x, y + 1, z - 1);
        } else if (isTrack(x + 1, y, z)) {
            return new Vector3i(x + 1, y, z);
        } else if (isTrack(x - 1, y, z)) {
            return new Vector3i(x - 1, y, z);
        } else if (isTrack(x, y, z + 1)) {
            return new Vector3i(x, y, z + 1);
        } else if (isTrack(x, y, z - 1)) {
            return new Vector3i(x, y, z - 1);
        }
        return null;
    }

}
