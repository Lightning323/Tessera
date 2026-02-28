package com.xbuilders.engine.common.world;

import com.xbuilders.engine.common.math.MathUtils;
import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.common.world.chunk.Chunk;
import com.xbuilders.engine.common.world.chunk.FutureChunk;
import com.xbuilders.engine.common.world.wcc.WCCi;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.server.block.BlockRegistry;
import com.xbuilders.engine.server.entity.Entity;
import com.xbuilders.engine.server.entity.EntitySupplier;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xbuilders.Main.LOGGER;
import static com.xbuilders.Main.game;
import static com.xbuilders.engine.common.math.MathUtils.positiveMod;
import static com.xbuilders.engine.common.world.wcc.WCCi.chunkDiv;

/*
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
public abstract class World<T extends Chunk> {
    public static final int CHUNK_LOAD_THREADS = 12; //Redicing the number of threads helps performance
    public static final int CHUNK_LIGHT_THREADS = 1;
    public static final int CHUNK_MESH_THREADS = 1;
    public static final int PLAYER_CHUNK_MESH_THREADS = 3; //The number of threads allocated to player based chunk updating
    public final static AtomicInteger newGameTasks = new AtomicInteger(0);


    // voxel boundaries
    public static final int WORLD_SIZE_NEG_X = -32000; // -X
    public static final int WORLD_TOP_Y = -32000; // up (-Y)
    public static final int WORLD_SIZE_NEG_Z = -32000; // -Z

    public static final int WORLD_SIZE_POS_X = 32000; // +X
    public static final int WORLD_BOTTOM_Y = 32000; // down (+Y)
    public static final int WORLD_SIZE_POS_Z = 32000; // +Z

    public boolean inYBounds(int y) {
        return y > WORLD_TOP_Y && y < WORLD_BOTTOM_Y - 1;
    }

    public boolean inBounds(int ix, int iy, int iz) {
        return ix >= WORLD_SIZE_NEG_X && ix < WORLD_SIZE_POS_X
                && iz >= WORLD_SIZE_NEG_Z && iz < WORLD_SIZE_POS_Z
                && inYBounds(iy);
    }

    //Model properties
    public final WorldEntityMap allEntities = new WorldEntityMap(); // <chunkPos, entity>
    private WorldData data;
    public Terrain terrain;

    public static final List<Chunk> unusedChunks = Collections.synchronizedList(new ArrayList<>());
    protected final Map<Vector3i, FutureChunk> futureChunks = new HashMap<>();
    public final Map<Vector3i, T> chunks = new ConcurrentHashMap<>();


    public boolean hasChunk(final Vector3i coords) {
        return this.chunks.containsKey(coords);
    }

    public T getChunk(final Vector3i coords) {
        return this.chunks.get(coords);
    }

    protected abstract T internal_createChunkObject(Chunk recycleChunk, final Vector3i coords, FutureChunk futureChunk);

    public T addChunk(final Vector3i coords) {
        T chunk = getChunk(coords);  //Return an existing chunk if it exists
        if (chunk != null) return chunk;

        Chunk unusedChunk = null;
//        synchronized (unusedChunks) {
//            unusedChunk = unusedChunks.isEmpty() ? null : unusedChunks.remove(unusedChunks.size() - 1);
//        }
        chunk = internal_createChunkObject(unusedChunk, coords, futureChunks.remove(coords));

        assert chunk != null;//The chunk cannot be null when created

        this.chunks.put(coords, chunk);
        return chunk;
    }

    public void removeChunk(final Vector3i coords) {
        if (hasChunk(coords)) {
            Chunk chunk = this.chunks.remove(coords);
            allEntities.removeAllEntitiesFromChunk(chunk);
            chunk.dispose();
            unusedChunks.add(chunk);
        }
    }
    // </editor-fold>


    public void close() {
        chunks.forEach((coords, chunk) -> chunk.dispose());
        unusedChunks.forEach((chunk) -> {
            chunk.dispose();
        });

        allEntities.clear();
        chunks.clear();
        unusedChunks.clear();
        futureChunks.clear(); // Important!

        System.gc();
        setData(null);
        terrain = null;
    }

    protected boolean chunkIsWithinRange_XYZ(Vector3f player, Vector3i chunk, int viewDistance) {
        return MathUtils.dist(
                player.x,
                player.y,
                player.z,
                chunk.x * Chunk.WIDTH,
                chunk.y * Chunk.WIDTH,
                chunk.z * Chunk.WIDTH) < viewDistance;
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
        Chunk chunk = chunks.get(wcc.chunk);
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
        return chunk.voxels.getBlock(blockX, blockY, blockZ);
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
            chunk.voxels.setBlockData(blockX, blockY, blockZ, data);
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
            chunk.voxels.setBlock(blockX, blockY, blockZ, blockID);
        }
        return chunk;
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
        return chunk.voxels.getBlockData(blockX, blockY, blockZ);
    }
    // </editor-fold>


    public Chunk markAsModified(int worldX, int worldY, int worldZ) {
        int chunkX = chunkDiv(worldX);
        int chunkY = chunkDiv(worldY);
        int chunkZ = chunkDiv(worldZ);
        Vector3i pos = new Vector3i(chunkX, chunkY, chunkZ);
        Chunk chunk = getChunk(pos);
        if (chunk != null) chunk.markAsModified();
        return chunk;
    }


    public long getTimeSinceLastSave() {
        return System.currentTimeMillis() - lastSaveMS;
    }

    protected long lastSaveMS;

    public void save() {
//        ClientWindow.printlnDev("Saving world...");
//        // Save all modified chunks
//        Iterator<T> iterator = chunks.values().iterator();
//        while (iterator.hasNext()) {
//            Chunk chunk = iterator.next();
//            chunk.save(getData());
//        }
//
//        //Save world info
//        try {
//            getData().save();
//        } catch (IOException ex) {
//            LOGGER.info( "World \"" + getData().getName() + "\" could not be saved", ex);
//        }
    }

    public WorldData getData() {
        return data;
    }

    public void setData(WorldData data) {
        this.chunks.clear();
        unusedChunks.clear();
        this.futureChunks.clear(); // Important!
        newGameTasks.set(0);
        allEntities.clear();

        if (data == null) return;
        this.data = data;
        this.terrain = game.getTerrainFromInfo(data);
        if (terrain == null) {
            LOGGER.error("Terrain not found");
        } else System.out.println("Terrain: " + this.terrain);
    }
}
