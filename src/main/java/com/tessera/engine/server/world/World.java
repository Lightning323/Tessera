package com.tessera.engine.server.world;

import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.gameScene.GameScene;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.entity.ChunkEntitySet;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.server.entity.EntitySupplier;
import com.tessera.engine.server.players.Player;
import com.tessera.engine.server.multiplayer.Local_MultiplayerPendingBlockChanges;
import com.tessera.engine.server.multiplayer.Local_MultiplayerPendingEntityChanges;
import com.tessera.engine.client.player.UserControlledPlayer;
import com.tessera.engine.client.player.camera.Camera;
import com.tessera.engine.server.players.pipeline.BlockHistory;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.ChunkShader;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.mesh.CompactOcclusionMesh;
import com.tessera.engine.common.settings.Settings;
import com.tessera.engine.utils.BFS.ChunkNode;
import com.tessera.engine.utils.math.AABB;
import com.tessera.engine.utils.math.MathUtils;
import com.tessera.engine.utils.progress.ProgressData;
import com.tessera.engine.utils.threadPoolExecutor.PriorityExecutor.ExecutorServiceUtils;
import com.tessera.engine.utils.threadPoolExecutor.PriorityExecutor.PriorityThreadPoolExecutor;
import com.tessera.engine.utils.threadPoolExecutor.PriorityExecutor.comparator.LowValueComparator;
import com.tessera.engine.server.world.chunk.BlockData;
import com.tessera.engine.server.world.chunk.FutureChunk;

import java.io.IOException;

import com.tessera.engine.server.world.chunk.Chunk;

import static com.tessera.Main.game;
import static com.tessera.engine.client.Client.userPlayer;
import static com.tessera.engine.client.Client.world;

import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockArrayTexture;
import com.tessera.engine.utils.ErrorHandler;

import static com.tessera.engine.utils.math.MathUtils.positiveMod;

import static com.tessera.engine.server.world.wcc.WCCi.chunkDiv;

import com.tessera.engine.server.world.chunk.pillar.PillarInformation;

import java.lang.Math;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.tessera.engine.server.world.data.WorldData;
import com.tessera.engine.server.world.light.SunlightUtils;
import com.tessera.engine.server.world.light.TorchUtils;
import com.tessera.engine.server.world.wcc.WCCi;
import org.joml.*;

/**
 * The world is the main class that manages the chunks and entities in the game.
 * It is the model for the game world and everything in it.
 */
public class World {


    /*
     * CHUNK GENERATION PERFORMANCE
     *
     * Valkyre:
     * Chunk Load distance: 5
     * Load distance in voxels: 160
     * (Minecraft has a default chunk distance of 160 blocks.)
     *
     * In valkyre,With the standard chunk distanceThe FPS sometimes dips when
     * loading lots of stuff, but not very often.
     * (But This issue is resolved when vsync is disabled.)
     *
     * COMPARING PERFORMANCE
     * - When comparing the two side by side, with the same chunk distance, my
     * preformance with that chunk distance is actually
     * at the exact same level as valkyre.
     * - Also note that valkyre has half the chunk height as my game.
     * - When valkyre loads chunks at 288 voxels away, things start getting choppy
     * just like my game does.
     */
    public static final int CHUNK_LOAD_THREADS = 12; //Redicing the number of threads helps performance
    public static final int CHUNK_LIGHT_THREADS = 1;
    public static final int CHUNK_MESH_THREADS = 1;
    public static final int PLAYER_CHUNK_MESH_THREADS = 3; //The number of threads allocated to player based chunk updating

    public static int VIEW_DIST_MIN = Chunk.WIDTH * 2;
    public static int VIEW_DIST_MAX = Chunk.WIDTH * 32; //Allowing higher view distances increases flexibility

    private int maxChunksForViewDistance;
    private final AtomicInteger viewDistance = new AtomicInteger(VIEW_DIST_MIN);
    public final static AtomicInteger newGameTasks = new AtomicInteger(0);
    public ChunkShader chunkShader;


    public void setViewDistance(Settings settings, int viewDistance2) {
        viewDistance.set(MathUtils.clamp(viewDistance2, VIEW_DIST_MIN, VIEW_DIST_MAX));
        // Settings
        settings.viewDistance.value = viewDistance.get();
        settings.save();
        chunkShader.setViewDistance(viewDistance.get() - Chunk.WIDTH);
        maxChunksForViewDistance = Integer.MAX_VALUE;
    }

    public int getViewDistance() {
        return viewDistance.get();
    } //The view distance reffers to rendering distance.

    public int getCreationViewDistance() {
        return viewDistance.get() + Chunk.WIDTH;
    }

    public int getDeletionViewDistance() {
        return viewDistance.get() + (Chunk.WIDTH * 6);
    }



