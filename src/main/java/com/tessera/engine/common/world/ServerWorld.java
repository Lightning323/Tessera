package com.tessera.engine.common.world;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.packets.BlockUpdatePacket;
import com.tessera.engine.common.packets.ChunkDataPacket;
import com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.ExecutorServiceUtils;
import com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.PriorityThreadPoolExecutor;
import com.tessera.engine.common.threadPoolExecutor.PriorityExecutor.comparator.LowValueComparator;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.chunk.FutureChunk;
import com.tessera.engine.common.world.chunk.ServerChunk;
import com.tessera.engine.common.world.gen.GenContext;
import org.joml.Vector3i;

import java.util.List;

public class ServerWorld extends World<ServerChunk> {

    /**
     * For a local server, we just want to share unused chunks for memory manegment
     */
    public ServerWorld(ClientWorld otherWorld) {
        this.setData(new WorldData(otherWorld.getData())); //Everything except for the chunks is its own instance
    }

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

    public static final PriorityThreadPoolExecutor lightService = new PriorityThreadPoolExecutor(CHUNK_LIGHT_THREADS,
            r -> {
                Thread thread = new Thread(r, "Light Thread");
                thread.setDaemon(true);
                return thread;
            }, new LowValueComparator());


    @Override
    protected ServerChunk internal_createChunkObject(Chunk recycleChunk, final Vector3i coords, FutureChunk futureChunk) {
        if (recycleChunk != null) return new ServerChunk(recycleChunk, coords, futureChunk, this);
        else return new ServerChunk(coords, futureChunk, this);
    }

    public ServerChunk addChunk(final Vector3i coords) {
        ServerChunk chunk = super.addChunk(coords);
        return chunk;
    }

    public void generateChunk(ServerChunk chunk, float distToPlayer) {
        generateChunk(chunk, distToPlayer, null);
    }

    /**
     * Generates a chunk (base terrain, staged spillover, decorations, light)
     * and sends it back. When {@code target} is the requesting channel we
     * unicast, avoiding the old behavior of spamming every chunk to every
     * player. Null falls back to broadcast (e.g. initial terrain that
     * late-joiners also need via their own requests).
     *
     * <p>Decorations that spill into already-sent neighbor chunks are
     * re-broadcast as block updates so clients converge instead of showing
     * trees cut off at the border.
     */
    public void generateChunk(ServerChunk chunk, float distToPlayer, ChannelBase target) {
        if (chunk == null) {
            return;
        }
        // Skip re-generation if already done; just (re)send to requester.
        if (chunk.getGenState() >= ServerChunk.GEN_SUN_GENERATED) {
            sendChunk(chunk, target);
            return;
        }
        if (chunk.loadFuture != null && !chunk.loadFuture.isDone()) {
            return;
        }
        chunk.loadFuture = generationService.submit(distToPlayer, () -> {
            try {
                GenContext ctx = chunk.generateTerrain();
                chunk.generateLight();
                sendChunk(chunk, target);
                chunk.markSentToClients();
                broadcastSpillover(ctx.drainSpilloverUpdates());
                return false;
            } finally {
                newGameTasks.incrementAndGet();
            }
        });
    }

    private void sendChunk(ServerChunk chunk, ChannelBase target) {
        ChunkDataPacket packet = new ChunkDataPacket(chunk);
        if (target != null && target.isActive()) {
            target.writeAndFlush(packet);
        } else if (Main.getServer() != null) {
            Main.getServer().writeAndFlushToAllPlayers(packet);
        }
    }

    private void broadcastSpillover(List<BlockUpdatePacket> updates) {
        if (updates.isEmpty() || Main.getServer() == null) return;
        for (BlockUpdatePacket update : updates) {
            Main.getServer().writeAndFlushToAllPlayers(update);
        }
    }

    public void close() {
        super.close();
        ExecutorServiceUtils.cancelAllTasks(generationService);
        ExecutorServiceUtils.cancelAllTasks(lightService);
    }

}
