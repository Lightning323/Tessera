package com.tessera.engine.common.world;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.ClientSettings;
import com.tessera.engine.client.visuals.gameScene.GameScene;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.ChunkShader;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.mesh.CompactOcclusionMesh;
import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.packets.ChunkRequestPacket;
import com.tessera.engine.common.players.localPlayer.camera.Camera;
import com.tessera.engine.common.progress.ProgressData;
import com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.ExecutorServiceUtils;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.chunk.ClientChunk;
import com.tessera.engine.common.world.chunk.FutureChunk;
import com.tessera.engine.server.block.BlockArrayTexture;
import com.tessera.engine.server.entity.ChunkEntitySet;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tessera.engine.client.Client.userPlayer;
import static com.tessera.engine.common.math.MathUtils.positiveMod;
import static com.tessera.engine.common.world.wcc.WCCi.chunkDiv;

/**
 * The world is the main class that manages the chunks and allEntities in the game.
 * It is the model for the game world and everything in it.
 */
public class ClientWorld extends World<ClientChunk> {

    public ChunkShader chunkShader;
    private final AtomicBoolean needsSorting = new AtomicBoolean(true);
    private final Vector3f lastPlayerPosition = new Vector3f();
    private SortByDistanceToPlayer sortByDistance;
    private final List<ClientChunk> sortedChunksToRender = Collections.synchronizedList(new ArrayList<>());
    public int blockTextureID;



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

    @Override
    protected ClientChunk internal_createChunkObject(Chunk recycleChunk, final Vector3i coords, FutureChunk futureChunk) {
        if (recycleChunk != null) return new ClientChunk(recycleChunk, coords, futureChunk, this, blockTextureID);
        else return new ClientChunk(coords, futureChunk, this, blockTextureID);
    }

    @Override
    public synchronized ClientChunk addChunk(final Vector3i coords) {
        ClientChunk chunk = super.addChunk(coords);
        if (chunk != null) {
            this.sortedChunksToRender.remove(chunk);
            needsSorting.set(true);
        }
        return chunk;
    }

    public void requestChunkFromServer(Vector3i coords, float distToPlayer) {
        Main.getClient().endpoint.getChannel().writeAndFlush(new ChunkRequestPacket(coords, distToPlayer));
        addChunk(coords);//This is important so we dont keep requesting chunks from the server
    }


    public ClientWorld() {
        super();
    }


    public static int VIEW_DIST_MIN = Chunk.WIDTH * 2;
    public static int VIEW_DIST_MAX = Chunk.WIDTH * 16; //Allowing higher view distances increases flexibility
    protected int maxChunksForViewDistance = 100;
    protected final AtomicInteger viewDistance = new AtomicInteger(VIEW_DIST_MIN);

