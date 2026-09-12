package com.tessera.engine.common.world.chunk;

import com.tessera.Main;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.ChunkMeshBundle;
import com.tessera.engine.common.world.ClientWorld;
import org.joml.Matrix4f;
import org.joml.Vector3i;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.tessera.engine.common.world.ClientWorld.meshService;
import static com.tessera.engine.common.world.ClientWorld.playerUpdating_meshService;

public class ClientChunk extends Chunk {
    private volatile ChunkMeshBundle meshBundle;
    public final Matrix4f client_modelMatrix;
    private volatile Future<ChunkMeshBundle> mesherFuture;
    static int blockTextureID;


    //Generation state
    public static final int GEN_VOXELS_GENERATED = 1;
    public static final int GEN_MESH_GENERATED = 2;




    public ChunkMeshBundle getMeshBundle() {
        return meshBundle;
    }

    public float getDistToPlayer() {
        return Main.getClient().userPlayer.worldPosition.distance(position.x * Chunk.WIDTH, position.y * Chunk.HEIGHT, position.z * Chunk.WIDTH);
    }

    /**
     * @param position     the position of the chunk
     * @param futureChunk
     * @param world
     * @param blockTexture the block texture id
     */
    public ClientChunk(Vector3i position,
                       FutureChunk futureChunk, ClientWorld world,
                       int blockTexture) {
        super(position, futureChunk, world);
        blockTextureID = blockTexture;
        this.client_modelMatrix = new Matrix4f();
        this.client_modelMatrix.identity().setTranslation(position.x * WIDTH, position.y * HEIGHT, position.z * WIDTH);
    }

    /**
     * @param other          Another chunk that we can reuse parts of for saving memory
     * @param position
     * @param futureChunk
     * @param world
     * @param blockTextureID
     */
    public ClientChunk(Chunk other, Vector3i position,
                       FutureChunk futureChunk, ClientWorld world,
                       int blockTextureID) {
        super(other, position, futureChunk, world);
        this.client_modelMatrix = new Matrix4f();
        this.client_modelMatrix.identity().setTranslation(position.x * WIDTH, position.y * HEIGHT, position.z * WIDTH);
    }


    public void updateMVP(Matrix4f projection, Matrix4f view) {
        mvp.update(projection, view, client_modelMatrix);
    }

    public void invalidateMeshes() {
        this.meshBundle.reset(aabb);
    }

    /**
     * Runs every frame
     *
     * @param frame
     * @param isSettingUpWorld
     */
    public void prepare(long frame, boolean isSettingUpWorld) {
        //We have to initialize all OPENGL stuff in a place where they wont crash the game
        if (meshBundle == null) {//Initialize our mesh bundle
            if (otherChunk != null && otherChunk instanceof ClientChunk clientOther) {//If we have a chunk to reuse
                this.meshBundle = clientOther.getMeshBundle();
                meshBundle.reset(aabb);
            } else {
                this.meshBundle = new ChunkMeshBundle(blockTextureID, this, world.terrain);
                this.meshBundle.reset(aabb);
            }
        }

        //Update the mesh
        if (inFrustum || isSettingUpWorld) {//Are we visible?
            if (!meshBundle.hasBeenGenerated() && mesherFuture == null && getGenState() >= GEN_VOXELS_GENERATED) {  //Generate the mesh for the first time
                generateMesh(meshService);
            }

            Future<ChunkMeshBundle> future = mesherFuture;
            if (future != null && future.isDone()) { //Send mesh to GPU if its done
                try {
                    ChunkMeshBundle bundle = future.get();
                    if (bundle != null) {
                        entities.chunkUpdatedMesh = true;
                        bundle.sendToGPU();
                        progressGenState(GEN_MESH_GENERATED);
                    }
                } catch (CancellationException e) {
                    //A newer generation request superseded this one; the new
                    //mesh will be uploaded when its future completes.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    Main.LOGGER.warn("Failed to upload mesh for chunk " + position, e);
                } finally {
                    if (mesherFuture == future) {
                        mesherFuture = null;
                    }
                }
            }
        }
    }


    /**
     * Queues a task to mesh the chunk. Safe to call from any thread.
     * <p>
     * The bundle is only created on the render thread (in {@link #prepare(long, boolean)}),
     * so if it does not exist yet we simply skip: {@code prepare} will start generation
     * as soon as the bundle exists. Without this guard, a block update arriving on the
     * network thread for a not-yet-prepared chunk would NPE inside the worker.
     */
    public synchronized void generateMesh(ThreadPoolExecutor service) {
        ChunkMeshBundle bundle = this.meshBundle;
        if (bundle == null) {
            return;
        }
        if (mesherFuture != null) {
            mesherFuture.cancel(true);
            mesherFuture = null;
        }
        mesherFuture = service.submit(() -> {
            bundle.compute();
            return bundle;
        });
    }


    /**
     * Regenerate only this chunk's mesh. Used when a facing neighbor loads or
     * unloads so the faces on this chunk's border are rebuilt against the new
     * neighbor state.
     */
    public void remesh() {
        generateMesh(playerUpdating_meshService);
    }

    public void updateMesh(boolean updateAllNeighbors, int x, int y, int z) {
        if (!neghbors.allFacingNeghborsLoaded) {
            neghbors.cacheNeighbors();
        }
        ThreadPoolExecutor service = playerUpdating_meshService;

        generateMesh(service);
        if (neghbors.allFacingNeghborsLoaded) {
            if (x == 0 || updateAllNeighbors) {
                if (neghbors.neighbors[neghbors.NEG_X_NEIGHBOR] != null)
                    ((ClientChunk) neghbors.neighbors[neghbors.NEG_X_NEIGHBOR]).generateMesh(service);
            } else if (x == Chunk.WIDTH - 1 || updateAllNeighbors) {
                if (neghbors.neighbors[neghbors.POS_X_NEIGHBOR] != null)
                    ((ClientChunk) neghbors.neighbors[neghbors.POS_X_NEIGHBOR]).generateMesh(service);
            }

            if (y == 0 || updateAllNeighbors) {
                if (neghbors.neighbors[neghbors.NEG_Y_NEIGHBOR] != null)
                    ((ClientChunk) neghbors.neighbors[neghbors.NEG_Y_NEIGHBOR]).generateMesh(service);
            } else if (y == Chunk.WIDTH - 1 || updateAllNeighbors) {
                if (neghbors.neighbors[neghbors.POS_Y_NEIGHBOR] != null)
                    ((ClientChunk) neghbors.neighbors[neghbors.POS_Y_NEIGHBOR]).generateMesh(service);
            }

            if (z == 0 || updateAllNeighbors) {
                if (neghbors.neighbors[neghbors.NEG_Z_NEIGHBOR] != null)
                    ((ClientChunk) neghbors.neighbors[neghbors.NEG_Z_NEIGHBOR]).generateMesh(service);
            } else if (z == Chunk.WIDTH - 1 || updateAllNeighbors) {
                if (neghbors.neighbors[neghbors.POS_Z_NEIGHBOR] != null)
                    ((ClientChunk) neghbors.neighbors[neghbors.POS_Z_NEIGHBOR]).generateMesh(service);
            }
        }
    }

}