    // World boundaries
    // chunk boundaries
    // These are the boundaries of the world. We can set int.min and int.max if we want them to be infinite
    public final static int TOP_Y_CHUNK = -2;
    public final static int BOTTOM_Y_CHUNK = 7;
    public final static int WORLD_CHUNK_HEIGHT = BOTTOM_Y_CHUNK - TOP_Y_CHUNK;

    // voxel boundaries
    public static final int WORLD_SIZE_NEG_X = -32000; // -X
    public static final int WORLD_TOP_Y = TOP_Y_CHUNK * Chunk.WIDTH; // up (-Y)
    public static final int WORLD_SIZE_NEG_Z = -32000; // -Z

    public static final int WORLD_SIZE_POS_X = 32000; // +X
    public static final int WORLD_BOTTOM_Y = (BOTTOM_Y_CHUNK * Chunk.WIDTH) + Chunk.WIDTH; // down (+Y)
    public static final int WORLD_SIZE_POS_Z = 32000; // +Z

    public static final int WORLD_SIZE_Y = WORLD_BOTTOM_Y - WORLD_TOP_Y;

    public static boolean inYBounds(int y) {
        return y > WORLD_TOP_Y && y < WORLD_BOTTOM_Y - 1;
    }

    public boolean inBounds(int ix, int iy, int iz) {
        return ix >= WORLD_SIZE_NEG_X && ix < WORLD_SIZE_POS_X
                && iz >= WORLD_SIZE_NEG_Z && iz < WORLD_SIZE_POS_Z
                && inYBounds(iy);
    }

    //Model properties
    public final Map<Vector3i, Chunk> chunks = new ConcurrentHashMap<>(); //Important if we want to use this in multiple threads
    public final WorldEntityMap entities = new WorldEntityMap(); // <chunkPos, entity>
    public final List<Player> players = new ArrayList<>();
    public WorldData data;
    public Terrain terrain;

    //Client properties
    private final AtomicBoolean needsSorting; // Atomic variables are thread update
    private final Vector3f lastPlayerPosition = new Vector3f();
    private SortByDistanceToPlayer sortByDistance;
    private final List<Chunk> unusedChunks = new ArrayList<>();
    private final Map<Vector3i, FutureChunk> futureChunks = new HashMap<>();
    private final List<Chunk> sortedChunksToRender = new ArrayList<>();
    private int blockTextureID;

    /**
     * This is a record of all the pending changes that need to be applied.
     * Before we load the world, all of the pending block changes must be applied to the world
     */
    public Local_MultiplayerPendingBlockChanges multiplayerPendingBlockChanges;
    public Local_MultiplayerPendingEntityChanges multiplayerPendingEntityChanges;



    /**
     * = new ScheduledThreadPoolExecutor(1, r -> { ... });: This line creates an
     * instance of ScheduledThreadPoolExecutor. It's a typeReference of
     * ScheduledExecutorService that uses a pool of threads to execute
     * tasks.<br>
     * <br>
     * <p>
     * - 1 specifies that the pool will have one thread. This means it will be
     * capable of executing one task at a time.<br>
     * <br>
     * <p>
     * - r -> { ... } is a lambda expression that provides a ThreadFactory to
     * the executor. It defines how threads are created. In this case, it
     * creates a new thread, sets its name to "Generation Thread", and marks it
     * as a daemon thread (meaning it won't prevent the JVM from exiting).
     */
    public static final PriorityThreadPoolExecutor generationService = new PriorityThreadPoolExecutor(
            CHUNK_LOAD_THREADS, r -> {
        Thread thread = new Thread(r, "Generation Thread");
        thread.setDaemon(true);
        return thread;
    }, new LowValueComparator());

    /**
     * THIS was the ONLY REASON why the chunk meshService.submit() in chunk mesh
     * generation was the performance bottleneck. We have to be careful the
     * settings we put here, because with the wrong settings, a task can take a
     * lot of time to execute() and block the main render thread
     */
    public static final ThreadPoolExecutor meshService = new ThreadPoolExecutor(
            CHUNK_MESH_THREADS, CHUNK_MESH_THREADS,
            3L, TimeUnit.MILLISECONDS, // It really just came down to tuning these settings for performance
            new LinkedBlockingQueue<Runnable>(), r -> {
        Client.frameTester.count("Mesh threads", 1);
        Thread thread = new Thread(r, "Mesh Thread");
        thread.setDaemon(true);
        thread.setPriority(1);
        return thread;
    });

    public static final PriorityThreadPoolExecutor lightService = new PriorityThreadPoolExecutor(CHUNK_LIGHT_THREADS,
            r -> {
                Thread thread = new Thread(r, "Light Thread");
                thread.setDaemon(true);
                return thread;
            }, new LowValueComparator());


    public static final ThreadPoolExecutor playerUpdating_meshService = new ThreadPoolExecutor(
            PLAYER_CHUNK_MESH_THREADS, PLAYER_CHUNK_MESH_THREADS,
            1L, TimeUnit.MILLISECONDS, // It really just came down to tuning these settings for performance
            new LinkedBlockingQueue<Runnable>(), r -> {
        Client.frameTester.count("Player Mesh threads", 1);
        Thread thread = new Thread(r, "Player Mesh Thread");
        thread.setDaemon(true);
        thread.setPriority(10);
        return thread;
    });

