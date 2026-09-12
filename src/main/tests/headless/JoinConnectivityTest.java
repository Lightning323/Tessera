package headless;

import com.tessera.Main;
import com.tessera.content.vanilla.Game;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.netty.NettyClient;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.option.OptionsList;
import com.tessera.engine.common.packets.ChunkDataPacket;
import com.tessera.engine.common.packets.ChunkRequestPacket;
import com.tessera.engine.common.packets.ClientEntrancePacket;
import com.tessera.engine.common.packets.GameStatePacket;
import com.tessera.engine.common.packets.ServerGatekeeperPacket;
import com.tessera.engine.common.packets.ServerWorldDataPacket;
import com.tessera.engine.common.world.ClientWorld;
import com.tessera.engine.common.world.ServerWorld;
import com.tessera.engine.common.world.Terrain;
import com.tessera.engine.common.world.WorldData;
import com.tessera.engine.common.world.chunk.ServerChunk;
import com.tessera.engine.common.world.gen.GenContext;
import com.tessera.engine.common.world.gen.TerrainRegistry;
import com.tessera.engine.server.Server;
import org.joml.Vector3i;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Headless end-to-end connectivity test against the REAL server and network
 * stack (no GL, no window, no GUI). Runs the actual {@link Server}
 * (NettyServer) and connects with the real {@link NettyClient}, then drives
 * the exact handshake the join flow uses:
 * <ol>
 *   <li>client connects to the bound server port;</li>
 *   <li>client sends {@link ClientEntrancePacket};</li>
 *   <li>server admits it ({@link ServerGatekeeperPacket} allowedIn=true);</li>
 *   <li>server sends {@link GameStatePacket};</li>
 *   <li>server sends {@link ServerWorldDataPacket} whose seed matches the
 *       server's world;</li>
 *   <li>client requests chunk (0,0,0) and receives a non-empty
 *       {@link ChunkDataPacket} back.</li>
 * </ol>
 * Every step must pass (exit 0), otherwise the test fails (exit 1) — so we can
 * iterate on connectivity bugs without ever opening a game window.
 */
public class JoinConnectivityTest {

    static final int TEST_PORT = 25566;
    static final int EXPECTED_SEED = 12345;

    static int total = 0;
    static int failures = 0;

    static final AtomicBoolean gatekeepAllowed = new AtomicBoolean(false);
    static final AtomicReference<String> gatekeepReason = new AtomicReference<>("");
    static final AtomicBoolean gameStateSeen = new AtomicBoolean(false);
    static final AtomicBoolean worldDataSeen = new AtomicBoolean(false);
    static final AtomicReference<String> worldName = new AtomicReference<>("");
    static final AtomicReference<String> worldJson = new AtomicReference<>("");
    static final AtomicInteger worldSeed = new AtomicInteger(0);
    static final AtomicReference<Vector3i> chunkPosition = new AtomicReference<>(null);
    static final AtomicInteger chunkLen = new AtomicInteger(-1);

    public static void main(String[] args) throws Exception {
        // --- server-side world -------------------------------------------------
        Terrain flat = new FlatTestTerrain();
        TerrainRegistry.clear();
        TerrainRegistry.register(flat);
        check("terrain registered", TerrainRegistry.get("HeadlessTestTerrain") == flat);

        ClientWorld shell = new ClientWorld();
        WorldData worldData = new WorldData();
        worldData.makeNew("HeadlessJoinWorld", 512, flat, EXPECTED_SEED);
        shell.setData(worldData);
        ServerWorld serverWorld = new ServerWorld(shell);
        check("server world seed", serverWorld.getData().getSeed() == EXPECTED_SEED);

        Game game = new Game();
        Server server;
        try {
            server = new Server(game, serverWorld, TEST_PORT);
        } catch (Exception e) {
            System.out.println("[FAIL] server bind on " + TEST_PORT);
            e.printStackTrace();
            System.exit(1);
            return;
        }
        check("server bound on " + TEST_PORT, true);
        Main.setServer(server);

        Thread serverThread = new Thread(() -> {
            try {
                server.run();
            } catch (Throwable t) {
                System.err.println("[harness] server.run() failed");
                t.printStackTrace();
                System.exit(1);
            }
        }, "harness-server-run");
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(1500); // registerTerrains + live propagation come up

        // --- real client connect ------------------------------------------------
        NettyClient client = new NettyClient("127.0.0.1", TEST_PORT, JoinConnectivityTest::observe) {
            @Override
            public void onConnected(boolean success, Throwable cause, ChannelBase channel) {
                System.out.println("[harness] onConnected success=" + success
                        + (cause != null ? " cause=" + cause : ""));
            }
        };
        check("client connected to 127.0.0.1:" + TEST_PORT, true);

        // --- entrance handshake -------------------------------------------------
        client.getChannel().writeAndFlush(new ClientEntrancePacket("HeadlessTest", 0));

        boolean gk = reachIn(gatekeepAllowed, 10_000);
        check("gatekeeper admitted client", gk);
        boolean gs = reachIn(gameStateSeen, 10_000);
        check("game state received", gs);
        boolean wd = reachIn(worldDataSeen, 10_000);
        check("world data received", wd);
        check("world data seed matches server", wd && worldSeed.get() == EXPECTED_SEED);

        // --- chunk round trip ----------------------------------------------------
        client.getChannel().writeAndFlush(new ChunkRequestPacket(new Vector3i(0, 0, 0), 1f));
        boolean cd = reachInLong(chunkLen::get, 15_000);
        check("chunk data received at requested position",
                cd && chunkLen.get() > 0
                        && chunkPosition.get() != null
                        && chunkPosition.get().equals(new Vector3i(0, 0, 0)));

        try {
            client.close();
        } catch (Exception e) {
            // closing is best-effort in the harness
        }

        System.out.println();
        System.out.println("=========================================");
        System.out.println("RESULT: " + total + " checks, " + failures + " failures");
        System.out.println("=========================================");
        System.exit(failures == 0 ? 0 : 1);
    }

