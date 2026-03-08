/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.terrain;

import com.tessera.content.vanilla.terrain.defaultTerrain.DefaultTerrain;
import com.tessera.engine.common.world.Terrain;
import com.tessera.engine.common.world.World;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.content.vanilla.Blocks;
import com.tessera.engine.common.option.BoundedInt;
import com.tessera.engine.common.option.OptionsList;

import static com.tessera.engine.common.world.chunk.Chunk.WIDTH;

/**
 * @author zipCoder933
 */
public class FlatTerrain extends Terrain {

    public FlatTerrain() {
        super("Flat Terrain");
        minSurfaceHeight = 220;
        maxSurfaceHeight = 220;
    }

    public void initOptions(OptionsList options) {
        options.put("Land Start", new BoundedInt(220, World.WORLD_TOP_Y + 2, World.WORLD_BOTTOM_Y - 2));
    }

    @Override
    public void loadWorld(OptionsList options, int version) {
        if (options.containsKey("Land Start")) {
            maxSurfaceHeight = options.getBoundedInt("Land Start").value;
            minSurfaceHeight = options.getBoundedInt("Land Start").value;
        }
    }

    private int getBiomeOfVoxel(float heat) {
        if (heat > 0.55f) {// 0.6 - 1
            // We lower down the minimum temperature of desert to compensate for it only
            // being at the bottom of the terrain
            return DefaultTerrain.BIOME_DESERT;
        } else if (heat > 0.2f) {// 0.2 - 0.6
            return DefaultTerrain.BIOME_SAVANNAH;
        } else if (heat > -0.2f) {// -0.2 - 0.2
            return DefaultTerrain.BIOME_DEFAULT;
        } else if (heat > -0.6f) {// -0.6 - -0.2
            return DefaultTerrain.BIOME_JUNGLE;
        } else {// -1 - -0.6
            return DefaultTerrain.BIOME_SNOWY;
        }
    }

    @Override
    protected void generateChunkInner(Chunk chunk, GenSession session) {

        for (int cx = 0; cx < WIDTH; cx++) {

            for (int cz = 0; cz < WIDTH; cz++) {
                for (int cy = 0; cy < WIDTH; cy++) {


//                    switch (biome) {
//                        case DEFAULT -> {
//                            chunk.data.setBlock(x, y, z, MyGame.BLOCK_GRASS);
//                        }
//                        case SNOWY -> {
//                            chunk.data.setBlock(x, y, z, MyGame.BLOCK_SNOW);
//                        }
//                        case DESERT -> {
//                            if (alpha > 0) {
//                                chunk.data.setBlock(x, y, z, MyGame.BLOCK_SAND);
//                                session.setBlockWorld(wx, wy + 1, wz, MyGame.BLOCK_SAND);
//                            } else {
//                                chunk.data.setBlock(x, y, z, MyGame.BLOCK_RED_SAND);
//                                session.setBlockWorld(wx, wy + 1, wz, MyGame.BLOCK_RED_SAND);
//                            }
//                        }
//                        case SAVANNAH -> {
//                            chunk.data.setBlock(x, y, z, MyGame.BLOCK_DRY_GRASS);
//                        }
//                        case JUNGLE -> {
//                            chunk.data.setBlock(x, y, z, MyGame.BLOCK_JUNGLE_GRASS);
//                        }


                    int wy = cy + (chunk.position.y * Chunk.WIDTH);
                    if (wy >= World.WORLD_BOTTOM_Y - 1) {
                        chunk.voxels.setBlock(cx, cy, cz, Blocks.BLOCK_BEDROCK);
                    } else if (wy == maxSurfaceHeight) {
                        chunk.voxels.setBlock(cx, cy, cz, Blocks.BLOCK_GRASS);
                    } else if (wy > maxSurfaceHeight + 10) {
                        chunk.voxels.setBlock(cx, cy, cz, Blocks.BLOCK_STONE);
                    } else if (wy > maxSurfaceHeight) {
                        chunk.voxels.setBlock(cx, cy, cz, Blocks.BLOCK_DIRT);
                    }
                }
            }
        }
    }

}
