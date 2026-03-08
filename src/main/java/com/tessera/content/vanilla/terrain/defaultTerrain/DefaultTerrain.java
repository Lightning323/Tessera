package com.tessera.content.vanilla.terrain.defaultTerrain;

import com.tessera.engine.common.option.OptionsList;
import com.tessera.engine.common.option.BoundedFloat;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.world.Terrain;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.content.vanilla.Blocks;
import com.tessera.content.vanilla.blocks.blocks.trees.AcaciaTreeUtils;
import com.tessera.content.vanilla.blocks.blocks.trees.JungleTreeUtils;

import java.util.*;

import static com.tessera.engine.common.world.World.WORLD_BOTTOM_Y;

public class DefaultTerrain extends Terrain {


    public final static int BIOME_BEACH = 0;
    public final static int BIOME_DESERT = 1;
    public final static int BIOME_SAVANNAH = 2;
    public final static int BIOME_SNOWY = 3;
    public final static int BIOME_JUNGLE = 4;
    public final static int BIOME_DEFAULT = 5;
    final static int WORLD_HEIGHT_OFFSET = 138;
    final static int OCEAN_LEVEL = 25; //Used to deepen lakes in heightmap generation
    final static int WATER_LEVEL = WORLD_HEIGHT_OFFSET + (OCEAN_LEVEL - 5); //5 blocks above ocean level
    final static int LAVA_LEVEL = WORLD_HEIGHT_OFFSET + 110;
    final static float CAVE_FREQUENCY = 5.0f;
    final static float CAVE_THRESHOLD = 0.25f;
    final static float ORE_FREQUENCY = 10.0f;
    // Threshold for common vs rare ores
    final static float COMMON_THRESHOLD = 0.5f;
    final static List<Ore> ORES = new ArrayList<Ore>();


    final Block water, lava;
    final short fern, deadBush;
    static final short COAL_ORE = 228;
    static final short IRON_ORE = 234;
    static final short GOLD_ORE = 237;
    static final short LAPIS_ORE = 544;
    static final short EMERALD_ORE = 551;
    static final short DIAMOND_ORE = 105;

    private void setTerrainBounds(int minSurfaceHeight) {
        this.minSurfaceHeight = minSurfaceHeight;
        maxSurfaceHeight = 230;
        terrainMinGenHeight = minSurfaceHeight;
    }

    public DefaultTerrain() {
        //Therrain loading happens after blocks are loaded
        super("Default Terrain");

        fern = Blocks.BLOCK_FERN;
        deadBush = Blocks.BLOCK_DEAD_BUSH;
        water = Registrys.getBlock(Blocks.BLOCK_WATER);
        lava = Registrys.getBlock(Blocks.BLOCK_LAVA);

        ORES.add(new Ore("coal", 0.9f, COAL_ORE));
        ORES.add(new Ore("iron", 0.9f, IRON_ORE));

        Ore gold = new Ore("gold", 0.5f, GOLD_ORE);
        gold.minYLevel = 120;
        ORES.add(gold);

        ORES.add(new Ore("lapis", 0.7f, LAPIS_ORE));

        Ore emerald = new Ore("emerald", 0.2f, EMERALD_ORE);
        emerald.amtExposedToAir = 0.2f;
        emerald.minYLevel = 180;
        ORES.add(emerald);

        //TODO: Figure out why diamond are only showing up as scattered ores
        Ore diamond = new Ore("diamond", 0.2f, DIAMOND_ORE);
        diamond.minYLevel = 180;
        diamond.amtExposedToAir = 0.2f;
        ORES.add(diamond);
    }

    //Properties
    float frequency;
    boolean caves;
    boolean trees;
    boolean mountains;

    public void initOptions(OptionsList options) {
        setTerrainBounds(100);
        /*
        Default values, these are set when there is no config
         */
        version = 1;//The latest version
        caves = false;
        trees = false;
        mountains = false;
        frequency = 0.9f;

        options.put("Generate Caves", caves);
        options.put("Generate Trees", trees);
        options.put("Generate Mountains", mountains);
        options.put("Frequency", new BoundedFloat(frequency, 0.1f, 10.0f));
    }

    @Override
    public void loadWorld(OptionsList options, int version) {
        if (options.containsKey("Generate Caves")) {
            caves = options.getBoolean("Generate Caves");
        }
        if (options.containsKey("Generate Trees")) {
            trees = options.getBoolean("Generate Trees");
        }
        if (options.containsKey("Generate Mountains")) {
            mountains = options.getBoolean("Generate Mountains");
        }
        if (options.containsKey("Frequency")) {
            frequency = options.getBoundedFloat("Frequency").value;
        }
        setTerrainBounds(mountains ? 0 : 100);
    }

