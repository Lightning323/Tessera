/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers;

import com.tessera.engine.client.Client;
import com.tessera.engine.common.world.chunk.ClientChunk;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.server.block.construction.BlockType;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet.CompactVertexSet;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.chunk.ChunkVoxels;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * @author zipCoder933
 */
public class Chunk_GreedyMesherWithLight extends ChunkMesher<CompactVertexSet> {
    // Nublada does not actually use indexed meshing, it is just regular meshes with
    // indexes attached to vertices
    // If I want to do indexed meshes, I need some sort of hashmap like thing, and
    // than compute the indexes later

    // Constants
    final int[] dims;
    boolean smoothLighting = true;

    // Variables used for greedy meshing
    int j, k, l, u, v, n, side = 0;
    final int[] x = new int[]{0, 0, 0};
    final int[] q = new int[]{0, 0, 0};
    final int[] du = new int[]{0, 0, 0};
    final int[] dv = new int[]{0, 0, 0};
    final int[] normal = new int[]{0, 0, 0};


    public Chunk_GreedyMesherWithLight(ChunkVoxels voxels, Vector3i chunkPosition) {
        super(voxels, chunkPosition);
        dims = new int[]{voxels.size.x, voxels.size.y, voxels.size.z};
        mask = MemoryUtil.memAllocInt(Chunk.WIDTH * Chunk.HEIGHT);
        lightMask = MemoryUtil.memAllocInt(Chunk.WIDTH * Chunk.HEIGHT);
    }


    final IntBuffer mask;
    final IntBuffer lightMask;

    /**
     * Check if 2 voxel faces are the same
     */
    private boolean equals(IntBuffer mask, IntBuffer lightMask, int indx1, int indx2) {
        return mask.get(indx2) == mask.get(indx1) && lightMask.get(indx2) == lightMask.get(indx1);
    }