    public void setViewDistance(ClientSettings settings, int viewDistance2) {
        viewDistance.set(MathUtils.clamp(viewDistance2, VIEW_DIST_MIN, VIEW_DIST_MAX));
        // Settings
        settings.internal_viewDistance.value = viewDistance.get();
        settings.save();
        maxChunksForViewDistance = Integer.MAX_VALUE;
        chunkShader.setViewDistance(viewDistance.get() - Chunk.WIDTH);
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


    public void initGL(BlockArrayTexture textures) {
        blockTextureID = textures.getTexture().id;
        chunkShader = new ChunkShader(ChunkShader.FRAG_MODE_CHUNK);
        setViewDistance(ClientWindow.settings, ClientWindow.settings.internal_viewDistance.value);
        sortByDistance = new SortByDistanceToPlayer(Client.userPlayer.worldPosition);
        allEntities.clear();
    }


    public boolean open(ProgressData prog, Vector3f spawnPosition) {
        prog.setTask("Generating chunks");
        prog.bar.setMax(fillChunksAroundPlayer(spawnPosition, true));
        return true;
    }

    public void close() {
        super.close();
        // We may or may not actually need to shut down the services, since chunks cancel
        // all tasks when they are disposed
        ExecutorServiceUtils.cancelAllTasks(meshService);
        ExecutorServiceUtils.cancelAllTasks(playerUpdating_meshService);
        sortedChunksToRender.clear();
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
        final int yStart = (centerY - viewDistanceY) / Chunk.WIDTH;
        final int yEnd = (centerY + viewDistanceY) / Chunk.WIDTH;

        int chunksRequested = 0;
        for (int chunkX = xStart; chunkX < xEnd; ++chunkX) {
            for (int chunkZ = zStart; chunkZ < zEnd; ++chunkZ) {
                for (int chunkY = yStart; chunkY < yEnd; ++chunkY) {

                    float distToPlayer = MathUtils.dist(player.x, player.y, player.z,
                            chunkX * Chunk.WIDTH, chunkY * Chunk.WIDTH, chunkZ * Chunk.WIDTH);

                    if (distToPlayer < viewDistanceXZ
                            && (generateOutOfFrustum || Camera.frustum.isChunkInside(chunkX, chunkY, chunkZ))) {
                        if (!hasChunk(new Vector3i(chunkX, chunkY, chunkZ))) {
                            requestChunkFromServer(new Vector3i(chunkX, chunkY, chunkZ), distToPlayer);
                            chunksRequested++;
                        }
                    }
                }
            }
        }
        return chunksRequested;
    }

    private final List<Chunk> chunksToUnload = new ArrayList<>();


    private void updateChunksToRenderList(Vector3f playerPosition) {
        chunksToUnload.clear();

        int removalViewDistance = getDeletionViewDistance();

        chunks.forEach((coords, chunk) -> {

            if (!chunkIsWithinRange_XYZ(playerPosition, coords, removalViewDistance)) {
                chunksToUnload.add(chunk);
                sortedChunksToRender.remove(chunk);
            } else {
                // frameTester.startProcess();
                if (needsSorting.get()) {
                    //Dont add chunk unless it is within the view distance
                    if (chunkIsWithinRange_XYZ(playerPosition, chunk.position, viewDistance.get() + Chunk.HALF_WIDTH)) {
                        assert chunk != null;
                        sortedChunksToRender.add(chunk);
                    }
                }
                chunk.inFrustum = Camera.frustum.isChunkInside(chunk.position.x, chunk.position.y, chunk.position.z);
                // frameTester.endProcess("UCTRL: sorting and frustum check");
                chunk.prepare(ClientWindow.frameCount, false);
            }
        });
        chunksToUnload.forEach(chunk -> {
            removeChunk(chunk.position);
        });
        Client.frameTester.set("all chunks", unusedChunks.size() + chunks.size());
        Client.frameTester.set("in-use chunks", chunks.size());
        Client.frameTester.set("chunksToRender", sortedChunksToRender.size());
        Client.frameTester.set("unused chunks", unusedChunks.size());
        Client.frameTester.set("world allEntities", allEntities.size());
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
            fillChunksAroundPlayer(playerPosition, false);
            Client.frameTester.endProcess("Fill chunks around player");
        }

        /*
         * If the chunks need sorting, newGame the render entities
         */
        if (needsSorting.get()) {
            sortedChunksToRender.clear();
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
                chunk.getMeshBundle().opaqueMesh.getQueryResult();
                chunk.getMeshBundle().opaqueMesh.drawVisible(GameScene.drawWireframe);

                if (GameScene.drawBoundingBoxes) chunk.getMeshBundle().opaqueMesh.drawBoundingBoxWithWireframe();

            }
        });
        // Render invisible opaque meshes
        CompactOcclusionMesh.startInvisible();
        sortedChunksToRender.forEach(chunk -> {
            if (chunkIsVisible(chunk, playerPosition)) {
                // chunkShader.setChunkPosition(chunk.position);
                chunk.getMeshBundle().opaqueMesh.drawInvisible();
            }
        });
        CompactOcclusionMesh.endInvisible();

        //Draw allEntities
        //The allEntities must be drawn BEFORE the transparent meshes, otherwise they will not be visible over the transparent meshes
        ChunkEntitySet.startDraw(projection, view);
        sortedChunksToRender.forEach(chunk -> {
            if (chunkIsVisible(chunk, playerPosition)) {
                chunk.entities.draw(Camera.frustum, playerPosition);
            }
        });

        //Draw transparent meshes
        sortedChunksToRender.forEach(chunk -> {
            if (!chunk.getMeshBundle().transMesh.isEmpty() && chunkIsVisible(chunk, playerPosition)) {
                if (chunk.getMeshBundle().opaqueMesh.isVisibleSafe(2) || chunk.getMeshBundle().opaqueMesh.isEmpty()) {
                    initShaderUniforms(chunk);
                    chunk.getMeshBundle().transMesh.draw(GameScene.drawWireframe);
                }
            }
        });


    }


    private void initShaderUniforms(Chunk chunk) {
        chunk.mvp.sendToShader(chunkShader.getID(), chunkShader.mvpUniform);
        chunkShader.setChunkPosition(chunk.position);
    }

    private boolean chunkIsVisible(ClientChunk chunk, Vector3f playerPosition) {
        return chunk.inFrustum
                && chunk.getMeshBundle().hasBeenGenerated()
                && chunkIsWithinRange_XYZ(playerPosition, chunk.position, getViewDistance());
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
        ClientChunk chunk = getChunk(pos);
        if (chunk != null) {
            if (markAsModified) chunk.markAsModified();
            chunk.updateMesh(updateAllNeighbors, blockX, blockY, blockZ);
        }
        return chunk;
    }


}
