/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.client.visuals.gameScene.rendering.entity;

import com.xbuilders.utils.resource.PathHandler;
import com.xbuilders.window.render.MVP;
import com.xbuilders.window.render.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

import static com.xbuilders.Main.LOGGER;

/**
 * @author zipCoder933
 */
public class EntityShader extends Shader {

    public final int
            uniform_projViewMatrix,
            uniform_modelMatrix,
            uniform_sun,
            uniform_torch,
            uniform_tint,
            uniform_view_distance;

    protected static MVP mvp = new MVP();

    public EntityShader() {
        loadShader();
        uniform_projViewMatrix = getUniformLocation("projViewMatrix");
        uniform_modelMatrix = getUniformLocation("modelMatrix");
        uniform_sun = getUniformLocation("sun");
        uniform_torch = getUniformLocation("torch");
        uniform_tint = getUniformLocation("tint");
        uniform_view_distance = getUniformLocation("viewDistance");
        setTint(new Vector3f(1, 1, 1));
        setSunAndTorch(1, 1);
    }

    public void loadShader() {
        try {
            init(
                    PathHandler.resourcePath("/shaders/entityShader/default.vs"),
                    PathHandler.resourcePath("/shaders/entityShader/default.fs"));
        } catch (IOException e) {
            LOGGER.info("error", e);
        }
    }

    public void updateProjectionViewMatrix(Matrix4f projection, Matrix4f view) {
        mvp.update(projection, view);
        mvp.sendToShader(getID(), uniform_projViewMatrix);
    }

    public void setTint(Vector3f tint) {
        loadVec3f(uniform_tint, tint);
    }

    public void setSunAndTorch(float sunValue, float torchValue) {
        loadFloat(uniform_sun, sunValue);
        loadFloat(uniform_torch, torchValue);
    }

    @Override
    public void bindAttributes() {
    }

}
