/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.worldInteraction.collision;

import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.World;
import org.joml.Vector3f;

/**
 * @author zipCoder933
 */
public class EntityAABB {

    public EntityAABB() {
        box = new AABB();
        offset = new Vector3f();
        size = new Vector3f();
        worldPosition = new Vector3f();
    }

    public void clamp(boolean clampToTopOfWorld) {
        //Clamp world position
        if (worldPosition.y > World.WORLD_BOTTOM_Y - box.getYLength()) {
            worldPosition.y = World.WORLD_BOTTOM_Y - box.getYLength();
        }
        if (clampToTopOfWorld && worldPosition.y < World.WORLD_TOP_Y) {
            worldPosition.y = World.WORLD_TOP_Y;

        }


//        //We still need to clamp to the top of the world
//        else if (worldPosition.y < World.WORLD_TOP_Y - GameScene.world.getViewDistance() * 2) {
//            worldPosition.y = World.WORLD_TOP_Y - GameScene.world.getViewDistance() * 2;
//        }


        worldPosition.x = MathUtils.clamp(worldPosition.x, World.WORLD_SIZE_NEG_X, World.WORLD_SIZE_POS_X);
        worldPosition.z = MathUtils.clamp(worldPosition.z, World.WORLD_SIZE_NEG_Z, World.WORLD_SIZE_POS_Z);
    }

    public void updateBox() {
        //Update AABB to match world position
        box.setPosAndSize(
                worldPosition.x + offset.x,
                worldPosition.y + offset.y,
                worldPosition.z + offset.z,
                size.x, size.y, size.z);
    }

    /**
     * Clamps world position and updates aabb box
     */
    public void update(boolean clampToTopOfWorld) {
        clamp(clampToTopOfWorld);
        updateBox();
    }

    public void update() {
        clamp(false);
        updateBox();
    }

    public final Vector3f worldPosition;
    public final AABB box;
    public final Vector3f offset;
    public final Vector3f size;
    public boolean isSolid = true;

    public void setOffsetAndSize(float sizeX, float sizeY, float sizeZ, boolean alignToGround) {
        size.set(sizeX, sizeY, sizeZ);
        if (alignToGround) offset.set(-(size.x / 2), -size.y, -(size.z / 2));
        else offset.set(-(size.x / 2), -(size.y / 2), -(size.z / 2));
        update();
    }

    public void setOffsetAndSize(float offX, float offY, float offZ,
                                 float sizeX, float sizeY, float sizeZ) {
        offset.set(offX, offY, offZ);
        size.set(sizeX, sizeY, sizeZ);
        update();
    }
}
