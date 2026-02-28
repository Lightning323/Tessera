/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.utils;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author zipCoder933
 */
public class MiscUtils {

    // Define the characters for encoding
    private final static String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    // Base of the encoding system
    private final static int base = 42;

    public static void setClipboard(String text) {
        try {
            // Create a StringSelection object with the text to copy
            StringSelection stringSelection = new StringSelection(text);

            // Get the system clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // Set the text to the clipboard
            clipboard.setContents(stringSelection, null);
        } catch (Exception e) {
            // Handle any exceptions that may occur
            System.out.println("Error setting clipboard: " + e.getMessage());
        }
    }

    public static String encodeNumber(long number) {
        StringBuilder encodedString = new StringBuilder();
        // Ensure the number is non-negative
        number = Math.abs(number);

        // Encode the number
        while (number > 0) {
            long remainder = number % base;
            encodedString.insert(0, characters.charAt((int) remainder));
            number = number / base;
        }
        return encodedString.toString();
    }

    public static String formatTime(long lastModifiedTime) {
        return lastModifiedTime == 0 ? "never" : new Date(lastModifiedTime).toString();
    }

    public static boolean isBlackCube(int x, int y, int z) {
        // Assume that the checkerboard has a size of 1 unit by 1 unit by 1 unit, and
        // that the origin (0, 0, 0) is black
        // To determine the color of any point (x, y, z), you can use the formula: (x +
        // y + z) % 2 == 0
        // This means that if the sum of the coordinates is even, the point is black;
        // otherwise, it is white
        return (x + y + z) % 2 == 0;
    }

    public static String toCamelCase(String input) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(Character.toLowerCase(c));
                }
                capitalizeNext = false;
            } else {
                capitalizeNext = true;
            }
        }

        return sb.toString();
    }

    public static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Pattern pattern = Pattern.compile("\\b(\\w)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    // Print Vector methods
    public static String printVec(Vector2f vec) {
        return vec.x + "," + vec.y;
    }

    public static String printVec(Vector2i vec) {
        return vec.x + "," + vec.y;
    }

    public static String printVec(Vector2d vec) {
        return vec.x + "," + vec.y;
    }

    public static String printVec(Vector3f vec) {
        return vec.x + "," + vec.y + "," + vec.z;
    }

    public static String printVec(Vector3i vec) {
        return vec.x + "," + vec.y + "," + vec.z;
    }

    public static String printVec(Vector3d vec) {
        return vec.x + "," + vec.y + "," + vec.z;
    }

    public static String printVec(Vector4f vec) {
        return vec.x + "," + vec.y + "," + vec.z + "," + vec.w;
    }

    public static String printVec(Vector4i vec) {
        return vec.x + "," + vec.y + "," + vec.z + "," + vec.w;
    }

    public static String printVec(Vector4d vec) {
        return vec.x + "," + vec.y + "," + vec.z + "," + vec.w;
    }

    public static <T> boolean equalOrNull(T str1, T str2) {
        // Both are null, consider them equal
        if (str1 == null && str2 == null) return true;
        // Only one is null, they are not equal
        if (str1 == null || str2 == null) return false;
        // Compare their actual content
        return str1.equals(str2);
    }


}
