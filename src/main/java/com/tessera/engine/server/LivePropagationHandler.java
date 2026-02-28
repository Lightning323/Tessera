package com.tessera.engine.server;

import com.tessera.engine.common.players.pipeline.BlockHistory;
import com.tessera.engine.common.world.WorldData;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class LivePropagationHandler {
    List<LivePropagationTask> tasks = new ArrayList<>();


    public void addTask(LivePropagationTask waterPropagation) {
        tasks.add(waterPropagation);
    }

    public void addNode(Vector3i pos, BlockHistory hist) {
        for (int i = 0; i < tasks.size(); i++) {
            LivePropagationTask task = tasks.get(i);
            task.addNode(pos, hist);
        }
    }

    public LivePropagationHandler() {
    }

    public void update() {
        for (int i = 0; i < tasks.size(); i++) {
            LivePropagationTask task = tasks.get(i);
            if (System.currentTimeMillis() - task.lastUpdate > task.updateIntervalMS) {
                task.lastUpdate = System.currentTimeMillis();
                tasks.get(i).update();
            }
        }
    }

    public void startGameEvent(WorldData world) {
    }

    public void stopGameEvent() {
    }

}
