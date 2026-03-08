/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.render;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;

/**
 * @author zipCoder933
 */
public class MVP extends Matrix4f{//TODO: Decide if this class should really EXTEND Matrix4f

    FloatBuffer buffer;

    public MVP() {
        super();
        buffer = BufferUtils.createFloatBuffer(16);
    }

//    public MVP(Matrix4f matrix) {
//        buffer = BufferUtils.createFloatBuffer(16);
//        this.matrix = matrix;
//    }

    //    public void update(final Matrix4f... matrices) {
//        mvp.identity();
//        for (int i = 0; i < matrices.length; i++) {
//            mvp.mul(matrices[i]);
//        }
//        mvp.get(buffer);
//    }

    public void update(final Matrix4f projection, final Matrix4f view, final Matrix4f model) {
        identity().mul(projection).mul(view).mul(model);
        get(buffer);
    }

    public void update(final Matrix4f projection, final Matrix4f view) {
        identity().mul(projection).mul(view);
        get(buffer);
    }


    public void update(Matrix4f model) {
        set(model);
        get(buffer);
    }

    public void update() {
        get(buffer);
    }

    public void sendToShader(int shaderID, int uniformID) {
        ARBShaderObjects.glUseProgramObjectARB(shaderID);
        ARBShaderObjects.glUniformMatrix4fvARB(uniformID, false, buffer);
    }

    public void updateAndSendToShader(int shaderID, int uniformID) {
        update();
        sendToShader(shaderID, uniformID);
    }
}
