package com.xbuilders.engine.client;

import com.xbuilders.Main;
import com.xbuilders.engine.common.network.netty.NettyClient;
import com.xbuilders.engine.common.packets.AllPackets;
import com.xbuilders.engine.common.packets.ClientEntrancePacket;
import com.xbuilders.engine.common.players.localPlayer.LocalPlayer;
import com.xbuilders.engine.client.visuals.Page;
import com.xbuilders.engine.common.network.ChannelBase;
import com.xbuilders.engine.common.network.ClientBase;
import com.xbuilders.engine.common.network.fake.FakeClient;
import com.xbuilders.engine.common.network.fake.FakeServer;
import com.xbuilders.engine.common.network.old.multiplayer.NetworkJoinRequest;
import com.xbuilders.engine.common.progress.ProgressData;
import com.xbuilders.engine.common.resource.ResourceUtils;
import com.xbuilders.engine.common.utils.LoggingUtils;
import com.xbuilders.engine.common.world.*;
import com.xbuilders.engine.common.world.chunk.Chunk;
import com.xbuilders.engine.server.Game;
import com.xbuilders.engine.server.GameMode;
import com.xbuilders.engine.server.Server;
import com.xbuilders.engine.common.players.Player;
import com.xbuilders.window.developmentTools.FrameTester;
import com.xbuilders.window.developmentTools.MemoryGraph;
import org.joml.Vector3f;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xbuilders.Main.LOGGER;
import static com.xbuilders.Main.versionStringToNumber;
import static com.xbuilders.engine.common.players.Player.PLAYER_HEIGHT;


public class Client {
    public static String CLIENT_VERSION_STRING = "1.8.0";
    public static long CLIENT_VERSION = versionStringToNumber(CLIENT_VERSION_STRING);

    //The world never changes objects
    public static final ClientWorld world = new ClientWorld();
    public static boolean LOAD_WORLD_ON_STARTUP = false;
    public static boolean FPS_TOOLS = false;
    public static boolean DEV_MODE = false;
    public static LocalPlayer userPlayer;
    public final ArrayList<Player> players = new ArrayList<>();
    public static FrameTester frameTester = new FrameTester("Game frame tester");
    public static FrameTester dummyTester = new FrameTester("");
    static MemoryGraph memoryGraph; //Make this priviate because it is null by default
    public final ClientWindow window;
    private final Game game;
    public ClientBase endpoint;
    public String title;

    static {
        AllPackets.registerPackets();
    }

    public void consoleOut(String s) {
        window.gameScene.ui.infoBox.addToHistory(s);
    }

    public void pauseGame() {
        if (window.isFullscreen()) window.minimizeWindow();
        window.gameScene.ui.baseMenu.setOpen(true);
    }

