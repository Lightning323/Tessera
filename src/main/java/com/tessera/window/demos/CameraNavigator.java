/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.demos;

import com.tessera.window.NKWindow;
import com.tessera.window.Window;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;

/**
 *
 * @author zipCoder933
 */
public class CameraNavigator {

    public CameraNavigator(Window window) {
        this.window = window;
        viewMatrix = new Matrix4f();
    }

    private final static float cameraSpeed = 0.07f; // The camera movement speed
    private final static float mouseSensitivityX = 0.3f; // The mouse sensitivity
    private final static float mouseSensitivityY = 0.3f; // The mouse sensitivity

    // Declare some variables outside the method for recording camera position, direction, etc.
    private Vector3f cameraPos = new Vector3f(0.0f, 0.0f, 3.0f); // The camera position
    private Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f); // The camera direction
    private final Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f); // The camera up vector
    private Window window;
    private float yaw = -90.0f; // The yaw angle of the camera
    private float pitch = 0.0f; // The pitch angle of the camera
    private boolean firstTime;
    private Vector2d lastMouse = new Vector2d();
    private float lastYaw, lastPitch;
    private final Matrix4f viewMatrix;

    /**
     * @return the viewMatrix
     */
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    private static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

// Define a method that takes in two floats and four booleans representing the mouse position and keyboard buttons respectively
    public void update() {
        if (window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            if (firstTime) {
                lastMouse.set(window.getCursorVector());
                lastYaw = yaw;
                lastPitch = pitch;
                firstTime = false;
            }

            float mouseX = (float) (window.getCursorPosX() - lastMouse.x);
            float mouseY = (float) (window.getCursorPosY() - lastMouse.y);

            yaw = lastYaw - (mouseSensitivityY * mouseX);
            pitch = lastPitch - ((-mouseSensitivityX) * mouseY);

            // Update your camera position or orientation here using yaw and pitch
        } else {
            firstTime = true;
        }

        // Clamp the pitch angle to avoid gimbal lock
        pitch = clamp(pitch, -70, 70);

        // Calculate the camera direction vector based on the yaw and pitch angles
        cameraFront.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        cameraFront.y = (float) Math.sin(Math.toRadians(pitch));
        cameraFront.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        cameraFront.normalize();

        // Move the camera position based on the keyboard buttons
        if (window.isKeyPressed(GLFW.GLFW_KEY_A) || window.isKeyPressed(GLFW.GLFW_KEY_LEFT)) {
            cameraPos.sub(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_D) || window.isKeyPressed(GLFW.GLFW_KEY_RIGHT)) {
            cameraPos.add(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            cameraPos.add(new Vector3f(cameraUp).normalize().mul(cameraSpeed));
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
            cameraPos.sub(new Vector3f(cameraUp).normalize().mul(cameraSpeed));
        }

        if (window.isKeyPressed(GLFW_KEY_UP)) {
            cameraPos.add(new Vector3f(cameraFront).mul(cameraSpeed));
        }
        if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            cameraPos.sub(new Vector3f(cameraFront).mul(cameraSpeed));
        }

        // Return a camera view matrix using the joml matrix4f.lookAt method
        getViewMatrix().identity().lookAt(cameraPos, new Vector3f(cameraPos).add(cameraFront), cameraUp);
    }
}
