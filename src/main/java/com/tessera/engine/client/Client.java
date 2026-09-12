package com.tessera.engine.client;

import com.tessera.Main;
import com.tessera.engine.common.network.netty.NettyClient;
import com.tessera.engine.common.network.NetworkJoinRequest;
import com.tessera.engine.common.packets.AllPackets;
import com.tessera.engine.common.packets.ClientEntrancePacket;
import com.tessera.engine.common.players.localPlayer.LocalPlayer;
import com.tessera.engine.client.visuals.Page;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.ClientBase;
import com.tessera.engine.common.network.fake.FakeClient;
import com.tessera.engine.common.network.fake.FakeServer;
import com.tessera.engine.common.progress.ProgressData;
import com.tessera.utils.resource.PathHandler;
import com.tessera.engine.common.world.*;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.server.Game;
import com.tessera.engine.server.Server;
import com.tessera.engine.common.players.Player;
import com.tessera.window.developmentTools.FrameTester;
import com.tessera.window.developmentTools.MemoryGraph;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tessera.Main.LOGGER;
import static com.tessera.engine.common.players.Player.PLAYER_HEIGHT;


public class Client {
    public static long VERSION = 0;

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
    /** Raw launch args (kept so test modes can spawn sibling processes). */
    public final String[] launchArgs;
    /** Block breaking/placing test run mode requested via launch args. */
    public final TestRunMode testRunMode;

    /**
     * Client-side mirror of the server's authoritative game mode.
     * Updated exclusively via {@code GameStatePacket}; client gameplay code
     * must read this instead of {@code Main.getServer().getGameMode()},
     * which does not exist on a remote (join-only) client and violates the
     * client/server boundary.
     */
    public volatile com.tessera.engine.server.GameMode cachedGameMode =
            com.tessera.engine.server.GameMode.FREEPLAY;
    public volatile com.tessera.engine.server.Difficulty cachedDifficulty =
            com.tessera.engine.server.Difficulty.NORMAL;

    /**
     * When set (auto-test multiplayer), a failed host bind or a refused
     * connection is rethrown so TestAutoRunner can retry/abort, instead of
     * falling back to the menu with an error popup.
     */
    public volatile boolean retryOnConnectFailures = false;

    /** Client-side view of the mode; safe on singleplayer and multiplayer. */
    public com.tessera.engine.server.GameMode getGameMode() {
        return cachedGameMode;
    }

    /** Client-side view of difficulty; safe on singleplayer and multiplayer. */
    public com.tessera.engine.server.Difficulty getDifficulty() {
        return cachedDifficulty;
    }

    /**
     * Tasks that must run on the window (GL) thread. Network handlers run on
     * FakeChannel/Netty IO threads where no GL context is current; any GL call
     * there (e.g. shader uniforms) hard-aborts the JVM. Handlers must only set
     * volatile state directly and enqueue everything else here. Drained at the
     * start of {@code ClientWindow.render()}, which always runs on the GL thread.
     */
    private final java.util.concurrent.ConcurrentLinkedQueue<Runnable> mainThreadTasks =
            new java.util.concurrent.ConcurrentLinkedQueue<>();

    public void runOnMainThread(Runnable task) {
        if (task != null) mainThreadTasks.offer(task);
    }

    public void drainMainThreadTasks() {
        Runnable task;
        while ((task = mainThreadTasks.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warn("Main-thread task failed", e);
            }
        }
    }

    static {
        AllPackets.registerPackets();
    }

    public void consoleOut(String s) {
        try {
            if (window == null || window.gameScene == null || window.gameScene.ui == null
                    || window.gameScene.ui.infoBox == null) {
                System.out.println("[consoleOut] " + s);
                return;
            }
            window.gameScene.ui.infoBox.addToHistory(s);
        } catch (Exception e) {
            System.out.println("[consoleOut] " + s);
            LOGGER.warn("consoleOut failed", e);
        }
    }

    public void pauseGame() {
        if (window.isFullscreen()) window.minimizeWindow();
        window.gameScene.ui.baseMenu.setOpen(true);
    }

