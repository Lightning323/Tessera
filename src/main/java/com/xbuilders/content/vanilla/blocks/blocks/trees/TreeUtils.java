/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.content.vanilla.blocks.blocks.trees;

import com.xbuilders.Main;
import com.xbuilders.content.vanilla.Blocks;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.server.loot.AllLootTables;
import com.xbuilders.engine.common.players.pipeline.BlockHistory;
import com.xbuilders.utils.BFS.TravelNode;
import com.xbuilders.engine.common.world.Terrain;
import com.xbuilders.engine.common.world.chunk.Chunk;
import com.xbuilders.engine.common.math.MathUtils;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import static com.xbuilders.engine.server.block.BlockRegistry.BLOCK_AIR;

/**
 * @author zipCoder933
 */
public class TreeUtils {


    public static int randomInt(Random rand, int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    public static void player_squareLeavesLayer(int x, int y, int z, int radius, short leaves) {
        int lowerBoundX = x - radius;
        int upperBoundX = x + radius;
        int lowerBoundZ = z - radius;
        int upperBoundZ = z + radius;

        for (int x2 = lowerBoundX; x2 <= upperBoundX; x2++) {
            for (int z2 = lowerBoundZ; z2 <= upperBoundZ; z2++) {
                player_setBlock(leaves, x2, y, z2);
            }
        }
    }

    public static void terrain_squareLeavesLayer(Terrain.GenSession terrain, Chunk source, int x, int y, int z, int radius, short leaves) {
        int lowerBoundX = x - radius;
        int upperBoundX = x + radius;
        int lowerBoundZ = z - radius;
        int upperBoundZ = z + radius;

        for (int x2 = lowerBoundX; x2 <= upperBoundX; x2++) {
            for (int z2 = lowerBoundZ; z2 <= upperBoundZ; z2++) {
                if (!Client.world.getBlock(x2, y, z2).solid) {
                    terrain.setBlockWorld(x2, y, z2, leaves);
                }
            }
        }
    }

    public static void player_roundedSquareLeavesLayer(int x, int y, int z, int radius, short leaves) {
        int lowerBoundX = x - radius;
        int upperBoundX = x + radius;
        int lowerBoundZ = z - radius;
        int upperBoundZ = z + radius;

        for (int x2 = lowerBoundX; x2 <= upperBoundX; x2++) {
            for (int z2 = lowerBoundZ; z2 <= upperBoundZ; z2++) {

                if (!((x2 == lowerBoundX && z2 == lowerBoundZ)
                        || (x2 == upperBoundX && z2 == upperBoundZ)
                        || (x2 == lowerBoundX && z2 == upperBoundZ)
                        || (x2 == upperBoundX && z2 == lowerBoundZ))) {
                    player_setBlock(leaves, x2, y, z2);
                }
            }
        }
    }

    public static void terrain_roundedSquareLeavesLayer(Terrain.GenSession terrain, Chunk source, int x, int y, int z, int radius, short leaves) {
        int lowerBoundX = x - radius;
        int upperBoundX = x + radius;
        int lowerBoundZ = z - radius;
        int upperBoundZ = z + radius;

        for (int x2 = lowerBoundX; x2 <= upperBoundX; x2++) {
            for (int z2 = lowerBoundZ; z2 <= upperBoundZ; z2++) {

                if (!((x2 == lowerBoundX && z2 == lowerBoundZ)
                        || (x2 == upperBoundX && z2 == upperBoundZ)
                        || (x2 == lowerBoundX && z2 == upperBoundZ)
                        || (x2 == upperBoundX && z2 == lowerBoundZ))) {
                    if (!Client.world.getBlock(x2, y, z2).solid) {
                        terrain.setBlockWorld(x2, y, z2, leaves);
                    }
                }
            }
        }
    }

    public static void player_setBlock(short id, int x, int y, int z) {
        if (!Client.world.getBlock(x, y, z).solid) {
            Main.getServer().setBlock(id, x, y, z);
        }
    }

    public static void player_setBlockAndOverride(short id, int x, int y, int z) {
        Main.getServer().setBlock(id, x, y, z);
    }

    //Terrain generators create terrains in world space,
    //so there is nothing we have to change here.
    public static void player_diamondLeavesLayer(int x, int y, int z, int travelDist, short leaves) {
        HashSet<TravelNode> exploredNodes = new HashSet<>();
        ArrayList<TravelNode> queue = new ArrayList<>();
        queue.add(new TravelNode(x, y, z, 0));

        while (!queue.isEmpty()) {
            TravelNode node = queue.remove(0);
            if (!exploredNodes.contains(node) && node.travel < travelDist) {
                player_setBlock(leaves, node.x, node.y, node.z);
                queue.add(new TravelNode(node.x + 1, node.y, node.z, node.travel + 1));
                queue.add(new TravelNode(node.x, node.y, node.z + 1, node.travel + 1));
                queue.add(new TravelNode(node.x - 1, node.y, node.z, node.travel + 1));
                queue.add(new TravelNode(node.x, node.y, node.z - 1, node.travel + 1));
                exploredNodes.add(node);
            }
        }
    }


    public static void terrain_diamondLeavesLayer(Terrain.GenSession terrain, Chunk source, int x, int y, int z, int travelDist, short leaves) {
        ArrayList<TravelNode> queue = new ArrayList<>();
        queue.add(new TravelNode(x, y, z, 0));

        while (!queue.isEmpty()) {
            TravelNode node = queue.remove(0);
            Block block = Client.world.getBlock(node.x, node.y, node.z);

            if (!block.equals(leaves) && node.travel < travelDist) {
                if (!block.solid) {
                    terrain.setBlockWorld(node.x, node.y, node.z, leaves);
                }
                queue.add(new TravelNode(node.x + 1, node.y, node.z, node.travel + 1));
                queue.add(new TravelNode(node.x, node.y, node.z + 1, node.travel + 1));
                queue.add(new TravelNode(node.x - 1, node.y, node.z, node.travel + 1));
                queue.add(new TravelNode(node.x, node.y, node.z - 1, node.travel + 1));
            }
        }
    }

    public static Vector3i player_generateBranch(int x, int y, int z, int length, int xDir, int zDir, short logType) {
        for (int i = 0; i < length; i++) {
            x += xDir;
            z += zDir;
            y--;
            player_setBlock(logType, x, y, z);
        }
        return new Vector3i(x, y, z);
    }

    public static Vector3i terrain_generateBranch(Terrain.GenSession terrain, Chunk source, int x, int y, int z, int length, int xDir, int zDir, short logType) {
        for (int i = 0; i < length; i++) {
            x += xDir;
            z += zDir;
            y--;
            if (!Client.world.getBlock(x, y, z).solid) {
                terrain.setBlockWorld(x, y, z, logType);
            }
        }
        return new Vector3i(x, y, z);
    }

    public static void vineEvents(Block vine, int leaf) {
        vine.localChangeEvent(false, (history, changedPos, thisPos) -> eraseVine(thisPos.x, thisPos.y, thisPos.z, vine, leaf));
        vine.randomTickEvent = (int x, int y, int z) -> eraseVine(x, y, z, vine, leaf);
    }

    private static boolean eraseVine(int x, int y, int z, Block vine, int leaf) {
        Block above = Client.world.getBlock(x, y - 1, z);
        if (
                !above.solid
                        && above.id != vine.id
                        && above.id != leaf) {//If there is nothing above us

            for (int i = 0; i < 50; i++) {
                if (Client.world.getBlockID(x, y + i, z) == vine.id) {
                    //Drop loot and erase the vine
                    AllLootTables.blockLootTables.dropLoot(vine.alias, new Vector3f(x, y + i, z));
                    Client.world.setBlock(BLOCK_AIR.id, x, y + i, z);
                } else break;
            }
            return true;
        }
        return false;
    }

    public static Block.RandomTickEvent leafTickEvent(Block leaf) {
        return (int x, int y, int z) -> {
            if (!isLeafOrLog(Client.world.getBlock(x, y + 1, z), leaf.id)) {//If there are no leaves/logs below us

                int connections = 0;
                //Direct
                connections += isLeafOrLog(Client.world.getBlock(x - 1, y, z), leaf.id) ? 1 : 0;
                connections += isLeafOrLog(Client.world.getBlock(x + 1, y, z), leaf.id) ? 1 : 0;
                connections += isLeafOrLog(Client.world.getBlock(x, y, z - 1), leaf.id) ? 1 : 0;
                connections += isLeafOrLog(Client.world.getBlock(x, y, z + 1), leaf.id) ? 1 : 0;
                if (connections > 2)
                    return false; //If there are more than 2 direct facing connections, don't remove the leaf

                //Diagonals
                connections += Client.world.getBlockID(x + 1, y, z + 1) == leaf.id ? 1 : 0;
                connections += Client.world.getBlockID(x - 1, y, z - 1) == leaf.id ? 1 : 0;
                connections += Client.world.getBlockID(x - 1, y, z + 1) == leaf.id ? 1 : 0;
                connections += Client.world.getBlockID(x + 1, y, z - 1) == leaf.id ? 1 : 0;
                if (connections > 3) return false; //If there are more than 3 connections, don't remove the leaf

                //Drop loot and erase the block
                AllLootTables.blockLootTables.dropLoot(leaf.alias, new Vector3f(x, y, z));
                Client.world.setBlock(BLOCK_AIR.id, x, y, z);
                return true;
            }
            return false;
        };
    }


    private static boolean isLeafOrLog(Block block, int leaf) {
        return block.solid || block.id == leaf;
    }

    public static Block.RemoveBlockEvent logRemovalEvent(Block log, Block leaves) {
        return (int x, int y, int z, BlockHistory history) -> {
            System.out.println("Removing log at " + x + ", " + y + ", " + z);

            if (Client.world.getBlockID(x, y - 1, z) != log.id //If there are no logs below, above or around
                    && Client.world.getBlockID(x - 1, y, z) != log.id
                    && Client.world.getBlockID(x + 1, y, z) != log.id
                    && Client.world.getBlockID(x, y, z - 1) != log.id
                    && Client.world.getBlockID(x, y, z + 1) != log.id
                    && Client.world.getBlockID(x - 1, y, z - 1) != log.id
                    && Client.world.getBlockID(x + 1, y, z + 1) != log.id
                    && Client.world.getBlockID(x - 1, y, z + 1) != log.id
                    && Client.world.getBlockID(x + 1, y, z - 1) != log.id
            ) {
                if ( //And there are leaves around it
                        Client.world.getBlockID(x - 1, y, z) == leaves.id
                                || Client.world.getBlockID(x + 1, y, z) == leaves.id
                                || Client.world.getBlockID(x, y, z - 1) == leaves.id
                                || Client.world.getBlockID(x, y, z + 1) == leaves.id
                ) {
                    final int radius = 10;

                    if (findAnotherLog(x, y, z, leaves.id, log.id, radius)) {
                        System.out.println("Found another log");
                        return;
                    }


                    System.out.println("Removing surrounding leaves");
                    HashSet<Vector3i> nodes = new HashSet();
                    addLeafNode(x + 1, y, z, leaves.id, nodes);
                    addLeafNode(x - 1, y, z, leaves.id, nodes);
                    addLeafNode(x, y - 1, z, leaves.id, nodes);
                    addLeafNode(x, y + 1, z, leaves.id, nodes);
                    addLeafNode(x, y, z + 1, leaves.id, nodes);
                    addLeafNode(x, y, z - 1, leaves.id, nodes);


                    while (!nodes.isEmpty()) {
                        Vector3i node = nodes.iterator().next();
                        nodes.remove(node);
                        if (MathUtils.dist(x, y, z, node.x, node.y, node.z) > radius) continue;
                        //If this is a leaf, remove it and add leaves around it
                        if (Client.world.getBlockID(node.x, node.y, node.z) == leaves.id) {

                            //Drop loot and erase the block
                            AllLootTables.blockLootTables.dropLoot(leaves.alias, new Vector3f(node.x, node.y, node.z));
                            Main.getServer().setBlock(Blocks.BLOCK_AIR, node.x, node.y, node.z);

                            addLeafNode(node.x + 1, node.y, node.z, leaves.id, nodes);
                            addLeafNode(node.x - 1, node.y, node.z, leaves.id, nodes);
                            addLeafNode(node.x, node.y - 1, node.z, leaves.id, nodes);
                            addLeafNode(node.x, node.y + 1, node.z, leaves.id, nodes);
                            addLeafNode(node.x, node.y, node.z + 1, leaves.id, nodes);
                            addLeafNode(node.x, node.y, node.z - 1, leaves.id, nodes);
                        }
                    }

                }

            }

        };
    }

    private static boolean findAnotherLog(int x, int y, int z, short leafBlock, short logBlock, int maxRadius) {
        HashSet<Vector3i> nodes = new HashSet();
        HashSet<Vector3i> searchedNodes = new HashSet();
        addCheckLogNode(x + 1, y, z, leafBlock, 0, nodes, searchedNodes);
        addCheckLogNode(x - 1, y, z, leafBlock, 0, nodes, searchedNodes);
        addCheckLogNode(x, y, z + 1, leafBlock, 0, nodes, searchedNodes);
        addCheckLogNode(x, y, z - 1, leafBlock, 0, nodes, searchedNodes);

        while (!nodes.isEmpty()) {
            Vector3i node = nodes.iterator().next();
            nodes.remove(node);
            if (MathUtils.dist(x, y, z, node.x, node.y, node.z) > maxRadius) continue;
            if (Client.world.getBlockID(node.x, node.y, node.z) == leafBlock) {
                searchedNodes.add(node);
                addCheckLogNode(node.x + 1, node.y, node.z, leafBlock, logBlock, nodes, searchedNodes);
                addCheckLogNode(node.x - 1, node.y, node.z, leafBlock, logBlock, nodes, searchedNodes);
                addCheckLogNode(node.x, node.y - 1, node.z, leafBlock, logBlock, nodes, searchedNodes);
                addCheckLogNode(node.x, node.y + 1, node.z, leafBlock, logBlock, nodes, searchedNodes);
                addCheckLogNode(node.x, node.y, node.z + 1, leafBlock, logBlock, nodes, searchedNodes);
                addCheckLogNode(node.x, node.y, node.z - 1, leafBlock, logBlock, nodes, searchedNodes);
            } else if (Client.world.getBlockID(node.x, node.y, node.z) == logBlock) {
                return true;
            }
        }
        return false;
    }

    private static void addCheckLogNode(int x, int y, int z,
                                        int leafBlock, int logBlock,
                                        HashSet<Vector3i> nodes, HashSet<Vector3i> searchedNodes) {
        Vector3i v = new Vector3i(x, y, z);
        if ((Client.world.getBlockID(x, y, z) == leafBlock ||
                Client.world.getBlockID(x, y, z) == logBlock)
                && !searchedNodes.contains(v)) {
            nodes.add(v);
        }
    }

    private static void addLeafNode(int x, int y, int z, short leafBlock, HashSet<Vector3i> nodes) {
        if (Client.world.getBlockID(x, y, z) == leafBlock) {
            nodes.add(new Vector3i(x, y, z));
        }
    }

}
