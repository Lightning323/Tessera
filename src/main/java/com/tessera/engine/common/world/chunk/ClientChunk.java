package com.tessera.engine.common.world.chunk;

import com.tessera.Main;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.ChunkMeshBundle;
import com.tessera.engine.common.world.ClientWorld;
import org.joml.Matrix4f;
import org.joml.Vector3i;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.tessera.engine.common.world.ClientWorld.meshService;
import static com.tessera.engine.common.world.ClientWorld.playerUpdating_meshService;

public class ClientChunk extends Chunk {
    private ChunkMeshBundle meshBundle;
    public final Matrix4f client_modelMatrix;
    private Future<ChunkMeshBundle> mesherFuture;
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
            if (!getMeshBundle().hasBeenGenerated() && mesherFuture == null && getGenState() >= GEN_VOXELS_GENERATED) {  //Generate the mesh for the first time
                generateMesh(meshService);
            }

            if (mesherFuture != null && mesherFuture.isDone()) { //Send mesh to GPU if its done
                try {
                    entities.chunkUpdatedMesh = true;
                    mesherFuture.get().sendToGPU();
                    progressGenState(GEN_MESH_GENERATED);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mesherFuture = null;
                }
            }
        }
    }


    /**
     * Queues a task to mesh the chunk
     */
    public void generateMesh(ThreadPoolExecutor service) {
        if (mesherFuture != null) {
            mesherFuture.cancel(true);
            mesherFuture = null;
        }
        mesherFuture = service.submit(() -> {
            getMeshBundle().compute();
            return getMeshBundle();
        });
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
