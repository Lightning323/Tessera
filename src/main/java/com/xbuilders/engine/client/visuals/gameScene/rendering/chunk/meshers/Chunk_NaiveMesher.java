/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.client.visuals.gameScene.rendering.chunk.meshers;

import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.server.block.BlockRegistry;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.engine.server.block.construction.BlockType;
import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.client.visuals.gameScene.rendering.VertexSet;
import com.xbuilders.engine.common.world.chunk.Chunk;

import com.xbuilders.engine.common.world.chunk.ChunkVoxels;
import com.xbuilders.engine.common.world.wcc.WCCi;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;


import static com.xbuilders.Main.LOGGER;
import static com.xbuilders.engine.common.math.MathUtils.positiveMod;

/**
 * For use ONLY with chunks
 *
 * @author zipCoder933
 */
public class Chunk_NaiveMesher extends ChunkMesher<VertexSet> {

    boolean generateAll;
    Chunk chunk;

    public Chunk_NaiveMesher(Chunk chunk, boolean generateAll) {
        super(chunk.voxels, chunk.position);
        this.chunk = chunk;
        this.generateAll = generateAll;
    }

    Block[] neighbors = new Block[6];
    byte[] lightNeghbors = new byte[6];
    BlockData[] neighborData = new BlockData[6];//TODO: NeighborData is not used very often, maybe we disable this if the block doesnt need it

    Chunk negXChunk;
    Chunk posXChunk;
    Chunk negYChunk;
    Chunk posYChunk;
    Chunk negZChunk;
    Chunk posZChunk;
    BlockType type;

    public void compute(VertexSet opaqueBuffers, VertexSet transparentBuffers,
                        MemoryStack stack, int lodLevel, boolean smoothShading) {

        Block block = null;
        BlockData blockData = null;
        final boolean isUsingGreedyMesher = !generateAll;

        for (int i = 0; i < 6; i++) {//reset neighbors
            neighbors[i] = BlockRegistry.BLOCK_AIR;
            lightNeghbors[i] = 0;
            neighborData[i] = null;
        }

        negXChunk = Client.world
                .getChunk(new Vector3i(chunkPosition.x - 1, chunkPosition.y, chunkPosition.z));
        posXChunk = Client.world
                .getChunk(new Vector3i(chunkPosition.x + 1, chunkPosition.y, chunkPosition.z));
        negYChunk = Client.world
                .getChunk(new Vector3i(chunkPosition.x, chunkPosition.y - 1, chunkPosition.z));
        posYChunk = Client.world
                .getChunk(new Vector3i(chunkPosition.x, chunkPosition.y + 1, chunkPosition.z));
        negZChunk = Client.world
                .getChunk(new Vector3i(chunkPosition.x, chunkPosition.y, chunkPosition.z - 1));
        posZChunk = Client.world
                .getChunk(new Vector3i(chunkPosition.x, chunkPosition.y, chunkPosition.z + 1));

        for (int x = -1; x < data.size.x + 1; ++x) {
            for (int y = -1; y < data.size.y + 1; ++y) {
                for (int z = -1; z < data.size.z + 1; ++z) {

                    boolean blockIsUsingGM = true;

                    //If out of bounds
                    if (x < 0 || y < 0 || z < 0 || x >= data.size.x || y >= data.size.y || z >= data.size.z) {
                        Chunk out_chunk = WCCi.getNeighboringChunk(Client.world, chunkPosition, x, y, z);
                        if (out_chunk != null) {
                            int ox = positiveMod(x, Chunk.WIDTH);
                            int oy = positiveMod(y, Chunk.HEIGHT);
                            int oz = positiveMod(z, Chunk.WIDTH);
                            block = Registrys.getBlock(out_chunk.voxels.getBlock(ox, oy, oz));
                            type = Registrys.blocks.getBlockType(block.type);

                            if (type.getGreedyMesherPermissions() == BlockType.PERMIT_GM) {
                                assignNeighbors(out_chunk.voxels, ox, oy, oz, block);
                                blockIsUsingGM = this.type.determineIfUsingGreedyMesher(block, blockData, neighbors,
                                        neighborData, lightNeghbors, out_chunk, ox, oy, oz);
                            }
                        }
                    } else { //If in bounds
                        block = Registrys.getBlock(data.getBlock(x, y, z));
                        type = Registrys.blocks.getBlockType(block.type);

                        if (!block.isAir() //If this block is not air
                                && (generateAll || //If we generate all
                                type.getGreedyMesherPermissions() <= BlockType.PERMIT_GM) //If we permit or dont allow greedy mesher
                        ) {
                            blockIsUsingGM = false;
                            assignNeighbors(data, x, y, z, block);
                            blockData = data.getBlockData(x, y, z);
                            try {
                                if (block.opaque) {
                                    blockIsUsingGM = this.type.constructBlock(opaqueBuffers, block, blockData,  //XYZ are in chunk space
                                            neighbors, neighborData, lightNeghbors, chunk, x, y, z, isUsingGreedyMesher);
                                } else {
                                    blockIsUsingGM = this.type.constructBlock(transparentBuffers, block, blockData,
                                            neighbors, neighborData, lightNeghbors, chunk, x, y, z, isUsingGreedyMesher);
                                }
                            } catch (Exception e) {
                                LOGGER.info("error", e);
                            }
                        }
                    }
                    buffer_shouldUseGreedyMesher.put(x, y, z, blockIsUsingGM);


                }
            }
        }
    }


