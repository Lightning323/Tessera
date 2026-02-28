/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.utils;

/**
 *
 * @author zipCoder933
 */
public class ArrayUtils {
    public static <T> T[] combineArrays(T[] array1, T[] array2) {
        // Create a new array with the combined length of both arrays
        @SuppressWarnings("unchecked")
        T[] combinedArray = (T[]) java.lang.reflect.Array.newInstance(
                array1.getClass().getComponentType(), array1.length + array2.length);

        // Copy elements from both arrays
        System.arraycopy(array1, 0, combinedArray, 0, array1.length);
        System.arraycopy(array2, 0, combinedArray, array1.length, array2.length);

        return combinedArray;
    }

    public static byte[] combineArrays(byte[] array1, byte[] array2) {
        // Create a new array with the combined length of both arrays
        byte[] combinedArray = new byte[array1.length + array2.length];

        // Copy elements from both arrays
        System.arraycopy(array1, 0, combinedArray, 0, array1.length);
        System.arraycopy(array2, 0, combinedArray, array1.length, array2.length);

        return combinedArray;
    }

    public static boolean startsWith(byte[] array, byte[] prefix) {
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }

        return true;
    }
}
