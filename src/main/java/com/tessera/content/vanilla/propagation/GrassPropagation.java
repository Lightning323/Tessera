package com.tessera.content.vanilla.propagation;

import com.tessera.Main;
import com.tessera.content.vanilla.terrain.defaultTerrain.DefaultTerrain;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.LivePropagationTask;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.players.pipeline.BlockHistory;
import com.tessera.content.vanilla.Blocks;
import org.joml.Vector3i;

import java.util.*;


public class GrassPropagation extends LivePropagationTask {

    final int UPDATE_INTERVAL = 10 * 1000;

    public GrassPropagation() {
        updateIntervalMS = UPDATE_INTERVAL;
    }

    public HashMap<Vector3i, Long> nodes = new HashMap<>();

    public boolean addNode(Vector3i worldPos, BlockHistory hist) {
        if (isGrass(hist.newBlock.id)
                || hist.newBlock.id == Blocks.BLOCK_DIRT) {
            long setTime = (long) (System.currentTimeMillis() + (Math.random() * 5000));
            nodes.put(worldPos, setTime);
            return true;
        }
        return false;
    }


    @Override
    public void update() {
        if (nodes.isEmpty()) {
            return;
        }
        ClientWindow.printlnDev("grass prop nodes: " + nodes.size());

        //Iterator is better than entrySet. Entryset creates a copy
        Iterator<Map.Entry<Vector3i, Long>> iterator = nodes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Vector3i, Long> entry = iterator.next();
            Vector3i node = entry.getKey();
            long setTime = entry.getValue();

            short thisBlock = Client.world.getBlockID(node.x, node.y, node.z);
            Block aboveBlock = Client.world.getBlock(node.x, node.y - 1, node.z);

            if (System.currentTimeMillis() - setTime > UPDATE_INTERVAL / 2) { //If it's been 10 seconds since we last set the block
                if (thisBlock == Blocks.BLOCK_DIRT && !aboveBlock.solid) {
                    Main.getServer().setBlock(
                            getGrassBlockOfBiome(node.x, node.y, node.z),
                            node.x, node.y, node.z);
                } else if (isGrass(thisBlock) && aboveBlock.solid) {
                    Main.getServer().setBlock(Blocks.BLOCK_DIRT, node.x, node.y, node.z);
                }
                iterator.remove(); // remove the entry from the map
            }
        }
    }

    private short getGrassBlockOfBiome(int wx, int wy, int wz) {
        int biome = Client.world.terrain.getBiomeOfVoxel(wx, wy, wz);
        switch (biome) {
            case DefaultTerrain.BIOME_SNOWY -> {
                return Blocks.BLOCK_SNOW_GRASS;
            }
            case DefaultTerrain.BIOME_JUNGLE -> {
                return Blocks.BLOCK_JUNGLE_GRASS;
            }
            case DefaultTerrain.BIOME_SAVANNAH, DefaultTerrain.BIOME_DESERT -> {
                return Blocks.BLOCK_DRY_GRASS;
            }
            default -> {
                return Blocks.BLOCK_GRASS;
            }
        }
    }

    public static boolean isGrass(short thisBlock) {
        return thisBlock == Blocks.BLOCK_GRASS ||
                thisBlock == Blocks.BLOCK_SNOW_GRASS ||
                thisBlock == Blocks.BLOCK_JUNGLE_GRASS ||
                thisBlock == Blocks.BLOCK_DRY_GRASS;
    }
}
