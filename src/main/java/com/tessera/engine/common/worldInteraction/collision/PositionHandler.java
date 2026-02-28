/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.worldInteraction.collision;

import com.tessera.engine.client.visuals.gameScene.GameScene;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.client.visuals.gameScene.rendering.wireframeBox.Box;
import com.tessera.engine.common.world.World;
import com.tessera.window.GLFWWindow;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.Consumer;

/**
 * @author zipCoder933
 */
public class PositionHandler {

    public boolean isFrozen() {
        return frozen && collisionsEnabled;
    }

    /**
     * @param frozen the frozen to setPosAndSize
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * @return the gravityEnabled
     */
    public boolean isGravityEnabled() {
        return gravityEnabled;
    }

    /**
     * @param gravityEnabled the gravityEnabled to setPosAndSize
     */
    public void setGravityEnabled(boolean gravityEnabled) {
        this.gravityEnabled = gravityEnabled;
    }

    public void setFallMedium(float gravity, float terminalVelocity) {
        this.gravity = gravity;
        this.terminalVelocity = Math.min(terminalVelocity, MAX_TERMINAL_VELOCITY);
    }

    public void resetFallMedium() {
        this.gravity = DEFAULT_GRAVITY;
        this.terminalVelocity = MAX_TERMINAL_VELOCITY;
    }


    //static Constants
    public static final boolean DRAW_ENTITY_BOX = false;
    public static final boolean DRAW_COLLISION_CANDIDATES = false;
    public static final float BLOCK_COLLISION_CANDIDATE_CHECK_RADIUS = 2;
    public static final float ENTITY_COLLISION_CANDIDATE_CHECK_RADIUS = 10;
    public static final float DEFAULT_GRAVITY = 0.42f;
    public static final float MAX_TERMINAL_VELOCITY = 60f;
    public static final float MIN_JUMP_GRAVITY = DEFAULT_GRAVITY / 2;
    public static final float DEFAULT_COAST = 0.6f;
    public static final float STEP_HEIGHT = 0.6f;

    //Variables
    public float maxFallSpeed;
    protected final Vector3f velocity = new Vector3f();
    private boolean frozen = false;
    private boolean gravityEnabled;

    public final GLFWWindow window;
    public float surfaceCoasting = 0.75f;
    public float surfaceFriction = 0f;
    private float gravity = DEFAULT_GRAVITY;
    private float terminalVelocity = MAX_TERMINAL_VELOCITY;
    public boolean collisionsEnabled = true;
    public final EntityAABB aabb;
    public final CollisionHandler collisionHandler;
    public boolean isFalling;

    protected Box renderedBox;
    private float frameDeltaSec;
    private float fallDistance;
    private boolean hitGround = false;
    private float movementY;
    public boolean onGround;

    public Consumer<Float> callback_onGround;

    public PositionHandler(GLFWWindow window, World world,
                           EntityAABB thisAABB,
                           EntityAABB UserPlayerAABB) {
        this.window = window;
        frozen = false;
        gravityEnabled = true;
        this.aabb = thisAABB;
        collisionsEnabled = true;
        collisionHandler = new CollisionHandler(world, this, thisAABB, UserPlayerAABB);
    }

    public void update() {
        update(1);
    }

    public void update(final int timestepMultiplier) {
        if (//Init the rendered box inside the draw method so that we dont get problems if it posHandler not constructed properly
                (DRAW_COLLISION_CANDIDATES || DRAW_ENTITY_BOX)
                        && renderedBox == null) {
            renderedBox = new Box();
            renderedBox.setLineWidth(15);
        }

        try {
            frameDeltaSec = window.smoothFrameDeltaSec;

            aabb.update(); //Update the aabb first
            if (DRAW_ENTITY_BOX) {
                renderedBox.setLineWidth(2);
                renderedBox.setColor(new Vector4f(1, 0, 0, 1));
                renderedBox.set(aabb.box);
                renderedBox.draw(GameScene.projection, GameScene.view);
            }

            if (!isFrozen()) {

                //Set the coast and friction
                if (!gravityEnabled || !collisionsEnabled ||
                        collisionHandler.floorBlock == null ||
                        collisionHandler.floorBlock == BlockRegistry.BLOCK_AIR) {
                    surfaceCoasting = DEFAULT_COAST;
                    surfaceFriction = 0;
                } else {
                    surfaceCoasting = collisionHandler.floorBlock.surfaceCoast;
                    surfaceFriction = collisionHandler.floorBlock.surfaceFriction;
                }

                if (surfaceFriction > 0) {//Apply friction
                    velocity.x *= 1 - surfaceFriction;
                    velocity.z *= 1 - surfaceFriction;
                }

                //Handle Y velocity
                if (collisionsEnabled && isGravityEnabled()) {
                    double fallSpeed = (gravity * frameDeltaSec);
                    //TODO: Cap the number of times we can update PositionHandler (10fps) (The movement is jittery when running against walls, if we try to limit that here)
                    if (window.getMsPerFrame() < 10)
                        fallSpeed /= 4; //For some reason, we need to fall slower if the MPF is too low
                    this.velocity.y += fallSpeed;
                } else {
                    velocity.y *= 0.75f; //Vertical coasting
                }


                //We dont want to modify the actual velocity because we need to compute fall damage
                maxFallSpeed = terminalVelocity * frameDeltaSec;
//                if (velocity.y > maxFallSpeed) {
//                    System.out.println("FALLING FAST!: " + velocity.y);
//                }

                movementY = (Math.min(velocity.y, maxFallSpeed) * timestepMultiplier);
                isFalling = movementY > 0.05f;
                if (isFalling) {
                    hitGround = false;
                    fallDistance += movementY;
                } else {
                    hitGround();
                }

                //Calculate new AABB
                aabb.box.setX(aabb.box.min.x + (velocity.x * timestepMultiplier));
                aabb.box.setY(aabb.box.min.y + movementY);
                aabb.box.setZ(aabb.box.min.z + (velocity.z * timestepMultiplier));

                //Apply coasting
                velocity.x *= surfaceCoasting;
                velocity.z *= surfaceCoasting;
            }

            onGround = false;
            if (collisionsEnabled) {
                collisionHandler.resolveCollisions(GameScene.projection, GameScene.view);
            }

            if (onGround) hitGround();

            //calculate new world position
            aabb.worldPosition.x = aabb.box.min.x - aabb.offset.x;
            aabb.worldPosition.y = aabb.box.min.y - aabb.offset.y;
            aabb.worldPosition.z = aabb.box.min.z - aabb.offset.z;
            aabb.clamp(false);
        } catch (Exception e) {
            e.printStackTrace(); //Safely serverExecute any exceptions
        }
    }

    protected void hitGround() {
        if (!hitGround && callback_onGround != null) {//Hit ground event
            callback_onGround.accept(fallDistance);
        }
        hitGround = true;
        fallDistance = 0;
    }


    public final void jump() {
        if (isGravityEnabled() && onGround) {
            double jumpHeight = (Math.max(MIN_JUMP_GRAVITY, gravity) * 18 * frameDeltaSec);
            this.velocity.y -= jumpHeight;
        }
    }

    public void addVelocity(float x, float y, float z) {
        this.velocity.add(x * frameDeltaSec, y * frameDeltaSec, z * frameDeltaSec);
    }

    public void setVelocity(float x, float y, float z) {
        this.velocity.set(x, y, z);
    }

    public void setVelocity(float x, float z) {
        this.velocity.x = x;
        this.velocity.z = z;
    }

    public Vector3f getVelocity() {
        return velocity;
    }
}
