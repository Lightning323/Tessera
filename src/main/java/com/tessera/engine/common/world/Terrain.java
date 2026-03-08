// 
// Decompiled by Procyon v0.5.36
// 
package com.tessera.engine.common.world;

import com.tessera.Main;
import com.tessera.engine.common.option.NuklearField;
import com.tessera.engine.common.option.OptionsList;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.math.FastNoise;
import com.tessera.engine.common.math.PerlinNoise;
import com.tessera.engine.common.world.chunk.Chunk;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Random;

import static com.tessera.engine.common.players.Player.PLAYER_HEIGHT;

public abstract class Terrain {

    public static final FastNoise fastNoise = new FastNoise();
    public static final PerlinNoise perlinNoise = new PerlinNoise(0, 150);
    private int seed = 0;
    public final String name;
    private OptionsList options;
    public int version = 0;
    public int maxSurfaceHeight = 10;
    public int minSurfaceHeight = -100;
    public int terrainMinGenHeight = 0;  //Anything above this is considered air

    public Terrain(String name) {
        this.name = name;
        resetOptions();
    }

    public final void initForWorld(int seed, OptionsList options, int version) {
        fastNoise.SetSeed(seed);//set seed
        perlinNoise.setSeed(((double) seed / Integer.MAX_VALUE) * 255);
        this.seed = seed;

        this.version = version;

        resetOptions(); //Pre-initialize options to get default values
        this.options.putAll(options); //Set the values of terrainOptions to the options map

        loadWorld(this.options, this.version);
    }

    public int getBiomeOfVoxel(int x, int y, int z) {
        return 0;
    }

    public abstract void initOptions(OptionsList optionsList);

    public abstract void loadWorld(OptionsList options, int version);

    public boolean isBelowMinHeight(Vector3i position, int offset) {
        //If the bottom of the chunk is below the minimum height, we need to generate the terrain
        return (position.y * Chunk.HEIGHT) + Chunk.HEIGHT >= terrainMinGenHeight + offset;
    }

    public void resetOptions() {
        options = new OptionsList();
        initOptions(options);
    }

    public ArrayList<NuklearField> options_resetAndGetNKOptionList() {
        options = new OptionsList();//Reset the options first
        initOptions(options); //Init the temporary options
        ArrayList<NuklearField> optionFields = new ArrayList<>();
        options.forEach((key, value) -> {
            optionFields.add(new NuklearField(key, value, (v) -> {
                options.put(key, v);
            }));
        });
        return optionFields;
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }

    public OptionsList getOptionsCopy() {
        return new OptionsList(options);
    }

    public class GenSession {

        //        public final HashSet<Chunk> modifiedMeshedChunks = new HashSet<>();
        public final Random random = new Random();
        public final Chunk homeChunk;
        public boolean generatedOutsideOfChunk = false;

        public int randomInt(int lowerBound, int upperBound) {
            return random.nextInt(upperBound - lowerBound) + lowerBound;
        }

        public float randomFloat(float lowerBound, float upperBound) {
            return (random.nextFloat() * upperBound - lowerBound) + lowerBound;
        }

        public double randomDouble(double lowerBound, double upperBound) {
            return (random.nextDouble() * upperBound - lowerBound) + lowerBound;
        }

        /**
         * Generates a random boolean with the specified probability.
         *
         * @param probability The probability of returning true (0.0 to 1.0).
         * @return true with the given probability, false otherwise.
         */
        public boolean randBoolWithProbability(float probability) {
            return random.nextFloat() < probability;
        }

        public GenSession(Chunk chunk) {
            this.homeChunk = chunk;
            random.setSeed(FastNoise.Hash3D(seed, chunk.position.x, chunk.position.y, chunk.position.z));
        }

        public void setBlockWorld(int x, int y, int z, short block) {
            Chunk chunk = Main.getClient().world.setBlock(block, x, y, z);//The world.setBlock automatically sets the block on a future chunk if it doesnt exist
//            if (chunk != null && !homeChunk.position.equals(chunk.position)) {
//                modifiedMeshedChunks.add(chunk);
//            }
        }
    }

    public final GenSession createTerrainOnChunk(final Chunk chunk) {
        GenSession session = new GenSession(chunk);
        this.generateChunkInner(chunk, session);
        return session;
    }

    protected abstract void generateChunkInner(final Chunk p0, GenSession session);


    //    public abstract int getHeightmapOfVoxel(final int p0, final int p1);
    public boolean canSpawnHere(World world,
                                int playerWorldPosX,
                                int playerWorldPosY,
                                int playerWorldPosZ) {

        //We are looking at the player foot
        int playerFeetY = (int) (playerWorldPosY + PLAYER_HEIGHT);

        Block footBlock = world.getBlock(playerWorldPosX, playerFeetY, playerWorldPosZ);
        Block bodyBlock1 = world.getBlock(playerWorldPosX, playerFeetY - 1, playerWorldPosZ);
        Block bodyBlock2 = world.getBlock(playerWorldPosX, playerFeetY - 2, playerWorldPosZ);
        Block bodyBlock3 = world.getBlock(playerWorldPosX, playerFeetY - 3, playerWorldPosZ);

        return footBlock.solid //Ground is solid
                && !bodyBlock1.solid //The player can move
                && !bodyBlock2.solid
                && !bodyBlock3.solid
                //The ground and air is safe to stand in
                && footBlock.enterDamage < 0.01
                && bodyBlock1.enterDamage < 0.01
                && bodyBlock2.enterDamage < 0.01
                && bodyBlock3.enterDamage < 0.01;
    }

    @Override
    public String toString() {
        return "Terrain{" +
                "version=" + version +
                ", options=" + options +
                ", name='" + name + '\'' +
                ", seed=" + seed +
                '}';
    }
}
