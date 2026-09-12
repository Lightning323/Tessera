package com.tessera.engine.client;

import com.tessera.Main;
import com.tessera.engine.common.network.NetworkJoinRequest;
import com.tessera.engine.common.world.WorldData;
import com.tessera.engine.common.world.WorldsHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * One-shot bootstrap for the {@code testSingleplayer} / {@code testMultiplayer}
 * / {@code testMultiplayerJoin} launch modes (see {@link TestRunMode}).
 *
 * <p>Runs once on the window thread right after startup (called from the top
 * menu's first frame, where the GL context and player already exist):
 * <ul>
 *   <li>singleplayer — loads the first world in the saves list via the
 *       singleplayer (FakeChannel) transport;</li>
 *   <li>multiplayer host — hosts the first world on the test port and spawns
 *       a second JVM window joined to it, so block breaking/placing can be
 *       tested across a real Netty connection in both directions;</li>
 *   <li>multiplayer join — joins the test host on loopback, retrying until
 *       the host is listening.</li>
 * </ul>
 *
 * <p>All modes auto-open the F8 block-interaction test panel once the world
 * starts loading. Fatal test preconditions (no worlds on disk, host bind
 * failure, join timeout) print to stderr and exit with status 1.
 */
public final class TestAutoRunner {

    /** How long the join window waits for the host to listen. */
    private static final int JOIN_ATTEMPTS = 30;
    private static final long JOIN_RETRY_MS = 1000;

    private TestAutoRunner() {
    }

    /** Runs the armed test mode once; no-op when no test mode was requested. */
    public static void runTestModeIfArmed(Client client) {
        TestRunMode t = client.testRunMode;
        if (t == null || !t.isTestMode()) return;

        applyPlayerNameOverride(client, t);

        switch (t.mode()) {
            case SINGLEPLAYER -> runTestSingleplayer(client);
            case MULTIPLAYER_HOST -> runTestMultiplayerHost(client, t);
            case MULTIPLAYER_JOIN -> runTestMultiplayerJoin(client, t);
            default -> {
            }
        }

        try {
            client.window.gameScene.ui.blockTestUI.setOpen(true);
            System.out.println("test mode: block-interaction test panel opened (F8 toggles)");
        } catch (Exception e) {
            Main.LOGGER.warn("test mode: could not open block test panel", e);
        }
    }

    private static void applyPlayerNameOverride(Client client, TestRunMode t) {
        if (t.playerNameOverride() == null) return;
        try {
            Client.userPlayer.setName(t.playerNameOverride());
            System.out.println("test mode: player name overridden to '" + Client.userPlayer.getName() + "'");
        } catch (Exception e) {
            System.err.println("test mode: could not override player name: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void runTestSingleplayer(Client client) {
        WorldData world = firstWorldOrExit("testSingleplayer");
        System.out.println("testSingleplayer: loading world '" + world.getName() + "' in singleplayer");
        client.loadWorld(world, null);
    }

    private static void runTestMultiplayerHost(Client client, TestRunMode t) {
        WorldData world = firstWorldOrExit("testMultiplayer");
        System.out.println("testMultiplayer: hosting world '" + world.getName()
                + "' on port " + t.port() + " and opening a second window joined to it");
        try {
            client.loadWorld(world, new NetworkJoinRequest(
                    true, t.port(), Client.userPlayer.getName(), TestRunMode.TEST_JOIN_HOST));
        } catch (Exception e) {
            System.err.println("testMultiplayer: failed to host world '" + world.getName()
                    + "' on port " + t.port() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            return;
        }
        spawnJoinChild(client.launchArgs, t.port());
    }

    private static void runTestMultiplayerJoin(Client client, TestRunMode t) {
        // Placeholder shell so terrain/meshing has world info until the host's
        // chunks arrive over Netty (same world on this machine).
        WorldData shell = firstWorldOrExit("testMultiplayerJoin");
        System.out.println("testMultiplayerJoin: joining " + TestRunMode.TEST_JOIN_HOST
                + ":" + t.port() + " as '" + Client.userPlayer.getName() + "'");

        Exception last = null;
        for (int attempt = 1; attempt <= JOIN_ATTEMPTS; attempt++) {
            try {
                client.loadWorld(shell, new NetworkJoinRequest(
                        false, t.port(), Client.userPlayer.getName(), TestRunMode.TEST_JOIN_HOST));
                System.out.println("testMultiplayerJoin: connected on attempt " + attempt);
                return;
            } catch (Exception e) {
                last = e;
                System.out.println("testMultiplayerJoin: host not ready (attempt "
                        + attempt + "/" + JOIN_ATTEMPTS + "): " + e.getMessage());
                try {
                    Thread.sleep(JOIN_RETRY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        System.err.println("testMultiplayerJoin: could not reach host at "
                + TestRunMode.TEST_JOIN_HOST + ":" + t.port()
                + " after " + JOIN_ATTEMPTS + " attempts. Is the host running?");
        if (last != null) last.printStackTrace();
        System.exit(1);
    }

    /**
     * Opens the second test window: a new JVM on the same classpath that joins
     * the host. The server rejects duplicate player names, so the child gets a
     * random guest name (a manual {@code playerName=} on the host is never
     * inherited).
     */
    private static void spawnJoinChild(String[] parentArgs, int port) {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String guestName = "TestGuest" + (100 + new Random().nextInt(900));

        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(javaBin);
        cmd.add("-cp");
        cmd.add(classpath);
        cmd.add("com.tessera.Main");
        cmd.add(TestRunMode.ARG_TEST_MULTIPLAYER_JOIN);
        cmd.add(TestRunMode.ARG_PORT_PREFIX + port);
        cmd.add(TestRunMode.ARG_PLAYER_NAME_PREFIX + guestName);
        cmd.add("name=Tessera-TestJoin");
        // Forward only these, so the child sees the same saves dir / flags but
        // never inherits this window's test mode, port intent, or player name.
        if (parentArgs != null) {
            for (String a : parentArgs) {
                if (a == null) continue;
                if (a.equals("devmode") || a.startsWith("appData")) cmd.add(a);
            }
        }

        System.out.println("testMultiplayer: launching join window as '" + guestName + "'");
        try {
            Process child = new ProcessBuilder(cmd).inheritIO().start();
            Runtime.getRuntime().addShutdownHook(new Thread(child::destroy));
        } catch (IOException e) {
            System.err.println("testMultiplayer WARNING: could not launch the join window: " + e.getMessage());
            System.err.println("The host window is still usable; join manually with: "
                    + TestRunMode.ARG_TEST_MULTIPLAYER_JOIN + " " + TestRunMode.ARG_PORT_PREFIX + port);
        }
    }

    /** First world in the saves list, or stderr + exit(1) when none exists. */
    private static WorldData firstWorldOrExit(String modeArg) {
        ArrayList<WorldData> worlds = new ArrayList<>();
        try {
            WorldsHandler.listWorlds(worlds);
        } catch (IOException e) {
            System.err.println(modeArg + ": could not list worlds: " + e.getMessage());
            System.exit(1);
        }
        if (worlds.isEmpty()) {
            System.err.println(modeArg + ": no worlds found in the saves folder."
                    + " Create a world first, then re-run.");
            System.exit(1);
        }
        WorldData world = worlds.get(0);
        System.out.println(modeArg + ": using first world '" + world.getName() + "'");
        return world;
    }
}
