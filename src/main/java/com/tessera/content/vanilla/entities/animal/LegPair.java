package com.tessera.content.vanilla.entities.animal;

import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityMesh;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityShader;
import com.tessera.window.render.MVP;
import org.joml.Matrix4f;

import java.io.IOException;

public class LegPair {

    EntityMesh leg;
    final Matrix4f legMatrix = new Matrix4f();
    MVP mvp = new MVP();

    public LegPair(EntityMesh leg) throws IOException {
        this.leg = leg;
    }

    public void draw(Matrix4f bodyMatrix,
                     EntityShader bodyShader,
                     float x, float y, float z,
                     float movement, int textureID) {
        legMatrix.identity().translate(x, y, z).rotateX((float) (Math.sin(movement * 2) * 0.4f));
        mvp.update(bodyMatrix, legMatrix);
        mvp.sendToShader(bodyShader.getID(), bodyShader.uniform_modelMatrix);
        leg.draw(false,textureID);


        legMatrix.identity().translate(-x, y, z).rotateX((float) (-Math.sin(movement * 2) * 0.4f));
        mvp.update(bodyMatrix, legMatrix);
        mvp.sendToShader(bodyShader.getID(), bodyShader.uniform_modelMatrix);
        leg.draw(false,textureID);
    }
}
