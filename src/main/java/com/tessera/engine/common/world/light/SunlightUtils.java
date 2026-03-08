package com.tessera.engine.common.world.light;

import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.utils.MiscUtils;
import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.utils.BFS.ChunkNode;
import com.tessera.engine.common.world.wcc.WCCi;
import org.joml.Vector3i;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.tessera.engine.common.math.MathUtils.positiveMod;
import static com.tessera.engine.common.world.wcc.WCCi.chunkDiv;

/**
 * We can save time in propagation by using BFS to propagate downward instead of setting the nodes initially.
 * <p>
 * BLOCK TRANSPARENT-> OPAQUE LIGHT DE-PROGATION
 * 1. We first black out everything underneath the block
 * 2. We Erase any light that is around the block that may now be occluded by the darkness
 * 2.1. We only erase light that is lower than 15 (not in direct line of sky)
 * 2.2. If there is light that is separated by darkness, and we cannot reach it with our erasure BFS, that means that the light will not be effected by our erasure
 * 3. We re-propagate the light from the edge of the erased area
 */
public class SunlightUtils {


    public static void addNodeForErasure(List<ChunkNode> queue, Chunk chunk, int x, int y, int z) {
        queue.add(new ChunkNode(chunk, x, y, z));
    }

    public static void addNodeForPropagation(List<ChunkNode> queue, final Chunk c,
                                             final int x, final int y, final int z) {
        //In this method, We just get the brightest neighboring node and add it to the queue
        AtomicInteger brightestSunlight = new AtomicInteger(0);
        ChunkNode brightestNode = null;
        WCCi wcc = new WCCi();
//        System.out.println("Searching for brightest node " + x + " " + y + " " + z);

        wcc.setNeighboring(c.position, x, y - 1, z);
        ChunkNode node = checkNeighbor_getBrightestNeighboringNode(wcc, brightestSunlight);
        if (node != null) {
            brightestNode = node;
        }

        wcc.setNeighboring(c.position, x - 1, y, z);
        node = checkNeighbor_getBrightestNeighboringNode(wcc, brightestSunlight);
        if (node != null) {
            brightestNode = node;
        }

        wcc.setNeighboring(c.position, x, y, z - 1);
        node = checkNeighbor_getBrightestNeighboringNode(wcc, brightestSunlight);
        if (node != null) {
            brightestNode = node;
        }

        wcc.setNeighboring(c.position, x + 1, y, z);
        node = checkNeighbor_getBrightestNeighboringNode(wcc, brightestSunlight);
        if (node != null) {
            brightestNode = node;
        }

        wcc.setNeighboring(c.position, x, y, z + 1);
        node = checkNeighbor_getBrightestNeighboringNode(wcc, brightestSunlight);
        if (node != null) {
            brightestNode = node;
        }

        wcc.setNeighboring(c.position, x, y + 1, z);
        node = checkNeighbor_getBrightestNeighboringNode(wcc, brightestSunlight);
        if (node != null) {
            brightestNode = node;
        }


        if (brightestNode != null) {
//            System.out.println("Found brightest node: " + brightestNode + " light: " + brightestSunlight.get());
            queue.add(brightestNode);
        }
    }

    private static ChunkNode checkNeighbor_getBrightestNeighboringNode(WCCi coords,
                                                                       AtomicInteger brightestSunlight) {

//        System.out.println("\tChecking: " + coords.getChunk(GameScene.world) + " " + coords.toString());
        Chunk coordsChunk = coords.getChunk(Client.world);
        if (coordsChunk != null) {
            int lightVal = coordsChunk.voxels.getSun(coords.chunkVoxel.x, coords.chunkVoxel.y, coords.chunkVoxel.z);
//            System.out.println("\t\tNeighboring: " + MiscUtils.printVector(coordsChunk.position) + "): " + lightVal + " brightest: " + brightestSunlight.get());
            if (lightVal > brightestSunlight.get()) {
//                System.out.println("\t\t\tNew brightest: " + lightVal);
                brightestSunlight.set(lightVal);
                return new ChunkNode(coordsChunk, coords.chunkVoxel.x, coords.chunkVoxel.y, coords.chunkVoxel.z);
            }
        }
        return null;
    }

    public static void println(String s) {
        ClientWindow.printlnDev(s);
    }