    public World() {
        this.needsSorting = new AtomicBoolean(true);

    }

    public void init(UserControlledPlayer player, BlockArrayTexture textures) throws IOException {
        multiplayerPendingBlockChanges = new Local_MultiplayerPendingBlockChanges(player);
        multiplayerPendingEntityChanges = new Local_MultiplayerPendingEntityChanges(player);

        blockTextureID = textures.getTexture().id;
        // Prepare for game
        chunkShader = new ChunkShader(ChunkShader.FRAG_MODE_CHUNK);

        setViewDistance(ClientWindow.settings, ClientWindow.settings.viewDistance.value);
        sortByDistance = new SortByDistanceToPlayer(Client.userPlayer.worldPosition);
        entities.clear();
    }

    // <editor-fold defaultstate="collapsed" desc="Chunk operations">
    public boolean hasChunk(final Vector3i coords) {
        return this.chunks.containsKey(coords);
    }

    public Chunk getChunk(final Vector3i coords) {
        return this.chunks.get(coords);
    }

    public Chunk addChunk(final Vector3i coords, boolean isTopLevel) {
        Chunk chunk = null;
        if (!unusedChunks.isEmpty()) {
            chunk = unusedChunks.remove(unusedChunks.size() - 1);
        } else if (chunks.size() < maxChunksForViewDistance) {
            chunk = new Chunk(blockTextureID, data, terrain);
        }
        if (chunk != null) {
            float distToPlayer = MathUtils.dist(
                    coords.x, coords.y, coords.z,
                    lastPlayerPosition.x, lastPlayerPosition.y, lastPlayerPosition.z);
            chunk.init(coords, futureChunks.remove(coords), distToPlayer, isTopLevel);
            this.chunks.put(coords, chunk);
            this.sortedChunksToRender.remove(chunk);
            needsSorting.set(true);
        }
        return chunk;
    }

    public void removeChunk(final Vector3i coords) {
        if (hasChunk(coords)) {
            Chunk chunk = this.chunks.remove(coords);
            entities.removeAllEntitiesFromChunk(chunk);
            chunk.save(data);
            unusedChunks.add(chunk);
        }
    }
    // </editor-fold>

    public void startGameEvent(WorldData info){
        players.clear();
        players.add(Client.userPlayer);
    }

    public boolean startGame(ProgressData prog, WorldData info, Vector3f playerPosition) {
        System.out.println("\n\nStarting new game: " + info.getName());
        prog.setTask("Starting new game");

        this.chunks.clear();
        this.unusedChunks.clear();
        this.futureChunks.clear(); // Important!

        newGameTasks.set(0);

        this.data = info;

        entities.clear();
        //Get the terrain from worldInfo
        this.terrain = game.getTerrainFromInfo(info);
        if (terrain == null) {
            ErrorHandler.report("Error", "Terrain " + info.getTerrain() + " not found");
            return false;
        } else System.out.println("Terrain: " + this.terrain);

        //Load pending blocks
        loadPendingMultiplayerChanges(prog);

        prog.setTask("Generating chunks");
        prog.bar.setMax(fillChunksAroundPlayer(playerPosition, true));
        return true;
    }

    private void loadPendingMultiplayerChanges(ProgressData prog) {
        prog.setTask("Applying multiplayer changes");

        multiplayerPendingBlockChanges.load(data);
        System.out.println("Block changes from other players: " + multiplayerPendingBlockChanges.blockChanges.size());
        multiplayerPendingEntityChanges.load(data);//TODO: Implement local entity multiplayer changes
        System.out.println("Entity changes from other players: " + 0);

        //Create lists of sorted changes
        HashMap<Vector2i, PendingPillarMultiplayerChanges> pillarSortedChanges = new HashMap<>();


        //Sort block changes
        for (Map.Entry<Vector3i, BlockHistory> entry : multiplayerPendingBlockChanges.blockChanges.entrySet()) {
            Vector3i worldPos = entry.getKey();
            BlockHistory blockHistory = entry.getValue();

            int chunkX = WCCi.chunkDiv(worldPos.x);
            int chunkZ = WCCi.chunkDiv(worldPos.z);
            Vector2i chunkCoords = new Vector2i(chunkX, chunkZ);
            if (!pillarSortedChanges.containsKey(chunkCoords)) {
                pillarSortedChanges.put(chunkCoords, new PendingPillarMultiplayerChanges());
            }
            pillarSortedChanges.get(chunkCoords).blockChanges.put(worldPos, blockHistory);
        }
        prog.bar.setMax(pillarSortedChanges.size());


        HashSet<Chunk> affectedChunks = new HashSet<>();
        ArrayList<ChunkNode> sunNode_OpaqueToTrans = new ArrayList<>();
        ArrayList<ChunkNode> sunNode_transToOpaque = new ArrayList<>();

        //Iterate over the sorted changes
        for (Map.Entry<Vector2i, PendingPillarMultiplayerChanges> entry : new HashMap<>(pillarSortedChanges).entrySet()) {
            Vector2i pillarCoords = entry.getKey();
            PendingPillarMultiplayerChanges pillarChanges = entry.getValue();
            System.out.println("\tPillar: (" + pillarCoords.x + ", " + pillarCoords.y + "); block changes: " + pillarChanges.blockChanges.size());
            prog.bar.setProgress(prog.bar.getIncrements() + 1);

            //Load the pillar and its neighbors
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    addChunkPillar(pillarCoords.x + x, pillarCoords.y + z, null);
                }
            }

