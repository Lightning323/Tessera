package com.tessera.content.vanilla.blocks;

import com.tessera.Main;
import com.tessera.content.vanilla.terrain.defaultTerrain.DefaultTerrain;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.block.Block;
import com.tessera.content.vanilla.Blocks;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.loot.AllLootTables;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

public class PlantBlockUtils {
    public final List<Short> snowyDefaultGrowth = new ArrayList<>();
    //    public final List<Short> grass = new ArrayList<>();
    Random random = new Random();

    public PlantBlockUtils() {
        //Reeds
        snowyDefaultGrowth.add(Blocks.BLOCK_GRASS_PLANT);
        snowyDefaultGrowth.add(Blocks.BLOCK_FERN);
        snowyDefaultGrowth.add(Blocks.BLOCK_DEAD_BUSH);
        snowyDefaultGrowth.add(Blocks.BLOCK_PANSIES);
        snowyDefaultGrowth.add(Blocks.BLOCK_ROSES);
        snowyDefaultGrowth.add(Blocks.BLOCK_DANDELION);
        snowyDefaultGrowth.add(Blocks.BLOCK_AZURE_BLUET);
        snowyDefaultGrowth.add(Blocks.BLOCK_ORANGE_TULIP);
        System.out.println("SnowDefaultGrowth: " + snowyDefaultGrowth.toString());
    }



    public final static float GROW_PROBABILITY = 0.05f;
    public final static float DECAY_PROBABILITY = 0.05f;

    public void addDecayTickEvent(Block block) {
        if (block == null) return;
        block.randomTickEvent = (x, y, z) -> {
            if (random.nextFloat() < DECAY_PROBABILITY) {
                Main.getServer().setBlock(Blocks.BLOCK_AIR, x, y, z);
                return true;
            }
            return false;
        };
    }

    public void addPlantGrowthEvents(final Block... stages) {
        for (int i = 0; i < stages.length - 1; i++) {
            Block b = stages[i];
            final int finalI = i;
            b.randomTickEvent = (x, y, z) -> {
                short below = Client.world.getBlockID(x, y + 1, z);

                //If this is dry farmland, the crops will grow slower
                if (below == Blocks.BLOCK_FARMLAND && Math.random() < 0.7) {
                    return false;
                }

                if (below == Blocks.BLOCK_WET_FARMLAND || below == Blocks.BLOCK_FARMLAND) {
                    Main.getServer().setBlock(stages[finalI + 1].id, x, y, z);
                    return true;
                }
                return false;
            };
        }
    }


    public boolean deepPlantable(final int x, final int y, final int z) {
        boolean val = blockIsGrassSnowOrDirt(Client.world.getBlock(x, y + 1, z))
                && blockIsGrassSnowOrDirt(Client.world.getBlock(x, y + 2, z));
        return val;
    }


    public boolean plantable(final int x, final int y, final int z) {
        return blockIsGrassSnowOrDirt(Client.world.getBlock(x, y + 1, z));
    }


    public boolean isGrass(short thisBlock) {
        return thisBlock == Blocks.BLOCK_GRASS ||
                thisBlock == Blocks.BLOCK_SNOW_GRASS ||
                thisBlock == Blocks.BLOCK_JUNGLE_GRASS ||
                thisBlock == Blocks.BLOCK_DRY_GRASS;
    }

    public boolean isFarmland(short thisBlock) {
        return thisBlock == Blocks.BLOCK_FARMLAND ||
                thisBlock == Blocks.BLOCK_WET_FARMLAND;
    }

    public boolean blockIsGrassSnowOrDirt(Block block) {
        return block.id == Blocks.BLOCK_DIRT ||
                isFarmland(block.id) ||
                isGrass(block.id);
    }

    public boolean blockIsSandOrGravel(Block block) {
        return block.id == Blocks.BLOCK_SAND
                || block.id == Blocks.BLOCK_RED_SAND
                || block.id == Blocks.BLOCK_GRAVEL;
    }

    public short getGrassBlockOfBiome(int wx, int wy, int wz) {
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


    public void makeStalk(Block stalk, Block sapling, int maxHeight, Predicate<Block> canGrow) {
        stalk.randomTickEvent = (x, y, z) -> {
            return growStalk(x, y, z, stalk, sapling, maxHeight, canGrow);
        };
        if (sapling != null) sapling.randomTickEvent = (x, y, z) -> {
            return growStalk(x, y, z, stalk, sapling, maxHeight, canGrow);
        };

        //We have to manually remove blocks above the stalk and add item drops
        stalk.removeBlockEvent(false, ((x, y, z, history) -> {
            for (int i = 0; i < 50; i++) {
                Block block = Client.world.getBlock(x, y - i, z);
                if (block != null && block.id == stalk.id) {
                    //Remove the block
                    Main.getServer().setBlock(Blocks.BLOCK_AIR, x, y - i, z);

                    if (Main.getServer().getGameMode() == GameMode.ADVENTURE) {//Drop loot tables
                        final int blockY = y - i;
                        AllLootTables.blockLootTables.getLoot(stalk.alias).randomItems((itemStack) -> {
                            Main.getServer().placeItemDrop(new Vector3f(x, blockY, z), itemStack, false);
                        });
                    }
                }
            }
        }));
    }

    private boolean growStalk(int x, int y, int z,
                              Block stalkBlock, Block stalkSapling, int maxHeight,
                              Predicate<Block> plantable) {

        Block belowBlock = (Client.world.getBlock(x, y + 1, z));
        if (plantable.test(belowBlock)) {//If this is the bottom of a stalk
            for (int i = 0; i > -maxHeight; i--) {//Go up -20 blocks max
                Block stalk = Client.world.getBlock(x, y + i, z);
                if ((stalk != null && stalk.id == stalkBlock.id)) {//If this is a stalk
                } else if (stalk.isAir() || stalk.isLiquid() || (stalkSapling != null && stalk.id == stalkSapling.id)) {//If this is air, liquid or sapling
                    Main.getServer().setBlock(stalkBlock.id, x, y + i, z); //Set bamboo
                    return true;
                } else {//this is not a stalk or air
                    return false;
                }
            }
        }
        return false;
    }


    public short growGrass(int x, int y, int z, Block aboveBlock) {
        short thisBlock = Client.world.getBlockID(x, y, z);
        /**
         * Grow grass
         */
        if (thisBlock == Blocks.BLOCK_GRASS || thisBlock == Blocks.BLOCK_SNOW_GRASS) {
            if (aboveBlock == BlockRegistry.BLOCK_AIR) {
                int indx = random.nextInt(snowyDefaultGrowth.size());
                return snowyDefaultGrowth.get(indx);
            }
        } else if (thisBlock == Blocks.BLOCK_DRY_GRASS) {
            if (aboveBlock == BlockRegistry.BLOCK_AIR) {
                return Blocks.BLOCK_DRY_GRASS_PLANT;
            } else if (aboveBlock.id == Blocks.BLOCK_DRY_GRASS_PLANT) {
                return Blocks.BLOCK_TALL_DRY_GRASS;
            }
        } else if (thisBlock == Blocks.BLOCK_JUNGLE_GRASS) {
            if (aboveBlock == BlockRegistry.BLOCK_AIR) {
                return Blocks.BLOCK_JUNGLE_GRASS_PLANT;
            } else if (aboveBlock.id == Blocks.BLOCK_JUNGLE_GRASS_PLANT) {
                return Blocks.BLOCK_TALL_GRASS;
            }
        }
        return -1;
    }


}