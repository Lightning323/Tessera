import com.tessera.Main;
import com.tessera.content.vanilla.Game;
import com.tessera.content.vanilla.blocks.blocks.trees.TreeUtils;
import com.tessera.engine.common.option.OptionsList;
import com.tessera.engine.common.world.ClientWorld;
import com.tessera.engine.common.world.ServerWorld;
import com.tessera.engine.common.world.Terrain;
import com.tessera.engine.common.world.WorldData;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.chunk.ServerChunk;
import com.tessera.engine.common.world.gen.GenContext;
import com.tessera.engine.common.world.gen.TerrainRegistry;
import com.tessera.engine.server.Server;
import com.tessera.utils.resource.PathHandler;
import org.joml.Vector3i;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Headless tests for server-side chunk generation:
 * <ol>
 *   <li>Generation never touches the client (proves the client/server split:
 *       {@code Main.getClient()} stays null the whole run).</li>
 *   <li>Same seed produces identical chunks (determinism).</li>
 *   <li>Trees crossing chunk borders are continuous (no cutoff), and the
 *       result is identical no matter which order the chunks generate in.</li>
 *   <li>Spillover into already-sent chunks is reported for client patching.</li>
 *   <li>The async {@code ServerWorld.generateChunk} wrapper converges to the
 *       same bytes as synchronous generation.</li>
 * </ol>
 *
 * Run: {@code java -cp <classes+deps> ChunkGenTest} (no window needed).
 */
public class ChunkGenTest {

    static final short LOG = 1, LEAF = 2, DIRT = 3, STONE = 4;
    static final int SEED = 1234567;

    /** Minimal terrain: flat ground plus one border tree per surface chunk. */
    static class BorderTreeTerrain extends Terrain {
        BorderTreeTerrain() {
            super("Test Border-Tree Terrain");
        }

        @Override
        public void initOptions(OptionsList optionsList) {
        }

        @Override
        public void loadWorld(OptionsList options, int version) {
        }

        @Override
        protected void generateChunkInner(ServerChunk chunk, GenContext ctx) {
            for (int x = 0; x < Chunk.WIDTH; x++) {
                for (int z = 0; z < Chunk.WIDTH; z++) {
                    for (int y = 0; y < Chunk.WIDTH; y++) {
                        int wy = y + chunk.position.y * Chunk.WIDTH;
                        if (wy == 1) chunk.voxels.setBlock(x, y, z, DIRT);
                        else if (wy > 1) chunk.voxels.setBlock(x, y, z, STONE);
                    }
                }
            }
            // One tree rooted at the east border of every surface chunk. The
            // trunk rises into the chunk above and the canopy spills into the
            // +X neighbor: every part outside the home chunk must survive.
            if (chunk.position.y == 0) {
                int tx = chunk.position.x * Chunk.WIDTH + (Chunk.WIDTH - 1);
                int tz = chunk.position.z * Chunk.WIDTH + 16;
                int ty = 1;
                for (int k = 0; k < 5; k++) {
                    ctx.setBlockWorld(tx, ty - k, tz, LOG);
                }
                TreeUtils.terrain_roundedSquareLeavesLayer(ctx, tx, ty - 1, tz, 2, LEAF);
                TreeUtils.terrain_diamondLeavesLayer(ctx, tx, ty - 4, tz, 2, LEAF);
            }
        }
    }

    static int failures = 0;

    static void check(boolean cond, String name) {
        System.out.println((cond ? "PASS " : "FAIL ") + name);
        if (!cond) failures++;
    }

    static ServerWorld newWorld() {
        WorldData data = new WorldData();
        data.makeNew("ChunkGenTest", 0, TerrainRegistry.get("Test Border-Tree Terrain"), SEED);
        ClientWorld shell = new ClientWorld();
        shell.setData(data);
        return new ServerWorld(shell);
    }

    static void gen(ServerWorld world, int cx, int cy, int cz) {
        ServerChunk chunk = world.addChunk(new Vector3i(cx, cy, cz));
        chunk.generateTerrain();
        chunk.generateLight();
    }