    public void compute(CompactVertexSet opaqueBuffers, CompactVertexSet transparentBuffers, MemoryStack stack,
                        int lodLevel, boolean smoothLighting) {
        this.smoothLighting = smoothLighting;
        /**
         * These are just working variables for the algorithm - almost all taken
         * directly from Mikola Lysenko's javascript implementation.
         */
        // Empty x,q,du and dv
        for (int i = 0; i < 3; i++) {
            x[i] = 0;
            q[i] = 0;
            du[i] = 0;
            dv[i] = 0;
        }
        // Reset all the variables
        int i = 0;
        j = 0;
        k = 0;
        l = 0;
        n = 0;
        u = 0;
        v = 0;
        final IntBuffer quadSize = stack.mallocInt(2);
        int maskValue, lightMaskValue;

        /**
         * These are just working variables to hold two faces during comparison.
         */
        Vector3i voxelPos = new Vector3i(stack.mallocInt(3));
        ShortBuffer thisPlaneVoxel = stack.mallocShort(1);
        ShortBuffer nextPlaneVoxel = stack.mallocShort(1);
        Block block, block1;

        /*
         * We create a mask - this will contain the groups of matching voxel faces
         * as we proceed through the chunk in 6 directions - once for each face.
         */
        for (int in = 0; in < mask.capacity(); in++) {
            mask.put(in, 0);
            lightMask.put(in, 0);
        }
        //If we experience problems with reusing of the mask:
        //Note: since stack.malloc is the most efficient way to allocate memory, reusing the mask may not be much of an optimization.
//        IntBuffer mask = stack.mallocInt(Chunk.WIDTH * Chunk.HEIGHT);
//        IntBuffer lightMask = stack.mallocInt(Chunk.WIDTH * Chunk.HEIGHT);

        for (boolean backFace = true, b = false; b != backFace; backFace = backFace && b, b = !b) {
            for (int d = 0; d < 3; d++) {

                u = (d + 1) % 3;// v and u are the perpendicular dimensions to d
                v = (d + 2) % 3;

                x[0] = 0;// X is the position vector, X[d] advances forward in the given dimension
                x[1] = 0;
                x[2] = 0;

                q[0] = 0;// Q is the dimension vector, for example, if d=1, q = [0,1,0]
                q[1] = 0;
                q[2] = 0;
                q[d] = 1;

                normal[0] = 0;// Normal is similar to q, but if we are on the negative side of the plane, it
                // is negative
                normal[1] = 0;
                normal[2] = 0;
                normal[d] = backFace ? -1 : 1;

                ClientChunk forwardChunk = Client.world
                        .getChunk(new Vector3i(chunkPosition.x + q[0], chunkPosition.y + q[1], chunkPosition.z + q[2]));
                ClientChunk backChunk = Client.world
                        .getChunk(new Vector3i(chunkPosition.x - q[0], chunkPosition.y - q[1], chunkPosition.z - q[2]));

                if (d == 0) {
                    side = backFace ? BlockType.NEG_X : BlockType.POS_X;
                } else if (d == 1) {
                    side = backFace ? BlockType.POS_Y : BlockType.NEG_Y;
                } else if (d == 2) {
                    side = backFace ? BlockType.NEG_Z : BlockType.POS_Z;
                }

//                // We could also create the mask here, but this probbly isnt the most optimal way to do it
//                final IntBuffer mask = stack.mallocInt(dims[u] * dims[v]);
//                final IntBuffer lightMask = stack.mallocInt(dims[u] * dims[v]);// Implement lightmask

                /*
                 * We move through the d from front to back
                 */
                int min = 0;// -1 (We changed this to 0, so chunks dont overlap. The dim starts at 0 and
                // ends up covering the next chunks faces)
                int max = dims[d];

                // If the chunk is not here, dont draw the side that it is on
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

                    for (x[v] = 0; x[v] < dims[v]; x[v]++) {// v and u are the perpendicular dimensions to d, we sweep
                        // across the plane of V and U
                        for (x[u] = 0; x[u] < dims[u]; x[u]++) {
                            retrieveMaskVoxels(x, q, d, backChunk, forwardChunk, voxelPos,
                                    thisPlaneVoxel, nextPlaneVoxel, lodLevel);
                            block = Registrys.getBlock(thisPlaneVoxel.get(0));
                            block1 = Registrys.getBlock(nextPlaneVoxel.get(0));


                            if (block == null || block.isAir()
                                    || !useGreedyMesher(block, x[0], x[1], x[2])) {
                                thisPlaneVoxel.put(0, (short) 0);
                            }
                            if (block1 == null || block1.isAir()
                                    || !useGreedyMesher(block1, x[0] + q[0], x[1] + q[1], x[2] + q[2])) {
                                nextPlaneVoxel.put(0, (short) 0);
                            }

                            boolean draw = (thisPlaneVoxel.get(0) == 0 || nextPlaneVoxel.get(0) == 0)
                                    || (block.opaque != block1.opaque);
                            // The opaque check is to prevent transparent mesh from overriding opaque

                            maskValue = draw
                                    ? (backFace // add the voxel for either this plane or the next plane
                                    // depending on our direction
                                    ? nextPlaneVoxel.get(0)
                                    : thisPlaneVoxel.get(0))
                                    : 0;
                            mask.put(n, maskValue);

                            lightMaskValue = draw
                                    ? (backFace // add the voxel for either this plane or the next plane
                                    // depending on our direction
                                    ? retrieveLightForNextPlane(nextPlaneVoxel.get(0), backChunk, forwardChunk, block1, d, backFace, x, q)
                                    : retrieveLightForThisPlane(thisPlaneVoxel.get(0), backChunk, forwardChunk, block, d, backFace, x, q))
                                    : 0;
                            lightMask.put(n, lightMaskValue);
                            n++;
                        }
                    }

                    x[d]++; // move forward

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

                                // <editor-fold defaultstate="collapsed" desc="make the quad">
                                // Compute the quad width
                                for (quadSize.put(0, 1); i + quadSize.get(0) < dims[u]

                                        && mask.get(n + quadSize.get(0)) != 0 &&

                                        equals(mask, lightMask, n + quadSize.get(0), n);

                                     quadSize.put(0,
                                             quadSize.get(0) + 1))
                                    ;
                                {
                                }
                                // compute the quad height
                                boolean done = false;
                                for (quadSize.put(1, 1); j + quadSize.get(1) < dims[v]; quadSize.put(1,
                                        quadSize.get(1) + 1)) {
                                    for (k = 0; k < quadSize.get(0); k++) {
                                        if (mask.get(n + k + quadSize.get(1) * dims[u]) == 0
                                                ||

                                                !equals(mask, lightMask, n + k + quadSize.get(1) * dims[u], n)) {
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
                                 * And here we call the quad function in order to render a merged quad in the
                                 * scene.
                                 *
                                 * We pass mask[n] to the function, which is an instance of the VoxelFace class
                                 * containing
                                 * all the attributes of the face - which allows for variables to be passed to
                                 * shaders - for
                                 * example lighting values used to create ambient occlusion.
                                 */
                                Mesher_makeQuad(opaqueBuffers, transparentBuffers, x, du, dv, quadSize.get(0),
                                        quadSize.get(1),
                                        (short) mask.get(n), lightMask.get(n), backFace, d, side, stack);
                                // </editor-fold>

                                /*
                                 * We zero out the mask
                                 * This is important as we don't want to render the same voxel twice
                                 */
                                for (l = 0; l < quadSize.get(1); ++l) {
                                    for (k = 0; k < quadSize.get(0); ++k) {
                                        mask.put(n + k + l * dims[u], (short) 0);
                                        lightMask.put(n + k + l * dims[u], 0);
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

    private boolean useGreedyMesher(Block block, int x, int y, int z) {
        BlockType blockType1 = Registrys.blocks.getBlockType(block.type);

        if (blockType1.getGreedyMesherPermissions() == BlockType.ALWAYS_USE_GM) {
            return true;
        } else if (blockType1.getGreedyMesherPermissions() == BlockType.DENY_GM) {
            return false;
        } else {//Permit GM
            return buffer_shouldUseGreedyMesher.get(x, y, z);
        }
    }

    // private int getMaskValue(IntBuffer mask, int index, int lodLevel) {
    // return mask.get((index / lodLevel) * lodLevel);
    // }

    private short getBlockLOD(ChunkVoxels data, int x, int y, int z, int lodLevel) {
//        if (lodLevel > 1) {//For now we don't use LOD
//            // We want to make the coordinates align with the LOD level,
//            // for example, if lod level is 2, the X coordinate would be as follows:
//            // Real X: 0,1,2,3,4,5,6,7,8,9,10
//            // LOD X: 0,0,2,2,4,4,6,6,8,8,10
//            // x = (x / lodLevel) * 2;
//            x = (x / lodLevel) * lodLevel;
//            y = (y / lodLevel) * lodLevel;
//            z = (z / lodLevel) * lodLevel;
//        }
        return data.getBlock(x, y, z);
    }

    private void retrieveMaskVoxels(int[] x, int[] q, int d, Chunk backChunk, Chunk forwardChunk,
                                    Vector3i voxelPos,
                                    ShortBuffer thisPlaneVoxel,
                                    ShortBuffer nextPlaneVoxel, int lodLevel) {
        // Here we retrieve two voxel faces for comparison.
        // thisPlaneVoxel literaly faces forward, while nextPlaneVoxel faces backward
        if (x[d] >= 0) { // Calculate the voxel of THIS plane
            thisPlaneVoxel.put(0, getBlockLOD(data, x[0], x[1], x[2], lodLevel));
        } else {// If we are out of bounds for this chunk:
            if (backChunk == null) {
                thisPlaneVoxel.put(0, (short) 0);
            } else {
                voxelPos.set(MathUtils.positiveMod(x[0], Chunk.WIDTH), MathUtils.positiveMod(x[1], Chunk.WIDTH),
                        MathUtils.positiveMod(x[2], Chunk.WIDTH));
                thisPlaneVoxel.put(0, getBlockLOD(backChunk.voxels, voxelPos.x, voxelPos.y, voxelPos.z, lodLevel));
            }
        }
        if (x[d] < dims[d] - 1) { // calculate the voxel of the NEXT plane
            voxelPos.set(x[0] + q[0], x[1] + q[1], x[2] + q[2]);
            nextPlaneVoxel.put(0, getBlockLOD(data, voxelPos.x, voxelPos.y, voxelPos.z, lodLevel));
        } else {// If we are out of bounds for this chunk:
            if (forwardChunk == null) {
                nextPlaneVoxel.put(0, (short) 0);
            } else {
                voxelPos.set(MathUtils.positiveMod(x[0] + q[0], Chunk.WIDTH),
                        MathUtils.positiveMod(x[1] + q[1], Chunk.WIDTH),
                        MathUtils.positiveMod(x[2] + q[2], Chunk.WIDTH));
                nextPlaneVoxel.put(0, getBlockLOD(forwardChunk.voxels, voxelPos.x, voxelPos.y, voxelPos.z, lodLevel));
            }
        }
    }

    // Variables used for quad generaiton
    final static int[] indexes1 = {2, 0, 1, 1, 3, 2}; // Constant
    final static int[] indexes2 = {2, 3, 1, 1, 0, 2}; // Constant
    final Vector3f[] vertices = {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
    final Vector2f[] uvs = {new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f()};
    final byte[] light = {(byte) 0, (byte) 0, (byte) 0, (byte) 0};
    final Vector3i[] completeVertex = {new Vector3i(), new Vector3i(), new Vector3i(), new Vector3i()};
    byte l_lt, l_rt, l_lb, l_rb;

    // d: 0=X,1=Y,2=Z
    protected void Mesher_makeQuad(CompactVertexSet buffers, CompactVertexSet transBuffers, int x[], int du[], int dv[], final int w,
                                   final int h,
                                   short blockVal, int packedLight,
                                   final boolean backFace, final int d, final int side, MemoryStack stack) {

        // packedLight = 0b11111111000000001111111100000000;// For testing purposes

        l_lt = (byte) packedLight; // top left
        l_rt = (byte) packedLight; // top right
        l_lb = (byte) packedLight; // bottom left
        l_rb = (byte) packedLight; // bottom right

        Block block = Registrys.getBlock(blockVal);

        if (block.isLuminous()) {//Light blocks are fullbright
            l_lb = (byte) 255;
            l_rb = (byte) 255;
            l_lt = (byte) 255;
            l_rt = (byte) 255;
        } else if (smoothLighting) { // We need a 32 bit number for the light not 16
            l_lb = (byte) ((packedLight >> 0) & 0xFF);
            l_rt = (byte) ((packedLight >> 8) & 0xFF);
            l_lt = (byte) ((packedLight >> 16) & 0xFF);
            l_rb = (byte) ((packedLight >> 24) & 0xFF);
        }


        if (block != null && block.texture != null) {
            int[] indexes = backFace ? indexes1 : indexes2;
            BlockTexture.FaceTexture texture;

            // The ONLY difference betweent this method and nublada is that nublada uses
            // packed single integer coordinates,
            // and that uvs and vertex positions are combined
            vertices[0].set(x[0], x[1], x[2]);
            vertices[1].set(x[0] + dv[0], x[1] + dv[1], x[2] + dv[2]);
            vertices[2].set(x[0] + du[0], x[1] + du[1], x[2] + du[2]);
            vertices[3].set(x[0] + du[0] + dv[0], x[1] + du[1] + dv[1], x[2] + du[2] + dv[2]);

            switch (d) {
                case 0 -> {
                    if (backFace) {
                        texture = block.texture.getNEG_X();
                        light[1] = l_lb;
                        light[2] = l_rt;
                        light[3] = l_lt;
                        light[0] = l_rb;
                    } else {
                        texture = block.texture.getPOS_X();
                        light[3] = l_rb;
                        light[0] = l_lt;
                        light[1] = l_rt;
                        light[2] = l_lb;
                    }

                    // Z=180 flip
                    uvs[3].set(0, w);
                    uvs[2].set(h, w);
                    uvs[1].set(0, 0);
                    uvs[0].set(h, 0);
                }
                case 1 -> {
                    if (backFace) {
                        texture = block.texture.getPOS_Y();
                        light[1] = l_lb;
                        light[2] = l_rt;
                        light[3] = l_lt;
                        light[0] = l_rb;
                    } else {
                        texture = block.texture.getNEG_Y();
                        light[3] = l_rb;
                        light[0] = l_lt;
                        light[1] = l_rt;
                        light[2] = l_lb;
                    }

                    uvs[0].set(0, w);
                    uvs[1].set(h, w);
                    uvs[2].set(0, 0);
                    uvs[3].set(h, 0);
                }
                default -> {
                    if (backFace) {
                        texture = block.texture.getNEG_Z();
                        light[1] = l_lb;
                        light[2] = l_rt;
                        light[3] = l_lt;
                        light[0] = l_rb;
                    } else {
                        texture = block.texture.getPOS_Z();
                        light[3] = l_rb;
                        light[0] = l_lt;
                        light[1] = l_rt;
                        light[2] = l_lb;
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
                //
                // int sun = 3 & 0xF;
                // int torch = 0 & 0xF;
                // byte light2 = ((sun << 4) | torch);

                completeVertex[i].set(
                        CompactVertexSet.packFirstInt(vertex.x, vertex.y, (byte) side, texture.getAnimationFrames()),
                        CompactVertexSet.packSecondInt(vertex.z, uvs[i].x, uvs[i].y),
                        CompactVertexSet.packThirdInt(texture.zLayer, light[i]));
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

    private byte getLightVal(int pos[]) {
        // TODO: Optimize this; right now we are potentially loading a neighbor 9 times
        // per voxel
        if (Chunk.inBounds(pos[0], pos[1], pos[2])) {// Center
            return data.getPackedLight(pos[0], pos[1], pos[2]);
        } else {
            Chunk chunk = WCCi.getNeighboringChunk(Client.world, chunkPosition, pos[0], pos[1], pos[2]);
            if (chunk != null) {
                return chunk.voxels.getPackedLight(
                        MathUtils.positiveMod(pos[0], Chunk.WIDTH),
                        MathUtils.positiveMod(pos[1], Chunk.WIDTH),
                        MathUtils.positiveMod(pos[2], Chunk.WIDTH));
            }
        }
        return 0;
    }

    private int[] getCoords(int x[], int xOffset, int yOffset, int[] normal, int u, int v, boolean backFace) {
        int[] pos = {x[0] + normal[0], x[1] + normal[1], x[2] + normal[2]};
        pos[u] += backFace ? -yOffset : yOffset;
        pos[v] += backFace ? -xOffset : xOffset;
        return pos;
    }

    //Variables used for smooth light generation
    //TODO: If I ever implement multithreaded greedy meshing, these variables need to be in the method!
    byte a1, b1, c1,
            a2, b2, c2,
            a3, b3, c3;
    byte a1Sun, b1Sun, c1Sun,
            a2Sun, b2Sun, c2Sun,
            a3Sun, b3Sun, c3Sun;
    byte a1Torch, b1Torch, c1Torch,
            a2Torch, b2Torch, c2Torch,
            a3Torch, b3Torch, c3Torch;

    private int retrieveSmoothLight(short thisPlaneVoxel, Chunk backChunk,
                                    Chunk frontChunk, Block block,
                                    boolean backFace,
                                    int x, int y, int z, // origin
                                    int[] normal) {
        if (thisPlaneVoxel == 0) //Skip if this is air
            return 0;

        int origin[] = {x, y, z};

        int[] pos = getCoords(origin, -1, -1, normal, u, v, backFace);
        // System.out.print("a1=(" + pos[0] + " " + pos[1] + " " + pos[2] + ") ");
        a1 = getLightVal(pos);

        pos = getCoords(origin, 0, -1, normal, u, v, backFace);
        // System.out.print("b1=(" + pos[0] + " " + pos[1] + " " + pos[2] + ") ");
        b1 = getLightVal(pos);

        pos = getCoords(origin, 1, -1, normal, u, v, backFace);
        // System.out.println("c1=(" + pos[0] + " " + pos[1] + " " + pos[2] + ")");
        c1 = getLightVal(pos);

        pos = getCoords(origin, -1, 0, normal, u, v, backFace);
        // System.out.print("a2=(" + pos[0] + " " + pos[1] + " " + pos[2] + ") ");
        a2 = getLightVal(pos);

        pos = getCoords(origin, 0, 0, normal, u, v, backFace);
        // System.out.print("b2=(" + pos[0] + " " + pos[1] + " " + pos[2] + ") ");
        b2 = getLightVal(pos);

        pos = getCoords(origin, 1, 0, normal, u, v, backFace);
        // System.out.println("c2=(" + pos[0] + " " + pos[1] + " " + pos[2] + ")");
        c2 = getLightVal(pos);

        pos = getCoords(origin, -1, 1, normal, u, v, backFace);
        // System.out.print("a3=(" + pos[0] + " " + pos[1] + " " + pos[2] + ") ");
        a3 = getLightVal(pos);

        pos = getCoords(origin, 0, 1, normal, u, v, backFace);
        // System.out.print("b3=(" + pos[0] + " " + pos[1] + " " + pos[2] + ") ");
        b3 = getLightVal(pos);

        pos = getCoords(origin, 1, 1, normal, u, v, backFace);
        // System.out.println("c3=(" + pos[0] + " " + pos[1] + " " + pos[2] + ")");
        c3 = getLightVal(pos);

        // We have to separate the channels, average them for each vertex and them pack
        // them back together


        a1Sun = (byte) ((a1 & 0b11110000) >> 4);
        b1Sun = (byte) ((b1 & 0b11110000) >> 4);
        c1Sun = (byte) ((c1 & 0b11110000) >> 4);
        a2Sun = (byte) ((a2 & 0b11110000) >> 4);
        b2Sun = (byte) ((b2 & 0b11110000) >> 4);
        c2Sun = (byte) ((c2 & 0b11110000) >> 4);
        a3Sun = (byte) ((a3 & 0b11110000) >> 4);
        b3Sun = (byte) ((b3 & 0b11110000) >> 4);
        c3Sun = (byte) ((c3 & 0b11110000) >> 4);

        a1Torch = (byte) ((a1 & 0b00001111));
        b1Torch = (byte) ((b1 & 0b00001111));
        c1Torch = (byte) ((c1 & 0b00001111));
        a2Torch = (byte) ((a2 & 0b00001111));
        b2Torch = (byte) ((b2 & 0b00001111));
        c2Torch = (byte) ((c2 & 0b00001111));
        a3Torch = (byte) ((a3 & 0b00001111));
        b3Torch = (byte) ((b3 & 0b00001111));
        c3Torch = (byte) ((c3 & 0b00001111));

        // average the channels
        byte leftTopSun = (byte) ((a1Sun + b1Sun + a2Sun + b2Sun) / 4);
        byte rightTopSun = (byte) ((b1Sun + c1Sun + b2Sun + c2Sun) / 4);
        byte leftBottomSun = (byte) ((a2Sun + b2Sun + a3Sun + b3Sun) / 4);
        byte rightBottomSun = (byte) ((b2Sun + c2Sun + b3Sun + c3Sun) / 4);

        byte leftTopTorch = (byte) ((a1Torch + b1Torch + a2Torch + b2Torch) / 4);
        byte rightTopTorch = (byte) ((b1Torch + c1Torch + b2Torch + c2Torch) / 4);
        byte leftBottomTorch = (byte) ((a2Torch + b2Torch + a3Torch + b3Torch) / 4);
        byte rightBottomTorch = (byte) ((b2Torch + c2Torch + b3Torch + c3Torch) / 4);

        // Pack the sun and torch values into a single byte
        // Pack the sun in the first 4 bits and the torch in the last 4 bits
        int leftTop = ((leftTopSun << 4) | leftTopTorch);
        int rightTop = ((rightTopSun << 4) | rightTopTorch);
        int leftBottom = ((leftBottomSun << 4) | leftBottomTorch);
        int rightBottom = ((rightBottomSun << 4) | rightBottomTorch);

        return (leftBottom | (rightTop << 8) | (leftTop << 16) | (rightBottom << 24));
    }

    private int retrieveLightForThisPlane(short thisPlaneVoxel,
                                          Chunk backChunk, Chunk forwardChunk, Block block,
                                          int d, boolean backFace, int[] x, int[] q) {
        // //This plane = top face, +X face and +Z face (x and z assuming you are
        // starting from the center of the chunk and moving outwards)
        if (thisPlaneVoxel != 0) {
            if (smoothLighting) {
                return retrieveSmoothLight(thisPlaneVoxel, backChunk, forwardChunk, block, backFace,
                        x[0], x[1], x[2], normal);
            } else {// Flat lighting
                if (!block.opaque) {
                    if (x[d] >= 0) {
                        return data.getPackedLight(x[0], x[1], x[2]);
                    } else if (backChunk != null) {
                        return backChunk.voxels.getPackedLight(
                                MathUtils.positiveMod(x[0], Chunk.WIDTH),
                                MathUtils.positiveMod(x[1], Chunk.WIDTH),
                                MathUtils.positiveMod(x[2], Chunk.WIDTH));
                    }
                } else {
                    if (x[d] + 1 < dims[d]) {
                        return data.getPackedLight(x[0] + q[0], x[1] + q[1], x[2] + q[2]);
                    } else if (forwardChunk != null) {
                        return forwardChunk.voxels.getPackedLight(MathUtils.positiveMod(x[0] + q[0], Chunk.WIDTH),
                                MathUtils.positiveMod(x[1] + q[1], Chunk.WIDTH),
                                MathUtils.positiveMod(x[2] + q[2], Chunk.WIDTH));
                    }
                }
            }
        }
        return 0;
    }

    // private void getLightFrag(int[]x, int[]dims, int d, int[]q, Chunk chunk,
    // Chunk forwardChunk){
    // if (x[d] + 1 < dims[d]) {
    // b2 = chunkVoxels.getPackedLight(x[0] + q[0], x[1] + q[1], x[2] + q[2]);
    // } else if (forwardChunk != null) {
    // b2 = forwardChunk.data.getPackedLight(
    // MathUtils.positiveMod(x[0] + q[0], Chunk.WIDTH),
    // MathUtils.positiveMod(x[1] + q[1], Chunk.WIDTH),
    // MathUtils.positiveMod(x[2] + q[2], Chunk.WIDTH));
    // }
    // }

    private int retrieveLightForNextPlane(short nextPlaneVoxel, Chunk backChunk, Chunk forwardChunk, Block block1,
                                          int d, boolean backFace, int[] x, int[] q) {
        // next plane = bottom face, -X face and -Z face (x and z assuming you are
        // starting from the center of the chunk and moving outwards)
        if (nextPlaneVoxel != 0) {
            if (smoothLighting) {
                return retrieveSmoothLight(nextPlaneVoxel, backChunk, forwardChunk, block1, backFace,
                        x[0] + q[0], x[1] + q[1], x[2] + q[2], normal);
            } else {// Flat lighting
                if (!block1.opaque) {
                    if (x[d] + 1 < dims[d]) {
                        return data.getPackedLight(x[0] + q[0], x[1] + q[1], x[2] + q[2]);
                    } else if (forwardChunk != null) {
                        return forwardChunk.voxels.getPackedLight(MathUtils.positiveMod(x[0] + q[0], Chunk.WIDTH),
                                MathUtils.positiveMod(x[1] + q[1], Chunk.WIDTH),
                                MathUtils.positiveMod(x[2] + q[2], Chunk.WIDTH));
                    }
                } else {
                    if (x[d] >= 0) {
                        return data.getPackedLight(x[0], x[1], x[2]);
                    } else if (backChunk != null) {
                        return backChunk.voxels.getPackedLight(MathUtils.positiveMod(x[0], Chunk.WIDTH),
                                MathUtils.positiveMod(x[1], Chunk.WIDTH), MathUtils.positiveMod(x[2], Chunk.WIDTH));
                    }
                }
            }
        }
        return 0;
    }
}
