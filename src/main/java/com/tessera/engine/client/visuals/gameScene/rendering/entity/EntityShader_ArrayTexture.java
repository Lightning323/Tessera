package com.tessera.engine.client.visuals.gameScene.rendering.entity;

import com.tessera.engine.server.Registrys;
import com.tessera.utils.resource.PathHandler;

import java.io.IOException;

import static com.tessera.Main.LOGGER;

public class EntityShader_ArrayTexture extends EntityShader {

    public final int uniform_textureLayerCount;

    public EntityShader_ArrayTexture() {
        super();
        //Texture layers (array texture)
        uniform_textureLayerCount = getUniformLocation("textureLayerCount");
        int textureLayers = Registrys.blocks.textures.layerCount;
        loadInt(uniform_textureLayerCount, textureLayers - 1);
    }

    public void loadShader() {
        try {
            init(
                    PathHandler.resourcePath("/shaders/entityShader/array texture.vs"),
                    PathHandler.resourcePath("/shaders/entityShader/array texture.fs"));
        } catch (IOException e) {
            LOGGER.info("error", e);
        }
    }
}

