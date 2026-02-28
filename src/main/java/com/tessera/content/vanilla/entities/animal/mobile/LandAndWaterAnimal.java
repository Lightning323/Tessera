/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities.animal.mobile;

import com.tessera.content.vanilla.Blocks;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.entity.EntitySupplier;
import com.tessera.engine.common.math.MathUtils;
import org.joml.Vector2f;

import static com.tessera.content.vanilla.entities.animal.mobile.AnimalAction.ActionType.*;

/**
 * @author zipCoder933
 */
public abstract class LandAndWaterAnimal extends ActionAnimal {

    private float activity = 0.8f;
    private float actionVelocity;

    private boolean inWater;
    private float walkAmt = 0;
    float yVelocity, rotationVelocity, forwardVelocity;

    public LandAndWaterAnimal(long uniqueIdentifier, ClientWindow window) {
        super(uniqueIdentifier, window);
    }

    @Override
    public void initSupplier(EntitySupplier entitySupplier) {
        super.initSupplier(entitySupplier);
        entitySupplier.spawnCondition = (x, y, z) -> {
            if (Client.world.getBlockID(x, y, z) == Blocks.BLOCK_WATER) return true;
            return false;
        };
        entitySupplier.isAutonomous = true;
    }

    /**
     * @return the walkAmt
     */
    public float getWalkAmt() {
        return walkAmt;
    }

    /**
     * @return the action
     */
    public AnimalAction getCurrentAction() {
        return currentAction;
    }

    /**
     * @param currentAction the action to set
     */
    public void setCurrentAction(AnimalAction currentAction) {
        this.currentAction = currentAction;
    }


    /**
     * @return the actionVelocity
     */
    public float getActionVelocity() {
        return actionVelocity;
    }

    /**
     * @return the activity
     */
    public float getActivity() {
        return activity;
    }


    /**
     * @param activity the activity to set
     */
    public void setActivity(float activity) {
        this.activity = MathUtils.clamp(activity, 0, 1);
    }


    public AnimalAction newRandomAction(Enum lastAction) {
        random.nextInt(AnimalAction.ActionType.values().length - 1);
        AnimalAction.ActionType actionType = AnimalAction.getRandomActionType(random,
                AnimalAction.ActionType.IDLE,
                WALK,
                TURN);

        if (lastAction != null) {
            AnimalAction.ActionType lastAction2 = (AnimalAction.ActionType) lastAction;
            if (actionType == lastAction2) {
                switch (lastAction2) {
                    case IDLE:
                        actionType = TURN;
                        break;
                    case TURN:
                        actionType = WALK;
                        break;
                    case FOLLOW:
                        actionType = TURN;
                        break;
                    default:
                        break;
                }
            }
        }

        long actionDuration = 0;

        if (distToPlayer < 5 && playerHasAnimalFeed()) {
            actionType = FOLLOW;
            tamed = true;
            actionDuration = random.nextLong(4000, 25000);
            actionVelocity = getMaxSpeed() / 2;
        } else if (actionType == TURN) {
            actionDuration = 50 + (random.nextInt(100));
            actionVelocity = computeTurnVelocity();
        } else if (actionType == WALK) {
            actionDuration = 600 + (random.nextInt(500) - 250);
            calculateWalkVelocity();
        } else {
            if (activity > 0.8) {
                actionDuration = random.nextInt(-40, 200);
                if (actionDuration < 0) {
                    actionDuration = 0;
                }
            } else {
                actionDuration = random.nextInt(50, 400);
            }
            actionDuration *= MathUtils.map(getActivity(), 0, 1, 5, 1);
            if (random.noise(10) > MathUtils.map(getActivity(), 0, 1, -0.2f, 0.3f)) {
                actionDuration *= MathUtils.map(getActivity(), 0, 1, 10, 3);
            }
        }
        return new AnimalAction(actionType, actionDuration);
    }


    public void walkForward(float speed) {
        if (inWater) {
            walkAmt = speed / 2;
        } else {
            walkAmt = speed;
        }

        Vector2f vec = MathUtils.getCircumferencePoint(-getRotationYDeg(), speed);
        worldPosition.add(vec.x, 0, vec.y);

    }