    static short[] snapshot(ServerWorld world, int cx, int cy, int cz) {
        ServerChunk chunk = world.getChunk(new Vector3i(cx, cy, cz));
        short[] out = new short[Chunk.WIDTH * Chunk.WIDTH * Chunk.WIDTH];
        int i = 0;
        for (int x = 0; x < Chunk.WIDTH; x++)
            for (int y = 0; y < Chunk.WIDTH; y++)
                for (int z = 0; z < Chunk.WIDTH; z++)
                    out[i++] = chunk.voxels.getBlock(x, y, z);
        return out;
    }

    public static void main(String[] args) throws Exception {
        check(Main.getClient() == null, "headless: no client instance (server-only path)");

        PathHandler.initialize("test-output-chunkgen");
        TerrainRegistry.clear();
        TerrainRegistry.register(new BorderTreeTerrain());

        // 1+2: determinism across identical worlds.
        ServerWorld worldA = newWorld();
        gen(worldA, 0, 0, 0);
        ServerWorld worldB = newWorld();
        gen(worldB, 0, 0, 0);
        check(Arrays.equals(snapshot(worldA, 0, 0, 0), snapshot(worldB, 0, 0, 0)),
                "same seed -> identical chunk bytes");

        // 3: continuity across borders (origin chunk first, neighbors after).
        gen(worldA, 1, 0, 0);
        gen(worldA, 0, -1, 0);
        gen(worldA, 1, -1, 0);
        boolean trunkContinuous = true;
        for (int wy = 1; wy >= -3; wy--) {
            if (worldA.getBlockID(31, wy, 16) != LOG) trunkContinuous = false;
        }
        check(trunkContinuous, "trunk continuous across chunk borders (no cutoff)");
        check(worldA.getBlockID(32, 0, 16) == LEAF, "canopy spills into +X neighbor");
        check(worldA.getBlockID(30, -3, 16) == LEAF, "canopy reaches into chunk above");
        check(worldA.getBlockID(31, -3, 16) == LOG, "canopy does not bury the trunk tip");
        check(worldA.getBlockID(31, 1, 16) == LOG, "trunk replaces ground at its base");

        // 3b: reverse generation order converges to identical bytes.
        ServerWorld worldC = newWorld();
        gen(worldC, 1, -1, 0);
        gen(worldC, 0, -1, 0);
        gen(worldC, 1, 0, 0);
        gen(worldC, 0, 0, 0);
        int[][] cells = {{0, 0, 0}, {1, 0, 0}, {0, -1, 0}, {1, -1, 0}};
        boolean orderIndependent = true;
        for (int[] c : cells) {
            if (!Arrays.equals(snapshot(worldA, c[0], c[1], c[2]), snapshot(worldC, c[0], c[1], c[2]))) {
                orderIndependent = false;
            }
        }
        check(orderIndependent, "generation order does not change the result");

        // 4: spillover into an already-sent chunk is reported for patching.
        ServerWorld worldD = newWorld();
        gen(worldD, 1, 0, 0);
        ServerChunk sent = worldD.getChunk(new Vector3i(1, 0, 0));
        sent.markSentToClients();
        ServerChunk origin = worldD.addChunk(new Vector3i(0, 0, 0));
        GenContext ctx = origin.generateTerrain();
        boolean spillsReported = ctx.drainSpilloverUpdates().stream().anyMatch(u -> u.x >= 32);
        check(spillsReported, "spillover into sent chunks reported as block updates");

        // 5: async pipeline wrapper converges to the synchronous result.
        ServerWorld worldE = newWorld();
        Main.setServer(new Server(new Game(), worldE));
        try {
            ServerChunk async = worldE.addChunk(new Vector3i(0, 0, 0));
            worldE.generateChunk(async, 0, null);
            async.loadFuture.get();
            check(async.isSentToClients(), "async generation marks chunk sent");
            check(Arrays.equals(snapshot(worldE, 0, 0, 0), snapshot(worldA, 0, 0, 0)),
                    "async generation matches synchronous bytes");
        } finally {
            Main.setServer(null);
        }

        deleteRecursively(new File("test-output-chunkgen"));

        if (failures > 0) {
            System.out.println("ChunkGenTest FAILED: " + failures + " failure(s)");
            System.exit(1);
        }
        System.out.println("ChunkGenTest PASSED");
    }

    static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            Arrays.stream(children)
                    .sorted(Comparator.comparing(File::getAbsolutePath))
                    .forEach(ChunkGenTest::deleteRecursively);
        }
        file.delete();
    }
}
