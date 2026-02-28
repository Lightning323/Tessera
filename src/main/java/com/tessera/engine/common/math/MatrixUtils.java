package com.tessera.engine.common.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

public class MatrixUtils {

    public static Vector3f getPositionFromMatrix(Matrix4f matrix) {
        // Extract the translation components from the matrix
        float x = matrix.m30();
        float y = matrix.m31();
        float z = matrix.m32();
        return new Vector3f(x, y, z);
    }

    public static void removeRotation(Matrix4f result) {
        // Set the rotation components to zero
        result.m00(1.0f).m01(0.0f).m02(0.0f);
        result.m10(0.0f).m11(1.0f).m12(0.0f);
        result.m20(0.0f).m21(0.0f).m22(1.0f);
    }

    public static void rotateToFaceCamera(Matrix4f matrix,
                                          final Vector3f cameraPosition,
                                          final Vector3f objectPosition,
                                          final Vector3f upVector) {
        // Calculate the direction from the object to the camera
        Vector3f dir = new Vector3f(cameraPosition).sub(objectPosition).normalize();
        matrix.m20(dir.x).m21(dir.y).m22(dir.z);

        // Calculate the right vector
        Vector3f right = new Vector3f(upVector).cross(dir).normalize();
        // Apply the rotation to the original matrix
        matrix.m00(right.x).m01(right.y).m02(right.z);

        // Calculate the up vector
        Vector3f up = new Vector3f(dir).cross(right).normalize();
        matrix.m10(up.x).m11(up.y).m12(up.z);
    }

    public static void rotateToFaceCamera(MemoryStack stack,
                                          Matrix4f matrix,
                                          final Vector3f cameraPosition,
                                          final Vector3f objectPosition,
                                          final Vector3f upVector) {
        // Calculate the direction from the object to the camera
        Vector3f dir = new Vector3f(stack.mallocFloat(3)).set(cameraPosition).sub(objectPosition).normalize();
        matrix.m20(dir.x).m21(dir.y).m22(dir.z);

        // Calculate the right vector
        Vector3f right = new Vector3f(stack.mallocFloat(3)).set(upVector).cross(dir).normalize();
        // Apply the rotation to the original matrix
        matrix.m00(right.x).m01(right.y).m02(right.z);

        // Calculate the up vector
        Vector3f up = new Vector3f(stack.mallocFloat(3)).set(dir).cross(right).normalize();
        matrix.m10(up.x).m11(up.y).m12(up.z);
    }
}