    private void calculateWalkVelocity() {
        actionVelocity = random.nextFloat(0.01f, getMaxSpeed());
        if (activity < 0.66 && random.nextBoolean()) {
            actionVelocity /= 3;
        }
    }

    private float computeTurnVelocity() {
        float rotationAction = MathUtils.clamp(getActivity(), 0.2f, 1);
        return random.noise(10) > 0 ? rotationAction * 10 : rotationAction * -10;
    }


    public void animal_move() {
        if (inFrustum) { //In Frustum movement
            if (ClientWindow.frameCount % 10 == 0) inWater = AnimalUtils.inWater(this);


            if (inWater) {
                moveInWater();
            } else {
                moveOnLand();
            }
        } else { //Out of Frustum movement
            if (ClientWindow.frameCount % 10 != 0) return;
            else if (getCurrentAction() != null && getCurrentAction().type == FOLLOW) {
                return;
            } else {
                walkForward(getMaxSpeed());
                walkAmt = 0;
            }
        }
        pos.update();
    }


    private void moveInWater() {
        pos.setGravityEnabled(false);

        //When we are ready to change the action
        if (currentAction == null || currentAction.pastDuration()) {
            if (getCurrentAction() == null) {
                setCurrentAction(newRandomAction(null));
            } else {
                setCurrentAction(newRandomAction(getCurrentAction().type));
            }
            rotationVelocity = computeTurnVelocity();
            forwardVelocity = (float) MathUtils.mapAndClamp(random.noise(4), -0.5f, 1, 0.01f, getMaxSpeed());
            yVelocity = random.nextFloat(-0.05f, 0.05f);
        }

        //Perform the action
        if (null != getCurrentAction().type) {
            switch (getCurrentAction().type) {
                case FOLLOW:
                    followAction();
                    break;
                case TURN:
                    setRotationYDeg(getRotationYDeg() + getActionVelocity());
                    walkAmt = 0;
                    break;
                case WALK:
                    setRotationYDeg(getRotationYDeg() + rotationVelocity);
                    walkForward(getActionVelocity());
                    break;
                case RUN_AWAY_DIE:
                    runAwayAction();
                    break;
                default:
                    walkAmt = 0;
                    break;
            }
        }
        worldPosition.y -= yVelocity;
    }

    private void runAwayAction() {
        setRotationYDeg((float) Math.toDegrees(getYDirectionToPlayer()) + 180 + random.noise(2f, -3, 3));
        goForward(currentAction.velocity, true);
        if (distToPlayer > 20 || currentAction.getDurationLeftMS() < 5000) {
            destroy();
        }
    }

    private void moveOnLand() {
        pos.setGravityEnabled(true);

        //When we are ready to change the action
        if (getCurrentAction() == null || getCurrentAction().pastDuration()) {
            if (getCurrentAction() == null) {
                setCurrentAction(newRandomAction(null));
            } else {
                setCurrentAction(newRandomAction(getCurrentAction().type));
            }
        }

        //Perform the action
        if (null != getCurrentAction().type) {
            switch (getCurrentAction().type) {
                case FOLLOW:
                    followAction();
                    break;
                case TURN:
                    setRotationYDeg(getRotationYDeg() + getActionVelocity());
                    walkAmt = 0;
                    break;
                case WALK:
                    if (isPendingDestruction() && random.nextFloat() > 0.6) {
                        setRotationYDeg(getRotationYDeg() + random.nextFloat() * 2);
                    }
                    walkForward(getActionVelocity());
                    break;
                case RUN_AWAY_DIE:
                    runAwayAction();
                    break;
                default:
                    walkAmt = 0;
                    break;
            }
        }
    }

    private void followAction() {
        if (distToPlayer < 15 && playerHasAnimalFeed()) {
            if (getCurrentAction().getTimeSinceStartedMS() > 500
                    && distToPlayer > 4
                    && random.noise(4) > -0.5f) {
                walkForward(getActionVelocity());
                setRotationYDeg(getRotationYDeg() + random.noise(2f, -3, 3));
            } else {
                walkAmt = 0;
                if (distToPlayer < 4) {
                    eatAnimalFeed();
                }
            }
            facePlayer();
        } else {
            currentAction = new AnimalAction(AnimalAction.ActionType.IDLE, 500);
        }
    }

}
