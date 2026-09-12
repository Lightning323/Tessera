package com.tessera.engine.client;

import java.util.Locale;

/**
 * Launch-argument spec for the block breaking/placing test run modes.
 *
 * <p>Modes (mutually exclusive):
 * <ul>
 *   <li>{@code testSingleplayer} — load the first world in the saves list in
 *       singleplayer (FakeChannel transport). Exits if no worlds exist.</li>
 *   <li>{@code testMultiplayer} — host the first world on {@link #DEFAULT_TEST_PORT}
 *       and automatically open a second window (separate JVM) joined to it,
 *       so block edits can be tested across a real Netty connection in both
 *       directions. Exits if no worlds exist.</li>
 *   <li>{@code testMultiplayerJoin} — join a test host at
 *       {@code 127.0.0.1:port}. Used internally by the spawned second window,
 *       but can also be run manually against any test host.</li>
 * </ul>
 *
 * <p>Optional extras (only honored when a test mode is active):
 * <ul>
 *   <li>{@code port=NNNN} — override the test port (default
 *       {@link #DEFAULT_TEST_PORT}). Must be 1024-65535.</li>
 *   <li>{@code playerName=Name} — override this window's player name
 *       (in-memory only, never saved). Needed because the server rejects
 *       duplicate player names, so two test windows cannot share one.</li>
 * </ul>
 *
 * <p>Parsing is a pure function of the raw args ({@link #fromArgs}) so it can
 * be verified without starting the game. Usage errors exit with status 2;
 * missing test preconditions (e.g. no worlds) exit with status 1.
 */
public final class TestRunMode {

    public enum Mode {
        NONE,
        SINGLEPLAYER,
        MULTIPLAYER_HOST,
        MULTIPLAYER_JOIN
    }

    public static final String ARG_TEST_SINGLEPLAYER = "testSingleplayer";
    public static final String ARG_TEST_MULTIPLAYER = "testMultiplayer";
    public static final String ARG_TEST_MULTIPLAYER_JOIN = "testMultiplayerJoin";
    public static final String ARG_PORT_PREFIX = "port=";
    public static final String ARG_PLAYER_NAME_PREFIX = "playerName=";

    /** Default port the test-multiplayer host listens on. */
    public static final int DEFAULT_TEST_PORT = 25565;
    /** Loopback address the test host and join client use. */
    public static final String TEST_JOIN_HOST = "127.0.0.1";
    public static final int MIN_PORT = 1024;
    public static final int MAX_PORT = 65535;

    private final Mode mode;
    private final int port;
    private final String playerNameOverride;

    public TestRunMode(Mode mode, int port, String playerNameOverride) {
        this.mode = mode == null ? Mode.NONE : mode;
        this.port = port;
        this.playerNameOverride = playerNameOverride == null || playerNameOverride.isBlank()
                ? null : playerNameOverride.strip();
    }

    public Mode mode() {
        return mode;
    }

    public boolean isTestMode() {
        return mode != Mode.NONE;
    }

    public int port() {
        return port;
    }

    /** In-memory player-name override, or null to keep the configured name. */
    public String playerNameOverride() {
        return playerNameOverride;
    }

    /**
     * Parse raw launch args into a {@link TestRunMode}. Never returns null.
     * Exits the JVM with status 2 on conflicting modes or a malformed
     * {@code port=} value when a test mode is active; unknown args are ignored
     * (other arg parsers own them).
     */
    public static TestRunMode fromArgs(String[] args) {
        Mode mode = Mode.NONE;
        String modeArg = null;
        int port = DEFAULT_TEST_PORT;
        boolean portSeen = false;
        String portRaw = null;
        String playerName = null;

        if (args != null) {
            for (String arg : args) {
                if (arg == null) continue;
                String lower = arg.toLowerCase(Locale.ROOT);
                if (lower.equals(ARG_TEST_SINGLEPLAYER.toLowerCase(Locale.ROOT))) {
                    if (mode != Mode.NONE) return conflict(modeArg, arg);
                    mode = Mode.SINGLEPLAYER;
                    modeArg = arg;
                } else if (lower.equals(ARG_TEST_MULTIPLAYER_JOIN.toLowerCase(Locale.ROOT))) {
                    // Must be checked before testMultiplayer: "testMultiplayerJoin"
                    // does not equal "testMultiplayer", but keep the ordering
                    // explicit so future prefix matching cannot misroute it.
                    if (mode != Mode.NONE) return conflict(modeArg, arg);
                    mode = Mode.MULTIPLAYER_JOIN;
                    modeArg = arg;
                } else if (lower.equals(ARG_TEST_MULTIPLAYER.toLowerCase(Locale.ROOT))) {
                    if (mode != Mode.NONE) return conflict(modeArg, arg);
                    mode = Mode.MULTIPLAYER_HOST;
                    modeArg = arg;
                } else if (lower.startsWith(ARG_PORT_PREFIX)) {
                    portSeen = true;
                    portRaw = arg.substring(ARG_PORT_PREFIX.length()).strip();
                } else if (arg.startsWith(ARG_PLAYER_NAME_PREFIX)) {
                    playerName = arg.substring(ARG_PLAYER_NAME_PREFIX.length()).strip();
                    if (playerName.isEmpty()) playerName = null;
                }
            }
        }

        if (portSeen && mode != Mode.NONE) {
            try {
                port = Integer.parseInt(portRaw);
            } catch (NumberFormatException e) {
                return usageError("Invalid " + ARG_PORT_PREFIX + " value '" + portRaw
                        + "': expected an integer " + MIN_PORT + "-" + MAX_PORT + ".");
            }
            if (port < MIN_PORT || port > MAX_PORT) {
                return usageError("Invalid " + ARG_PORT_PREFIX + " value '" + portRaw
                        + "': expected " + MIN_PORT + "-" + MAX_PORT + ".");
            }
        }

        return new TestRunMode(mode, port, playerName);
    }

    private static TestRunMode conflict(String first, String second) {
        return usageError("Conflicting test modes '" + first + "' and '" + second
                + "': pass only one of " + ARG_TEST_SINGLEPLAYER + ", "
                + ARG_TEST_MULTIPLAYER + ", " + ARG_TEST_MULTIPLAYER_JOIN + ".");
    }

    private static TestRunMode usageError(String message) {
        System.err.println("test run mode error: " + message);
        System.err.println("Usage: testSingleplayer | testMultiplayer | testMultiplayerJoin"
                + " [port=NNNN] [playerName=Name]");
        System.exit(2);
        return new TestRunMode(Mode.NONE, DEFAULT_TEST_PORT, null);
    }
}