    public static long updateFromQueue(
            List<ChunkNode> opaqueToTransparent,
            List<ChunkNode> transparentToOpaque,
            HashSet<Chunk> affectedChunks, Consumer<Long> callback) {


//        println("\n\nUpdating sunlight:");
        AtomicBoolean complete = new AtomicBoolean();
        AtomicLong start = new AtomicLong(System.currentTimeMillis());
        new Thread(() -> {
            try {
                for (int i = 0; i < 60; i++) {//We wait as long as 1 minute
                    Thread.sleep(1000);
                    if (complete.get()) return;
                    else callback.accept(System.currentTimeMillis() - start.get());
                }
            } catch (InterruptedException e) {
            }
        }).start();

        if (!transparentToOpaque.isEmpty()) {
            HashSet<ChunkNode> repropagationNodes = new HashSet<>();
            eraseSunlight(transparentToOpaque, affectedChunks, repropagationNodes);
            transparentToOpaque.clear();

            if (!repropagationNodes.isEmpty()) {
                transparentToOpaque.addAll(opaqueToTransparent);
                transparentToOpaque.addAll(repropagationNodes);
                propagateSunlight(transparentToOpaque, affectedChunks);
            } else if (!opaqueToTransparent.isEmpty()) {
                propagateSunlight(opaqueToTransparent, affectedChunks);
            }
        } else if (!opaqueToTransparent.isEmpty()) {
            propagateSunlight(opaqueToTransparent, affectedChunks);
        }

        opaqueToTransparent.clear();
        transparentToOpaque.clear();
        complete.set(true);

//        println("Finished with sunlight, " + (System.currentTimeMillis() - start.get()) / 1000 + "s");
        return System.currentTimeMillis() - start.get();
    }


