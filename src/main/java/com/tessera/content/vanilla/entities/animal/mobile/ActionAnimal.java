package com.tessera.content.vanilla.entities.animal.mobile;

import com.tessera.Main;
import com.tessera.engine.server.Difficulty;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.entity.LivingEntity;

public class ActionAnimal extends LivingEntity {
    private float maxSpeed = 0.10f;
    public AnimalAction currentAction;


    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }


    public ActionAnimal(long uniqueId, ClientWindow window) {
        super(uniqueId, window);
    }

    @Override
    public void server_update() {
        super.server_update();
        if (health <= 0) {
            runAwayAndDie();
        }
        if (isHostile()
                && Main.getServer().getDifficulty() == Difficulty.EASY
                && Main.getServer().getGameMode() == GameMode.ADVENTURE) {
            destroy();
        }
    }

    public void runAwayAndDie() {
        currentAction = new AnimalAction(AnimalAction.ActionType.RUN_AWAY_DIE, 30000);
        currentAction.velocity = maxSpeed;
    }

    public void walkAwayAndDie() {
        currentAction = new AnimalAction(AnimalAction.ActionType.RUN_AWAY_DIE, 30000);
        currentAction.velocity = maxSpeed / 2;
    }

    @Override
    public void animal_move() {

    }

    @Override
    public void animal_drawBody() {

    }
}
