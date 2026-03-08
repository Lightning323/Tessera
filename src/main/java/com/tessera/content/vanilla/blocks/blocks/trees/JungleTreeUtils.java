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
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Random;

import static com.tessera.engine.common.math.RandomUtils.randInt;

/**
 * @author zipCoder933
 */
public class JungleTreeUtils {
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

    static class VineBranchPair {

        public ArrayList<Vector3i> vines;
        public ArrayList<Vector3i> branches;
    }

    private static VineBranchPair setVinesAndBranches(Random random, int x, int z, int radius) {
        int lowerBoundX = x - radius;
        int upperBoundX = x + radius;
        int lowerBoundZ = z - radius;
        int upperBoundZ = z + radius;
        VineBranchPair ret = new VineBranchPair();

        ret.vines = new ArrayList<>();
        ret.branches = new ArrayList<>();

        for (int x2 = lowerBoundX; x2 <= upperBoundX; x2++) {
            for (int z2 = lowerBoundZ; z2 <= upperBoundZ; z2++) {

                if (!((x2 == lowerBoundX && z2 == lowerBoundZ)
                        || (x2 == upperBoundX && z2 == upperBoundZ)
                        || (x2 == lowerBoundX && z2 == upperBoundZ)
                        || (x2 == upperBoundX && z2 == lowerBoundZ))) {
                    if (!(x2 == 0 && z2 == 0)) {
                        if (random.nextFloat() > 0.95) {
                            ret.branches.add(new Vector3i(x2, 0, z2));
                        } else if (random.nextFloat() > 0.96) {
                            ret.vines.add(new Vector3i(x2, 0, z2));
                        }
                    }
                }
            }
        }
        return ret;
    }

    final static int MIN_HEIGHT = 6;
    final static int MAX_HEIGHT = 17;

    public static void player_plantTree(Random rand, int x, int y, int z) {
        int height = randInt(rand, MIN_HEIGHT, MAX_HEIGHT);
        int firstLayerWide = 0;
        firstLayerWide = randInt(rand, 2, 4);
        TreeUtils.player_roundedSquareLeavesLayer(x, (y - height + 2), z, firstLayerWide, Blocks.BLOCK_JUNGLE_LEAVES);
        TreeUtils.player_roundedSquareLeavesLayer(x + randInt(rand, -1, 1),
                (y - height + 1),
                z + randInt(rand, -1, 1),
                firstLayerWide, Blocks.BLOCK_JUNGLE_LEAVES);

        VineBranchPair vb = setVinesAndBranches(rand, x, z, firstLayerWide);
        int h4 = (int) (height * 0.4);
        for (int k = 0; k < height; k++) {
            Main.getServer().setBlock(Blocks.BLOCK_JUNGLE_LOG, x, y - k, z);
            if (k < height - 1) {
                if (k > h4) {
                    for (Vector3i branch : vb.branches) {
                        TreeUtils.player_setBlock(Blocks.BLOCK_JUNGLE_LEAVES, branch.x, y - k, branch.z);
                    }
                }
                for (Vector3i vine : vb.vines) {
                    TreeUtils.player_setBlock(Blocks.BLOCK_VINES, vine.x, y - k, vine.z);
                }
            }
        }

        TreeUtils.player_diamondLeavesLayer(x, y - height, z, 3, Blocks.BLOCK_JUNGLE_LEAVES);
        if (rand.nextDouble() > 0.8) {
            TreeUtils.player_diamondLeavesLayer(x, y - height - 1, z, 2, Blocks.BLOCK_JUNGLE_LEAVES);
        }
    }

    public static void terrain_plantTree(Terrain.GenSession terrain, Chunk source, int x, int y, int z) {
        int height = randInt(terrain.random, MIN_HEIGHT, MAX_HEIGHT);
        int firstLayerWide = 0;
        firstLayerWide = randInt(terrain.random, 2, 4);
        TreeUtils.terrain_roundedSquareLeavesLayer(terrain, source, x, (y - height + 2), z, firstLayerWide, Blocks.BLOCK_JUNGLE_LEAVES);
        TreeUtils.terrain_roundedSquareLeavesLayer(terrain, source,
                x + randInt(terrain.random, -1, 1),
                (y - height + 1),
                z + randInt(terrain.random, -1, 1),
                firstLayerWide, Blocks.BLOCK_JUNGLE_LEAVES);

        VineBranchPair vb = setVinesAndBranches(terrain.random, x, z, firstLayerWide);
        int h4 = (int) (height * 0.4);
        for (int k = 0; k < height; k++) {
            terrain.setBlockWorld(x, y - k, z, Blocks.BLOCK_JUNGLE_LOG);
            if (k < height - 1) {
                if (k > h4) {
                    for (Vector3i branch : vb.branches) {
                        if (branch.x != x && branch.z != z) {
                            terrain.setBlockWorld(branch.x, y - k, branch.z, Blocks.BLOCK_JUNGLE_LEAVES);
                        }
                    }
                }
                for (Vector3i vine : vb.vines) {
                    if (vine.x != x && vine.z != z) {
                        terrain.setBlockWorld(vine.x, y - k, vine.z, Blocks.BLOCK_VINES);
                    }
                }
            }
        }

        TreeUtils.terrain_diamondLeavesLayer(terrain, source, x, y - height, z, 3, Blocks.BLOCK_JUNGLE_LEAVES);
        if (terrain.random.nextDouble() > 0.8) {
            TreeUtils.terrain_diamondLeavesLayer(terrain, source, x, y - height - 1, z, 2, Blocks.BLOCK_JUNGLE_LEAVES);
        }
    }
}
