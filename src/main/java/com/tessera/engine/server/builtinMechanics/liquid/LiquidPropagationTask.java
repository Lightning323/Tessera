package com.tessera.engine.server.builtinMechanics.liquid;

import com.tessera.Main;
import com.tessera.content.vanilla.Blocks;
import com.tessera.content.vanilla.blocks.RenderType;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.LivePropagationTask;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.common.players.pipeline.BlockHistory;
import com.tessera.engine.common.world.chunk.BlockData;
import org.joml.Vector3i;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class LiquidPropagationTask extends LivePropagationTask {

    public final Block liquidBlock;

    //Create a concurrent set so that we can add nodes from multiple threads
    ConcurrentHashMap<Vector3i, Boolean> map = new ConcurrentHashMap<>();
    private final Set<Vector3i> nodes = Collections.newSetFromMap(map);


    public LiquidPropagationTask(Block liquidBlock, int updateIntervalMS) {
        this.updateIntervalMS = updateIntervalMS;
        this.liquidBlock = liquidBlock;
    }

    public boolean addNode(Vector3i worldPos, BlockHistory hist) {
        if (hist.newBlock.id == liquidBlock.id) {
            nodes.add(worldPos);
            return true;
        }
        return false;
    }

    private boolean addNode(HashSet<Vector3i> nodes, Vector3i worldPos) {
        if (Client.world.getBlockID(worldPos.x, worldPos.y, worldPos.z) == liquidBlock.id) {
            nodes.add(worldPos);
            return true;
        }
        return false;
    }

    public boolean isPenetrable(Block block) {
        return block.isAir() ||
                (!block.solid
                        && (
                        block.type == RenderType.SPRITE
                                || block.type == RenderType.WALL_ITEM)
                        && block.toughness < 1f);
    }

    public static int getFlow(BlockData thisBD, int nullFlow) {
        return (thisBD != null && thisBD.size() > 0) ? thisBD.get(0) : nullFlow;
    }

    /**
     * maxFlow+1 = source water
     * maxFlow = down-flowing water
     * > maxFlow = sideways-flowing water
     */
    @Override
    public void update() {
        if (nodes.isEmpty()) {
            return;
        }
        HashSet<Vector3i> newNodes = new HashSet<>();
        ClientWindow.printlnDev(liquidBlock.alias + " prop nodes: " + nodes.size());
        final int SOURCE_FLOW = liquidBlock.liquidMaxFlow + 1; //The source block is block max flow + 1

        for (Vector3i v : nodes) {
            //Get the flow from this node
            int thisFlow = getFlow(Client.world.getBlockData(v.x, v.y, v.z), 0);
            /**
             * FLOW DEPROPAGATION
             */
            if (thisFlow == liquidBlock.liquidMaxFlow && //If we are 100% flowing
                    Client.world.getBlockID(v.x, v.y - 1, v.z) != liquidBlock.id && //and there is nothing above
                    getFlow(Client.world.getBlockData(v.x - 1, v.y, v.z), 0) != SOURCE_FLOW &&//and there is no neighboring source
                    getFlow(Client.world.getBlockData(v.x + 1, v.y, v.z), 0) != SOURCE_FLOW &&
                    getFlow(Client.world.getBlockData(v.x, v.y, v.z - 1), 0) != SOURCE_FLOW &&
                    getFlow(Client.world.getBlockData(v.x, v.y, v.z + 1), 0) != SOURCE_FLOW
            ) {
                Block below = Client.world.getBlock(v.x, v.y + 1, v.z);
                if (below.solid) reduceFlow(newNodes, v.x, v.y, v.z);
                else Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, v.x, v.y, v.z);
                continue;
            } else if (thisFlow < liquidBlock.liquidMaxFlow && //If we are flowing sideways
                    !(getFlow(Client.world.getBlockData(v.x - 1, v.y, v.z), 0) > thisFlow || //and there is no neighboring value higher than us
                            getFlow(Client.world.getBlockData(v.x + 1, v.y, v.z), 0) > thisFlow ||
                            getFlow(Client.world.getBlockData(v.x, v.y, v.z - 1), 0) > thisFlow ||
                            getFlow(Client.world.getBlockData(v.x, v.y, v.z + 1), 0) > thisFlow)
            ) {
                reduceFlow(newNodes, v.x, v.y, v.z);
            }
            /**
             * FLOW PROPAGATION
             */
            if (setWater(v.x, v.y + 1, v.z, liquidBlock.liquidMaxFlow)) {
            } else if (thisFlow > 0) {
                setWater(v.x - 1, v.y, v.z, thisFlow - 1);
                setWater(v.x + 1, v.y, v.z, thisFlow - 1);
                setWater(v.x, v.y, v.z - 1, thisFlow - 1);
                setWater(v.x, v.y, v.z + 1, thisFlow - 1);
            }

        }
        nodes.clear();
        nodes.addAll(newNodes);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param flow
     * @return if we were able to set the water
     */
    public boolean setWater(int x, int y, int z, int flow) {
        Block existingBlock = Client.world.getBlock(x, y, z);

        if (existingBlock.id == Blocks.BLOCK_LAVA && liquidBlock.id == Blocks.BLOCK_WATER) { //If that is lava and we are water
            Main.getServer().setBlock(Blocks.BLOCK_COBBLESTONE, x, y, z);
        } else if (existingBlock.id == liquidBlock.id || isPenetrable(existingBlock)) {
            int existingFlow = getFlow(Client.world.getBlockData(x, y, z), 0);
            flow = Math.max(flow, existingFlow); //We dont want to set something lower than the existing flow
            Main.getServer().setBlock(liquidBlock.id, new BlockData(new byte[]{(byte) flow}), x, y, z);
            return true;
        }
        return !existingBlock.solid;
    }

    private boolean reduceFlow(HashSet<Vector3i> nodes, int x, int y, int z) {
        short block = Client.world.getBlockID(x, y, z);
        if (block == liquidBlock.id) {
            BlockData bd = Client.world.getBlockData(x, y, z);
            int flow = getFlow(bd, 0);

            if (flow > 0) {
                if (bd != null && bd.size() > 0) {   //set the flow down 1
                    bd.set(0, (byte) (flow - 1));
                } else bd = new BlockData(new byte[]{(byte) (flow - 1)});
                Main.getServer().setBlock(liquidBlock.id, bd, x, y, z);

/**
 *     the block event pipeline doesnt key local events for block data changes,so we need to add the nodes from
 *     these events ourselves
 */
                addNode(nodes, new Vector3i(x, y, z));
                addNode(nodes, new Vector3i(x + 1, y, z));
                addNode(nodes, new Vector3i(x - 1, y, z));
                addNode(nodes, new Vector3i(x, y, z + 1));
                addNode(nodes, new Vector3i(x, y, z - 1));

                return true;
            } else {
                Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, x, y, z);
            }
        }
        return false;
    }
}