    private static void observe(Packet p) {
        if (p instanceof ServerGatekeeperPacket) {
            ServerGatekeeperPacket g = (ServerGatekeeperPacket) p;
            gatekeepAllowed.set(g.isAllowedIn());
            gatekeepReason.set(g.getReason());
            System.out.println("[harness] ServerGatekeeperPacket allowedIn=" + g.isAllowedIn()
                    + " reason='" + g.getReason() + "'");
        } else if (p instanceof GameStatePacket) {
            GameStatePacket gs = (GameStatePacket) p;
            gameStateSeen.set(true);
            System.out.println("[harness] GameStatePacket gameMode=" + gs.gameModeOrdinal);
        } else if (p instanceof ServerWorldDataPacket) {
            ServerWorldDataPacket wx = (ServerWorldDataPacket) p;
            worldDataSeen.set(true);
            worldName.set(wx.name);
            worldJson.set(wx.dataJson);
            try {
                WorldData wd = new WorldData();
                wd.makeNew(wx.name, wx.dataJson);
                worldSeed.set(wd.getSeed());
                System.out.println("[harness] ServerWorldDataPacket name='" + wx.name
                        + "' seed=" + wd.getSeed());
            } catch (Throwable t) {
                System.out.println("[harness] ServerWorldDataPacket received but seed parse failed: " + t);
            }
        } else if (p instanceof ChunkDataPacket) {
            ChunkDataPacket c = (ChunkDataPacket) p;
            chunkPosition.set(c.chunkPosition);
            chunkLen.set(c.chunkData == null ? -1 : c.chunkData.length);
            System.out.println("[harness] ChunkDataPacket at " + c.chunkPosition
                    + " dataBytes=" + chunkLen.get());
        }
    }

    /** Poll a boolean flag until it turns true or the timeout elapses. */
    private static boolean reachIn(AtomicBoolean flag, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (flag.get()) return true;
            Thread.sleep(25);
        }
        return flag.get();
    }

    /** Poll an int-holder until it leaves the sentinel value or the timeout elapses. */
    private static boolean reachInLong(java.util.function.IntSupplier value, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (value.getAsInt() != -1) return true;
            Thread.sleep(25);
        }
        return value.getAsInt() != -1;
    }

    private static void check(String what, boolean passed) {
        total++;
        System.out.println((passed ? "[PASS] " : "[FAIL] ") + what);
        if (!passed) failures++;
    }

    /**
     * Tiny deterministic terrain for the harness: a solid floor, no decorations,
     * no cross-chunk reads, nothing that needs block type registries (GL).
     */
    private static final class FlatTestTerrain extends Terrain {
        static final short FILL = 1;

        FlatTestTerrain() {
            super("HeadlessTestTerrain");
        }

        @Override
        public void initOptions(OptionsList optionsList) {
        }

        @Override
        public void loadWorld(OptionsList options, int version) {
        }

        @Override
        protected void generateChunkInner(ServerChunk chunk, GenContext ctx) {
            for (int x = 0; x < ServerChunk.WIDTH; x++) {
                for (int z = 0; z < ServerChunk.WIDTH; z++) {
                    for (int y = 0; y < 4; y++) {
                        ctx.setBlockWorld(x, y, z, FILL);
                    }
                }
            }
            ctx.random.setSeed(1L);
            ctx.setBlockWorld(0, 4, 0, FILL);
        }
    }
}