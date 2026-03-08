/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.blocks.blocks.trees;

import com.tessera.Main;
import com.tessera.content.vanilla.Blocks;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.world.Terrain;
import com.tessera.engine.common.world.chunk.Chunk;

import java.util.Random;

import static com.tessera.content.vanilla.blocks.blocks.trees.TreeUtils.randomInt;

/**
 * @author zipCoder933
 */
public class SpruceTreeUtils {
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
    static final int MIN_HEIGHT = 7;
    static final int MAX_HEIGHT = 14;

    public static void player_plantTree(Random rand, int x, int y, int z) {
        int height = randomInt(rand, MIN_HEIGHT, MAX_HEIGHT);
        for (int k = 0; k < height; k++) {
            Main.getServer().setBlock(Blocks.BLOCK_SPRUCE_LOG, x, y - k, z);
        }

        int heightVal = 4;
        int layerValue = 2;

        TreeUtils.player_squareLeavesLayer(x, y - height + heightVal, z, layerValue, Blocks.BLOCK_SPRUCE_LEAVES);
        heightVal--;
        TreeUtils.player_diamondLeavesLayer(x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
        heightVal--;
        layerValue--;

        if (height > 8) {
            layerValue++;
            TreeUtils.player_squareLeavesLayer(x, y - height + heightVal, z, layerValue, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            TreeUtils.player_diamondLeavesLayer(x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            layerValue--;
        }
        if (height > 10) {
            layerValue++;
            TreeUtils.player_squareLeavesLayer(x, y - height + heightVal, z, layerValue, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            TreeUtils.player_diamondLeavesLayer(x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            layerValue--;
        }

        while (layerValue > 0) {
            TreeUtils.player_squareLeavesLayer(x, y - height + heightVal, z, layerValue, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            TreeUtils.player_diamondLeavesLayer(x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
            if (rand.nextBoolean()) {
                TreeUtils.player_diamondLeavesLayer(x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
            }
            heightVal--;
            layerValue--;
        }
        Main.getServer().setBlock(Blocks.BLOCK_SPRUCE_LEAVES, z, x, y - height + heightVal);
    }

    public static void terrain_plantTree(Terrain.GenSession terrain, Chunk source, int x, int y, int z) {
        int height = randomInt(terrain.random, MIN_HEIGHT, MAX_HEIGHT);
        for (int k = 0; k < height; k++) {
            terrain.setBlockWorld(x, y - k, z, Blocks.BLOCK_SPRUCE_LOG);
        }

        int heightVal = 4;
        int layerValue = 2;

        TreeUtils.terrain_squareLeavesLayer(terrain, source, x, y - height + heightVal, z, layerValue, Blocks.BLOCK_SPRUCE_LEAVES);
        heightVal--;
        TreeUtils.terrain_diamondLeavesLayer(terrain, source, x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
        heightVal--;
        layerValue--;

        if (height > 8) {
            layerValue++;
            TreeUtils.terrain_squareLeavesLayer(terrain, source, x, y - height + heightVal, z, layerValue, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            TreeUtils.terrain_diamondLeavesLayer(terrain, source, x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            layerValue--;
        }
        if (height > 12) {
            layerValue++;
            TreeUtils.terrain_squareLeavesLayer(terrain, source, x, y - height + heightVal, z, layerValue, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            TreeUtils.terrain_diamondLeavesLayer(terrain, source, x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            layerValue--;
        }

        while (layerValue > 0) {
            TreeUtils.terrain_squareLeavesLayer(terrain, source, x, y - height + heightVal, z, layerValue, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            TreeUtils.terrain_diamondLeavesLayer(terrain, source, x, y - height + heightVal, z, layerValue + 1, Blocks.BLOCK_SPRUCE_LEAVES);
            heightVal--;
            layerValue--;
        }
        terrain.setBlockWorld(x, y - height + heightVal, z, Blocks.BLOCK_SPRUCE_LEAVES);
    }
}