    public static void eraseSunlight(List<ChunkNode> nodes, HashSet<Chunk> affectedChunks,
                                     HashSet<ChunkNode> repropagationNodes) {

        if (nodes.isEmpty()) return;
        //Create a boundary and erase everything below that boundary:
        AABB queueBox = new AABB();

        queueBox.min.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        queueBox.max.set(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        //Find the min and max of all the nodes
        for (ChunkNode node : nodes) {
            int worldX = (node.chunk.position.x * Chunk.WIDTH) + node.x;
            int worldY = (node.chunk.position.y * Chunk.WIDTH) + node.y;
            int worldZ = (node.chunk.position.z * Chunk.WIDTH) + node.z;

            //We have to check both because the first node needs to set the boundaries especially if it is the only node
            if (worldY < queueBox.min.y) queueBox.min.y = worldY;
            if (worldY > queueBox.max.y) queueBox.max.y = worldY;

            if (worldZ < queueBox.min.z) queueBox.min.z = worldZ;
            if (worldZ > queueBox.max.z) queueBox.max.z = worldZ;

            if (worldX < queueBox.min.x) queueBox.min.x = worldX;
            if (worldX > queueBox.max.x) queueBox.max.x = worldX;
        }
        nodes.clear();

        //Erase all nodes in the box and go down until we hit all black nodes
        repropagationNodes.clear();

        //Add repropagation nodes above the top of the box
        for (int wx = (int) queueBox.min.x - 1; wx <= queueBox.max.x + 1; wx++) {
            for (int wz = (int) queueBox.min.z - 1; wz <= queueBox.max.z + 1; wz++) {
                WCCi wcc = new WCCi().set(wx, (int) (queueBox.min.y - 1), wz);
                Chunk chunk = wcc.getChunk(Client.world);
                if (chunk == null) continue;
                byte sun = chunk.voxels.getSun(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                if (sun > 1) {
                    affectedChunks.add(chunk);
                    repropagationNodes.add(new ChunkNode(chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z));
                }
            }
        }

        //Propagate darkness from the top of the box
        downwardLoop:
        for (int wy = (int) queueBox.min.y; true; wy++) {
            boolean foundLight = false;
            for (int wx = (int) queueBox.min.x - 1; wx <= queueBox.max.x + 1; wx++) {
                for (int wz = (int) queueBox.min.z - 1; wz <= queueBox.max.z + 1; wz++) {
                    WCCi wcc = new WCCi().set(wx, wy, wz);
                    Chunk chunk = wcc.getChunk(Client.world);
                    if (chunk == null) continue;
                    affectedChunks.add(chunk);

                    //We also want to check the perimiter of the boundary for extra nodes to erase or repropagate
                    if (wx > queueBox.max.x || wx < queueBox.min.x || wz > queueBox.max.z || wz < queueBox.min.z) { //if out of bounds
                        byte sun = chunk.voxels.getSun(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                        if (sun < 15 && sun > 0) {//Add any nodes greater than 1 to a erasure BFS
                            nodes.add(new ChunkNode(chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z));
                        }
                        //Reduntant:
//                        else if (sun > 1 && wy == queueBox.min.y) { //Add any nodes greater than 1 to a repropagation BFS
//                            repropagationNodes.add(new ChunkNode(chunk, wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z));
//                        }
                    } else {
                        if (chunk.voxels.getSun(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z) > 0) {
                            foundLight = true;
                        }
                        chunk.voxels.setSun(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, (byte) 0);
                    }
                }
            }
            if (!foundLight) break downwardLoop;
        }
        //  println("Finished darkening boundary: " + queueBox.toString() + " x-len: " + ((int) queueBox.getXLength()) + " z-len: " + ((int) queueBox.getZLength()));


        //Now do a BFS with the remaining nodes
        HashSet<ChunkNode> BFS_repropNodes = new HashSet<>();
        while (!nodes.isEmpty()) {
            ChunkNode node = nodes.remove(nodes.size() - 1);//Remove the last node (its faster this way)
            byte lightValue = node.chunk.voxels.getSun(node.x, node.y, node.z);
            node.chunk.voxels.setSun(node.x, node.y, node.z, (byte) 0);
            affectedChunks.add(node.chunk);
            BFS_repropNodes.remove(node);
            checkNeighborErase(node.chunk, node.x - 1, node.y, node.z, lightValue, nodes, BFS_repropNodes, false);
            checkNeighborErase(node.chunk, node.x + 1, node.y, node.z, lightValue, nodes, BFS_repropNodes, false);
            checkNeighborErase(node.chunk, node.x, node.y, node.z + 1, lightValue, nodes, BFS_repropNodes, false);
            checkNeighborErase(node.chunk, node.x, node.y, node.z - 1, lightValue, nodes, BFS_repropNodes, false);
            checkNeighborErase(node.chunk, node.x, node.y + 1, node.z, lightValue, nodes, BFS_repropNodes, true);
            checkNeighborErase(node.chunk, node.x, node.y - 1, node.z, lightValue, nodes, BFS_repropNodes, false);
        }
        repropagationNodes.addAll(BFS_repropNodes);
        //I think that when the propagation takes to long it is because there are so many BFS repropagation nodes
        // println ("Finished erasing BFS nodes (BFS reprop " + BFS_repropNodes.size() + ", total " + repropagationNodes.size() + ")");
    }


// Experimental method for optimizing light erasure DO NOT DELETE!!!
//      if (isBelow) {
//        while (true) {
//            node.y--;
//
//            if (y >= Chunk.WIDTH) {
//                int coordsY = node.chunk.position.y + 1;
//                node.chunk = GameScene.world.getChunk(new Vector3i(node.chunk.position.x, coordsY, node.chunk.position.z));
//                if (node.chunk == null) return;
//            }
//            node.y = positiveMod(node.y, Chunk.WIDTH);
//            if (ItemList.getBlock(node.chunk.data.getBlock(node.x, node.y, node.z)).opaque) break;
//            else {
//                chunk.data.setSun(x, y, z, (byte) 0);
//            }
//        }
//    }

    private static void checkNeighborErase(Chunk chunk, int x, int y, int z, int centerLightLevel,
                                           List<ChunkNode> queue, HashSet<ChunkNode> repropNodes,
                                           boolean isBelow) {
        byte thisLevel;
        if (chunk.inBounds(x, y, z)) {
            thisLevel = chunk.voxels.getSun(x, y, z);
        } else {
            WCCi newCoords = new WCCi().setNeighboring(chunk.position, x, y, z);
            chunk = Client.world.getChunk(newCoords.chunk);
            x = newCoords.chunkVoxel.x;
            y = newCoords.chunkVoxel.y;
            z = newCoords.chunkVoxel.z;
            if (chunk == null) {
                return;
            }
            thisLevel = chunk.voxels.getSun(x, y, z);
        }
        if (thisLevel > 0) {
            ChunkNode node = new ChunkNode(chunk, x, y, z);
            if (centerLightLevel >= thisLevel && thisLevel < 15) {
                queue.add(node);
            } else if (thisLevel > 1) { //This is actually important
                int wx = (chunk.position.x * Chunk.WIDTH) + x;
                int wy = (chunk.position.y * Chunk.WIDTH) + y;
                int wz = (chunk.position.z * Chunk.WIDTH) + z;
                if (MiscUtils.isBlackCube(wx, wy, wz)) {//Reduces the initial nodes
                    repropNodes.add(node);
                }
            }
        }
    }

    public static void propagateSunlight(List<ChunkNode> queue, HashSet<Chunk> affectedChunks) {
        //This repropagation is now the bottleneck, Erasure is instantaneous
        /**
         * IMPORTANT NOTE ABOUT ANY LIGHT PROPAGATION:
         * Start with as few nodes as possible!
         * The less nodes we start with, the faster we can finish the job.
         */
        //  println("Starting propagating nodes: " + queue.size());
        while (!queue.isEmpty()) {
            ChunkNode node = queue.remove(0);
            if (node == null) continue;
            byte lightValue = node.chunk.voxels.getSun(node.x, node.y, node.z);
            affectedChunks.add(node.chunk);

            checkNeighbor(node.chunk, node.x - 1, node.y, node.z, lightValue, queue, false);
            checkNeighbor(node.chunk, node.x + 1, node.y, node.z, lightValue, queue, false);
            checkNeighbor(node.chunk, node.x, node.y, node.z + 1, lightValue, queue, false);
            checkNeighbor(node.chunk, node.x, node.y, node.z - 1, lightValue, queue, false);
            checkNeighbor(node.chunk, node.x, node.y + 1, node.z, lightValue, queue, true);
            checkNeighbor(node.chunk, node.x, node.y - 1, node.z, lightValue, queue, false);
//            System.out.println("Queue size: " + queue.size());
        }
    }

    private static void checkNeighbor(Chunk chunk, int x, int y, int z, int lightLevel,
                                      List<ChunkNode> queue,
                                      boolean isBelowNode) {

        Block neigborBlock;
        if (Chunk.inBounds(x, y, z)) {
            neigborBlock = Registrys.getBlock(chunk.voxels.getBlock(x, y, z));
        } else {
            final Vector3i neighboringChunk = new Vector3i();
            WCCi.getNeighboringChunk(neighboringChunk, chunk.position, x, y, z);

            chunk = Client.world.getChunk(neighboringChunk);
            if (chunk != null) {
                x = MathUtils.positiveMod(x, Chunk.WIDTH);
                y = MathUtils.positiveMod(y, Chunk.WIDTH);
                z = MathUtils.positiveMod(z, Chunk.WIDTH);
                neigborBlock = Registrys.getBlock(chunk.voxels.getBlock(x, y, z));
            } else return;
        }
        if (neigborBlock != null && !neigborBlock.opaque) {
            ChunkNode node = new ChunkNode(chunk, x, y, z);

//            if (isBelowNode && lightLevel == 15) {
//                while (true) {
//                    node.y++;
//                    if (y >= Chunk.WIDTH) {
//                        node.chunk = GameScene.world.getChunk(new Vector3i(
//                                node.chunk.position.x,
//                                node.chunk.position.y + 1,
//                                node.chunk.position.z));
//                        if (node.chunk == null) return;
//                    }
//                    node.y = positiveMod(node.y, Chunk.WIDTH);
//                    if (ItemList.getBlock(node.chunk.data.getBlock(node.x, node.y, node.z)).opaque) break;
//                    else {
//                        chunk.data.setSun(x, y, z, (byte) 15);
//                        affectedChunks.add(chunk);
//                    }
//                }
//            }
            if (isBelowNode && lightLevel == 15) {
                chunk.voxels.setSun(x, y, z, (byte) 15);
                queue.add(node);
            } else if (chunk.voxels.getSun(x, y, z) + 2 <= lightLevel) {
                chunk.voxels.setSun(x, y, z, (byte) (lightLevel - 1));
                queue.add(node);
            }
        }
    }
}