    public Client(String[] args, Game game) throws Exception {
        this.game = game;
        LOGGER.info("XBuilders Client (" + CLIENT_VERSION_STRING + ") started on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        //Process args
        System.out.println("args: " + Arrays.toString(args));
        String appDataDir = null;
        title = "XBuilders";

        for (String arg : args) {
            if (arg.equals("devmode")) {
                DEV_MODE = true;
            } else if (arg.startsWith("appData")) {
                appDataDir = arg.split("=")[1];
            } else if (arg.startsWith("name")) {
                title = arg.split("=")[1];
            } else if (arg.equals("loadWorldOnStartup")) {
                Client.LOAD_WORLD_ON_STARTUP = true;
            }
        }
        ResourceUtils.initialize(DEV_MODE, appDataDir);

        /**
         * Testers
         */
        if (!Client.DEV_MODE) Client.FPS_TOOLS = false;
        dummyTester.setEnabled(false);
        if (Client.FPS_TOOLS) {
            frameTester.setEnabled(true);
            frameTester.setStarted(true);
            frameTester.setUpdateTimeMS(1000);
            memoryGraph = new MemoryGraph();
        } else {
            frameTester.setEnabled(false);
        }

        window = new ClientWindow(title, this);
        window.init(game, world);
//        LOGGER.addHandler(new LoggingUtils.SevereErrorHandler(window));
    }


    public void onConnected(boolean success, Throwable cause, ChannelBase channel) {
        prog = new ProgressData(title);

        if (success) {
            System.out.println("Connected to " + channel.remoteAddress());
            window.topMenu.progress.enable(prog, () -> {//update
                try {
                    joinGameUpdate(prog, channel);
                } catch (Exception e) {
                    LOGGER.info("error", e);
                    prog.abort();
                }
            }, () -> {//finished
                window.goToGamePage();
                window.topMenu.setPage(Page.HOME);
            }, () -> {//canceled
                stopGame();
                window.topMenu.setPage(Page.HOME);
            });
        } else {
            prog.abort();
            com.xbuilders.Main.LOGGER.warn("Connection refused", cause);
        }
    }

    /**
     * We can either summon the localServer or we can join an existing server
     *
     * @param singleplayerWorld
     * @param remoteWorld
     */
    public void loadWorld(final WorldData singleplayerWorld, final NetworkJoinRequest remoteWorld) {
        Main.getClient().window.gameScene.setProjection();

        if (singleplayerWorld != null) { //Spin up a local server
            world.setData(singleplayerWorld); //set the world data
            //The server must have a separate world even if it's a single-player game
            //In singleplayer, the chunks are shared by both client and server to save memory
            ServerWorld serverWorld = new ServerWorld(world);

            try {
                if (remoteWorld != null)
                    Main.setServer(new Server(game, serverWorld, remoteWorld.port)); //Create a server with a real endpoint
                else Main.setServer(new Server(game, serverWorld)); //Create a server with a fake endpoint
            } catch (InterruptedException e) {
                LOGGER.warn("Error starting server", e);
                return;
            }

            new Thread(() -> { //Start the server on another thread
                try {
                    Main.getServer().run();
                } finally {
                    stopGame();
                }
            }).start();
        }

        if (remoteWorld != null) { //Start up real endpoint
            System.out.println("Starting endpoint...");
            try {
                endpoint = new NettyClient(remoteWorld.address, remoteWorld.port) {
                    @Override
                    public void onConnected(boolean success, Throwable cause, ChannelBase channel) {
                        Client.this.onConnected(success, cause, channel);
                    }
                };
            } catch (InterruptedException e) {
                LOGGER.warn("Error starting endpoint", e);
                return;
            }
        } else { //Start up fake endpoint
            endpoint = new FakeClient((FakeServer) Main.getServer().endpoint) {
                @Override
                public void onConnected(boolean success, Throwable cause, ChannelBase channel) {
                    Client.this.onConnected(success, cause, channel);
                }
            };
        }


    }

    private void waitForTasksToComplete(ProgressData prog) {
        if (world.newGameTasks.get() < prog.bar.getMax()) {
            prog.bar.setProgress(world.newGameTasks.get());
        } else {
            prog.stage++;
            world.newGameTasks.set(0);
        }
    }

    private void terminateIfTimeout(long maxWaitMS, ProgressData prog) {
        long now = System.currentTimeMillis();
        if (now - lastProgressTime > maxWaitMS) {
            prog.abort("Request timed out");
        }
    }

    ProgressData prog;
    long lastProgressTime;
    int completeChunks, framesWithCompleteChunkValue;
    final boolean WAIT_FOR_ALL_CHUNKS_TO_LOAD_BEFORE_JOINING = true;


    public void joinGameUpdate(ProgressData prog, ChannelBase channel) {
        switch (prog.stage) {
            case 0 -> {
                completeChunks = 0;
                framesWithCompleteChunkValue = 0;
                prog.setTask("Requesting entrance...");
                channel.writeAndFlush(new ClientEntrancePacket(userPlayer));
                lastProgressTime = System.currentTimeMillis();
                prog.stage++;
            }
            case 1 -> {
                //waiting for incoming serverGatekeeperPacket to update the progress
                terminateIfTimeout(30000, prog);
            }
            case 2 -> {
                boolean ok;
                if (world.getData().getSpawnPoint() == null) { //Create spawn point
                    Client.userPlayer.worldPosition.set(0, 0, 0);
                    ok = world.open(prog, new Vector3f(0, 0, 0));
                } else {//Load spawn point
                    Client.userPlayer.worldPosition.set(world.getData().getSpawnPoint().x, world.getData().getSpawnPoint().y, world.getData().getSpawnPoint().z);
                    ok = world.open(prog, Client.userPlayer.worldPosition);
                }
                if (!ok) {
                    prog.abort();
                    window.goToMenuPage();
                }
                prog.stage++;
            }
            case 3 -> {
                waitForTasksToComplete(prog);
            }
            case 4 -> { //Prepare chunks
                if (WAIT_FOR_ALL_CHUNKS_TO_LOAD_BEFORE_JOINING) {
                    prog.setTask("Preparing chunks");
                    AtomicInteger finishedChunks = new AtomicInteger();
                    world.chunks.forEach((vec, c) -> { //For simplicity, We call the same prepare method the same as in world class
                        c.prepare(0, true);
                        finishedChunks.getAndIncrement();
                    });

                    prog.bar.setProgress(finishedChunks.get(), world.chunks.size() / 2);
                    if (finishedChunks.get() != completeChunks) {
                        completeChunks = finishedChunks.get();
                        framesWithCompleteChunkValue = 0;
                    } else {
                        framesWithCompleteChunkValue++; //We cant easily determine how many chunks can be loaded, so we just wait
                        if (framesWithCompleteChunkValue > 50) {
                            prog.stage++;
                        }
                    }
                } else prog.stage++;
            }
            default -> {
                //The client controls the player and so it should decide where to spawn
                if (world.getData().getSpawnPoint() == null) {
                    //Find spawn point
                    //new World Event runs for the first time in a new world
                    Vector3f spawnPoint = getInitialSpawnPoint(world.terrain);
                    Client.userPlayer.worldPosition.set(spawnPoint);
                    System.out.println("Spawn point: " + spawnPoint.x + ", " + spawnPoint.y + ", " + spawnPoint.z);
                    Client.userPlayer.setSpawnPoint(spawnPoint.x, spawnPoint.y, spawnPoint.z);
                }
                userPlayer.loadFromWorld(world.getData());
                game.startGameEvent(world.getData());
                prog.finish();
            }
        }
    }

    public ProgressData getJoinProgressData() {
        return prog;
    }


    public Vector3f getInitialSpawnPoint(Terrain terrain) {
        Vector3f worldPosition = new Vector3f();
        System.out.println("Setting new spawn point...");
        int radius = Chunk.WIDTH * 2;
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                for (int y = terrain.minSurfaceHeight - 20; y < terrain.maxSurfaceHeight + 20; y++) {
                    if (terrain.canSpawnHere(world, x, y, z)) {
                        System.out.println("Found new spawn point!");
                        worldPosition.set(x, y - 0.5f, z);
                        return worldPosition;
                    }
                }
            }
        }
        System.out.println("Spawn point not found!");
        worldPosition.set(0, terrain.minSurfaceHeight - PLAYER_HEIGHT - 0.5f, 0);
        return worldPosition;
    }

    public void stopGame() {
        try {
            if (Main.getServer() != null) Main.getServer().stop();  //If we have a local server
        } catch (Exception e) {
            LOGGER.info("error", e);
        } finally {
            Main.setServer(null);
            System.gc();
        }
    }

}
