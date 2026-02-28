/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lwjgltest.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import org.joml.Matrix4f;

/**
 *
 * @author zipCoder933
 */
public class MatrixUtils {

    /**
     * Converts a matrix to a human-readable string.
     *
     * @param matrix
     * @return
     */
    public static String matrixToString(float[][] matrix) {
        DecimalFormat format = new DecimalFormat("#.##");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(format.format(matrix[i][j])).append("  ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Converts a matrix to a human-readable string.
     *
     * @param matrix
     * @return
     */
    public static String matrixToString(Matrix4f matrix) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%.2f  ", matrix.get(i, j)));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * to find out matrix multiplication
     *
     * @param matrix1 First matrix
     * @param matrix2 Second matrix
     * @return the result matrix (matrix 1 and matrix 2 multiplication)
     */
    public static float[][] multiply(float[][] matrix1, float[][] matrix2) {
        int rows1 = matrix1.length;
        int cols1 = matrix1[0].length;
        int rows2 = matrix2.length;
        int cols2 = matrix2[0].length;

        // Required condition for matrix multiplication
        if (cols1 != rows2) {
            throw new IllegalArgumentException("Invalid matrix given.");
        }

        // create a result matrix
        float resultMatrix[][] = new float[rows1][cols2];

        // Core logic for 2 matrices multiplication
        for (int i = 0; i < resultMatrix.length; i++) {
            for (int j = 0; j < resultMatrix[i].length; j++) {
                for (int k = 0; k < cols1; k++) {
                    resultMatrix[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return resultMatrix;
    }

    /**
     * to find out matrix multiplication
     *
     * @param matrix1 First matrix
     * @param matrix2 Second matrix
     * @return the result matrix (matrix 1 and matrix 2 multiplication)
     */
    public static float[][] multiply(float[][] matrix1, Matrix4f matrix2) {
        int rows1 = matrix1.length;
        int cols1 = matrix1[0].length;
        int rows2 = 4;
        int cols2 = 4;

        // Required condition for matrix multiplication
        if (cols1 != rows2) {
            throw new IllegalArgumentException("Invalid matrix given.");
        }

        // create a result matrix
        float resultMatrix[][] = new float[rows1][cols2];

        // Core logic for 2 matrices multiplication
        for (int i = 0; i < resultMatrix.length; i++) {
            for (int j = 0; j < resultMatrix[i].length; j++) {
                for (int k = 0; k < cols1; k++) {
                    resultMatrix[i][j] += matrix1[i][k] * matrix2.get(k, j);
                }
            }
        }
        return resultMatrix;
    }

    /**
     * to find out matrix multiplication
     *
     * @param matrix1 First matrix
     * @param matrix2 Second matrix
     * @return the result matrix (matrix 1 and matrix 2 multiplication)
     */
    public static float[][] multiply(Matrix4f matrix1, float[][] matrix2) {
        int rows1 = 4;
        int cols1 = 4;
        int rows2 = matrix2.length;
        int cols2 = matrix2[0].length;

        // Required condition for matrix multiplication
        if (cols1 != rows2) {
            throw new IllegalArgumentException("Invalid matrix given.");
        }

        // create a result matrix
        float resultMatrix[][] = new float[rows1][cols2];

        // Core logic for 2 matrices multiplication
        for (int i = 0; i < resultMatrix.length; i++) {
            for (int j = 0; j < resultMatrix[i].length; j++) {
                for (int k = 0; k < cols1; k++) {
                    resultMatrix[i][j] += matrix1.get(i, k) * matrix2[k][j];
                }
            }
        }
        return resultMatrix;
    }

    public enum MatrixOrder {
        ROWS, COLUMNS
    }

    /**
     * Concatenates 2 matrices together. (based on the python function,
     * np.concatenate())
     *
     * @param matrixA
     * @param matrixB
     * @param axis
     */
    public static double[][] concatenateMatrices(
            ArrayList<ArrayList<Double>> matrixA,
            ArrayList<ArrayList<Double>> matrixB, MatrixOrder axis) {

        //Concatenate the matrices
        ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();
        if (axis == MatrixOrder.ROWS) {
            result.addAll(matrixA);
            result.addAll(matrixB);
        } else {
            for (int i = 0; i < matrixA.size(); i++) {
                ArrayList<Double> row = new ArrayList<Double>();
                for (int j = 0; j < matrixA.get(i).size(); j++) {
                    row.add(matrixA.get(i).get(j));
                }
                for (int j = 0; j < matrixB.get(i).size(); j++) {
                    row.add(matrixB.get(i).get(j));
                }
                result.add(row);
            }
        }
        //Convert the result back to a matrix
        double[][] matrix = new double[result.size()][result.get(0).size()];
        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.get(i).size(); j++) {
                matrix[i][j] = result.get(i).get(j);
            }
        }
        return matrix;
    }
}