    private void assignNeighbors(ChunkVoxels chunk, int x, int y, int z, Block block) {
        byte centerLight = chunk.getPackedLight(x, y, z);
        //The code that assigns neighbors produces the most memory:
        //THE REASON, It could be hashmap.get()
        // This is the main bottleneck of naive mesher but not all of the bottleneck
        if (x > 0) {
            neighbors[BlockType.NEG_X] = Registrys.getBlock(chunk.getBlock(x - 1, y, z));
            lightNeghbors[BlockType.NEG_X] = block.opaque ? chunk.getPackedLight(x - 1, y, z)
                    : centerLight;
            neighborData[BlockType.NEG_X] = chunk.getBlockData(x - 1, y, z);
        } else if (negXChunk != null) {
            neighbors[BlockType.NEG_X] = Registrys
                    .getBlock(negXChunk.voxels.getBlock(negXChunk.voxels.size.x - 1, y, z));
            lightNeghbors[BlockType.NEG_X] = block.opaque ? negXChunk.voxels
                    .getPackedLight(negXChunk.voxels.size.x - 1, y, z)
                    : centerLight;
            neighborData[BlockType.NEG_X] = negXChunk.voxels.getBlockData(negXChunk.voxels.size.x - 1, y, z);
        } else {
            neighbors[BlockType.NEG_X] = BlockRegistry.BLOCK_AIR;
            lightNeghbors[BlockType.NEG_X] = 15;
            neighborData[BlockType.NEG_X] = null;
        }

        if (x < chunk.size.x - 1) {
            neighbors[BlockType.POS_X] = Registrys.getBlock(chunk.getBlock(x + 1, y, z));
            lightNeghbors[BlockType.POS_X] = block.opaque ? chunk.getPackedLight(x + 1, y, z)
                    : centerLight;
            neighborData[BlockType.POS_X] = chunk.getBlockData(x + 1, y, z);
        } else if (posXChunk != null) {
            neighbors[BlockType.POS_X] = Registrys.getBlock(posXChunk.voxels.getBlock(0, y, z));
            lightNeghbors[BlockType.POS_X] = block.opaque ? posXChunk.voxels.getPackedLight(0, y, z)
                    : centerLight;
            neighborData[BlockType.POS_X] = posXChunk.voxels.getBlockData(0, y, z);
        } else {
            neighbors[BlockType.POS_X] = BlockRegistry.BLOCK_AIR;
            lightNeghbors[BlockType.POS_X] = 15;
            neighborData[BlockType.POS_X] = null;
        }

        if (y > 0) {
            neighbors[BlockType.POS_Y] = Registrys.getBlock(chunk.getBlock(x, y - 1, z));
            lightNeghbors[BlockType.POS_Y] = block.opaque ? chunk.getPackedLight(x, y - 1, z)
                    : centerLight;
            neighborData[BlockType.POS_Y] = chunk.getBlockData(x, y - 1, z);
        } else if (negYChunk != null) {
            neighbors[BlockType.POS_Y] = Registrys
                    .getBlock(negYChunk.voxels.getBlock(x, negYChunk.voxels.size.y - 1, z));
            lightNeghbors[BlockType.POS_Y] = block.opaque ? negYChunk.voxels.getPackedLight(x,
                    negYChunk.voxels.size.y - 1, z)
                    : centerLight;
            neighborData[BlockType.POS_Y] = negYChunk.voxels.getBlockData(x, negYChunk.voxels.size.y - 1, z);
        } else {
            neighbors[BlockType.POS_Y] = BlockRegistry.BLOCK_AIR;
            lightNeghbors[BlockType.POS_Y] = 15;
            neighborData[BlockType.POS_Y] = null;
        }

        if (y < chunk.size.y - 1) {
            neighbors[BlockType.NEG_Y] = Registrys.getBlock(chunk.getBlock(x, y + 1, z));
            lightNeghbors[BlockType.NEG_Y] = block.opaque ? chunk.getPackedLight(x, y + 1, z)
                    : centerLight;
            neighborData[BlockType.NEG_Y] = chunk.getBlockData(x, y + 1, z);
        } else if (posYChunk != null) {
            neighbors[BlockType.NEG_Y] = Registrys.getBlock(posYChunk.voxels.getBlock(x, 0, z));
            lightNeghbors[BlockType.NEG_Y] = block.opaque ? posYChunk.voxels.getPackedLight(x, 0, z)
                    : centerLight;
            neighborData[BlockType.NEG_Y] = posYChunk.voxels.getBlockData(x, 0, z);
        } else {
            neighbors[BlockType.NEG_Y] = BlockRegistry.BLOCK_AIR;
            lightNeghbors[BlockType.NEG_Y] = 15;
            neighborData[BlockType.NEG_Y] = null;
        }

        if (z > 0) {
            neighbors[BlockType.NEG_Z] = Registrys.getBlock(chunk.getBlock(x, y, z - 1));
            lightNeghbors[BlockType.NEG_Z] = block.opaque ? chunk.getPackedLight(x, y, z - 1)
                    : centerLight;
            neighborData[BlockType.NEG_Z] = chunk.getBlockData(x, y, z - 1);
        } else if (negZChunk != null) {
            neighbors[BlockType.NEG_Z] = Registrys
                    .getBlock(negZChunk.voxels.getBlock(x, y, negZChunk.voxels.size.z - 1));
            lightNeghbors[BlockType.NEG_Z] = block.opaque ? negZChunk.voxels.getPackedLight(x, y,
                    negZChunk.voxels.size.z - 1)
                    : centerLight;
            neighborData[BlockType.NEG_Z] = negZChunk.voxels.getBlockData(x, y, negZChunk.voxels.size.z - 1);
        } else {
            neighbors[BlockType.NEG_Z] = BlockRegistry.BLOCK_AIR;
            lightNeghbors[BlockType.NEG_Z] = 15;
            neighborData[BlockType.NEG_Z] = null;
        }

        if (z < chunk.size.z - 1) {
            neighbors[BlockType.POS_Z] = Registrys.getBlock(chunk.getBlock(x, y, z + 1));
            lightNeghbors[BlockType.POS_Z] = block.opaque ? chunk.getPackedLight(x, y, z + 1)
                    : centerLight;
            neighborData[BlockType.POS_Z] = chunk.getBlockData(x, y, z + 1);
        } else if (posZChunk != null) {
            neighbors[BlockType.POS_Z] = Registrys
                    .getBlock(posZChunk.voxels.getBlock(x, y, 0));
            lightNeghbors[BlockType.POS_Z] = block.opaque ? posZChunk.voxels.getPackedLight(x, y, 0)
                    : centerLight;
            neighborData[BlockType.POS_Z] = posZChunk.voxels.getBlockData(x, y, 0);
        } else {
            neighbors[BlockType.POS_Z] = BlockRegistry.BLOCK_AIR;
            lightNeghbors[BlockType.POS_Z] = 15;
            neighborData[BlockType.POS_Z] = null;
        }
    }

}