            //Apply the block changes
            for (Map.Entry<Vector3i, BlockHistory> entry2 : pillarChanges.blockChanges.entrySet()) {
                Vector3i worldPos = entry2.getKey();
                BlockHistory blockHist = entry2.getValue();
                if (blockHist.previousBlock == null) { //Get the previous block if it doesn't exist
                    blockHist.previousBlock = Client.world.getBlock(worldPos.x, worldPos.y, worldPos.z);
                }
                int blockX = positiveMod(worldPos.x, Chunk.WIDTH);
                int blockY = positiveMod(worldPos.y, Chunk.WIDTH);
                int blockZ = positiveMod(worldPos.z, Chunk.WIDTH);
                Vector3i chunkPos = new Vector3i(pillarCoords.x, chunkDiv(worldPos.y), pillarCoords.y);

                //Set the block
                Chunk chunk = getChunk(chunkPos);
                chunk.data.setBlock(blockX, blockY, blockZ, blockHist.newBlock.id);
                if (blockHist.updateBlockData && blockHist.newBlockData != null) {
                    chunk.data.setBlockData(blockX, blockY, blockZ, blockHist.newBlockData);
                }

                affectedChunks.add(chunk);
                // <editor-fold defaultstate="collapsed" desc="update torchlight and add nodes for sunlight">
                if (blockHist.previousBlock.opaque && !blockHist.newBlock.opaque) {
                    SunlightUtils.addNodeForPropagation(sunNode_OpaqueToTrans, chunk, blockX, blockY, blockZ);
                    TorchUtils.opaqueToTransparent(affectedChunks, chunk, blockX, blockY, blockZ);
                } else if (!blockHist.previousBlock.opaque && blockHist.newBlock.opaque) {
                    SunlightUtils.addNodeForErasure(sunNode_transToOpaque, chunk, blockX, blockY, blockZ);
                    TorchUtils.transparentToOpaque(affectedChunks, chunk, blockX, blockY, blockZ);
                }

                if (!blockHist.previousBlock.isLuminous() && blockHist.newBlock.isLuminous()) {
                    TorchUtils.setTorch(affectedChunks, chunk, blockX, blockY, blockZ,
                            blockHist.newBlock.torchlightStartingValue);
                } else if (blockHist.previousBlock.isLuminous() && !blockHist.newBlock.isLuminous()) {
                    TorchUtils.removeTorch(affectedChunks, chunk, blockX, blockY, blockZ);
                }
                // </editor-fold>
            }

            //Update sunlight for the pillar
            SunlightUtils.updateFromQueue(
                    sunNode_OpaqueToTrans,
                    sunNode_transToOpaque,
                    affectedChunks,
                    (time) -> {
                        System.out.println("\t\tUpdating sunlight... " + time);
                    });
            for (Chunk chunk : affectedChunks) { //Mark the chunks as modified
                chunk.markAsModified();
            }
            affectedChunks.clear();
            sunNode_OpaqueToTrans.clear();
            sunNode_transToOpaque.clear();

            //Remove the records from the list
            pillarSortedChanges.remove(pillarCoords);

