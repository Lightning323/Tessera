/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.worldInteraction.collision;

import com.tessera.engine.common.math.AABB;

import java.lang.Math;
import java.nio.FloatBuffer;

import org.joml.*;
import org.lwjgl.system.MemoryUtil;

/**
 * @author zipCoder933
 */
public class CollisionData {

    //Information only useful to collision handler
    protected Vector3i collisionNormal;//just a pointer to one of the 6 possible faces
    protected final FloatBuffer penetration;//penetration amount
    protected final FloatBuffer distances; //a list of distances
    protected final Vector3f penPerAxes = new Vector3f(); //penetration per axes

    //Information useful everywhere
    public final Vector3f totalPenPerAxes = new Vector3f(); //sum of all penetrations per axes
    public final Vector3f block_penPerAxes = new Vector3f(); //penetration per axes
    public final Vector3f entity_penPerAxes = new Vector3f(); //sum of all penetrations per axes

    public CollisionData() {
        distances = MemoryUtil.memAllocFloat(6);
        penetration = MemoryUtil.memAllocFloat(1);
        collisionNormal = null;
    }

    public static final Vector3i[] faces = {
            new Vector3i(-1, 0, 0), new Vector3i(1, 0, 0),
            new Vector3i(0, -1, 0), new Vector3i(0, 1, 0),
            new Vector3i(0, 0, -1), new Vector3i(0, 0, 1),};

    public static double calculateXIntersection(AABB boxA, AABB boxB) {
        // Determine the intersection rectangle's boundaries
        double intersectMinX = Math.max(boxA.min.x, boxB.min.x);
        double intersectMaxX = Math.min(boxA.max.x, boxB.max.x);
        return Math.max(0, intersectMaxX - intersectMinX);
    }

    public static double calculateZIntersection(AABB boxA, AABB boxB) {
        // Determine the intersection rectangle's boundaries
        double intersectMinZ = Math.max(boxA.min.z, boxB.min.z);
        double intersectMaxZ = Math.min(boxA.max.z, boxB.max.z);
        return Math.max(0, intersectMaxZ - intersectMinZ);
    }

    public void calculateCollision(AABB boxA, AABB boxB, boolean otherBoxIsEntity) {
        Vector3f minA = boxA.min;
        Vector3f minB = boxB.min;
        Vector3f maxA = new Vector3f(minA).add(boxA.getXLength(), boxA.getYLength(), boxA.getZLength());
        Vector3f maxB = new Vector3f(minB).add(boxB.getXLength(), boxB.getYLength(), boxB.getZLength());
        penetration.put(0, Float.MAX_VALUE);

        distances.put(0, (maxB.x - minA.x));// distance of box 'block' to 'left ' of 'a '.
        distances.put(1, (maxA.x - minB.x));// distance of box 'block' to 'right ' of 'a '.
        distances.put(2, (maxB.y - minA.y));// distance of box 'block' to 'bottom ' of 'a '.
        distances.put(3, (maxA.y - minB.y));// distance of box 'block' to 'top ' of 'a '.
        distances.put(4, (maxB.z - minA.z));// distance of box 'block' to 'far ' of 'a '.
        distances.put(5, (maxA.z - minB.z)); // distance of box 'block' to 'near ' of 'a '.

        for (int i = 0; i < 6; i++) {
            if (distances.get(i) < penetration.get(0)) {
                penetration.put(0, distances.get(i));
                collisionNormal = faces[i];
            }
        }
        penPerAxes.set(collisionNormal).mul(penetration.get(0));

        totalPenPerAxes.add(penPerAxes);
        if (otherBoxIsEntity) {
            entity_penPerAxes.add(penPerAxes);
        } else {
            block_penPerAxes.add(penPerAxes);
        }
    }

    public void reset() {
        block_penPerAxes.set(0, 0, 0);
        entity_penPerAxes.set(0, 0, 0);
        totalPenPerAxes.set(0, 0, 0);
    }
}
