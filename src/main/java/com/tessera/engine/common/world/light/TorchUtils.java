package com.tessera.engine.common.world.light;


import com.tessera.engine.client.Client;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TorchUtils {

    public static void transparentToOpaque(HashSet<Chunk> affectedChunks, final Chunk chunk, final int x, final int y, final int z) {
        final List<TorchNode> queue = new ArrayList<>();
        queue.add(new TorchNode(chunk, x, y, z));
        final HashSet<TorchNode> edges = eraseSection(affectedChunks, queue);
        queue.clear();
        queue.addAll(edges);
        continueBFS(affectedChunks, queue);
    }

    public static void opaqueToTransparent(HashSet<Chunk> affectedChunks, final Chunk chunk, final int x, final int y, final int z) {
        final List<TorchNode> queue = new ArrayList<>();
        startingNodesForPropagation(queue, chunk, x, y, z);
        continueBFS(affectedChunks, queue);
    }

    public static void setTorch(HashSet<Chunk> affectedChunks, final Chunk chunk,
                                final int x, final int y, final int z, byte startingValue) {
        final List<TorchNode> queue = new ArrayList<>();
        startingValue = (byte) MathUtils.clamp(startingValue, 0, 15);
        queue.add(new TorchNode(chunk, x, y, z, startingValue));
        affectedChunks.add(chunk);
        continueBFS(affectedChunks, queue);
    }

    public static void removeTorch(HashSet<Chunk> affectedChunks, final Chunk chunk, final int x, final int y, final int z) {
        if (chunk.voxels.getTorch(x, y, z) > 0) {
            final List<TorchNode> queue = new ArrayList<>();
            queue.add(new TorchNode(chunk, x, y, z));
            final HashSet<TorchNode> edges = eraseSection(affectedChunks, queue);
            queue.clear();
            queue.addAll(edges);
            continueBFS(affectedChunks, queue);
        }
    }

    protected static void startingNodesForPropagation(List<TorchNode> queue, final Chunk chunk, final int x, final int y, final int z) {
        findNeg2(chunk, x + 1, y, z, queue);
        findNeg2(chunk, x - 1, y, z, queue);
        findNeg2(chunk, x, y + 1, z, queue);
        findNeg2(chunk, x, y - 1, z, queue);
        findNeg2(chunk, x, y, z + 1, queue);
        findNeg2(chunk, x, y, z - 1, queue);
    }

    private static void findNeg2(Chunk chunk, int x, int y, int z, final List<TorchNode> queue) {
        if (!Chunk.inBounds(x, y, z)) {//Correct coordinates if out of bounds
            final WCCi wcc = new WCCi().setNeighboring(chunk.position, x, y, z);
            chunk = wcc.getChunk(Client.world);
            if (chunk == null) {
                return;
            }
            x = wcc.chunkVoxel.x;
            y = wcc.chunkVoxel.y;
            z = wcc.chunkVoxel.z;
        }
        if (chunk.voxels.getTorch(x, y, z) > 1) {
            queue.add(new TorchNode(chunk, x, y, z));
        }
    }


    public static void continueBFS(HashSet<Chunk> affectedChunks,
                                   final List<TorchNode> queue) {
        while (queue.size() > 0) {
            final TorchNode node = queue.remove(0);
            int lightValue = node.chunk.voxels.getTorch(node.x, node.y, node.z);
            if (node.lightVal > -1) {//If the node has its own light value
                //(We may not need to do this if we just set the light value before continueBFS is called)
                node.chunk.voxels.setTorch(node.x, node.y, node.z, Math.max(lightValue, node.lightVal));//Make sure the light value is greater than the light value of the node
                lightValue = node.lightVal;
                affectedChunks.add(node.chunk);
            }
            checkNeighborCont(node.chunk, node.x - 1, node.y, node.z, lightValue, queue, affectedChunks);
            checkNeighborCont(node.chunk, node.x + 1, node.y, node.z, lightValue, queue, affectedChunks);
            checkNeighborCont(node.chunk, node.x, node.y, node.z + 1, lightValue, queue, affectedChunks);
            checkNeighborCont(node.chunk, node.x, node.y, node.z - 1, lightValue, queue, affectedChunks);
            checkNeighborCont(node.chunk, node.x, node.y + 1, node.z, lightValue, queue, affectedChunks);
            checkNeighborCont(node.chunk, node.x, node.y - 1, node.z, lightValue, queue, affectedChunks);
        }
    }

    private static void checkNeighborCont(Chunk chunk, int x, int y, int z, final int lightLevel,
                                          final List<TorchNode> queue, HashSet<Chunk> affectedChunks) {
        if (!Chunk.inBounds(x, y, z)) {//Correct coordinates if out of bounds
            final WCCi wcc = new WCCi().setNeighboring(chunk.position, x, y, z);
            chunk = wcc.getChunk(Client.world);
            if (chunk == null) {
                return;
            }
            x = wcc.chunkVoxel.x;
            y = wcc.chunkVoxel.y;
            z = wcc.chunkVoxel.z;
        }
        final int neighborLevel = chunk.voxels.getTorch(x, y, z);
        final Block block = Registrys.getBlock(chunk.voxels.getBlock(x, y, z));
        if ((!block.opaque || block.isLuminous()) && neighborLevel + 2 <= lightLevel) {
            chunk.voxels.setTorch(x, y, z, lightLevel - 1);

            affectedChunks.add(chunk);
            queue.add(new TorchNode(chunk, x, y, z));
        }
    }

    public static HashSet<TorchNode> eraseSection(HashSet<Chunk> affectedChunks, final List<TorchNode> queue) {
        final HashSet<TorchNode> repropagationNodes = new HashSet<>();
        while (queue.size() > 0) {
            final TorchNode node = queue.remove(0);
            final int lightValue = node.chunk.voxels.getTorch(node.x, node.y, node.z);
            affectedChunks.add(node.chunk);
            node.chunk.voxels.setTorch(node.x, node.y, node.z, (byte) 0);
            checkNeighborErase(node.chunk, node.x - 1, node.y, node.z, lightValue, queue, repropagationNodes);
            checkNeighborErase(node.chunk, node.x + 1, node.y, node.z, lightValue, queue, repropagationNodes);
            checkNeighborErase(node.chunk, node.x, node.y, node.z + 1, lightValue, queue, repropagationNodes);
            checkNeighborErase(node.chunk, node.x, node.y, node.z - 1, lightValue, queue, repropagationNodes);
            checkNeighborErase(node.chunk, node.x, node.y + 1, node.z, lightValue, queue, repropagationNodes);
            checkNeighborErase(node.chunk, node.x, node.y - 1, node.z, lightValue, queue, repropagationNodes);
        }
//        for (TorchNode node : repropagationNodes) {
//            node.chunk.data.setTorch(node.x, node.y, node.z, node.lightVal);
//        }
        return repropagationNodes;
    }

    private static void checkNeighborErase(Chunk chunk, int x, int y, int z, final int lightLevel,
                                           final List<TorchNode> queue, final HashSet<TorchNode> repropagation) {
        if (!Chunk.inBounds(x, y, z)) {//Correct coordinates if out of bounds
            final WCCi wcc = new WCCi().setNeighboring(chunk.position, x, y, z);
            chunk = wcc.getChunk(Client.world);
            if (chunk == null) {
                return;
            }
            x = wcc.chunkVoxel.x;
            y = wcc.chunkVoxel.y;
            z = wcc.chunkVoxel.z;
        }
        final int neighborLevel = chunk.voxels.getTorch(x, y, z);
        if (neighborLevel < lightLevel) {
            Block block = Registrys.getBlock(chunk.voxels.getBlock(x, y, z));
            if (block.isLuminous()) {//Instead of deleting source light of other torches, reset its original light value
                repropagation.add(new TorchNode(chunk, x, y, z, block.torchlightStartingValue));
            }
            queue.add(new TorchNode(chunk, x, y, z));
        } else {
            repropagation.add(new TorchNode(chunk, x, y, z));
        }
    }
}