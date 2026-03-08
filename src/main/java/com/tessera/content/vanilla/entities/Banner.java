/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.content.vanilla.entities.vehicle.Vehicle;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.entity.Entity;
import com.tessera.content.vanilla.blocks.RenderType;

import java.io.IOException;

/**
 * @author zipCoder933
 */
public class Banner extends Entity {
    final String textureFile;
    int texture;
    static Vehicle.Vehicle_staticData staticData;

    public Banner(long uniqueIdentifier, String textureFile) {
        super(uniqueIdentifier);
        frustumSphereRadius = 2f;
        this.textureFile = textureFile;
        aabb.isSolid = false;
    }

    int xzOrientation = 0;
    float seed = 0;
    boolean againstFencepost;


    @Override
    public void serializeDefinitionData(JsonGenerator generator) throws IOException {
        super.serializeDefinitionData(generator);//Always call super!
        generator.writeNumberField("XZ", xzOrientation);
        generator.writeBooleanField("fencepost", againstFencepost);
    }

    @Override
    public void server_update() {

    }

    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);//Always call super!
        if (staticData == null) {
            staticData = new Vehicle.Vehicle_staticData(
                    "assets/tessera/entities/banner\\banner.obj",
                    "assets/tessera/entities/banner\\textures");
        }
        if (textureFile != null) {
            texture = staticData.textures.get(textureFile);
        }
        if (hasData) {
            if (node.has("XZ")) xzOrientation = node.get("XZ").asInt();
            if (node.has("fencepost")) againstFencepost = node.get("fencepost").asBoolean();
        } else {
            xzOrientation = Client.userPlayer.camera.simplifiedPanTilt.x;
            int wx = (int) worldPosition.x;
            int wy = (int) worldPosition.y;
            int wz = (int) worldPosition.z;

            if (xzOrientation == 0) {
                againstFencepost = Client.world.getBlock(wx, wy, wz - 1)
                        .type == RenderType.FENCE;
            } else if (xzOrientation == 1) {
                againstFencepost = Client.world.getBlock(wx + 1, wy, wz)
                        .type == RenderType.FENCE;
            } else if (xzOrientation == 2) {
                againstFencepost = Client.world.getBlock(wx, wy, wz + 1)
                        .type == RenderType.FENCE;
            } else {
                againstFencepost = Client.world.getBlock(wx - 1, wy, wz)
                        .type == RenderType.FENCE;
            }
        }

        seed = (float) (Math.random() * 1000);
        aabb.setOffsetAndSize(0, 0, 0,
                1, 2, 1);
    }

    int frameCount = 0;
    final float ONE_SIXTEENTH = 0.16666667f;

    @Override
    public void client_draw() {
        modelMatrix.identity().translate(worldPosition);
        if (xzOrientation == 0) {
            modelMatrix.translate(0, 0, 1);
            modelMatrix.rotateY((float) (Math.PI / 2));

        } else if (xzOrientation == 2) {
            modelMatrix.translate(1, 0, 0);
            modelMatrix.rotateY((float) -(Math.PI / 2));
        } else if (xzOrientation == 3) {
            modelMatrix.translate(1, 0, 1);
            modelMatrix.rotateY((float) Math.PI);
        }

        if (againstFencepost) {
            modelMatrix.translate(0.4f, 0, 0);
        }
        modelMatrix.translate(1f - (ONE_SIXTEENTH * 2), 0, 0.5f);

        float flow = (float) ((Math.sin((frameCount * 0.05) + seed) * 0.1) + 0.1f);
        //flow *= (float) sunValue / 15;

        modelMatrix.rotateZ(flow);
        modelMatrix.update();
        modelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);
        staticData.body.draw(false, texture);
        frameCount++;
    }


}