    public int getTerrainHeight(int x, int z) {
        return getTerrainHeight(valley(x, z), x, z);
    }

    public int getBiomeOfVoxel(int x, int y, int z) {
        return getBiomeOfVoxelV2(
                valley(x, z),
                getHeat(x, z),
                getTerrainHeight(x, z),
                x, y, z);
    }


    final float savannahTreeOdds = .995f;
    final float treeOdds = .994f;
    final float jungleTreeOdds = .99f;

    private void plantSod(GenSession session,
                          int x, int y, int z,
                          int wx, int wy, int wz,
                          float alpha, int biome,
                          Chunk chunk) {

        float f = session.random.nextFloat();
        boolean makePlants = true;
        if (f < 0.02 && wy < WATER_LEVEL - 1) {
            session.setBlockWorld(wx, wy - 1, wz, deadBush);
            makePlants = false;
        }

        switch (biome) {
            case BIOME_DEFAULT -> {
                chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_GRASS);
                if (makePlants) {
                    if (trees && f > treeOdds) {
                        DefaultTerrainUtils.plantRandomTree(session, alpha, chunk, wx, wy, wz);
                    } else if (f > 0.95) {
                        session.setBlockWorld(wx, wy - 1, wz, fern);
                    } else if (f > 0.9) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_GRASS_PLANT);
                    } else if (f > 0.89) {
                        session.setBlockWorld(wx, wy - 1, wz, DefaultTerrainUtils.randomFlower(session));
                    }
                }
            }
            case BIOME_SNOWY -> {
                chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_SNOW_GRASS);
                if (makePlants) {
                    if (trees && f > treeOdds) {
                        DefaultTerrainUtils.plantRandomTree(session, alpha, chunk, wx, wy, wz);
                    } else if (f > 0.9999 && alpha > 0.5) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_PUMPKIN);
                    } else if (f > 0.98) {
                        session.setBlockWorld(wx, wy - 1, wz, fern);
                    } else if (f > 0.96) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_GRASS_PLANT);
                    }
                }
            }
            case BIOME_BEACH -> {
                if (alpha > 0) {
                    chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_SAND);
                    session.setBlockWorld(wx, wy + 1, wz, Blocks.BLOCK_SAND);
                } else {
                    chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_GRAVEL);
                    session.setBlockWorld(wx, wy + 1, wz, Blocks.BLOCK_GRAVEL);
                }
                if (wy > WATER_LEVEL + 2) {
                    if (session.random.nextFloat() > 0.9) {
                        switch (session.random.nextInt(6)) {
                            case 0 -> {
                                session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_FIRE_CORAL_FAN);
                            }
                            case 1 -> {
                                session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_HORN_CORAL_FAN);
                            }
                            case 2 -> {
                                session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_BUBBLE_CORAL_FAN);
                            }
                            case 3 -> {
                                session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_TUBE_CORAL_FAN);
                            }
                            default -> {
                                // session.setBlockWorld(wx,wy-1,wz, MyGame.BLOCK_SEA_GRASS.id); //TODO: Add
                                // seagrass as JSON block
                                // When we load seagrass without putting it int he block entities, the chunk cant
                                // load because it doesnt know what kind of block it is
                            }
                        }
                    }
                } else if (makePlants && wy < WATER_LEVEL - 2) {
                    float rand = session.random.nextFloat();
                    if (rand > 0.999 && alpha > 0.5) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_SUGAR_CANE);
                        session.setBlockWorld(wx, wy - 2, wz, Blocks.BLOCK_SUGAR_CANE);
                        session.setBlockWorld(wx, wy - 3, wz, Blocks.BLOCK_SUGAR_CANE);
                    } else if (rand > 0.98) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_MINI_CACTUS);
                    }
                }
            }
            case BIOME_DESERT -> {
                if (alpha > 0) {
                    chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_SAND);
                    session.setBlockWorld(wx, wy + 1, wz, Blocks.BLOCK_SAND);
                } else {
                    chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_RED_SAND);
                    session.setBlockWorld(wx, wy + 1, wz, Blocks.BLOCK_RED_SAND);
                }
                if (makePlants) {
                    if (session.random.nextFloat() > 0.99 && y > 4 && y < 140) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_CACTUS);
                        session.setBlockWorld(wx, wy - 2, wz, Blocks.BLOCK_CACTUS);
                        session.setBlockWorld(wx, wy - 3, wz, Blocks.BLOCK_CACTUS);
                    }
                }
            }
            case BIOME_SAVANNAH -> {
                chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_DRY_GRASS);
                if (makePlants) {
                    if (trees && f > savannahTreeOdds) {
                        AcaciaTreeUtils.terrain_plantTree(session, chunk, wx, wy, wz);
                    } else if (f < 0.15) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_DRY_GRASS_PLANT);
                    } else if (f > 0.99) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_TALL_DRY_GRASS);
                        session.setBlockWorld(wx, wy - 2, wz, Blocks.BLOCK_TALL_DRY_GRASS_TOP);
                    }
                }
            }
            case BIOME_JUNGLE -> {
                chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_JUNGLE_GRASS);
                if (makePlants) {
                    if (trees && f > jungleTreeOdds) {
                        JungleTreeUtils.terrain_plantTree(session, chunk, wx, wy, wz);
                    } else if (f < 0.15) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_TALL_GRASS);
                        session.setBlockWorld(wx, wy - 2, wz, Blocks.BLOCK_TALL_GRASS_TOP);
                    } else if (f > 0.98) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_JUNGLE_GRASS_PLANT);
                    } else if (f > 0.94 && alpha > 0.65) {
                        session.setBlockWorld(wx, wy - 1, wz, Blocks.BLOCK_BAMBOO);
                        session.setBlockWorld(wx, wy - 2, wz, Blocks.BLOCK_BAMBOO);
                        session.setBlockWorld(wx, wy - 3, wz, Blocks.BLOCK_BAMBOO);
                    }
                }
            }
        }
    }


    public float valley(final int wx, final int wz) {
        //Scale: -1 to 1. higher values = more valley
        float valley = getValueFractal((float) (wz * 0.5) - 10000, (float) (wx * 0.5));
        if (version >= 1) {//Causes more valley to appear
            valley += 0.15f;
        }

        return valley;
    }

    final float MOUNTAIN_THRESH = 0.2f;
    final float MOUNTAIN_HEIGHT = 2.5f;

    public int getTerrainHeight(float valley, final int wx, final int wz) {
        double val = getValueFractal((float) wx, (float) wz) * 50f;

        if (val > OCEAN_LEVEL) { //Deepen the oceans and lakes
            val = ((val - OCEAN_LEVEL) * 1.7f) + OCEAN_LEVEL;
        } else if (mountains && val < -MOUNTAIN_THRESH) {
            val = ((val + MOUNTAIN_THRESH) * MOUNTAIN_HEIGHT) - MOUNTAIN_THRESH;
        }

        if (valley > 0) { //Valley only applies if the value is greater than 0
            val = MathUtils.map(valley,
                    0, 1, //From 0-1
                    val, //Regular terrain
                    (val / 5f) - 5); //Raised, flattened terrain
        }


        return (int) (WORLD_HEIGHT_OFFSET + val);
    }

    public float getHeat(int x, int z) {
        return (getValueFractal((float) x / 5, (float) z / 5) + 1) / 2.0f;
    }

    public int getBiomeOfVoxelV2(float valley, float heat, int heightmap, final int wx, final int wy, final int wz) {
        if (heat > 0.65f  // (higher than 1 - .35) We lower the temp to compensate for being at the bottom of the terrain
                && wy > WORLD_HEIGHT_OFFSET - 8 - (heat * 5) &&
                wy < WATER_LEVEL - 1) {
            return BIOME_DESERT;
        }

        if (wy > WATER_LEVEL - 10) {
            return BIOME_BEACH;
        }
        //Heat will stay within 0-1 range
        //Noise tends to be more biased towards the center, meaning we either have to normalize the fastNoise function
        //somehow to produce even distribution, or we have to favor the edges more

        if (heat > 0.57f) {
            return BIOME_SAVANNAH;
        } else if (heat > 0.47f) {
            return BIOME_JUNGLE;
        } else if (heat > 0.35f) { //Keep this!
            return BIOME_DEFAULT;
        } else { //lower than 0.35
            return BIOME_SNOWY;
        }
    }

    private float getFrequency() {
        return frequency;
    }

    private float getValueFractal(float x, float y, float z) {
        return fastNoise.GetValueFractal(x * getFrequency(), y * getFrequency(), z * getFrequency());
    }

    private float getValueFractal(float x, float y) {
        return fastNoise.GetValueFractal(x * getFrequency(), y * getFrequency());
    }


    @Override
    protected void generateChunkInner(Chunk chunk, GenSession session) {
        int wx, wy, wz, heightmap; //IMPORTANT: We cant put this outside generateChunkInner() because multiple chunks are generated at the same time
        float valley, heat;
        int biome = BIOME_DEFAULT;
        float caveFractal = 0;
        float oreFractal = 0;
        int chunkTop = chunk.position.y * Chunk.WIDTH;
        int chunkButton = chunkTop + Chunk.WIDTH - 1;


        Ore[] commonOres = new Ore[2];
        Ore[] rareOres = new Ore[3];
        int commonCount = 0;
        int rareCount = 0;

        //Add all ores that are within range of this chunk
        ArrayList<Ore> oreList = new ArrayList<Ore>();
        for (int i = 0; i < ORES.size(); i++) {
            Ore ore = ORES.get(i);
            if (ore.minYLevel <= chunkTop && ore.maxYLevel >= chunkButton) {
                oreList.add(ore);
            }
        }

        short crystalBlock = selectRandomCrystal(session);

        while ((commonCount < commonOres.length || rareCount < rareOres.length) && !oreList.isEmpty()) {
            int index = session.random.nextInt(oreList.size()); // Randomly pick an ore
            Ore ore = oreList.get(index);
            boolean isCommon = ore.common > COMMON_THRESHOLD; //Decide if it is common

            if (isCommon && commonCount < commonOres.length) {
                commonOres[commonCount] = ore;
                commonCount++;
            } else if (!isCommon && rareCount < rareOres.length) {
                rareOres[rareCount] = ore;
                rareCount++;
            }
            // Remove the selected ore to avoid picking it again
            oreList.remove(index);
        }
//        System.out.println("\nCommon ores: " + Arrays.toString(commonOres));
//        System.out.println("Rare ores: " + Arrays.toString(rareOres));


        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int z = 0; z < Chunk.WIDTH; z++) {
                wx = x + chunk.position.x * Chunk.WIDTH;
                wz = z + chunk.position.z * Chunk.WIDTH;
                valley = valley(wx, wz);
                heightmap = getTerrainHeight(valley, wx, wz);
                boolean placeWater = true;
                heat = getHeat(wx, wz);

                for (int y = 0; y < Chunk.WIDTH; y++) {
                    wy = y + (chunk.position.y * Chunk.WIDTH);

                    if (
                            wy > WORLD_BOTTOM_Y - 2
                                    || (wy > WORLD_BOTTOM_Y - 3 && session.random.nextBoolean())
                    ) {
                        chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_BEDROCK);
                    } else if (wy > WORLD_BOTTOM_Y - 3 && session.randBoolWithProbability(0.01f)) {
                        chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_OBSIDIAN);
                    } else if (wy == heightmap && wy > 1) {// Place sod
                        biome = getBiomeOfVoxelV2(valley, heat, heightmap, wx, wy, wz);

                        //Alpha is a high frequency fastNoise value, from -1 to 1
                        final float alpha = getValueFractal((float) wx * 3, (float) wz * 3 - 500.0f);
                        plantSod(session, x, y, z, wx, wy, wz, alpha, biome, chunk);
                    } else if (wy > heightmap && wy < heightmap + 2) {
                        if (chunk.voxels.getBlock(x, y, z) == BlockRegistry.BLOCK_AIR.id) {
                            chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_DIRT);
                        }
                    } else if (wy > heightmap && //if we are below the ground
                            (!caves || //If caves are disabled
                                    wy < heightmap + 10 || //Or we arent below the ground enough
                                    (caveFractal = getValueFractal(wx * CAVE_FREQUENCY, wy * 14.0f, wz * CAVE_FREQUENCY)) <= CAVE_THRESHOLD)
                    ) {
                        oreFractal = getValueFractal(wx * ORE_FREQUENCY, wy * ORE_FREQUENCY, wz * ORE_FREQUENCY);
                        placeStoneAndOres(chunk, session, x, y, z, wx, wy, wz, oreFractal, caveFractal, commonOres, rareOres);

                        /**
                         * Generate crystals
                         */
                        if (
                            //crystalBlock > 0 && //If we should even generate crystals
                                session.random.nextFloat() < 0.0005 &&
                                        y > 1 && //If this isnt the top of the chunk
                                        chunk.voxels.getBlock(x, y - 2, z) == Blocks.BLOCK_AIR) { //And the block above us is air
                            chunk.voxels.setBlock(x, y - 1, z, crystalBlock);
                        }

                        placeWater = false;
                    } else if (wy <= heightmap) {
                        if (wy > WATER_LEVEL && heat < -0.4f) {
                            chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_ICE_BLOCK);
                        } else if (wy > WATER_LEVEL && placeWater) {
                            //Whenever we set a source block, we MUST set the max flow of the water
                            chunk.voxels.setBlock(x, y, z, water.id);
                            chunk.voxels.setBlockData(x, y, z, new BlockData(new byte[]{(byte) (water.liquidMaxFlow + 1)}));  //set the max flow of the water
                        }
                    }
