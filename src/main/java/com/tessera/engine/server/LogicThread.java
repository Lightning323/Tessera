package com.tessera.engine.server;

import java.util.*;

public class LogicThread {
    Timer timer;
    /**
     * The tick rate is constant, we only shorten the time between ticks if the game is running slow.
     * random tick amont (how fast plants grow, etc) is controlled by how many blocks are updated per tick.
     */
    final int TICK_RATE_MS = 50;
    final int CHUNK_RANDOM_TICK_RATE = 40;
    private long lastTickTime = 0;
    private int ticks = 0;
    Server server;

    public LogicThread(Server server) {
        this.server = server;
    }


    /**
     * CHUNK TICKING
     * A chunk is ticked every game tick
     * - random ticking can occur less often but the random distribution of ticks should be uniform
     */
    public void startGameEvent() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    tickEvent();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, TICK_RATE_MS);
    }

//    public void update() {
//        if (System.currentTimeMillis() - lastTickTime > TICK_RATE_MS) {
//            lastTickTime = System.currentTimeMillis();//We set the last tick here to keep the time between ticks constant
//            tickEvent();
//        }
//    }


    public void tickEvent() {
//        ticks++;
//        server.livePropagationHandler.update();
//        //Update chunk every N ticks
//        if (ticks % CHUNK_RANDOM_TICK_RATE == 0) {
//            int chunksMeshUpdated = 0;
//            //HashSet<Chunk> chunks = new HashSet<>();
//            Iterator<Chunk> iterator = Client.world.chunks.values().iterator();
//
//            while (iterator.hasNext()) {
//                Chunk chunk = iterator.next();
//                int simDistance = ClientWindow.settings.internal_simulationDistance.value;
//
//                int spawnDistance = (int) Math.min(Chunk.WIDTH * 2, ClientWindow.settings.internal_simulationDistance.value * 0.6f);
//                spawnDistance = Math.min(ClientWindow.settings.video_entityDistance.value, spawnDistance);//Spawn distance is the distance at which allEntities are spawned
//                //System.out.println("Chunk " + chunk.client_distToPlayer + " " + simDistance + " " + spawnDistance);
//
//                if (chunk.client_distToPlayer < simDistance) {
//                    boolean spawnEntities = chunk.client_distToPlayer < spawnDistance;//
//
//                    if (Client.DEV_MODE &&
//                            Client.world.terrain.name.toLowerCase().contains("dev")) spawnEntities = false;
//                    boolean hasUpdatedMesh = chunk.tick(spawnEntities);
//                    chunksMeshUpdated += (hasUpdatedMesh ? 1 : 0);
//                }
//            }
////            ClientWindow.printlnDev("Tick updated " + chunksMeshUpdated + " chunk meshes");
//            //chunks.forEach(chunk -> chunk.updateMesh(true, 0, 0, 0));
//        }
    }

    public void stopGameEvent() {
        if (timer != null) timer.cancel();
        timer = null;
    }
}
