package com.tessera.engine.common.math;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.joml.*;

import java.lang.Math;

/**
 * @author sampw
 */
public class MathUtils {


    public static Vector3f intToFloat(Vector3i vec) {
        Vector3f vec2 = new Vector3f((float) vec.x, (float) vec.y, (float) vec.z);
        return vec2;
    }

    public static Vector3i floatToInt(Vector3f vec) {
        Vector3i vec2 = new Vector3i((int) vec.x, (int) vec.y, (int) vec.z);
        return vec2;
    }

    public static Matrix4f createTransformationMatrix(Vector2f position, Vector3f rotation) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0));
        matrix.rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        matrix.translate(position.x, position.y, 0);
        return matrix;
    }

    public static Matrix4f createTransformationMatrix(Vector3f position, int offset) {
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.translate(position.x, position.y, position.z);
        return matrix;
    }

    public static float intbound(float a, float b) {
        if (b < 0) {
            return intbound(-a, -b);
        } else {
            a = mod(a, 1);
            return (1 - a) / b;
        }
    }

    public static float mod(float value, float modulus) {
        return (value % modulus + modulus) % modulus;
    }

    public static int signum(float x) {
        return x > 0 ? 1 : x < 0 ? -1 : 0;
    }

    /**
     * Maps a variable from range A to range B
     *
     * @param value  the variable
     * @param start1 range A minimum
     * @param stop1  range A maximum
     * @param start2 range B minimum
     * @param stop2  range B maximum
     * @return the new variable
     */
    static public final float map(float value,
                                  float start1, float stop1,
                                  float start2, float stop2) {
        float outgoing = start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
        return outgoing;
    }

    /**
     * Maps a variable from range A to range B
     *
     * @param value  the variable
     * @param start1 range A minimum
     * @param stop1  range A maximum
     * @param start2 range B minimum
     * @param stop2  range B maximum
     * @return the new variable
     */
    static public final double map(double value,
                                   double start1, double stop1,
                                   double start2, double stop2) {
        double outgoing = start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
        return outgoing;
    }

    static public final float mapAndClamp(float value,
                                          float start1, float stop1,
                                          float start2, float stop2) {
        float outgoing = start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
        return MathUtils.clamp(outgoing, start2, stop2);
    }

    /**
     * Creates a value based on a range and sets the value somewhere in between
     * that range.
     *
     * @param start the range min
     * @param stop  the range max
     * @param amt   where to place the value within the range.
     * @return the value
     */
    static public final double lerp(double start, double stop, double amt) {
        return start + (stop - start) * amt;
    }

    /**
     * Creates a value based on a range and sets the value somewhere in between
     * that range.
     *
     * @param start the range min
     * @param stop  the range max
     * @param amt   where to place the value within the range.
     * @return the value
     */
    static public final float lerp(float start, float stop, float amt) {
        return start + (stop - start) * amt;
    }

    /**
     * Gradually changes val over time to target
     *
     * @param val    the variable
     * @param target the target position
     * @param speed  how long to get there (lower numbers means slower
     *               transition)
     * @return the variable
     */
    public static double curve(double val, double target, double speed) {
        if (val < target) {
            val += (target - val) * speed;
        } else {
            val -= (val - target) * speed;
        }
        return val;
    }

    public static double linearTravel(double val, double target, double speed) {
        if (val < target) {
            val += speed;
            if (val > target) {
                val = target;
            }

        } else {
            val -= speed;
            if (val < target) {
                val = target;
            }
        }

        return val;
    }

    /**
     * Clamp a value from minimum to maximum
     *
     * @param val
     * @param min
     * @param max
     * @return
     */
    public static float clamp(float val, float min, float max) {
        if (val > max) {
            return max;
        } else if (val < min) {
            return min;
        }

        return val;
    }

    /**
     * Clamp a value from minimum to maximum
     *
     * @param val
     * @param min
     * @param max
     * @return
     */
    public static double clamp(double val, double min, double max) {
        if (val > max) {
            return max;
        } else if (val < min) {
            return min;
        }

        return val;
    }

    /**
     * Clamp a value from minimum to maximum
     *
     * @param val
     * @param min
     * @param max
     * @return
     */
    public static int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        }
        return val;
    }

    /**
     * Clamp a value from minimum to maximum
     *
     * @param val
     * @param min
     * @param max
     * @return
     */
    public static long clamp(long val, long min, long max) {
        if (val > max) {
            return max;
        } else if (val < min) {
            return min;
        }

        return val;
    }

    public static float dist(
            final float dist1, final float dist2) {
        return Math.abs(dist1 - dist2);
    }

    static public final float dist(
            final float x1, final float y1,
            final float x2, final float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    static public final float dist(
            final float x1, final float y1, final float z1,
            final float x2, final float y2, final float z2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    static public final float dist(final Vector3f a, final Vector3f b) {
        return (float) Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2) + Math.pow(b.z - a.z, 2));
    }

    public static final double HALF_PI = 1.5707963267948966192313215;
    public static final double PI = 3.141592653589793238462643;
    public static final double TWO_PI = 6.283185307179586476925286;

    public static Vector2f getCircumferencePoint(Vector2f origPos, double rot, double d) {
        rot = ((float) rot / 360) * TWO_PI;
        float tX = (float) (Math.cos(rot + HALF_PI) * d);
        float tY = (float) (Math.sin(rot + HALF_PI) * d);
        return new Vector2f(origPos.x + tX, origPos.y + tY);
    }

    /**
     * Calculates the angle from centerPt to targetPt in radians. This method
     * only works for 2 dimensions
     *
     * @param centerPtX the center point
     * @param centerPtY the center point
     * @param targetPtX the target point
     * @param targetPtY the target point
     * @return angle in radians. This is the angle from centerPt to targetPt. (π
     * radians = 180 degrees)
     */
    public static double calcRotationAngle(double centerPtX, double centerPtY,
                                           double targetPtX, double targetPtY) {
        return Math.atan2(targetPtY - centerPtY, targetPtX - centerPtX);
    }

    /**
     * @param number the number
     * @param min    the minimum bound
     * @param max    the maximum bound
     * @return if the number is within range
     */
    public static boolean inRange(double number, double min, double max) {
        return number >= max && number <= min;
    }

    /**
     *
     * @param x the z position of the center-point
     * @param z the x position of the center-point
     * @param rot the angle (in degrees)
     * @param d the distance from the center-point
     * @return the point in space from the center point given the angle
     */
    public static Vector2f getCircumferencePoint(double rot, double d) {
        rot = ((float) rot / 360) * TWO_PI;
        float tX = (float) (Math.cos(rot + HALF_PI) * d);
        float tY = (float) (Math.sin(rot + HALF_PI) * d);
        return new Vector2f(tX, tY);
    }

    /**
     *
     * @param targetX
     * @param targetZ
     * @param thisX
     * @param thisZ
     * @return the angle between two 2D points (in degrees)
     */
    public static float getAngleOfPoints(float thisX, float thisZ, float targetX, float targetZ) {
        float angle = (float) Math.toDegrees(Math.atan2(targetX - thisX, targetZ - thisZ));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    /**
     * @return the Shortest distance (angular) between two angles. It will be in
     * range [0, 180].
     */
    public static float getSignedAngleDistance(float targetA, float sourceA) {
        float a = targetA - sourceA;
        a = (a + 180) % 360 - 180;
        return a;
    }

    public double degreesToRadiants(double degrees) {
        return degrees * (Math.PI / 180);
    }

    public double radiantsToDegrees(double radiants) {
        return (radiants * 180) / PI;
    }

    /**
     * Java uses negative modulus for negative numbers. See
     * https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.17.3
     * https://stackoverflow.com/questions/4403542/how-does-java-do-modulus-calculations-with-negative-numbers
     *
     * @return positive modulus operation result
     */
    public static float positiveMod(float dividend, float divisor) {
        return ((dividend % divisor) + divisor) % divisor;
    }

    /**
     * Java uses negative modulus for negative numbers. See
     * https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.17.3
     * https://stackoverflow.com/questions/4403542/how-does-java-do-modulus-calculations-with-negative-numbers
     *
     * @return positive modulus operation result
     */
    public static int positiveMod(int dividend, int divisor) {
        return ((dividend % divisor) + divisor) % divisor;
//         int modulus = dividend % divisor;
//        if (modulus < 0) {
//            modulus += divisor;
//        }
    }



}
