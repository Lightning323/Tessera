/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.block.meshers.BlockMesher;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet.CompactVertexSet;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.chunk.ChunkVoxels;
import com.tessera.engine.common.world.chunk.Chunk;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * @author zipCoder933
 */
public class GreedyMesher extends BlockMesher<CompactVertexSet> {
    private static final int NEG_X = 0;
    private static final int POS_X = 1;

    private static final int NEG_Z = 2;
    private static final int POS_Z = 3;

    private static final int POS_Y = 4;
    private static final int NEG_Y = 5;

    int[] dims;

    public GreedyMesher(ChunkVoxels voxels, Vector3i chunkPositionOffset) {
        super(voxels, chunkPositionOffset);
        this.dims = new int[]{voxels.size.x, voxels.size.y, voxels.size.z};
    }

    /**
     * Check if 2 voxel faces are the same
     */
    private boolean equals(int val1, int val2) {
        return val1 == val2;
    }

    @Override
    public void compute(CompactVertexSet opaqueBuffers, CompactVertexSet transparentBuffers,
                        MemoryStack stack,
                        int lodLevel, boolean smoothShading) {

        /**
         * These are just working variables for the algorithm - almost all taken
         * directly from Mikola Lysenko's javascript implementation.
         */
        int i, j, k, l, u, v, n, side = 0;

        final int[] x = new int[]{0, 0, 0};
        final int[] q = new int[]{0, 0, 0};
        final int[] du = new int[]{0, 0, 0};
        final int[] dv = new int[]{0, 0, 0};
        final IntBuffer quadSize = stack.mallocInt(2);

        /**
         * These are just working variables to hold two faces during comparison.
         */
        Vector3i voxelPos = new Vector3i(stack.mallocInt(3));

        ShortBuffer thisPlaneVoxel = stack.mallocShort(1);
        ShortBuffer nextPlaneVoxel = stack.mallocShort(1);
        byte sun, sun1;
        Block block, block1;

        for (boolean backFace = true, b = false; b != backFace; backFace = backFace && b, b = !b) {
            for (int d = 0; d < 3; d++) {

                u = (d + 1) % 3;//v and u are the perpendicular dimensions to d
                v = (d + 2) % 3;

                x[0] = 0;//X is the position vector, X[d] advances forward in the given dimension
                x[1] = 0;
                x[2] = 0;

                q[0] = 0;//Q is the dimension vector, for example, if d=1, q = [0,1,0]
                q[1] = 0;
                q[2] = 0;
                q[d] = 1;

                Chunk forwardChunk = Client.world.getChunk(new Vector3i(chunkPosition.x + q[0], chunkPosition.y + q[1], chunkPosition.z + q[2]));
                Chunk backChunk = Client.world.getChunk(new Vector3i(chunkPosition.x - q[0], chunkPosition.y - q[1], chunkPosition.z - q[2]));

                if (d == 0) {
                    side = backFace ? NEG_X : POS_X;
                } else if (d == 1) {
                    side = backFace ? NEG_Y : POS_Y;
                } else if (d == 2) {
                    side = backFace ? NEG_Z : POS_Z;
                }

                //We create the mask here (We MUST use it this way IF the chunk dimensions are not cubic)
                final ShortBuffer mask = stack.mallocShort(dims[u] * dims[v]);

                /*
                 * We move through the d from front to back
                 */
                int min = 0;//-1 (We changed this to 0, so chunks dont overlap. The dim starts at 0 and ends up covering the next chunks faces)
                int max = dims[d];

                //If the chunk is not here, dont draw the side that it is on
                if (forwardChunk == null) {
                    max = dims[d] - 1;
                }
                if (backChunk == null) {
                    min = 0;
                }

                for (x[d] = min; x[d] < max; ) {
                    /**
                     * -------------------------------------------------------------------
                     * We compute the mask
                     * -------------------------------------------------------------------
                     */
                    n = 0;

                    for (x[v] = 0; x[v] < dims[v]; x[v]++) {//v and u are the perpendicular dimensions to d, we sweep across the plane of V and U
                        for (x[u] = 0; x[u] < dims[u]; x[u]++) {
                            retrieveMaskVoxels(x, q, d, backChunk, forwardChunk, voxelPos,
                                    thisPlaneVoxel, nextPlaneVoxel, lodLevel);
                            block = Registrys.getBlock(thisPlaneVoxel.get(0));
                            block1 = Registrys.getBlock(nextPlaneVoxel.get(0));

                            if (block.isAir() || block.type != BlockRegistry.DEFAULT_BLOCK_TYPE_ID) {
                                thisPlaneVoxel.put(0, (short) 0);
                            }
                            if (block1.isAir() || block1.type != BlockRegistry.DEFAULT_BLOCK_TYPE_ID) {
                                nextPlaneVoxel.put(0, (short) 0);
                            }

                            short maskValue = (thisPlaneVoxel.get(0) == 0 || nextPlaneVoxel.get(0) == 0) || (block.opaque != block1.opaque)
                                    //The opaque check is to prevent transparent mesh from overriding opaque one
                                    ? (backFace //add the voxel for either this plane or the next plane depending on our direction
                                    ? nextPlaneVoxel.get(0) : thisPlaneVoxel.get(0)) : 0;
                            mask.put(n++, maskValue);
                        }
                    }

                    x[d]++; //move forward

                    /*
                     * Now we generate the mesh for the mask
                     */
                    n = 0;

                    for (j = 0; j < dims[v]; j++) {
                        for (i = 0; i < dims[u]; ) {
                            if (mask.get(n) != 0) {

                                /*
                                 * Add quad
                                 */
                                x[u] = i;
                                x[v] = j;
                                du[0] = 0;
                                du[1] = 0;
                                du[2] = 0;
                                dv[0] = 0;
                                dv[1] = 0;
                                dv[2] = 0;

                                //<editor-fold defaultstate="collapsed" desc="make the quad">
                                //Compute the quad width
                                for (quadSize.put(0, 1);
                                     i + quadSize.get(0) < dims[u] && mask.get(n
                                             + quadSize.get(0)) != 0 && equals(mask.get(n + quadSize.get(0)),
                                             mask.get(n)); quadSize.put(0, quadSize.get(0) + 1))
                                    ;
                                {
                                }
                                //compute the quad height
                                boolean done = false;
                                for (quadSize.put(1, 1); j + quadSize.get(1) < dims[v]; quadSize.put(1, quadSize.get(1) + 1)) {
                                    for (k = 0; k < quadSize.get(0); k++) {
                                        if (mask.get(n + k + quadSize.get(1) * dims[u]) == 0
                                                || mask.get(n + k + quadSize.get(1) * dims[u]) != mask.get(n)) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (done) {
                                        break;
                                    }
                                }
                                du[u] = quadSize.get(0);
                                dv[v] = quadSize.get(1);

                                /*
                                 * And here we call the quad function in order to render a merged quad in the scene.
                                 *
                                 * We pass mask[n] to the function, which is an instance of the VoxelFace class containing
                                 * all the attributes of the face - which allows for variables to be passed to shaders - for
                                 * example lighting values used to create ambient occlusion.
                                 */
                                Mesher_makeQuad(opaqueBuffers, transparentBuffers, x, du, dv, quadSize.get(0), quadSize.get(1),
                                        mask.get(n), backFace, d, side, stack);
                                //</editor-fold>

                                /*
                                 * We zero out the mask
                                 * This is important as we don't want to render the same voxel twice
                                 */
                                for (l = 0; l < quadSize.get(1); ++l) {
                                    for (k = 0; k < quadSize.get(0); ++k) {
                                        mask.put(n + k + l * dims[u], (short) 0);
                                    }
                                }
                                /*
                                 * And then finally increment the counters and continue
                                 */
                                i += quadSize.get(0);
                                n += quadSize.get(0);
                            } else {
                                i++;
                                n++;
                            }
                        }
                    }
                }
            }
        }
    }

    private int getMaskValue(IntBuffer mask, int index, int lodLevel) {
        return mask.get((index / lodLevel) * lodLevel);
    }

    private short getBlockLOD(ChunkVoxels data, int x, int y, int z, int lodLevel) {
        if (lodLevel > 1) {//might be redundant
            //We want to make the coordinates align with the LOD level,
            //for example, if lod level is 2, the X coordinate would be as follows:
            //Real X: 0,1,2,3,4,5,6,7,8,9,10
            //LOD X:  0,0,2,2,4,4,6,6,8,8,10
            //x = (x / lodLevel) * 2;
            x = (x / lodLevel) * lodLevel;
            y = (y / lodLevel) * lodLevel;
            z = (z / lodLevel) * lodLevel;
        }

        return data.getBlock(x, y, z);
    }

    private void retrieveMaskVoxels(int[] x, int[] q, int d, Chunk backChunk, Chunk forwardChunk,
                                    Vector3i voxelPos, ShortBuffer thisPlaneVoxel, ShortBuffer nextPlaneVoxel, int lodLevel) {
        //Here we retrieve two voxel faces for comparison.
        //thisPlaneVoxel literaly faces forward, while nextPlaneVoxel faces backward
        if (x[d] >= 0) { //Calculate the voxel of THIS plane
            thisPlaneVoxel.put(0, getBlockLOD(data, x[0], x[1], x[2], lodLevel));
        } else {//If we are out of bounds for this chunk:
            if (backChunk == null) {
                thisPlaneVoxel.put(0, (short) 0);
            } else {
                voxelPos.set(MathUtils.positiveMod(x[0], Chunk.WIDTH), MathUtils.positiveMod(x[1], Chunk.WIDTH), MathUtils.positiveMod(x[2], Chunk.WIDTH));
                thisPlaneVoxel.put(0, getBlockLOD(backChunk.voxels, voxelPos.x, voxelPos.y, voxelPos.z, lodLevel));
            }
        }
        if (x[d] < dims[d] - 1) { //calculate the voxel of the NEXT plane
            voxelPos.set(x[0] + q[0], x[1] + q[1], x[2] + q[2]);
            nextPlaneVoxel.put(0, getBlockLOD(data, voxelPos.x, voxelPos.y, voxelPos.z, lodLevel));
        } else {//If we are out of bounds for this chunk:
            if (forwardChunk == null) {
                nextPlaneVoxel.put(0, (short) 0);
            } else {
                voxelPos.set(MathUtils.positiveMod(x[0] + q[0], Chunk.WIDTH), MathUtils.positiveMod(x[1] + q[1], Chunk.WIDTH), MathUtils.positiveMod(x[2] + q[2], Chunk.WIDTH));
                nextPlaneVoxel.put(0, getBlockLOD(forwardChunk.voxels, voxelPos.x, voxelPos.y, voxelPos.z, lodLevel));
            }
        }
    }

    final int[] indexes1 = {2, 0, 1, 1, 3, 2};
    final int[] indexes2 = {2, 3, 1, 1, 0, 2};

    final Vector3f[] vertices = {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
    final Vector2f[] uvs = {new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f()};
    final Vector3i[] completeVertex = {new Vector3i(), new Vector3i(), new Vector3i(), new Vector3i()};

    //TODO: Determine why nublada is 2ms faster than this. Hint: its not mesher_makeQuad()
    // d: 0=X,1=Y,2=Z
    protected void Mesher_makeQuad(CompactVertexSet buffers,
                                   CompactVertexSet transBuffers,
                                   int x[], int du[], int dv[], final int w, final int h,
                                   final short blockVal, final boolean backFace, final int d, final int side, MemoryStack stack) {

//        short blockVal = (short) ((voxel >> 8) & 0xFFFF);
//        byte sun = (byte) (voxel & 0xFF);
        Block block = Registrys.getBlock(blockVal);

        if (block != null && block.texture != null) {
            int[] indexes = backFace ? indexes1 : indexes2;
            BlockTexture.FaceTexture texture;

            vertices[0].set(x[0], x[1], x[2]);
            vertices[1].set(x[0] + dv[0], x[1] + dv[1], x[2] + dv[2]);
            vertices[2].set(x[0] + du[0], x[1] + du[1], x[2] + du[2]);
            vertices[3].set(x[0] + du[0] + dv[0], x[1] + du[1] + dv[1], x[2] + du[2] + dv[2]);

            switch (d) {
                case 0 -> {
                    if (backFace) {
                        texture = block.texture.getNEG_X();
                    } else {
                        texture = block.texture.getPOS_X();
                    }

                    // Z=180 flip
                    uvs[3].set(0, w);
                    uvs[2].set(h, w);
                    uvs[1].set(0, 0);
                    uvs[0].set(h, 0);
                }
                case 1 -> {
                    if (backFace) {
                        texture = block.texture.getNEG_Y();
                    } else {
                        texture = block.texture.getPOS_Y();
                    }

                    uvs[0].set(0, w);
                    uvs[1].set(h, w);
                    uvs[2].set(0, 0);
                    uvs[3].set(h, 0);
                }
                default -> {
                    if (backFace) {
                        texture = block.texture.getNEG_Z();
                    } else {
                        texture = block.texture.getPOS_Z();
                    }

                    // X=90 flip
                    uvs[1].set(0, h);
                    uvs[3].set(w, h);
                    uvs[0].set(0, 0);
                    uvs[2].set(w, 0);
                }
            }


            for (int i = 0; i < 4; i++) {
                Vector3f vertex = vertices[i];
                completeVertex[i].set(
                        CompactVertexSet.packFirstInt(vertex.x, vertex.y, (byte) side, texture.getAnimationFrames()),
                        CompactVertexSet.packSecondInt(vertex.z, uvs[i].x, uvs[i].y),
                        CompactVertexSet.packThirdInt(texture.zLayer, (byte) 15));
            }

            if (block.opaque) {
                for (int i = 0; i < indexes.length; i++) {
                    int j = indexes[i];
                    buffers.vertex(0, completeVertex[j].x, completeVertex[j].y, completeVertex[j].z);
                }
            } else {
                for (int i = 0; i < indexes.length; i++) {
                    int j = indexes[i];
                    transBuffers.vertex(0, completeVertex[j].x, completeVertex[j].y, completeVertex[j].z);
                }
            }
        }
    }
}
