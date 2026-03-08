/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities.animal.mobile;

import com.tessera.utils.bytes.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zipCoder933
 */
public class AnimalAction {

    public ActionType type;
    public int duration;
    public float velocity;
    private long createdTimeMS;




    public enum ActionType {
        TURN,
        IDLE,
        WALK,
        FOLLOW,
        CLIMB,
        JUMP,
        FLY,
        RUN_AWAY_DIE, OTHER
    }

    public static ActionType getRandomActionType(AnimalRandom random, ActionType... types) {
        int indx = random.nextInt(types.length);
        return types[indx];
    }

    public void toBytes(ByteArrayOutputStream baos) throws IOException {
        ByteUtils.writeInt(baos, type.ordinal());
        ByteUtils.writeFloat(baos, velocity);

        int timeLeftMS = getDurationLeftMS();
//        System.out.println("Duration: " + duration);
//        System.out.println("Created at: " + createdTimeMS);
//        System.out.println("Time left: " + timeLeftMS);
        ByteUtils.writeInt(baos, timeLeftMS);
    }

    public AnimalAction fromBytes(byte[] bytes, AtomicInteger start) {
        type = ActionType.values()[ByteUtils.bytesToInt(bytes, start)];
        velocity = ByteUtils.bytesToFloat(bytes, start);
        int timeLeft = ByteUtils.bytesToInt(bytes, start);

        //Get duration
        duration = (int) (System.currentTimeMillis() - createdTimeMS + timeLeft);
        //Get created time
        createdTimeMS = System.currentTimeMillis();

        return this;
    }

    public AnimalAction() {
        this.createdTimeMS = System.currentTimeMillis();
    }

    public AnimalAction(ActionType type) {
        this.type = type;
        this.createdTimeMS = System.currentTimeMillis();
    }

    public AnimalAction(ActionType type, long duration) {
        this.type = type;
        this.createdTimeMS = System.currentTimeMillis();
        this.duration = (int) duration;
    }

    public long getTimeSinceStartedMS() {
        return System.currentTimeMillis() - createdTimeMS;
    }

    public boolean pastDuration() {
        return getTimeSinceStartedMS() > duration;
    }

    public int getDurationLeftMS() {
        return (int) (duration - (System.currentTimeMillis() - createdTimeMS));
    }


    @Override
    public String toString() {
        return "AnimalAction{" + "typeReference=" + type.ordinal() + ", duration=" + duration + ", velocity=" + velocity + " duration: " + duration + " created at: " + createdTimeMS + '}';
    }

}
