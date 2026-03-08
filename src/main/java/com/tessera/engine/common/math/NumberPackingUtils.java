/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.math;

/**
 * https://stackoverflow.com/questions/54814212/encoding-a-set-of-three-integers-to-one-unique-number
 *
 * @author zipCoder933
 */
public class NumberPackingUtils {

    public static long packLong(long a, long b, long c, long max) {
        return (a + (b * max) + c * max * max);
    }

    public static int packInt(int a, int b, int c, int max) {
        return (a + (b * max) + c * max * max);
    }

    /**
     * Packs a set of short numbers into a single number
     *
     * The short number range is -32,768 to 32,767 sometimes you will pack
     * numbers like 161,685, that cant be compressed down into shorts. Therefore
     * the return is an int.
     *
     * @param a
     * @param b
     * @param c
     * @param max the maximum number size
     * @return the packed number (integer).
     */
    public static int packShort(short a, short b, short c, short max) {
        return (a + (b * max) + c * max * max);
    }

//This "packs" A into the least significant 16 bits of the long (bits 49-64), B into bits 33-48, and C into bits 17-32. (Nothing is packed into bits 0-16, so those bits remain cleared.)
//Also, your unpack method changes to:
    public static long[] unpackLong(long packed, long max) {
        long a = (packed % max);
        long b = (packed / max) % max;
        long c = (packed / (max * max));  // Use / not %.
        return new long[]{a, b, c};
    }

    public static int[] unpackInt(int packed, int max) {
        int a = (packed % max);
        int b = (packed / max) % max;
        int c = (packed / (max * max));  // Use / not %.
        return new int[]{a, b, c};
    }

    public static int[] unpackShort(int packed, short max) {
        int a = (packed % max);
        int b = ((packed / max) % max);
        int c = (packed / (max * max));  // Use / not %.
        return new int[]{a, b, c};
    }
}
