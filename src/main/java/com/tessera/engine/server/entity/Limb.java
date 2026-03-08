package com.tessera.engine.server.entity;

import com.tessera.window.render.MVP;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class Limb {
    public MVP limbMatrix = new MVP();
    public Limb[] limbs;
    Consumer<MVP> drawCallback;

    public Limb(Consumer<MVP> drawCallback) {
        this.drawCallback = drawCallback;
        limbs = new Limb[0];
    }

    protected void inner_draw_limb(Matrix4f parentMatrix) {
        limbMatrix.set(parentMatrix);//Initialize the matrix
        limbMatrix.updateAndSendToShader(Entity.shader.getID(), Entity.shader.uniform_modelMatrix);
        drawCallback.accept(limbMatrix);

        if (limbs != null) for (int i = 0; i < limbs.length; i++) {
            limbs[i].inner_draw_limb(limbMatrix);
        }
    }

}
