/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.math;

import java.util.Random;

/**
 *
 * @author zipCoder933
 */
public class RandomUtils {

    public static Random random = new Random();

    public static int randInt(int lowerBound, int upperBound) {
        return random.nextInt(upperBound - lowerBound) + lowerBound;
    }

    public static int randInt(Random random, int lowerBound, int upperBound) {
        return random.nextInt(upperBound - lowerBound) + lowerBound;
    }

    public static float randFloat(Random random, float lowerBound, float upperBound) {
        return (random.nextFloat() * upperBound - lowerBound) + lowerBound;
    }

    /**
     * Generates a random boolean with the specified probability.
     * @param probability The probability of returning true (0.0 to 1.0).
     * @return true with the given probability, false otherwise.
     */
    public static boolean randBoolWithProbability(Random random, float probability) {
        return random.nextFloat() < probability;
    }
}
