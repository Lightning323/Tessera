/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.terrain;

import com.tessera.engine.common.world.chunk.Chunk;

import static com.tessera.engine.common.world.chunk.Chunk.WIDTH;

import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.Terrain;
import com.tessera.content.vanilla.Blocks;
import com.tessera.engine.common.option.OptionsList;

import java.util.Random;

/**
 * @author zipCoder933
 */
public class BasicTerrain extends Terrain {

    public BasicTerrain() {
        super("Basic Terrain");
        minSurfaceHeight = 70;
        maxSurfaceHeight = 140;
    }

    @Override
    public void initOptions(OptionsList optionsList) {
    }

    @Override
    public void loadWorld(OptionsList options, int version) {
    }

    @Override
    protected void generateChunkInner(Chunk chunk, GenSession session) {
        boolean genOutsideBoundary = false;
        if ((chunk.position.y * Chunk.WIDTH) + Chunk.WIDTH > minSurfaceHeight - 2) {
            for (int cx = 0; cx < WIDTH; cx++) {
                for (int cy = 0; cy < WIDTH; cy++) {
                    for (int cz = 0; cz < WIDTH; cz++) {

                        int wx = cx + (chunk.position.x * Chunk.WIDTH);
                        int wy = cy + (chunk.position.y * Chunk.WIDTH);
                        int wz = cz + (chunk.position.z * Chunk.WIDTH);

                        int heightmap = (int) MathUtils.map(
                                (float) perlinNoise.noise(wx * 0.3f, wz * 0.3f),
                                -1, 1, maxSurfaceHeight, minSurfaceHeight);

                        if (wy == heightmap) {
                            chunk.voxels.setBlock(cx, cy, cz, Blocks.BLOCK_GRASS);
                            if (session.random.nextFloat() > 0.995) {
                                makeTree(session.random, session, wx, wy + 1, wz);
                                genOutsideBoundary = true;
                            } else if (session.random.nextFloat() > 0.95) {
                                chunk.voxels.setBlock(cx, cy - 1, cz, randomFlower(session));
                            }
                        } else if (heightmap < wy) {
                            chunk.voxels.setBlock(cx, cy, cz, Blocks.BLOCK_DIRT);
                        }
                    }
                }
            }

        }
        session.generatedOutsideOfChunk = false;
    }


    private static short randomFlower(Terrain.GenSession session) {
        short block = 0;
        switch (session.random.nextInt(4)) {
            case 0 -> {
                block = Blocks.BLOCK_ROSES;
            }
            case 1 -> {
                block = Blocks.BLOCK_PANSIES;
            }
            case 2 -> {
                block = Blocks.BLOCK_AZURE_BLUET;
            }
            case 3 -> {
                block = Blocks.BLOCK_DANDELION;
            }
            default -> {
                block = Blocks.BLOCK_BLUE_ORCHID;
            }
        }
        return block;
    }


    public static void makeTree(Random random, Terrain.GenSession session, int treeX, int treeY, int treeZ) {
        int height = random.nextInt(7) + 3;

        for (int x = 0; x < 5; x++) {
            for (int y = -3; y < 0; y++) {
                for (int z = 0; z < 5; z++) {
                    if ((x == 0 && z == 0)
                            || (x == 4 && z == 4)
                            || (x == 0 && z == 4)
                            || (x == 4 && z == 0)) {
                    } else {
//                        session.setBlockWorld(
//                                x + treeX - 2, y + treeY - height + 2, z + treeZ - 2, Blocks.BLOCK_OAK_LEAVES
//                        );
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
//            session.setBlockWorld(treeX, y + treeY - height, treeZ, Blocks.BLOCK_OAK_LOG);
        }
    }

}
