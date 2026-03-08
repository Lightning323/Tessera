/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.blocks.blocks.trees;

import com.tessera.Main;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.world.Terrain;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.content.vanilla.Blocks;

import java.util.Random;

import static com.tessera.content.vanilla.blocks.blocks.trees.TreeUtils.randomInt;

/**
 * @author zipCoder933
 */
public class OakTreeUtils {

    public static final Block.RandomTickEvent randomTickEvent = new Block.RandomTickEvent() {
        @Override
        public boolean run(int x, int y, int z) {
            if (Blocks.plantUtils.plantable(x, y, z)) {
                player_plantTree(new Random(), x, y, z);
                return true;
            }
            return false;
        }
    };

    public static void player_plantTree(Random rand, int x, int y, int z) {
        int height = randomInt(rand, 5, 7);
        for (int k = 0; k < height; k++) {
            Main.getServer().setBlock(Blocks.BLOCK_OAK_LOG, x, y - k, z);
        }

        TreeUtils.player_roundedSquareLeavesLayer(x, y - height + 2, z, 2, Blocks.BLOCK_OAK_LEAVES);
        TreeUtils.player_roundedSquareLeavesLayer(x, y - height + 1, z, 2, Blocks.BLOCK_OAK_LEAVES);
        TreeUtils.player_diamondLeavesLayer(x, y - height, z, 2, Blocks.BLOCK_OAK_LEAVES);
        if (rand.nextDouble() > 0.8) {
            TreeUtils.player_diamondLeavesLayer(x, y - height - 1, z, 2, Blocks.BLOCK_OAK_LEAVES);
        }
    }

    public static void terrain_plantTree(Terrain.GenSession terrain, Chunk source, int x, int y, int z) {
        int height = randomInt(terrain.random, 5, 7);
        for (int k = 0; k < height; k++) {
            terrain.setBlockWorld(x, y - k, z, Blocks.BLOCK_OAK_LOG);
        }

        TreeUtils.terrain_roundedSquareLeavesLayer(terrain, source, x, y - height + 2, z, 2, Blocks.BLOCK_OAK_LEAVES);
        TreeUtils.terrain_roundedSquareLeavesLayer(terrain, source, x, y - height + 1, z, 2, Blocks.BLOCK_OAK_LEAVES);
        TreeUtils.terrain_diamondLeavesLayer(terrain, source, x, y - height, z, 2, Blocks.BLOCK_OAK_LEAVES);
        if (terrain.random.nextDouble() > 0.8) {
            TreeUtils.terrain_diamondLeavesLayer(terrain, source, x, y - height - 1, z, 2, Blocks.BLOCK_OAK_LEAVES);
        }
    }
}
