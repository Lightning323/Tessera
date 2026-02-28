package com.xbuilders.content.vanilla.entities.animal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.xbuilders.engine.client.ClientWindow;
import com.xbuilders.engine.client.visuals.gameScene.rendering.entity.EntityMesh;
import com.xbuilders.engine.common.math.MathUtils;
import com.xbuilders.engine.common.math.RandomUtils;
import com.xbuilders.content.vanilla.entities.animal.mobile.LandAnimal;
import com.xbuilders.utils.resource.ResourceLister;
import com.xbuilders.window.utils.texture.TextureUtils;

import java.io.IOException;
import java.util.Objects;

public abstract class StaticLandAnimal extends LandAnimal {


    public StaticLandAnimal(long uniqueIdentifier, ClientWindow window) {
        super(uniqueIdentifier, window);
        aabb.setOffsetAndSize(0.8f, 0.9f, 0.8f, true);
        jumpOverBlocks = true;
        frustumSphereRadius = 2;
    }

    public static class StaticLandAnimal_StaticData {
        public final EntityMesh body;
        public final int[] textures;

        public StaticLandAnimal_StaticData(String bodyMesh, String texturesDir) throws IOException {
            body = new EntityMesh();
            body.loadFromOBJ(resourceLoader.getResourceAsStream(bodyMesh));
            //Generate textures
            String[] textureList = ResourceLister.listSubResources(texturesDir);
            this.textures = new int[textureList.length];
            for (int i = 0; i < textureList.length; i++) {
                int textureID = Objects.requireNonNull(
                        TextureUtils.loadTextureFromResource(
                                textureList[i], false)).id;
                this.textures[i] = textureID;
            }
        }
    }

    @Override
    public void serializeDefinitionData(JsonGenerator generator) throws IOException {
        super.serializeDefinitionData(generator);
        generator.writeNumberField(JSON_SPECIES, textureIndex);
    }

    /**
     * These are just references to the static data, so they arent taking up space in the instance.
     */
    EntityMesh body;
    int[] textures;
    int textureIndex;

    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);
        StaticLandAnimal_StaticData ead = getStaticData();
        this.body = ead.body;
        this.textures = ead.textures;

        if (hasData) {
            textureIndex = node.get(JSON_SPECIES).asInt();
            textureIndex = MathUtils.clamp(textureIndex, 0, textures.length - 1);
        } else textureIndex = RandomUtils.random.nextInt(textures.length);
    }

    public abstract StaticLandAnimal_StaticData getStaticData() throws IOException;

    @Override
    public void animal_drawBody() {
        shader.bind();
        modelMatrix.update();
        modelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);
        body.draw(false, textures[textureIndex]);
    }
}