/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window;

import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;

/**
 * @author zipCoder933
 */
public abstract class Window extends GLFWWindow {

    @Override
    public void createWindow(String title, int width, int height) {
        super.createWindow(title, width, height);
        glfwSetCursorPosCallback(getWindow(), (window, xpos, ypos) -> {
            cursor.x = xpos;
            cursor.y = ypos;
        });
    }


}
