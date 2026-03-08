package com.tessera.engine.common.players;

import com.tessera.engine.server.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.tessera.engine.common.players.Player.PLAYER_HEIGHT;

public class PositionLock {

    
    public PositionLock(Entity lock, float yOffset) {
        this.entity = lock;
        this.playerDisplacement = new Matrix4f();
        setOffset(yOffset);
    }

    public void setOffset(float yOffset) {
        playerDisplacement.identity().translate(
                0,
                yOffset - PLAYER_HEIGHT,
                0);
    }

    public void setOffset(float xOffset, float yOffset, float zOffset) {
        playerDisplacement.identity().translate(
                xOffset,
                yOffset - PLAYER_HEIGHT,
                zOffset);
    }

    public final Entity entity;
    public final Matrix4f playerDisplacement;//Meant to be updated by the entity every frame or so
    private final Vector3f translation = new Vector3f();//Calculated from displacement, used to position the player relatively to the entity
    private final Vector3f position = new Vector3f();

    public Vector3f getPosition() {
        position.set(entity.worldPosition).add(playerDisplacement.getTranslation(translation));
        return position;
    }

}
