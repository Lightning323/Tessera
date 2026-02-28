/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.content.vanilla.terrain;

import com.xbuilders.engine.common.world.World;
import com.xbuilders.engine.common.world.chunk.Chunk;

import static com.xbuilders.engine.common.world.chunk.Chunk.WIDTH;

import com.xbuilders.utils.MiscUtils;
import com.xbuilders.engine.common.world.Terrain;
import com.xbuilders.content.vanilla.Blocks;
import com.xbuilders.engine.common.option.OptionsList;

/**
 * @author zipCoder933
 */
public class DevTerrain extends Terrain {

    public DevTerrain() {
        super("Dev Terrain");
        minSurfaceHeight = World.WORLD_TOP_Y;
        maxSurfaceHeight = World.WORLD_BOTTOM_Y;
    }

    @Override
    public void initOptions(OptionsList optionsList) {

    }

    @Override
    public void loadWorld(OptionsList options, int version) {

    }

    @Override
    protected void generateChunkInner(Chunk chunk, GenSession session) {
        int wy;
        int heightmap = 200;

        for (int cy = 0; cy < WIDTH; cy++) {
            wy = cy + (chunk.position.y * Chunk.WIDTH);

            for (int cx = 0; cx < WIDTH; cx++) {
                for (int cz = 0; cz < WIDTH; cz++) {
                    if (wy >= heightmap) {
                        if (MiscUtils.isBlackCube(chunk.position.x, chunk.position.y, chunk.position.z)) {
                            chunk.voxels.setBlock(cx, cy, cz, Blocks.BLOCK_SANDSTONE);
                        } else chunk.voxels.setBlock(cx, cy, cz, Blocks.BLOCK_STONE);
                    }
                }
            }
        }
    }

}
