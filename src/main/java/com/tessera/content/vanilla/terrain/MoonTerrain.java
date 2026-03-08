// 
// Decompiled by Procyon v0.5.36
// 
package com.tessera.content.vanilla.terrain;

import com.tessera.engine.common.world.Terrain;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.option.OptionsList;

import static com.tessera.content.vanilla.Blocks.BLOCK_ANDESITE;
import static com.tessera.content.vanilla.Blocks.BLOCK_BEDROCK;

public class MoonTerrain extends Terrain {

    boolean deep;
    int height;

    public MoonTerrain() {
        super("Moon Terrain");
        minSurfaceHeight = 100;
        maxSurfaceHeight = 300;
    }

    public void initOptions(OptionsList optionsList) {
//        OptionsList options = new OptionsList();
//        options.put("Deep", true);
//        return options;
    }

    @Override
    public void loadWorld(OptionsList options, int version) {
//        if (options.containsKey("Deep")) {
//            deep = options.getBoolean("Deep");
//        }
//        height = deep ? 100 : 240;
    }

    @Override
    protected void generateChunkInner(Chunk chunk, GenSession session) {
        final short block = BLOCK_ANDESITE;

        for (int x = 0; x < Chunk.WIDTH; ++x) {
            final int wx = x + chunk.position.x * Chunk.WIDTH;
            for (int z = 0; z < Chunk.WIDTH; ++z) {
                final int wz = z + chunk.position.z * Chunk.WIDTH;
                final int heightMap = (int) (height + fastNoise.GetValueFractal((float) (wx * 2), (float) (wz * 2)) * 5.0f);


                for (int y = 0; y < Chunk.WIDTH; ++y) {
                    final int wy = y + chunk.position.y * Chunk.WIDTH;
                    if (wy > 252) {
                        chunk.voxels.setBlock(x, y, z, BLOCK_BEDROCK);
                    } else if (y == heightMap) {
                        if (fastNoise.GetValueFractal(wx * 6.0f, wy * 14.0f, wz * 6.0f) <= 0.5) {
                            chunk.voxels.setBlock(x, y, z, block);
                            chunk.voxels.setBlock(x, y + 1, z, block);
                            chunk.voxels.setBlock(x, y + 2, z, block);
                        }
                    } else if (y > heightMap) {
                        chunk.voxels.setBlock(x, y, z, block);
                    }
                }

            }
        }
    }


}