            //Save and remove any pillars that are no longer needed
            new HashMap<>(chunks).forEach((coords, chunk) -> {
                int chunksThatAreUnfinished = 0;
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (pillarSortedChanges.containsKey(new Vector2i(coords.x + x, coords.z + z))) {
                            chunksThatAreUnfinished++;
                        }
                    }
                }
                //If the chunk pillar and all its neighbors are finished, save and remove it
                if (chunksThatAreUnfinished == 0) {
                    removeChunk(coords);
                }
            });
            System.out.println("\tChunks: " + chunks.size() + "; unused chunks: " + unusedChunks.size());
        }

        prog.bar.setMax(multiplayerPendingBlockChanges.blockChanges.size());

        chunks.clear();
        unusedChunks.clear();
        System.gc();

        //Do this last, (If an error occurs, the code shouldnt be able to reach this point)
        multiplayerPendingEntityChanges.clear();
        multiplayerPendingBlockChanges.clear();
        multiplayerPendingEntityChanges.save(data);
        multiplayerPendingBlockChanges.save(data);
    }

    public void stopGameEvent() {
        save();

        // We may or may not actually need to shutdown the services, since chunks cancel
        // all tasks when they are disposed
        ExecutorServiceUtils.cancelAllTasks(generationService);
        ExecutorServiceUtils.cancelAllTasks(lightService);
        ExecutorServiceUtils.cancelAllTasks(meshService);
        ExecutorServiceUtils.cancelAllTasks(playerUpdating_meshService);

        chunks.forEach((coords, chunk) -> chunk.dispose());
        unusedChunks.forEach((chunk) -> {
            chunk.dispose();
        });

        entities.clear();
        chunks.clear();
        unusedChunks.clear();
        sortedChunksToRender.clear();
        chunksToUnload.clear();
        futureChunks.clear(); // Important!

        System.gc();
        data = null;
        terrain = null;
    }

    /*
     * ONE VERY IMPORTANT THING is to make sure that the chunks are deleted with the
     * same criteria that they were created with.
     * This is so that the chunks dont end up being deleted and than recreated over
     * and over again
     */
    private boolean chunkIsWithinRange_XZ(Vector3f player, Vector3i chunk, float viewDistance) {
        return MathUtils.dist(
                player.x,
                player.z,
                chunk.x * Chunk.WIDTH,
                chunk.z * Chunk.WIDTH) < viewDistance;
    }


    private boolean chunkIsWithinRange_XYZ(Vector3f player, Vector3i chunk, int viewDistance) {
        return MathUtils.dist(
                player.x,
                player.y,
                player.z,
                chunk.x * Chunk.WIDTH,
                chunk.y * Chunk.WIDTH,
                chunk.z * Chunk.WIDTH) < viewDistance;
    }


    public static final int CHUNK_QUANTITY_Y = 16;

    public int addChunkPillar(int chunkX, int chunkZ, Vector3f playerPos) {
        int chunksGenerated = 0;
        boolean isTopChunk = true;

        Chunk[] chunkPillar = new Chunk[PillarInformation.CHUNKS_IN_PILLAR];
        for (int y = TOP_Y_CHUNK; y <= BOTTOM_Y_CHUNK; ++y) {
            final Vector3i chunkCoords = new Vector3i(chunkX, y, chunkZ);
            boolean isWithinReach = playerPos == null || chunkIsWithinRange_XZ(playerPos, chunkCoords, getCreationViewDistance());

            if (!chunks.containsKey(chunkCoords) && isWithinReach) {
                chunkPillar[y - TOP_Y_CHUNK] = addChunk(chunkCoords, isTopChunk);
                isTopChunk = false;
                chunksGenerated++;
            } else {
                chunkPillar[y - TOP_Y_CHUNK] = getChunk(chunkCoords);
            }
        }
        for (Chunk chunk : chunkPillar) {
            chunk.pillarInformation = new PillarInformation(chunkPillar);
        }
        // chunkPillar[0].pillarInformation.loadChunks(terrain, info);

        return chunksGenerated;
    }

    public synchronized int fillChunksAroundPlayer(Vector3f player, boolean generateOutOfFrustum) {
        int centerX = (int) player.x;
        int centerY = (int) player.y;
        int centerZ = (int) player.z;

        int viewDistanceXZ = getCreationViewDistance();
        int viewDistanceY = getCreationViewDistance();

        final int xStart = (centerX - viewDistanceXZ) / Chunk.WIDTH;
        final int xEnd = (centerX + viewDistanceXZ) / Chunk.WIDTH;
        final int zStart = (centerZ - viewDistanceXZ) / Chunk.WIDTH;
        final int zEnd = (centerZ + viewDistanceXZ) / Chunk.WIDTH;
        // final int yStart = (centerY - viewDistanceY) / Chunk.WIDTH;
        // final int yEnd = (centerY + viewDistanceY) / Chunk.WIDTH;

        // Having fixed y bounds makes the chunk generation much faster
        int chunksGenerated = 0;

        for (int chunkX = xStart; chunkX < xEnd; ++chunkX) {
            for (int chunkZ = zStart; chunkZ < zEnd; ++chunkZ) {
                if (MathUtils.dist(
                        player.x,
                        player.z,
                        chunkX * Chunk.WIDTH,
                        chunkZ * Chunk.WIDTH) < viewDistanceXZ
                        && (generateOutOfFrustum
                        || Camera.frustum.isPillarChunkInside(chunkX, chunkZ, TOP_Y_CHUNK, BOTTOM_Y_CHUNK))) {
                    chunksGenerated += addChunkPillar(chunkX, chunkZ, player);
                }
            }
        }
        return chunksGenerated;
    }

    private final List<Chunk> chunksToUnload = new ArrayList<>();
    private long lastSaveMS;

    private void updateChunksToRenderList(Vector3f playerPosition) {
        chunksToUnload.clear();

        int removalViewDistance = getDeletionViewDistance();

        chunks.forEach((coords, chunk) -> {
            //This is used for 1) chunk task prioritization and 2) chunk ticking distance
            chunk.client_distToPlayer = MathUtils.dist(
                    coords.x * Chunk.WIDTH,
                    coords.y * Chunk.HEIGHT,
                    coords.z * Chunk.WIDTH,
                    playerPosition.x,
                    playerPosition.y,
                    playerPosition.z);

            if (!chunkIsWithinRange_XZ(playerPosition, coords, removalViewDistance)) {
                chunksToUnload.add(chunk);
                sortedChunksToRender.remove(chunk);
            } else {
                // frameTester.startProcess();
                if (needsSorting.get()) {
                    //Dont add chunk unless it is within the view distance
                    if (chunkIsWithinRange_XYZ(playerPosition, chunk.position, viewDistance.get() + Chunk.HALF_WIDTH)) {
                        sortedChunksToRender.add(chunk);
                    }
                }
                chunk.inFrustum = Camera.frustum.isChunkInside(chunk.position);
                // frameTester.endProcess("UCTRL: sorting and frustum check");
                chunk.prepare(terrain, ClientWindow.frameCount, false);
            }
        });
        chunksToUnload.forEach(chunk -> {
            removeChunk(chunk.position);
        });
        Client.frameTester.set("all chunks", unusedChunks.size() + chunks.size());
        Client.frameTester.set("in-use chunks", chunks.size());
        Client.frameTester.set("chunksToRender", sortedChunksToRender.size());
        Client.frameTester.set("unused chunks", unusedChunks.size());
        Client.frameTester.set("world entities", world.entities.size());
    }

    final Vector3f chunkShader_cursorMin = new Vector3f();
    final Vector3f chunkShader_cursorMax = new Vector3f();

    public void client_updateAndRenderChunks(Matrix4f projection, Matrix4f view, Vector3f playerPosition) {
        // <editor-fold defaultstate="collapsed" desc="chunk updating">
        if (!lastPlayerPosition.equals(playerPosition)) {
            needsSorting.set(true);
            lastPlayerPosition.set(playerPosition);
        }

        if (ClientWindow.frameCount % 10 == 0) {
            Client.frameTester.startProcess();
            world.fillChunksAroundPlayer(playerPosition, false);
            Client.frameTester.endProcess("Fill chunks around player");
        }

        /*
         * If the chunks need sorting, newGame the render list
         */
        if (needsSorting.get()) {
            sortedChunksToRender.clear();
        }

        if (System.currentTimeMillis() - lastSaveMS > 25000) {
            lastSaveMS = System.currentTimeMillis();
            // Save chunks
            generationService.submit(0.0f, () -> {
                save();
            });
        }

        updateChunksToRenderList(playerPosition);
        if (needsSorting.get()) {
            sortedChunksToRender.sort(sortByDistance);
            needsSorting.set(false);
        }
        // <editor-fold defaultstate="collapsed" desc="For testing sorted chunk distance
        // (KEEP THIS!)">
        // int i = 0; //For testing sorted chunk distance (KEEP THIS!)
        // for (Chunk chunk : sortedChunksToRender) {
        // if (chunk.getGenerationStatus() == Chunk.GEN_COMPLETE) {
        // chunk.updateMVP(projection, view); // we must update the MVP within each
        // model;
        // chunk.mvp.sendToShader(chunkShader.getID(), chunkShader.mvpUniform);
        // chunk.meshes.opaqueMesh.draw(true);
        // chunk.meshes.opaqueMesh.drawBoundingBoxWithWireframe();
        // i++;
        // if (i > 0) break;
        // }
        // }
        // </editor-fold>
        Client.frameTester.endProcess("Sort chunks if needed");
        // </editor-fold>

        /*
         * The basic layout for query occlusion culling is:
         *
         * 1. Create the query (or queries).
         * 2. Render loop:
         * a. Do AI / physics etc...
         * b. Rendering:
         * i. Check the query result from the previous frame.
         * ii. Issue query begin:
         * 1. If the object was visible in the last frame:
         * a. Enable rendering to screen.
         * b. Enable or disable writing to depth buffer (depends on whether the object
         * is translucent or opaque).
         * c. Render the object itself.
         * 2. If the object wasn't visible in the last frame:
         * a. Disable rendering to screen.
         * b. Disable writing to depth buffer.
         * c. "Render" the object's bounding box.
         * iii. (End query)
         * iv. (Repeat for every object in scene.)
         * c. Swap buffers.
         * (End of render loop)
         */


        if (userPlayer.camera.cursorRay.hitTarget()) {
            chunkShader_cursorMin.set(userPlayer.camera.cursorRay.getHitPos());
            chunkShader_cursorMax.set(chunkShader_cursorMin).add(1, 1, 1);

            List<AABB> cursorBoxes = userPlayer.camera.cursorRay.cursorRay.cursorBoxes;
            if (cursorBoxes != null && !cursorBoxes.isEmpty()) {
                chunkShader_cursorMin.set(cursorBoxes.get(0).min);
                chunkShader_cursorMax.set(cursorBoxes.get(0).max);
                for (AABB aabb : cursorBoxes) {
                    chunkShader_cursorMin.set(Math.min(chunkShader_cursorMin.x, aabb.min.x), Math.min(chunkShader_cursorMin.y, aabb.min.y), Math.min(chunkShader_cursorMin.z, aabb.min.z));
                    chunkShader_cursorMax.set(Math.max(chunkShader_cursorMax.x, aabb.max.x), Math.max(chunkShader_cursorMax.y, aabb.max.y), Math.max(chunkShader_cursorMax.z, aabb.max.z));
                }
            }
            chunkShader.setCursorPosition(chunkShader_cursorMin, chunkShader_cursorMax);
            chunkShader.setBlockBreakPercentage(userPlayer.camera.cursorRay.breakPercentage);
        } else {
            chunkShader.setBlockBreakPercentage(0);
            chunkShader_cursorMin.set(0, 0, 0);
            chunkShader.setCursorPosition(chunkShader_cursorMin, chunkShader_cursorMin);
        }

        // Render visible opaque meshes
        chunkShader.bind();
        chunkShader.tickAnimation();
        sortedChunksToRender.forEach(chunk -> {
            if (chunkIsVisible(chunk, playerPosition)) {
                chunk.updateMVP(projection, view); // we must update the MVP within each model;
                initShaderUniforms(chunk);
                chunk.meshes.opaqueMesh.getQueryResult();
                chunk.meshes.opaqueMesh.drawVisible(GameScene.drawWireframe);

                if (GameScene.drawBoundingBoxes) chunk.meshes.opaqueMesh.drawBoundingBoxWithWireframe();

            }
        });
        // Render invisible opaque meshes
        CompactOcclusionMesh.startInvisible();
        sortedChunksToRender.forEach(chunk -> {
            if (chunkIsVisible(chunk, playerPosition)) {
                // chunkShader.setChunkPosition(chunk.position);
                chunk.meshes.opaqueMesh.drawInvisible();
            }
        });
        CompactOcclusionMesh.endInvisible();

        //Draw entities
        //The entities must be drawn BEFORE the transparent meshes, otherwise they will not be visible over the transparent meshes
        ChunkEntitySet.startDraw(projection, view);
        sortedChunksToRender.forEach(chunk -> {
            if (chunkIsVisible(chunk, playerPosition)) {
                chunk.entities.draw(Camera.frustum, playerPosition);
            }
        });

        //Draw transparent meshes
        sortedChunksToRender.forEach(chunk -> {
            if (!chunk.meshes.transMesh.isEmpty() && chunkIsVisible(chunk, playerPosition)) {
                if (chunk.meshes.opaqueMesh.isVisibleSafe(2) || chunk.meshes.opaqueMesh.isEmpty()) {
                    initShaderUniforms(chunk);
                    chunk.meshes.transMesh.draw(GameScene.drawWireframe);
                }
            }
        });
    }

    private void initShaderUniforms(Chunk chunk) {
        chunk.mvp.sendToShader(chunkShader.getID(), chunkShader.mvpUniform);
        chunkShader.setChunkPosition(chunk.position);
    }

    private boolean chunkIsVisible(Chunk chunk, Vector3f playerPosition) {
        return chunk.inFrustum
                && chunk.getGenerationStatus() == Chunk.GEN_COMPLETE
                && chunkIsWithinRange_XYZ(playerPosition, chunk.position, getViewDistance());
    }

    // <editor-fold defaultstate="collapsed" desc="block operations">

    /**
     * does NOT notify other players and does not guarantee save of the chunk
     *
     * @param entity
     * @param w
     * @return the entity
     */
    public Entity placeEntity(EntitySupplier entity, Vector3i w, byte[] data) {
        WCCi wcc = new WCCi();
        wcc.set(w);
        Chunk chunk = Client.world.chunks.get(wcc.chunk);
        if (chunk != null) {
            Entity e = chunk.entities.placeNew(w, entity, data);
            e.sendMultiplayer = false;
            return e;
        }
        return null;
    }

    public short getBlockID(int worldX, int worldY, int worldZ) {
        int blockX = positiveMod(worldX, Chunk.WIDTH);
        int blockY = positiveMod(worldY, Chunk.WIDTH);
        int blockZ = positiveMod(worldZ, Chunk.WIDTH);

        int chunkX = chunkDiv(worldX);
        int chunkY = chunkDiv(worldY);
        int chunkZ = chunkDiv(worldZ);

        Chunk chunk = getChunk(new Vector3i(chunkX, chunkY, chunkZ));
        if (chunk == null) { // By default, all get block events should return air otherwise the chunk will
            // be null
            return 0;
        }
        return chunk.data.getBlock(blockX, blockY, blockZ);
    }

    public Chunk setBlockData(BlockData data, int worldX, int worldY, int worldZ) {
        int blockX = positiveMod(worldX, Chunk.WIDTH);
        int blockY = positiveMod(worldY, Chunk.WIDTH);
        int blockZ = positiveMod(worldZ, Chunk.WIDTH);

        int chunkX = chunkDiv(worldX);
        int chunkY = chunkDiv(worldY);
        int chunkZ = chunkDiv(worldZ);

        Vector3i pos = new Vector3i(chunkX, chunkY, chunkZ);
        Chunk chunk = getChunk(pos);
        if (chunk == null) {
            // FutureChunk futureChunk = newFutureChunk(pos);
        } else {
            chunk.data.setBlockData(blockX, blockY, blockZ, data);
        }
        return chunk;
    }

    public Chunk setBlock(short blockID, int worldX, int worldY, int worldZ) {
        int blockX = positiveMod(worldX, Chunk.WIDTH);
        int blockY = positiveMod(worldY, Chunk.WIDTH);
        int blockZ = positiveMod(worldZ, Chunk.WIDTH);

        int chunkX = chunkDiv(worldX);
        int chunkY = chunkDiv(worldY);
        int chunkZ = chunkDiv(worldZ);

        Vector3i pos = new Vector3i(chunkX, chunkY, chunkZ);
        Chunk chunk = getChunk(pos);
        if (chunk == null) { // We automatically set the block on a future chunk
            FutureChunk futureChunk = newFutureChunk(pos);
            futureChunk.addBlock(blockID, blockX, blockY, blockZ);
        } else {
            chunk.data.setBlock(blockX, blockY, blockZ, blockID);
        }
        return chunk;
    }

    public Chunk updateMesh(boolean updateAllNeighbors, boolean markAsModified,
                            int worldX, int worldY, int worldZ) {
        int blockX = positiveMod(worldX, Chunk.WIDTH);
        int blockY = positiveMod(worldY, Chunk.WIDTH);
        int blockZ = positiveMod(worldZ, Chunk.WIDTH);

        int chunkX = chunkDiv(worldX);
        int chunkY = chunkDiv(worldY);
        int chunkZ = chunkDiv(worldZ);
        Vector3i pos = new Vector3i(chunkX, chunkY, chunkZ);
        Chunk chunk = getChunk(pos);
        if (chunk != null) {
            if (markAsModified) chunk.markAsModified();
            chunk.updateMesh(updateAllNeighbors, blockX, blockY, blockZ);
        }
        return chunk;
    }

    public Chunk markAsModified(int worldX, int worldY, int worldZ) {
        int chunkX = chunkDiv(worldX);
        int chunkY = chunkDiv(worldY);
        int chunkZ = chunkDiv(worldZ);
        Vector3i pos = new Vector3i(chunkX, chunkY, chunkZ);
        Chunk chunk = getChunk(pos);
        if (chunk != null) chunk.markAsModified();
        return chunk;
    }

    public Block getBlock(int worldX, int worldY, int worldZ) {
        Block block = Registrys.getBlock(getBlockID(worldX, worldY, worldZ));
        return block == null ? BlockRegistry.BLOCK_AIR : block;
    }

    public Block getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    public BlockData getBlockData(int worldX, int worldY, int worldZ) {
        int blockX = positiveMod(worldX, Chunk.WIDTH);
        int blockY = positiveMod(worldY, Chunk.WIDTH);
        int blockZ = positiveMod(worldZ, Chunk.WIDTH);

        int chunkX = chunkDiv(worldX);
        int chunkY = chunkDiv(worldY);
        int chunkZ = chunkDiv(worldZ);

        Chunk chunk = getChunk(new Vector3i(chunkX, chunkY, chunkZ));
        if (chunk == null) {
            return null;
        }
        return chunk.data.getBlockData(blockX, blockY, blockZ);
    }
    // </editor-fold>

    public long getTimeSinceLastSave() {
        return System.currentTimeMillis() - lastSaveMS;
    }

    public void save() {
        Vector3f playerPos = userPlayer.worldPosition;
        ClientWindow.printlnDev("Saving world...");
        // Save all modified chunks
        Iterator<Chunk> iterator = chunks.values().iterator();
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            chunk.save(data);
        }

        //Save world info
        try {
            data.setSpawnPoint(playerPos);
            data.save();
        } catch (IOException ex) {
            ErrorHandler.report(ex);
        }

        //Save player info
        userPlayer.saveToWorld(data);

        //Save multiplayer pending block changes
        multiplayerPendingBlockChanges.save(data);
        multiplayerPendingEntityChanges.save(data);
    }

    public FutureChunk newFutureChunk(Vector3i pos) {
        FutureChunk futureChunk = futureChunks.get(pos);
        if (futureChunk == null) {
            futureChunk = new FutureChunk(pos);
            futureChunks.put(new Vector3i(pos), futureChunk);// We have to create a new vector, because chunk vector can
            // change when it is repurposed
        }
        return futureChunk;
    }

}
