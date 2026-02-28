/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.math;

import com.tessera.utils.MiscUtils;

import java.nio.FloatBuffer;
import java.util.Objects;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author zipCoder933
 */
public class AABB {

    /**
     * @return the xLength
     */
    public float getXLength() {
        return max.x - min.x;
    }

    /**
     * @return the yLength
     */
    public float getYLength() {
        return max.y - min.y;
    }

    /**
     * @return the zLength
     */
    public float getZLength() {
        return max.z - min.z;
    }

    public void setX(float x) {
        max.x = x + getXLength();
        min.x = x;
    }

    public void setY(float y) {
        max.y = y + getYLength();
        min.y = y;
    }

    public void setZ(float z) {
        max.z = z + getZLength();
        min.z = z;
    }

    public Vector3f min, max;

    public AABB(MemoryStack stack) {
        min = new Vector3f(stack.mallocFloat(3));
        max = new Vector3f(stack.mallocFloat(3));
    }

    public AABB(FloatBuffer minPoint2, FloatBuffer maxPoint2) {
        min = new Vector3f(minPoint2);
        max = new Vector3f(maxPoint2);
    }

    public AABB() {
        min = new Vector3f(0);
        max = new Vector3f(0);
    }

    public AABB setPosAndSize(float x, float y, float z, float xLength, float yLength, float zLength) {
        min.set(x, y, z);
        max.set(x + xLength, y + yLength, z + zLength);
        return this;
    }

    public void set(AABB box) {
        min.set(box.min);
        max.set(box.max);
    }

    public AABB(AABB aabb) {
        this.min = new Vector3f(aabb.min);
        this.max = new Vector3f(aabb.max);
    }

    public boolean intersects(AABB other) {
        return max.x > other.min.x && min.x < other.max.x
                && max.y > other.min.y && min.y < other.max.y
                && max.z > other.min.z && min.z < other.max.z;
    }



    /**
     *
     * @param other the other AABB
     * @return the amount that the other box is intersecting with this box on
     * the X axis, or in other words, <b>the amount of displacement on the X
     * axis to make the other bounding box not colliding with this bounding
     * box.</b>
     */
    public float getXPenetrationDepth(AABB other) {
        if (min.x < other.min.x) {
            return getXLength() - (other.min.x - min.x);
        } else if (min.x > other.min.x) {
            return 0 - (other.getXLength() - (min.x - other.min.x));
        } else {
            return 0;
        }
    }

    /**
     * @param other the other AABB
     * @return the amount that the other box is intersecting with this box on
     * the Y axis, or in other words, <b>the amount of displacement on the Y
     * axis to make the other bounding box not colliding with this bounding
     * box.</b>
     */
    public float getYPenetrationDepth(AABB other) {
        if (min.y < other.min.y) {
            return getYLength() - (other.min.y - min.y);
        } else if (min.y > other.min.y) {
            return 0 - (other.getYLength() - (min.y - other.min.y));
        } else {
            return 0;
        }
    }

    /**
     * @param other the other AABB
     * @return the amount that the other box is intersecting with this box on
     * the Z axis, or in other words, <b>the amount of displacement on the Z
     * axis to make the other bounding box not colliding with this bounding
     * box.</b>
     */
    public float getZPenetrationDepth(AABB other) {
        if (min.z < other.min.z) {
            return getZLength() - (other.min.z - min.z);
        } else if (min.z > other.min.z) {
            return 0 - (other.getZLength() - (min.z - other.min.z));
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "AABB{" + MiscUtils.printVec(min) + ", " + MiscUtils.printVec(max) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AABB aabb = (AABB) o;
        return Objects.equals(min, aabb.min) && Objects.equals(max, aabb.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}
