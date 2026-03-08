package com.tessera.engine.common.world.chunk;

import com.tessera.engine.common.world.World;
import org.joml.Vector3i;

public class NeighborInformation {

    World world;

    public NeighborInformation(World world) {
        this.world = world;
        for (int i = 0; i < NEIGHBOR_VECTORS.length; i++) {
            neighborChunkPositions[i] = new Vector3i();
        }
    }

    Vector3i thisChunkCoordinates = new Vector3i();

    public void init(Vector3i position) {
        thisChunkCoordinates = position;
        allFacingNeghborsLoaded = false;
        allNeghborsLoaded = false;
        allChunksCreated = false;
        for (int i = 0; i < NEIGHBOR_VECTORS.length; i++) {
            neighborChunkPositions[i].set(
                    position.x + NEIGHBOR_VECTORS[i].x,
                    position.y + NEIGHBOR_VECTORS[i].y,
                    position.z + NEIGHBOR_VECTORS[i].z);
            neighbors[i] = null;
        }
    }

    public final static int NEG_X_NEIGHBOR = 0;
    public final static int POS_X_NEIGHBOR = 1;
    public final static int NEG_Z_NEIGHBOR = 2;
    public final static int POS_Z_NEIGHBOR = 3;
    public final static int POS_Y_NEIGHBOR = 4;
    public final static int NEG_Y_NEIGHBOR = 5;

    protected static final Vector3i[] NEIGHBOR_VECTORS_2D = {//size: 8
            //Facing sides
            new Vector3i(-1, 0, 0),
            new Vector3i(1, 0, 0),
            new Vector3i(0, 0, -1),
            new Vector3i(0, 0, 1),
            //Non facing sides
            new Vector3i(-1, 0, -1),
            new Vector3i(-1, 0, 1),
            new Vector3i(1, 0, -1),
            new Vector3i(1, 0, 1),
    };

    protected static final Vector3i[] NEIGHBOR_VECTORS = {//size: 26
            //XZ Facing sides
            new Vector3i(-1, 0, 0),
            new Vector3i(1, 0, 0),
            new Vector3i(0, 0, -1),
            new Vector3i(0, 0, 1),
            //y facing sides
            new Vector3i(0, 1, 0),
            new Vector3i(0, -1, 0),
            //Non facing sides
            new Vector3i(-1, -1, -1),
            new Vector3i(-1, -1, 0),
            new Vector3i(-1, -1, 1),
            new Vector3i(-1, 0, -1),
            new Vector3i(-1, 0, 1),
            new Vector3i(-1, 1, -1),
            new Vector3i(-1, 1, 0),
            new Vector3i(-1, 1, 1),
            new Vector3i(0, -1, -1),
            new Vector3i(0, -1, 1),
            new Vector3i(0, 1, -1),
            new Vector3i(0, 1, 1),
            new Vector3i(1, -1, -1),
            new Vector3i(1, -1, 0),
            new Vector3i(1, -1, 1),
            new Vector3i(1, 0, -1),
            new Vector3i(1, 0, 1),
            new Vector3i(1, 1, -1),
            new Vector3i(1, 1, 0),
            new Vector3i(1, 1, 1)
    };
    public final Chunk neighbors[] = new Chunk[NEIGHBOR_VECTORS.length];
    public final Vector3i[] neighborChunkPositions = new Vector3i[NEIGHBOR_VECTORS.length];
    public boolean allFacingNeghborsLoaded, XYFacingNeghborsLoaded, allNeghborsLoaded;
    boolean allChunksCreated;

    /**
     * This process takes a fair amount of time.
     */
    public void cacheNeighbors() {
        boolean facingchunksLoaded2 = true;
        boolean XYFacingNeghborsLoaded2 = true;
        boolean allNeghborsLoaded2 = true;
        if (allChunksCreated) {
            for (int i = 0; i < NEIGHBOR_VECTORS.length; i++) {
                Chunk chunk = neighbors[i];
                if (chunk != null && chunkMeshGenerated(chunk)) {
                    allNeghborsLoaded2 = false;
                    if (i < 6) {
                        facingchunksLoaded2 = false;
                    }
                    if (i < 4) {
                        XYFacingNeghborsLoaded2 = false;
                    }
                }
            }
        } else {
            boolean allChunksCreated2 = true;
            for (int i = 0; i < NEIGHBOR_VECTORS.length; i++) {
                Chunk chunk = world.getChunk(neighborChunkPositions[i]);
                if (chunk == null) {
                    allChunksCreated2 = false;
                    allNeghborsLoaded2 = false;
                    if (i < 6) {
                        facingchunksLoaded2 = false;
                    }
                    if (i < 4) {
                        XYFacingNeghborsLoaded2 = false;
                    }
                } else {
                    if (chunkMeshGenerated(chunk)) {
                        allNeghborsLoaded2 = false;
                        if (i < 6) {
                            facingchunksLoaded2 = false;
                        }
                        if (i < 4) {
                            XYFacingNeghborsLoaded2 = false;
                        }
                    }
                    neighbors[i] = chunk;
                }
            }
            allChunksCreated = allChunksCreated2;
        }
        allFacingNeghborsLoaded = facingchunksLoaded2;
        XYFacingNeghborsLoaded = XYFacingNeghborsLoaded2;
        allNeghborsLoaded = allNeghborsLoaded2;
    }

    private boolean chunkMeshGenerated(Chunk chunk) {
        return false;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NEIGHBOR_VECTORS.length; i++) {
            Chunk c = neighbors[i];
            sb.append(c == null ? "null" : (getGenerationStatus(c)));
            if (i < NEIGHBOR_VECTORS.length - 1) sb.append(", ");
        }
        sb.append(" all-exist: " + allChunksCreated + " all-loaded: " + allNeghborsLoaded + ", all-facing: " + allFacingNeghborsLoaded);
        return sb.toString();
    }

    private String getGenerationStatus(Chunk c) {
        return c.getGenState()+"";
    }
}
