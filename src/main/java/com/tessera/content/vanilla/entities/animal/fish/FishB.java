/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities.animal.fish;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.content.vanilla.entities.animal.mobile.FishAnimal;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityMesh;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.math.RandomUtils;
import com.tessera.utils.resource.ResourceLister;
import com.tessera.window.utils.texture.TextureUtils;

import java.io.IOException;
import java.util.Objects;


/**
 * @author zipCoder933
 */

public class FishB extends FishAnimal {

    public FishB(long uniqueIdentifier, ClientWindow window) {
        super(uniqueIdentifier, window);
    }

    static EntityMesh body;
    static int[] textures;

    int textureIndex;

    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);//Always call super!

        if (body == null) {
            body = new EntityMesh();
            body.loadFromOBJ(resourceLoader.getResourceAsStream("assets/tessera/allEntities\\animal\\fish\\fish_B.obj"));
            String[] textureFiles = ResourceLister.listSubResources("assets/tessera/allEntities\\animal\\fish\\textures\\fish_B");
            textures = new int[textureFiles.length];
            for (int i = 0; i < textureFiles.length; i++) {
                textures[i] = Objects.requireNonNull(
                        TextureUtils.loadTextureFromResource(textureFiles[i], false)).id;
            }
        }

        if (hasData) {
            textureIndex = node.get(JSON_SPECIES).asInt();
            textureIndex = MathUtils.clamp(textureIndex, 0, textures.length - 1);
        } else textureIndex = RandomUtils.random.nextInt(textures.length);
    }

    @Override
    public final void renderFish() {
        body.draw(false, textures[textureIndex]);
    }
}
