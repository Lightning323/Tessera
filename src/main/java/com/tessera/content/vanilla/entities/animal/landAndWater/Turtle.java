/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities.animal.landAndWater;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.content.vanilla.entities.animal.mobile.LandAndWaterAnimal;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityMesh;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.math.RandomUtils;
import com.tessera.utils.resource.ResourceLister;
import com.tessera.window.render.MVP;
import com.tessera.window.utils.texture.TextureUtils;

import java.io.IOException;
import java.util.Objects;

import static com.tessera.Main.LOGGER;

/**
 * @author zipCoder933
 */


public class Turtle extends LandAndWaterAnimal {

    static EntityMesh left_fin, right_fin, left_back_fin, right_back_fin, body;
    static int[] textures;

    int textureIndex;

    public Turtle( long uniqueIdentifier, ClientWindow window) {
        super( uniqueIdentifier, window);
        aabb.setOffsetAndSize(1f, 1f, 1f, true);
        frustumSphereRadius = 3;
        setMaxSpeed(0.1f);
        setActivity(0.5f);
    }

    @Override
    public void serializeDefinitionData(JsonGenerator generator) throws IOException {
        super.serializeDefinitionData(generator);//Always call super!
        generator.writeNumberField(JSON_SPECIES, textureIndex);
    }

    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);//Always call super!

        if (body == null) {
            body = new EntityMesh();
            left_fin = new EntityMesh();
            right_fin = new EntityMesh();
            left_back_fin = new EntityMesh();
            right_back_fin = new EntityMesh();
            try {
                String[] textureFiles = ResourceLister.listSubResources("assets/tessera/entities/animal\\turtle\\textures");
                textures = new int[textureFiles.length];
                for (int i = 0; i < textureFiles.length; i++) {
                    textures[i] = Objects.requireNonNull(
                            TextureUtils.loadTextureFromResource(textureFiles[i], false)).id;
                }

                body.loadFromOBJ(resourceLoader.getResourceAsStream("assets/tessera/entities/animal\\turtle\\body.obj"));
                left_fin.loadFromOBJ(resourceLoader.getResourceAsStream("assets/tessera/entities/animal\\turtle\\left_fin.obj"));
                right_fin.loadFromOBJ(resourceLoader.getResourceAsStream("assets/tessera/entities/animal\\turtle\\right_fin.obj"));

                left_back_fin.loadFromOBJ(resourceLoader.getResourceAsStream("assets/tessera/entities/animal\\turtle\\left_back_fin.obj"));
                right_back_fin.loadFromOBJ(resourceLoader.getResourceAsStream("assets/tessera/entities/animal\\turtle\\right_back_fin.obj"));
            } catch (IOException e) {
                LOGGER.info("Error", e);
            }
        }

        if (hasData) {
            textureIndex = node.get(JSON_SPECIES).asInt();
            textureIndex = MathUtils.clamp(textureIndex, 0, textures.length - 1);
        } else textureIndex = RandomUtils.random.nextInt(textures.length);
    }

    @Override
    public void animal_drawBody() {
        modelMatrix.update();
        modelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);
        body.draw(false, textures[textureIndex]);

        float animationTarget = 0f;
        if (getWalkAmt() > 0) {
            animationTarget = MathUtils.map(getWalkAmt(), 0, getMaxSpeed(), 0, 0.4f);
        }
        drawFin(right_fin, 0, 0, ONE_SIXTEENTH * 7,
                animationTarget, 0.0f, 0.4f);

        drawFin(left_fin, 0, 0, ONE_SIXTEENTH * 7,
                animationTarget, 1.5f, 0.4f);

        drawFin(left_back_fin,
                0, 0, ONE_SIXTEENTH * -4,
                animationTarget, 0.0f, 0.05f);

        drawFin(right_back_fin,
                0, 0, ONE_SIXTEENTH * -4,
                animationTarget, 0.0f, -0.05f);
    }

    private static final float ONE_SIXTEENTH = (float) 1 / 16;
    MVP finModelMatrix = new MVP();

    private void drawFin(EntityMesh fin,
                         float x, float y, float z,
                         float animationSpeed, float animationAdd, float multiplier) {

        finModelMatrix.set(modelMatrix).translate(x, y, z);
        if (animationSpeed != 0) {
            float rot = (float) Math.sin((ClientWindow.frameCount * animationSpeed) + animationAdd) * multiplier;
            finModelMatrix.rotateY(rot);
        }
        finModelMatrix.update();
        finModelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);
        fin.draw(false, textures[textureIndex]);
    }

}