//                    else if (wy > LAVA_LEVEL) {
//                        chunk.data.setBlock(x, y, z, Blocks.BLOCK_LAVA);
//                    }
                }
            }
        }
    }

    private short selectRandomCrystal(GenSession session) {
        switch (session.random.nextInt(4)) { //Set a random crystal
            case 0 -> {
                return (Blocks.BLOCK_AMETHYST_CRYSTAL);
            }
            case 1 -> {
                return (Blocks.BLOCK_RUBY_CRYSTAL);
            }
            case 2 -> {
                return (Blocks.BLOCK_AQUAMARINE_CRYSTAL);
            }
            default -> {
                return (Blocks.BLOCK_JADE_CRYSTAL);
            }
        }
    }

    private final short RED_BLOCK = Blocks.BLOCK_WOOL_RED;
    private final short BLUE_BLOCK = Blocks.BLOCK_WOOL_SKY_BLUE;
    private final short GREEN_BLOCK = Blocks.BLOCK_WOOL_GREEN;
    private final short YELLOW_BLOCK = Blocks.BLOCK_WOOL_YELLOW;

    private void placeStoneAndOres(Chunk chunk,
                                   GenSession session,
                                   int x, int y, int z,
                                   int wx, int wy, int wz,
                                   float alpha, float caveFractal,
                                   Ore[] commonOres, Ore[] rareOres) {

        boolean orbA = alpha < 0.5 && alpha > 0.47;
        boolean orbB = alpha < -0.47 && alpha > -0.5;

        boolean orbRareA = alpha > 0.72;
        boolean orbRareB = alpha < -0.72;

        boolean rareScatter = alpha < 0.0014 && alpha > -0.0014;

        short impureBlock = Blocks.BLOCK_DIORITE;

        boolean exposedToAir = caves && caveFractal > CAVE_THRESHOLD - 1;

        //Select rock
        if (alpha < 0.4 && alpha > -0.4) chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_STONE);
        else if (alpha > 0) chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_ANDESITE);
        else chunk.voxels.setBlock(x, y, z, Blocks.BLOCK_GRAVEL);