    public Client(String[] args, Game game) throws Exception {
        LOGGER.info("Client v{} started", VERSION);
        this.game = game;
        //Process args
        System.out.println("args: " + Arrays.toString(args));
        String appDataDir = null;
        title = "tessera";

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
        launchArgs = args == null ? new String[0] : args.clone();
        testRunMode = TestRunMode.fromArgs(args);
        switch (testRunMode.mode()) {
            case SINGLEPLAYER -> title += " [test-singleplayer]";
            case MULTIPLAYER_HOST -> title += " [test-host:" + testRunMode.port() + "]";
            case MULTIPLAYER_JOIN -> title += " [test-join:" + testRunMode.port() + "]";
            default -> {
            }
        }
        PathHandler.initialize(appDataDir);

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
            com.tessera.Main.LOGGER.warn("Connection refused", cause);
        }
    }

    /**
     * We can either summon the localServer or we can join an existing server.
     *
     * <p>Modes:
     * <ul>
     *   <li>Singleplayer: {@code singleplayerWorld != null, remoteWorld == null}
     *       - local {@code Server} with {@code FakeServer} endpoint, client via
     *       {@code FakeClient} loopback. Same packets as multiplayer.</li>
     *   <li>Host: {@code singleplayerWorld != null, remoteWorld.hosting == true}
     *       - local {@code Server} with {@code NettyServer}, client via
     *       {@code NettyClient} loopback. Tests real Netty framing locally.</li>
     *   <li>Join: {@code remoteWorld != null && !remoteWorld.hosting}
     *       - no local server; client via {@code NettyClient} to the remote.
     *       Block edits must flow as packets (no local Server to touch).</li>
     * </ul>
     *
     * @param singleplayerWorld local world file (null when joining only)
     * @param remoteWorld       netty request (null for singleplayer loopback)
     */
    public void loadWorld(final WorldData singleplayerWorld, final NetworkJoinRequest remoteWorld) {
        Main.getClient().window.gameScene.setProjection();

        boolean joiningOnly = remoteWorld != null && !remoteWorld.hosting;
        boolean spinningLocalServer = singleplayerWorld != null && !joiningOnly;

        if (spinningLocalServer) { //Spin up a local server
            world.setData(singleplayerWorld); //set the world data
            // Prime the client-side mode cache so mining/UI is correct even
            // before GameStatePacket round-trips (FakeChannel is async).
            try {
                if (singleplayerWorld.data != null) {
                    if (singleplayerWorld.data.gameMode != null) cachedGameMode = singleplayerWorld.data.gameMode;
                    if (singleplayerWorld.data.difficulty != null) cachedDifficulty = singleplayerWorld.data.difficulty;
                }
            } catch (Exception ignored) {
            }
            //The server must have a separate world even if it's a single-player game
            //In singleplayer, the chunks are shared by both client and server to save memory
            ServerWorld serverWorld = new ServerWorld(world);

            try {
                if (remoteWorld != null)
                    Main.setServer(new Server(game, serverWorld, remoteWorld.port)); //Create a server with a real endpoint
                else Main.setServer(new Server(game, serverWorld)); //Create a server with a fake endpoint
            } catch (Exception e) {
                LOGGER.warn("Error starting server", e);
                handleLoadFailure(e, "Could not start the server (port "
                        + (remoteWorld != null ? remoteWorld.port : "internal") + " may already be in use)");
                return;
            }

            new Thread(() -> { //Start the server on another thread
                try {
                    Main.getServer().run();
                } finally {
                    stopGame();
                }
            }).start();
        } else if (joiningOnly) {
            // Join-only: there is deliberately NO local Server. Any client
            // code touching Main.getServer() here is a separation bug; block
            // edits flow as packets and game state arrives via GameStatePacket.
            // The authoritative WorldData arrives during the entrance handshake
            // (ServerWorldDataPacket); do NOT apply the local shell here or the
            // client would render the wrong terrain/seed for a remote host.
            System.out.println("Joining remote server at " + remoteWorld.address + ":" + remoteWorld.port
                    + " (no local server; packets only)");
            // Fresh shell: clear any leftover local world so the server's
            // authoritative ServerWorldDataPacket (handshake) always applies.
            world.setData(null);
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
            } catch (Exception e) {
                LOGGER.warn("Error starting endpoint to " + remoteWorld.address + ":" + remoteWorld.port, e);
                handleLoadFailure(e, "Could not connect to "
                        + remoteWorld.address + ":" + remoteWorld.port);
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

    /**
     * Routes a host/join setup failure (port in use, connection refused) to the
     * UI popup instead of letting it crash the render thread. In auto-test mode
     * the failure is rethrown so TestAutoRunner can retry (join) or abort (host).
     */
    private void handleLoadFailure(Exception e, String message) {
        stopGame();
        if (retryOnConnectFailures) {
            throw new RuntimeException(message + " (" + e.getMessage() + ")", e);
        }
        try {
            ClientWindow.popupMessage.message("Could not load world", message + "\n\n" + e.getMessage());
        } catch (Exception ignored) {
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
                if (world.getData() == null) { //Waiting for server world data (join without a local world)
                    prog.setTask("Waiting for world data...");
                    terminateIfTimeout(30000, prog);
                    return;
                }
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
                if (world.getData() == null) { //Waiting for server world data
                    prog.setTask("Waiting for world data...");
                    terminateIfTimeout(30000, prog);
                    return;
                }
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
