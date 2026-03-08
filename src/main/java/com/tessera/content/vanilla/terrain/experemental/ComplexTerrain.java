////
//// Decompiled by Procyon v0.5.36
////
//package com.tessera.content.vanilla.terrain.complexTerrain;
//
//import com.tessera.content.vanilla.terrain.defaultTerrain.DefaultTerrain;
//import com.tessera.engine.server.LocalServer;
//import com.tessera.engine.server.block.BlockRegistry;
//import com.tessera.engine.utils.math.MathUtils;
//import com.tessera.engine.server.world.Terrain;
//import com.tessera.engine.server.world.World;
//import com.tessera.engine.server.world.chunk.Chunk;
//import com.tessera.content.vanilla.Blocks;
//
//import java.util.HashMap;
//
//import static com.tessera.engine.server.world.chunk.Chunk.WIDTH;
//
//public class ComplexTerrain extends Terrain {
//
//    final static float WORLD_HEIGHT = 0.5f;
//    public static final int TERRAIN_BOTTOM = World.WORLD_BOTTOM_Y - 2;
//    public static final int TERRAIN_MOUNTAINS_TOP = World.WORLD_TOP_Y;
//    public static final int TERRAIN_All_ROCK_POINT = World.WORLD_TOP_Y + Chunk.WIDTH * 8;
//
//    public final int TERRAIN_HEIGHT = 256;
//    public final float TERRAIN_SCALE = 2.0f; //We are scaling the fastNoise function. higher values = more packed fastNoise
//    public final float DRYNESS_SCALE = 0.5f;
//
//    public ComplexTerrain() {
//        super("Complex Terrain");
//    }
//
//
//    public int getBiomeOfVoxel(float valley, float dryness,
//                                 final int wx, final int wy, final int wz) {
//        if (dryness > 0.7) {
//            if (valley > 0.25) {
//                return DefaultTerrain.BIOME_DESERT;
//            } else {
//                return DefaultTerrain.BIOME_SAVANNAH;
//            }
//        } else if (dryness > 0.6) {
//            return DefaultTerrain.BIOME_JUNGLE;
//        } else if (dryness > 0.5) {
//            if (valley > 0.5) {
//                return DefaultTerrain.BIOME_SAVANNAH;
//            } else return DefaultTerrain.BIOME_DEFAULT;
//        } else {
//            return DefaultTerrain.BIOME_SNOWY;
//        }
//    }
//
//
//    @Override
//    public void loadWorld(HashMap<String, Object> options, int version) {
//
//    }
//
//    public void generateChunkInner(Chunk chunk, GenSession session) {
//
//        //X Axis
//        for (int cx = 0; cx < WIDTH; cx++) {
//            final int wx = cx + chunk.position.x * Chunk.WIDTH;
//            //Z Axis
//            for (int cz = 0; cz < WIDTH; cz++) {
//                final int wz = cz + chunk.position.z * Chunk.WIDTH;
//                //the valley percentage from -1 to 1
//                final float valleyLikelyhood = (float) perlinNoise.noise((float) (wz / 2), 0, (float) (wx / 2));
//
//                //Y Axis
//                for (int cy = 0; cy < WIDTH; cy++) {
//                    final int wy = cy + (chunk.position.y * Chunk.WIDTH);
//
//                    if (wy >= TERRAIN_BOTTOM - 1) {
//                        chunk.data.setBlock(cx, cy, cz, Blocks.BLOCK_BEDROCK);
//                    } else if (wy > TERRAIN_All_ROCK_POINT) {
//                        chunk.data.setBlock(cx, cy, cz, Blocks.BLOCK_STONE);
//                    } else if (wy > TERRAIN_MOUNTAINS_TOP) {
//                        //The rock percentage from 0 to 1 (im assuming)
//                        final float makeTerrain = this.placeRock(valleyLikelyhood, wx, wy, wz);
//
//                        if (makeTerrain > 0.5f) {
//                            boolean placeSod =
//                                    LocalServer.world.getBlockID(wx, wy - 1, wz) == BlockRegistry.BLOCK_AIR.id;
//                            if (placeSod) {
//                                //Terrain dryness from 0 to 1
//                                final float dryness = (float) perlinNoise.noise(wx * DRYNESS_SCALE, 0, wz * DRYNESS_SCALE) + 1;
//
//                                TerrainSod.placeSod(this, session, chunk, cx, cy, cz, wx, wy, wz,
//                                        getBiomeOfVoxel(valleyLikelyhood, dryness, wx, wy, wz),
//                                        valleyLikelyhood, dryness, makeTerrain);
//                            } else if (chunk.data.getBlock(cx, cy, cz) == BlockRegistry.BLOCK_AIR.id) {
//                                chunk.data.setBlock(cx, cy, cz, Blocks.BLOCK_STONE);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private static final double valleyHeight = 0.5;
//
//    private float placeRock(double valleyLikelyhood,
//                            final int wx, int wy, final int wz) {
//        wy -= 120;
//        float noise = (float) Terrain.perlinNoise.noise(wx * TERRAIN_SCALE, wy * TERRAIN_SCALE, wz * TERRAIN_SCALE);
//        if (noise > 0.59) {
//            noise += (noise - 0.59);
//        }
//        float rand = (valleyLikelyhood > 0.25) ? MathUtils.mapAndClamp((float) valleyLikelyhood, 0.25f, 0.8f, (float) noise, (float) (valleyHeight - noise * 0.1f)) : noise;
//        return (float) Math.pow(wy / (float) TERRAIN_HEIGHT * 4.0f, 1.1) - rand * this.WORLD_HEIGHT;
//    }
//
//
//}
