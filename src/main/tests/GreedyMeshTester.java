///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.tessera.tests;
//
//import com.tessera.engine.game.model.items.BlockList;
//import com.tessera.engine.game.model.items.block.Block;
//import com.tessera.engine.game.model.items.block.BlockArrayTexture;
//import com.tessera.engine.client.visuals.localServer.rendering.chunk.meshers.bufferSet.BufferSet;
//import com.tessera.engine.client.visuals.localServer.rendering.chunk.meshers.GreedyMesher;
//import com.tessera.engine.client.visuals.localServer.rendering.chunk.withBakedLight.GreedyMesherWithLight;
//import com.tessera.engine.client.visuals.localServer.rendering.old.GreedyMesherNublada;
//import com.tessera.engine.utils.MiscUtils;
//import com.tessera.engine.utils.resource.ResourceUtils;
//import com.tessera.engine.utils.math.PerlinNoise;
//import com.tessera.window.development.MemoryProfiler;
//import com.tessera.window.utils.preformance.Stopwatch;
//import com.tessera.engine.game.model.world.chunk.ChunkVoxels;
//import com.tessera.game.MyGame;
//import java.io.IOException;
//
//import java.util.HashMap;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.joml.Vector3i;
//import org.lwjgl.system.MemoryStack;
//import org.lwjgl.system.MemoryUtil;
//
///**
// *
// * @author zipCoder933
// */
//public class GreedyMeshTester {
//
//    final static Mode mode = Mode.TEST_SPEED;
//    final static int trials = 200;
//
//    public final static PerlinNoise fastNoise = new PerlinNoise(0.0, 1000);
//    static Stopwatch watch_Nublada = new Stopwatch();
//    static Stopwatch watch_tesseraLight = new Stopwatch();
//    static Stopwatch watch_tessera = new Stopwatch();
//    static long times_tessera = 0;
//    static long times_nublada, times_tessera_light;
//    static ChunkVoxels voxels = new ChunkVoxels(32, 32, 32);
//
//    enum Mode {
//        TEST_SPEED,
//        TEST_NUBLADA_MEMORY,
//        TEST_tessera_MEMORY
//    }
//
//    private static void generateVoxels() {
//        voxels.clear();
//        for (int i = 0; i < voxels.size.x; i++) {
//            for (int j = 0; j < voxels.size.y; j++) {
//                for (int k = 0; k < voxels.size.z; k++) {
//                    if (fastNoise.smoothNoise(i * 0.1f, j * 0.1f, k * 0.1f) > 0) {
//                        voxels.setBlock(i, j, k, MyGame.BLOCK_GRASS.id);
//                        voxels.setSun(i, j, k, (byte) 0);
//                    } else {
//                        voxels.setBlock(i, j, k, BlockList.BLOCK_AIR.id);
//                        voxels.setSun(i, j, k, (byte) (Math.random() * 15));
//                    }
//                }
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//
//        System.out.println("Chunk Size: " + MiscUtils.printVector(voxels.size));
//        HashMap<Short, Block> blockList = new HashMap<>();
//        blockList.put(MyGame.BLOCK_GRASS.id, MyGame.BLOCK_GRASS);
//        blockList.put(BlockList.BLOCK_AIR.id, BlockList.BLOCK_AIR);
//
//        try {
//            BlockArrayTexture textureMap = new BlockArrayTexture(ResourceUtils.BLOCK_TEXTURE_DIR);
//            for (Block b : blockList.values()) {
//                if (b.texture != null) {
//                    b.texture.init(textureMap);
//                }
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(GreedyMeshTester.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        generateVoxels();
//
//        GreedyMesherNublada mesher_Nublada = new GreedyMesherNublada(voxels, blockList);
//        GreedyMesher mesher_tessera = new GreedyMesher(voxels, blockList);
//        GreedyMesherWithLight mesher_tesseraLight = new GreedyMesherWithLight(voxels, blockList);
//
//        long startMem = MemoryProfiler.getMemoryUsed();
//        long totalMemory = 0;
//        int start = 5;
//
//        if (mode == Mode.TEST_SPEED) {
//            for (int b = 0; b < trials; b++) {
//                generateVoxels();
//
//                //TEST XB
//                try (MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
//                    watch_tessera.start();
//                    mesher_tessera.compute(new BufferSet(), new BufferSet(),
//                            new Vector3i(0, 0, 0), stack, 1);
//                    watch_tessera.calculateElapsedTime();
//                }
//
//                //TEST NUBLADA
//                watch_Nublada.start();
//                mesher_Nublada.compute(new Vector3i(0, 0, 0));
//                watch_Nublada.calculateElapsedTime();
//
//                //TEST XB WITH LIGHT
//                try (MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
//                    watch_tesseraLight.start();
//                    mesher_tesseraLight.compute(new BufferSet(), new BufferSet(),
//                            new Vector3i(0, 0, 0), stack, 1);
//                    watch_tesseraLight.calculateElapsedTime();
//                }
//
//                //CALCULATE TIMES
//                if (b > start) {
//                    times_tessera += (watch_tessera.getElapsedMilliseconds());
//                    times_nublada += (watch_Nublada.getElapsedMilliseconds());
//                    times_tessera_light += (watch_tesseraLight.getElapsedMilliseconds());
//
//                    System.out.print("NUBLADA:\t" + watch_Nublada.toString());
//                    System.out.print("\ttessera:\t" + watch_tessera.toString());
//                    System.out.print("\ttessera LIGHT:\t" + watch_tesseraLight.toString());
//                    System.out.println("\t\t (trial " + b + "/" + trials + ") (" + MemoryProfiler.getMemoryUsageAsString() + ") seed:" + fastNoise.getSeed());
//                }
//            }
//        } else {
//            for (int b = 0; b < trials; b++) {
//                if (mode == Mode.TEST_NUBLADA_MEMORY) {
//                    watch_Nublada.start();
//                    mesher_Nublada.compute(new Vector3i(0, 0, 0));
//                    watch_Nublada.calculateElapsedTime();
//                } else if (mode == Mode.TEST_tessera_MEMORY) {
//                    try (MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
//                        watch_tessera.start();
//                        BufferSet buff1 = new BufferSet();
//                        BufferSet buff2 = new BufferSet();
//                        GreedyMesher mesher_tessera2 = new GreedyMesher(voxels, blockList);
////                        mesher_tessera2.compute(buff1, buff2, new Vector3i(0, 0, 0), false, stack);
//                        watch_tessera.calculateElapsedTime();
//
////                        MemoryUtil.memFree(buff1.getVertexSet());
////                        MemoryUtil.memFree(buff2.getVertexSet());
//                    }
//                }
//
//                times_tessera += (watch_tessera.getElapsedMilliseconds());
//                times_nublada += (watch_Nublada.getElapsedMilliseconds());
//                if (mode == Mode.TEST_NUBLADA_MEMORY) {
//                    System.out.print("NUBLADA:\t" + watch_Nublada.toString());
//                } else {
//                    System.out.print("tessera:\t" + watch_tessera.toString());
//                }
//                System.out.println("\t\t (trial " + b + "/" + trials + ") (" + MemoryProfiler.getMemoryUsageAsString() + ") seed:" + fastNoise.getSeed());
//                long memDiff = MemoryProfiler.getMemoryUsed() - startMem;
//                startMem = MemoryProfiler.getMemoryUsed();
//                if (memDiff >= 0) {
//                    totalMemory += memDiff;
//                    System.out.println("Memory increase: " + MemoryProfiler.formatBytes(memDiff));
//                }
//
//            }
//        }
//
//        int realTrials = (trials - start);
//        System.out.println("\nNUBLADA TIME AVERAGE:\t" + (times_nublada / realTrials) + " ms"
//                + "\ntessera TIME AVERAGE:\t" + (times_tessera / realTrials) + " ms"
//                + "\ntessera (W/ LIGHT) TIME AVERAGE:\t" + (times_tessera_light / realTrials) + " ms"
//                + "\n\nTotal Memory used:\t" + MemoryProfiler.formatBytes(totalMemory)
//                + "\nMemory used/compute (avg):\t" + MemoryProfiler.formatBytes(totalMemory / trials));
//
//    }
//
//}
