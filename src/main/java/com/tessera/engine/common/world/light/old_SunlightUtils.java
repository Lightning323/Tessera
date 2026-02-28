package com.tessera.engine.common.world.light;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.utils.BFS.ChunkNode;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;
import org.joml.Vector3i;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
public class old_SunlightUtils {
    public static void addInitialNodesForSunlightPropagation(List<ChunkNode> queue, final Chunk c,
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

    public static void updateFromQueue(
            List<ChunkNode> opaqueToTransparent,
            List<ChunkNode> transparentToOpaque,
            HashSet<Chunk> affectedChunks) {

        if (transparentToOpaque.size() > 0) {
            HashSet<ChunkNode> repropagationNodes = new HashSet<>();
            eraseSunlight(transparentToOpaque, affectedChunks, repropagationNodes);
            transparentToOpaque.clear();

            if (!repropagationNodes.isEmpty()) {
                transparentToOpaque.addAll(opaqueToTransparent);
                transparentToOpaque.addAll(repropagationNodes);
                propagateSunlight(transparentToOpaque, affectedChunks, true);
            } else if (opaqueToTransparent.size() > 0) {
                propagateSunlight(opaqueToTransparent, affectedChunks, true);
            }
        } else if (opaqueToTransparent.size() > 0) {
            propagateSunlight(opaqueToTransparent, affectedChunks, true);
        }
        opaqueToTransparent.clear();
        transparentToOpaque.clear();
    }


    /**
     * Put the x y z position on the actual block that was turned to opaque, NOT below it
     *
     * @param queue
     * @param chunk
     * @param x
     * @param y
     * @param z
     */
    public static void addInitialNodesForSunlightErasure(List<ChunkNode> queue, Chunk chunk, int x, int y, int z) {
        Block block = BlockRegistry.BLOCK_AIR;
        if(chunk == null) {
            return;
        }
        queue.add(new ChunkNode(chunk, x, y, z));

        while (true) {
            y++;
            if (chunk.inBoundsY(y)) {
                block = Registrys.getBlock(chunk.voxels.getBlock(x, y, z));
            } else {
                WCCi newCoords = new WCCi().setNeighboring(chunk.position, x, y, z);
                chunk = Client.world.getChunk(newCoords.chunk);
                x = newCoords.chunkVoxel.x;
                y = newCoords.chunkVoxel.y;
                z = newCoords.chunkVoxel.z;
                if (chunk != null) {
                    block = Registrys.getBlock(chunk.voxels.getBlock(x, y, z));
                }
            }
            if (block == null || block.opaque) {
                break;
            } else {
                chunk.voxels.setSun(x, y, z, (byte) 0);
                queue.add(new ChunkNode(chunk, x, y, z));
            }
        }
//        System.out.println("Initial nodes: " + queue.size());
    }

    public static void eraseSunlight(List<ChunkNode> queue, HashSet<Chunk> affectedChunks,
                                     HashSet<ChunkNode> repropagationNodes) {
        while (queue.size() > 0) {
            ChunkNode node = queue.remove(0);
            if (node == null) continue;
            byte lightValue = node.chunk.voxels.getSun(node.x, node.y, node.z);
            node.chunk.voxels.setSun(node.x, node.y, node.z, (byte) 0);
            affectedChunks.add(node.chunk);
            repropagationNodes.remove(node);
            checkNeighborErase(node.chunk, node.x - 1, node.y, node.z, lightValue, queue, repropagationNodes, false);
            checkNeighborErase(node.chunk, node.x + 1, node.y, node.z, lightValue, queue, repropagationNodes, false);
            checkNeighborErase(node.chunk, node.x, node.y, node.z + 1, lightValue, queue, repropagationNodes, false);
            checkNeighborErase(node.chunk, node.x, node.y, node.z - 1, lightValue, queue, repropagationNodes, false);
            checkNeighborErase(node.chunk, node.x, node.y + 1, node.z, lightValue, queue, repropagationNodes, true);
            checkNeighborErase(node.chunk, node.x, node.y - 1, node.z, lightValue, queue, repropagationNodes, false);
        }
        queue.clear();
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
                                           List<ChunkNode> queue, HashSet<ChunkNode> totalNodes,
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
            } else if (thisLevel > 1) {
                totalNodes.add(node);
            }
        }
    }

    public static void propagateSunlight(List<ChunkNode> queue, HashSet<Chunk> affectedChunks, boolean propagateDownward) {
        while (queue.size() > 0) {
            ChunkNode node = queue.remove(0);
            if (node == null) continue;
            byte lightValue = node.chunk.voxels.getSun(node.x, node.y, node.z);
            checkNeighbor(node.chunk, node.x - 1, node.y, node.z, lightValue, queue, affectedChunks, false);
            checkNeighbor(node.chunk, node.x + 1, node.y, node.z, lightValue, queue, affectedChunks, false);
            checkNeighbor(node.chunk, node.x, node.y, node.z + 1, lightValue, queue, affectedChunks, false);
            checkNeighbor(node.chunk, node.x, node.y, node.z - 1, lightValue, queue, affectedChunks, false);
            checkNeighbor(node.chunk, node.x, node.y + 1, node.z, lightValue, queue, affectedChunks, propagateDownward);
            checkNeighbor(node.chunk, node.x, node.y - 1, node.z, lightValue, queue, affectedChunks, false);
//            System.out.println("Queue size: " + queue.size());
        }
        queue.clear();
    }

    private static void checkNeighbor(Chunk chunk, int x, int y, int z, int lightLevel,
                                      List<ChunkNode> queue, HashSet<Chunk> affectedChunks, boolean isBelowNode) {

        Block neigborBlock;
        if (chunk.inBounds(x, y, z)) {
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
            if (isBelowNode && lightLevel == 15) {
                chunk.voxels.setSun(x, y, z, (byte) 15);
                affectedChunks.add(chunk);
                queue.add(new ChunkNode(chunk, x, y, z));
            } else if (chunk.voxels.getSun(x, y, z) + 2 <= lightLevel) {
                chunk.voxels.setSun(x, y, z, (byte) (lightLevel - 1));
                affectedChunks.add(chunk);
                queue.add(new ChunkNode(chunk, x, y, z));
            }
        }
    }
}

