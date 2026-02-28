///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.tessera.engine.common.world.chunk;
//
//import com.tessera.engine.client.Client;
//import com.tessera.engine.common.world.ServerWorld;
//import com.tessera.engine.common.world.Terrain;
//import com.tessera.engine.common.world.World;
//import com.tessera.engine.server.Registrys;
//import com.tessera.engine.server.block.Block;
//import com.tessera.utils.BFS.ChunkNode;
//import com.tessera.engine.common.math.MathUtils;
//import com.tessera.engine.common.world.wcc.WCCi;
//import org.joml.Vector3i;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.tessera.engine.common.world.chunk.Chunk.WIDTH;
//
///**
// * @author zipCoder933
// */
//public class ChunkSunlightGenerator {
//
//    private static boolean isNeghboringDarkness(Chunk chunk, int x, int y, int z) {
//        if (x - 1 >= 0) {
//            if (chunk.data.getSun(x - 1, y, z) == 0 && chunk.data.getBlock(x - 1, y, z) == 0) {
//                return true;
//            }
//        } else {
//            Chunk neghboringChunk = Client.world
//                    .getChunk(new Vector3i(chunk.position.x - 1, chunk.position.y, chunk.position.z));
//            if (neghboringChunk != null && neghboringChunk.data.getSun(neghboringChunk.data.size.x - 1, y, z) == 0) {
//                return true;
//            }
//        }
//        if (x + 1 < WIDTH) {
//            if (chunk.data.getSun(x + 1, y, z) == 0) {
//                return true;
//            }
//        } else {
//            Chunk neghboringChunk = Client.world
//                    .getChunk(new Vector3i(chunk.position.x + 1, chunk.position.y, chunk.position.z));
//            if (neghboringChunk != null && neghboringChunk.data.getSun(0, y, z) == 0) {
//                return true;
//            }
//        }
//        if (z - 1 >= 0) {
//            if (chunk.data.getSun(x, y, z - 1) == 0) {
//                return true;
//            }
//        } else {
//            Chunk neghboringChunk = Client.world
//                    .getChunk(new Vector3i(chunk.position.x, chunk.position.y, chunk.position.z - 1));
//            if (neghboringChunk != null && neghboringChunk.data.getSun(x, y, neghboringChunk.data.size.z - 1) == 0) {
//                return true;
//            }
//        }
//        if (z + 1 < WIDTH) {
//            if (chunk.data.getSun(x, y, z + 1) == 0) {
//                return true;
//            }
//        } else {
//            Chunk neghboringChunk = Client.world
//                    .getChunk(new Vector3i(chunk.position.x, chunk.position.y, chunk.position.z + 1));
//            if (neghboringChunk != null && neghboringChunk.data.getSun(x, y, 0) == 0) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//
//    private static void addAnyBrightNeighbors(ArrayList<ChunkNode> queue, Chunk chunk, int x, int y, int z) {
//        //Negative x
//        if (x - 1 >= 0) {
//            if (chunk.data.getSun(x - 1, y, z) > 0) {
//                addNodeToQueue(queue, chunk, x - 1, y, z);
//            }
//        } else {
//            Chunk neghboringChunk = Client.world
//                    .getChunk(new Vector3i(chunk.position.x - 1, chunk.position.y, chunk.position.z));
//            if (neghboringChunk != null && neghboringChunk.gen_sunLoaded() && neghboringChunk.data.getSun(neghboringChunk.data.size.x - 1, y, z) > 0) {
//                addNodeToInitialQueue(queue, neghboringChunk, neghboringChunk.data.size.x - 1, y, z, neghboringChunk);
//            }
//        }
//
//        //Positive x
//        if (x + 1 < WIDTH) {
//            if (chunk.data.getSun(x + 1, y, z) > 0) {
//                addNodeToQueue(queue, chunk, x + 1, y, z);
//            }
//        } else {
//            Chunk neghboringChunk = Client.world
//                    .getChunk(new Vector3i(chunk.position.x + 1, chunk.position.y, chunk.position.z));
//            if (neghboringChunk != null && neghboringChunk.gen_sunLoaded() && neghboringChunk.data.getSun(0, y, z) > 0) {
//                addNodeToInitialQueue(queue, neghboringChunk, 0, y, z, neghboringChunk);
//            }
//        }
//
//        //Negative z
//        if (z - 1 >= 0) {
//            if (chunk.data.getSun(x, y, z - 1) > 0) {
//                addNodeToQueue(queue, chunk, x, y, z - 1);
//            }
//        } else {
//            Chunk neghboringChunk = Client.world
//                    .getChunk(new Vector3i(chunk.position.x, chunk.position.y, chunk.position.z - 1));
//            if (neghboringChunk != null && neghboringChunk.gen_sunLoaded() && neghboringChunk.data.getSun(x, y, neghboringChunk.data.size.z - 1) > 0) {
//                addNodeToInitialQueue(queue, neghboringChunk, x, y, neghboringChunk.data.size.z - 1, neghboringChunk);
//            }
//        }
//
//        //Positive z
//        if (z + 1 < WIDTH) {
//            if (chunk.data.getSun(x, y, z + 1) > 0) {
//                addNodeToQueue(queue, chunk, x, y, z + 1);
//            }
//        } else {
//            Chunk neghboringChunk = Client.world
//                    .getChunk(new Vector3i(chunk.position.x, chunk.position.y, chunk.position.z + 1));
//            if (neghboringChunk != null && neghboringChunk.gen_sunLoaded() && neghboringChunk.data.getSun(x, y, 0) > 0) {
//                addNodeToInitialQueue(queue, neghboringChunk, x, y, 0, neghboringChunk);
//            }
//        }
//    }
//
//
//    public static void generateSunlight(ServerChunk thisChunk, Terrain terrain, ServerWorld world) {
//
///**
// * The suns default value when a chunk is create is 15 (full sun)
// * The basic outline of sun generation for a single chunk
// * 1. If we are above the terrain min height, we can generate sunlight because the chunk is above the terrain
// *      a, If not,
// */
//
//        if (thisChunk.position.y * Chunk.HEIGHT <= terrain.minSurfaceHeight) {
//
//        }
//
//        //First, we need a chunk pillar comprising of all the chunks below this chunk including this chunk
//        List<ServerChunk> pillar = new ArrayList<>((World.BOTTOM_Y_CHUNK - thisChunk.position.y) + 1);
//        pillar.add(thisChunk);
//        //Add chunks below us
//        for (int i = 1; i < World.BOTTOM_Y_CHUNK; i++) {
//            ServerChunk chunk = world.getChunk(new Vector3i(thisChunk.position.x, thisChunk.position.y + i, thisChunk.position.z));
//            //If the chunk below us is null or already loaded, skip the rest
//            if (chunk == null || chunk.gen_sunLoaded()) break;
//            else pillar.add(chunk);
//        }
//
//        System.out.println("Pillar size: " + thisChunk.toString() + " ---- " + pillar);
//
//
//        ArrayList<ChunkNode> queue = new ArrayList<>();
//        for (int x = 0; x < WIDTH; x++) {
//            for (int z = 0; z < WIDTH; z++) {
//                boolean addSun = true;
//
//                //Go DOWN from Y
//                for (int i = 0; i < pillar.size(); i++) {
//                    ServerChunk target = pillar.get(i);
//
//                    for (int y = 0; y < Chunk.WIDTH; y++) {
//                        Block block = Registrys.getBlock(target.data.getBlock(x, y, z));
//                        if (addSun) {
//                            if (block.opaque) {
//                                //target.data.setSun(x, y, z, (byte) 0);
//                                addSun = false;
//                            }
//                        } else {
//                            //target.data.setSun(x, y, z, (byte) 0);
//                        }
//                    }
//                }
//            }
//        }
//
//        for (ServerChunk chunk : pillar) {
//            chunk.sunGenerated = true;
//        }
//
////        //Add nodes in a separate loop so that most of the dark nodes are added first
////        /**
////         * If we add nodes in a separate loop, we
////         * + significantly reduce the initial number of nodes
////         * - have to iterate twice
////         */
////        for (int x = 0; x < WIDTH; x++) {
////            for (int z = 0; z < WIDTH; z++) {
////
////                yLoop:
////                for (Chunk chunk : pillarChunk1.pillarInformation.chunks) {// Go DOWN from Y
////                    for (int y = 0; y < Chunk.WIDTH; y++) {
////                        Block block = Registrys.getBlock(chunk.data.getBlock(x, y, z));
////                        if (!block.opaque) {
////                            int sun = chunk.data.getSun(x, y, z);
////                            if (sun == 0) {
//////                                addNodeToQueue(queue,chunk, x, y, z));
////                                addAnyBrightNeighbors(queue, chunk, x, y, z);
////                                break yLoop; //If we propagate downward via BFS, we dont have to check the rest of the chunk.
////                                // This also saves time because addAnyBrightNeighbors() takes a relatively fair amount of time
////                            }
////                        }
////                    }
////                }
////            }
////        }
////
////
////        while (!queue.isEmpty()) {
////            ChunkNode node = removeNode(queue);
//////            node.chunk.data.setSun(node.x, node.y, node.z, (byte) 10); //KEEP. used to visualize where the nodes are placed initially
////
////            byte lightValue = node.chunk.data.getSun(node.x, node.y, node.z);
////
////            //Checking neighbors in the queue might be faster?
//////            if (lightValue == 0) {
//////                checkForLightNeighbor(node.chunk, node.x - 1, node.y, node.z, queue);
//////                checkForLightNeighbor(node.chunk, node.x + 1, node.y, node.z, queue);
//////                checkForLightNeighbor(node.chunk, node.x, node.y, node.z + 1, queue);
//////                checkForLightNeighbor(node.chunk, node.x, node.y, node.z - 1, queue);
//////                checkForLightNeighbor(node.chunk, node.x, node.y + 1, node.z, queue);
//////                checkForLightNeighbor(node.chunk, node.x, node.y - 1, node.z, queue);
//////            } else {
////            checkNeighbor(node.chunk, node.x - 1, node.y, node.z, lightValue, queue, false);
////            checkNeighbor(node.chunk, node.x + 1, node.y, node.z, lightValue, queue, false);
////            checkNeighbor(node.chunk, node.x, node.y, node.z + 1, lightValue, queue, false);
////            checkNeighbor(node.chunk, node.x, node.y, node.z - 1, lightValue, queue, false);
////            checkNeighbor(node.chunk, node.x, node.y + 1, node.z, lightValue, queue, true);
////            checkNeighbor(node.chunk, node.x, node.y - 1, node.z, lightValue, queue, false);
//////            }
////        }
//
//    }
//
//    //DO NOT DELETE THIS CODE!!! KEPT FOR TESTING AND POSSIBLY FUTURE USE
////    private static void checkForLightNeighbor(Chunk chunk, int x, int y, int z, ArrayList<ChunkNode> queue) {
////        int neighborLevel = 0;
////        if (Chunk.inBounds(x, y, z)) {
////            neighborLevel = chunk.data.getSun(x, y, z);
////        } else {//If out of bounds, check the neighboring chunk
////            final Vector3i neighboringChunk = new Vector3i();
////            WCCi.getNeighboringChunk(neighboringChunk, chunk.position, x, y, z);
////
////            chunk = GameScene.world.getChunk(neighboringChunk);
////            if (chunk != null && chunk.gen_sunLoaded()) {
////                x = MathUtils.positiveMod(x, Chunk.WIDTH);
////                y = MathUtils.positiveMod(y, Chunk.WIDTH);
////                z = MathUtils.positiveMod(z, Chunk.WIDTH);
////                neighborLevel = chunk.data.getSun(x, y, z);
////            }
////        }
////        if (neighborLevel > 1) {
////            queue.add(new ChunkNode(chunk, x, y, z));
////        }
////    }
//
//    private static synchronized void checkNeighbor(Chunk chunk, int x, int y, int z, final byte lightLevel,
//                                                   final ArrayList<ChunkNode> queue, boolean isBelow) {
//        Block neigborBlock;
//        if (Chunk.inBounds(x, y, z)) {
//            neigborBlock = Registrys.getBlock(chunk.data.getBlock(x, y, z));
//        } else {
//            final Vector3i neighboringChunk = new Vector3i();
//            WCCi.getNeighboringChunk(neighboringChunk, chunk.position, x, y, z);
//
//            chunk = Client.world.getChunk(neighboringChunk);
//            if (chunk != null) {
//                x = MathUtils.positiveMod(x, Chunk.WIDTH);
//                y = MathUtils.positiveMod(y, Chunk.WIDTH);
//                z = MathUtils.positiveMod(z, Chunk.WIDTH);
//                neigborBlock = Registrys.getBlock(chunk.data.getBlock(x, y, z));
//            } else {
//                return;
//            }
//        }
//
//        if (!neigborBlock.opaque) {
//            if (isBelow && lightLevel == 15) {
//                chunk.data.setSun(x, y, z, (byte) 15);
//                addNodeToQueue(queue, chunk, x, y, z);
//            } else {
//                final int neighborLevel = chunk.data.getSun(x, y, z);
//                if (neighborLevel + 2 <= lightLevel) {
//                    chunk.data.setSun(x, y, z, (byte) (lightLevel - 1));
//                    addNodeToQueue(queue, chunk, x, y, z);
//                }
//            }
//        }
//    }
//
//    private static void addNodeToInitialQueue(ArrayList<ChunkNode> queue,
//                                              Chunk chunk, int x, int y, int z,
//                                              Chunk neghbor) {
//
////        if (Main.devkeyF4) {
//        queue.add(new ChunkNode(chunk, x, y, z));
////        } else {
////
//////Add initially before BFS propagation
////            if (unusedNodes.isEmpty()) {
////                queue.add(new ChunkNode(chunk, x, y, z));
////                Main.frameTester.count("New sun nodes", 1);
////            } else {
////                ChunkNode node = unusedNodes.remove(0);
////                node.set(chunk, x, y, z);
////                queue.add(node);
////            }
////        }
//    }
//
//    private static void addNodeToQueue(ArrayList<ChunkNode> queue, Chunk chunk, int x, int y, int z) {
////        if (Main.devkeyF4) {
//        queue.add(new ChunkNode(chunk, x, y, z));
////        } else {
////            if (unusedNodes.isEmpty()) {
////                queue.add(new ChunkNode(chunk, x, y, z));
////                Main.frameTester.count("New sun nodes", 1);
////            } else {
////                ChunkNode node = unusedNodes.remove(0);
////                node.set(chunk, x, y, z);
////                queue.add(node);
////            }
////        }
//    }
//
////    private static final List<ChunkNode> unusedNodes = Collections.synchronizedList(new ArrayList<ChunkNode>());
//
//    private static ChunkNode removeNode(ArrayList<ChunkNode> queue) {
//        ChunkNode node = queue.remove(0);
////        if (!Main.devkeyF4) unusedNodes.add(node);
//        return node;
//    }
//
//}
