package com.tessera.content.vanilla.terrain.defaultTerrain;

import com.tessera.engine.common.world.Terrain.GenSession;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.content.vanilla.Blocks;
import com.tessera.content.vanilla.blocks.blocks.trees.BirchTreeUtils;
import com.tessera.content.vanilla.blocks.blocks.trees.OakTreeUtils;
import com.tessera.content.vanilla.blocks.blocks.trees.SpruceTreeUtils;

public class DefaultTerrainUtils {


    public static void plantRandomTree(GenSession session, float alpha, final Chunk sourceChunk, final int x, final int y, final int z) {

        //Noise tends to be more biased towards the center, meaning we either have to normalize the fastNoise function
        //somehow to produce even distribution, or we have to favor the edges more
        float bias = alpha + ((session.random.nextFloat() - 0.5f) * 0.2f);

        if (bias > 0.3) {
            SpruceTreeUtils.terrain_plantTree(session, sourceChunk, x, y, z);
        } else if (bias < -0.3) {
            BirchTreeUtils.terrain_plantTree(session, sourceChunk, x, y, z);
        } else {
            OakTreeUtils.terrain_plantTree(session, sourceChunk, x, y, z);
        }
//        switch (session.random.nextInt(3)) {
//            case 0: {
//                OakTreeUtils.terrain_plantTree(session, sourceChunk, x, y, z);
//                break;
//            }
//            case 1: {
//                BirchTreeUtils.terrain_plantTree(session, sourceChunk, x, y, z);
//                break;
//            }
//            case 2: {
//                SpruceTreeUtils.terrain_plantTree(session, sourceChunk, x, y, z);
//                break;
//            }
//        }
    }

// public void placeHorse(GenSession session, final Chunk chunk, final int cx, final int wy, final int cz) {
//     switch (session.random.nextInt(6)) {
//         case 0: {
//             this.terrain.setEntity(chunk, GameItems.CHESTNUT_HORSE, cx, wy, cz);
//             break;
//         }
//         case 1: {
//             this.terrain.setEntity(chunk, GameItems.BROWN_HORSE, cx, wy, cz);
//             break;
//         }
//         case 2: {
//             this.terrain.setEntity(chunk, GameItems.WHITE_HORSE, cx, wy, cz);
//             break;
//         }
//         case 3: {
//             this.terrain.setEntity(chunk, GameItems.BLACK_HORSE, cx, wy, cz);
//             break;
//         }
//         case 4: {
//             this.terrain.setEntity(chunk, GameItems.CREAMY_HORSE, cx, wy, cz);
//             break;
//         }
//         case 5: {
//             this.terrain.setEntity(chunk, GameItems.DARK_BROWN_HORSE, cx, wy, cz);
//             break;
//         }
//         case 6: {
//             this.terrain.setEntity(chunk, GameItems.GRAY_HORSE, cx, wy, cz);
//             break;
//         }
//     }
// }

// public boolean isStone(final Block b) {
//     if (b != GameItems.BLOCK_DIORITE) {
//         if (b != GameItems.BLOCK_ANDESITE) {
//             if (b != GameItems.BlockCobblestone) {
//                 if (b != GameItems.RedSandstone) {
//                     return false;
//                 }
//             }
//         }
//     }
//     return true;
// }

    public static short randomFlower(GenSession session) {
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

// public void propagateCoral(final int x, final int y, final int z, final Block block, final Block coralBlock1, final Block coralBlock2) {
//     final ListQueue<TravelNode> queue = new ListQueue<>();
//     final int maxTravel = MiscUtils.randomInt(session.random, 7, 8);
//     queue.add(new TravelNode(x, y, z, 0));
//     while (queue.containsNodes()) {
//         final TravelNode node = queue.getAndRemove(0);
//         final int travel = node.getTravel();
//         final Vector3i coords = node.getCoords();
//         if (travel < maxTravel) {
//             this.checkNeighbor2(coords.x - 1, coords.y, coords.z, queue, travel, block, coralBlock1, coralBlock2);
//             this.checkNeighbor2(coords.x + 1, coords.y, coords.z, queue, travel, block, coralBlock1, coralBlock2);
//             this.checkNeighbor2(coords.x, coords.y, coords.z + 1, queue, travel, block, coralBlock1, coralBlock2);
//             this.checkNeighbor2(coords.x, coords.y, coords.z - 1, queue, travel, block, coralBlock1, coralBlock2);
//             this.checkNeighbor2(coords.x, coords.y + 1, coords.z, queue, travel, block, coralBlock1, coralBlock2);
//             this.checkNeighbor2(coords.x, coords.y - 1, coords.z, queue, travel, block, coralBlock1, coralBlock2);
//         }
//     }
// }

// public boolean penetrableByCoral(final Block b) {
//     if (!b.isLiquid()) {
//         if (b != GameItems.SeaGrass) {
//             return false;
//         }
//     }
//     return true;
// }

// public void checkNeighbor2(final int x, final int y, final int z, final ListQueue<TravelNode> queue, final int travel, final Block block, final Block coralBlock1, final Block coralBlock2) {
//     final Block b = this.terrain.getPointerHandler().getWorld().getBlock(x, y, z);
//     final Block higherBlock = this.terrain.getPointerHandler().getWorld().getBlock(x, y - 1, z);
//     if (b.isSolid()) {
//         if (!b.name.toLowerCase().contains("coral") && b != block && this.travelOdds(session.random, travel)) {
//             if (travel <= 5) {
//                 this.terrain.getPointerHandler().getWorld().setBlock(block, x, y, z);
//             }
//             if (y > this.WATER_LEVEL + 3 && this.penetrableByCoral(higherBlock)) {
//                 if (session.random.nextFloat() > 0.8) {
//                     this.terrain.getPointerHandler().getWorld().setBlock(coralBlock1, x, y - 1, z);
//                 } else if (session.random.nextFloat() > 0.5) {
//                     this.terrain.getPointerHandler().getWorld().setBlock(coralBlock2, x, y - 1, z);
//                 } else {
//                     final Block block2 = higherBlock;
//                     if (block2 != GameItems.BLOCK_WATER) {
//                         final World world = this.terrain.getPointerHandler().getWorld();
//                         world.setBlock(GameItems.BLOCK_WATER, x, y - 1, z);
//                     }
//                 }
//             }
//             queue.add(new TravelNode(x, y, z, travel + 1));
//         }
//     }
// }

// private boolean travelOdds(final Random rand, final int travel) {
//     return travel <= 2 || rand.nextFloat() > 0.2;
// }
}