//For testing
//        if (ClientWindow.devMode && exposedToAir) {
//            chunk.data.setBlock(x, y, z, Blocks.BLOCK_GLASS);
//        }

        Ore ore = commonOres[0];
        if (orbA && ore != null) {
            if (session.randBoolWithProbability(ore.clusterPurity)) { //if we are pure
                if (!exposedToAir || session.randBoolWithProbability(ore.amtExposedToAir)) {
                    chunk.voxels.setBlock(x, y, z, ore.block);
                }
            } else chunk.voxels.setBlock(x, y, z, impureBlock);
        }

        ore = commonOres[1];
        if (orbB && ore != null) {
            if (session.randBoolWithProbability(ore.clusterPurity)) {//if we are pure
                if (!exposedToAir || session.randBoolWithProbability(ore.amtExposedToAir)) {
                    chunk.voxels.setBlock(x, y, z, ore.block);
                }
            } else chunk.voxels.setBlock(x, y, z, impureBlock);
        }

        ore = rareOres[0];
        if (orbRareA && ore != null) {
            if (!exposedToAir || session.randBoolWithProbability(ore.amtExposedToAir)) {
                chunk.voxels.setBlock(x, y, z, ore.block);
            }
        }

        ore = rareOres[1];
        if (orbRareB && ore != null) {
            if (!exposedToAir || session.randBoolWithProbability(ore.amtExposedToAir)) {
                chunk.voxels.setBlock(x, y, z, ore.block);
            }
        }

        ore = rareOres[2];
        if (rareScatter && ore != null) {
            if (!exposedToAir || session.randBoolWithProbability(ore.amtExposedToAir)) {
                chunk.voxels.setBlock(x, y, z, ore.block);
            }
        }

    }
}