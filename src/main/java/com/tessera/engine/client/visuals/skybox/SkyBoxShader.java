/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.skybox;

import com.tessera.utils.resource.PathHandler;
import com.tessera.window.render.MVP;
import com.tessera.window.render.Shader;
import org.joml.Matrix4f;

import java.io.IOException;

import static com.tessera.Main.LOGGER;

/**
 * @author zipCoder933
 */
 class SkyBoxShader extends Shader {



    public final int uniform_cycle_value,
            uniform_projViewMatrix;

    static MVP mvp = new MVP();

    public SkyBoxShader() {
        try {
            init(
                    PathHandler.resourcePath("/shaders/skybox/sky_shader.vs"),
                    PathHandler.resourcePath("/shaders/skybox/sky_shader.fs"));
        } catch (IOException e) {
            LOGGER.info("error", e);
        }
        uniform_cycle_value = getUniformLocation("cycle_value");
        uniform_projViewMatrix = getUniformLocation("projViewMatrix");
    }

    public void updateMatrix(Matrix4f projection, Matrix4f view) {
        mvp.update(projection, view);
        mvp.sendToShader(getID(), uniform_projViewMatrix);
    }

    @Override
    public void bindAttributes() {
    }

}
