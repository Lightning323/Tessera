/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.client.visuals.gameScene.rendering.entity.block.meshers;

import com.xbuilders.engine.server.block.BlockRegistry;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.engine.server.block.construction.BlockType;
import com.xbuilders.engine.client.visuals.gameScene.rendering.VertexSet;
import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.common.world.chunk.ChunkVoxels;

import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;


import static com.xbuilders.Main.LOGGER;

/**
 * This one is designed for static block rendering, (but can technically still be used for chunk rendering)
 *
 * @author zipCoder933
 */
public class Block_NaiveMesher extends BlockMesher {

    boolean generateAll;

    public Block_NaiveMesher(ChunkVoxels data, Vector3i position, boolean generateAll) {
        super(data, position);
        this.generateAll = generateAll;
    }

    final byte[] light = {15, 15, 15, 15, 15, 15};

    public void compute(VertexSet opaqueBuffers,
                        VertexSet transparentBuffers,
                        MemoryStack stack, int lodLevel, boolean smoothShading) {
        Block block;
        Block[] neighbors = new Block[6];
        BlockData[] neighborData = new BlockData[6];

        BlockData blockData;
        for (int x = 0; x < data.size.x; ++x) {
            for (int y = 0; y < data.size.y; ++y) {
                for (int z = 0; z < data.size.z; ++z) {

                    block = Registrys.getBlock(data.getBlock(x, y, z));

                    if (!block.isAir() && (generateAll || block.type != BlockRegistry.DEFAULT_BLOCK_TYPE_ID)) {

                        if (x > 0) {
                            neighbors[BlockType.NEG_X] = Registrys.getBlock(data.getBlock(x - 1, y, z));
                            neighborData[BlockType.NEG_X] = data.getBlockData(x - 1, y, z);
                        } else {
                            neighbors[BlockType.NEG_X] = BlockRegistry.BLOCK_AIR;
                            neighborData[BlockType.NEG_X] = null;
                        }

                        if (x < data.size.x - 1) {
                            neighbors[BlockType.POS_X] = Registrys.getBlock(data.getBlock(x + 1, y, z));
                            neighborData[BlockType.POS_X] = data.getBlockData(x + 1, y, z);
                        } else {
                            neighbors[BlockType.POS_X] = BlockRegistry.BLOCK_AIR;
                            neighborData[BlockType.POS_X] = null;
                        }

                        if (y > 0) {
                            neighbors[BlockType.POS_Y] = Registrys.getBlock(data.getBlock(x, y - 1, z));
                            neighborData[BlockType.POS_Y] = data.getBlockData(x, y - 1, z);
                        } else {
                            neighbors[BlockType.POS_Y] = BlockRegistry.BLOCK_AIR;
                            neighborData[BlockType.POS_Y] = null;
                        }

                        if (y < data.size.y - 1) {
                            neighbors[BlockType.NEG_Y] = Registrys.getBlock(data.getBlock(x, y + 1, z));
                            neighborData[BlockType.NEG_Y] = data.getBlockData(x, y + 1, z);
                        } else {
                            neighbors[BlockType.NEG_Y] = BlockRegistry.BLOCK_AIR;
                            neighborData[BlockType.NEG_Y] = null;
                        }

                        if (z > 0) {
                            neighbors[BlockType.NEG_Z] = Registrys.getBlock(data.getBlock(x, y, z - 1));
                            neighborData[BlockType.NEG_Z] = data.getBlockData(x, y, z - 1);
                        } else {
                            neighbors[BlockType.NEG_Z] = BlockRegistry.BLOCK_AIR;
                            neighborData[BlockType.NEG_Z] = null;
                        }

                        if (z < data.size.z - 1) {
                            neighbors[BlockType.POS_Z] = Registrys.getBlock(data.getBlock(x, y, z + 1));
                            neighborData[BlockType.POS_Z] = data.getBlockData(x, y, z + 1);
                        } else {
                            neighbors[BlockType.POS_Z] = BlockRegistry.BLOCK_AIR;
                            neighborData[BlockType.POS_Z] = null;
                        }

                        try { //Handle any exceptions
                            blockData = data.getBlockData(x, y, z);
                            BlockType type = Registrys.blocks.getBlockType(block.type);
                            if (block.opaque) {
                                type.constructBlock(opaqueBuffers, block, blockData, neighbors, neighborData, light, null, x, y, z, false);
                            } else {
                                type.constructBlock(transparentBuffers, block, blockData, neighbors, neighborData, light, null, x, y, z, false);
                            }
                        } catch (Exception e) {
                            LOGGER.info("error", e);
                        }
                    }
                }
            }
        }
    }

}
