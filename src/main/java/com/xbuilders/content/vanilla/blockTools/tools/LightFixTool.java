//package com.xbuilders.content.vanilla.blockTools.tools;
//
//import com.xbuilders.engine.client.Client;
//import com.xbuilders.engine.server.Registrys;
//import com.xbuilders.engine.server.block.Block;
//import com.xbuilders.engine.common.players.localPlayer.raycasting.CursorRay;
//import com.xbuilders.utils.BFS.ChunkNode;
//import com.xbuilders.engine.common.resource.ResourceUtils;
//import com.xbuilders.engine.common.world.World;
//import com.xbuilders.engine.common.world.chunk.Chunk;
//import com.xbuilders.engine.common.world.light.SunlightUtils;
//import com.xbuilders.engine.common.world.wcc.WCCi;
//import com.xbuilders.content.vanilla.blockTools.BlockTool;
//import com.xbuilders.content.vanilla.blockTools.BlockTools;
//import org.joml.Matrix4f;
//import org.joml.Vector3i;
//import org.lwjgl.glfw.GLFW;
//import org.lwjgl.nuklear.NkContext;
//import org.lwjgl.nuklear.NkRect;
//import org.lwjgl.nuklear.Nuklear;
//import org.lwjgl.system.MemoryStack;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashSet;
//
//import static com.xbuilders.engine.common.world.chunk.Chunk.WIDTH;
//import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
//
//public class LightFixTool extends BlockTool {
//    public LightFixTool(BlockTools tools, CursorRay cursorRay) {
//        super("Light Fix Tool", tools, cursorRay);
//        hasOptions = true;
//        try {
//            setIcon(ResourceUtils.file("blockTools\\lightbulb.png"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void drawOptionsUI(MemoryStack stack, NkContext ctx, NkRect windowSize) {
//        nk_layout_row_dynamic(ctx, 30, 1);
//        Nuklear.nk_label(ctx, "Mode: ", Nuklear.NK_TEXT_ALIGN_LEFT);
//        if (Nuklear.nk_button_label(ctx, (resetChunksMode ? "Reset Chunks" : "Light Fix Pen"))) {
//            resetChunksMode = !resetChunksMode;
//        }
//    }
//
//    boolean resetChunksMode;
//
//    public String toolDescription() {
//        return "Light Fix (Mode: " + (resetChunksMode ? "Reset Chunks" : "Light Fix Pen") + ")";
//    }
//
//    public static boolean fixSunlightPillar(HashSet<Chunk> affectedChunks, Chunk pillarChunk1) {
//
//        ArrayList<ChunkNode> repropQueue = new ArrayList<>();
//
//        for (Chunk chunk : pillarChunk1.pillarInformation.chunks) {
//            chunk.data.resetSun();
//        }
//
//        for (int x = 0; x < WIDTH; x++) {
//            for (int z = 0; z < WIDTH; z++) {
//                boolean addSun = true;
//                for (Chunk chunk : pillarChunk1.pillarInformation.chunks) {// Go DOWN from Y
//                    affectedChunks.add(chunk);
//                    for (int y = 0; y < Chunk.WIDTH; y++) {
//                        Block block = Registrys.getBlock(chunk.data.getBlock(x, y, z));
//                        if (addSun) {
//                            if (block.opaque) {
//                                chunk.data.setSun(x, y, z, (byte) 0);
//                                addSun = false;
//                            }
//                        } else {
//                            chunk.data.setSun(x, y, z, (byte) 0);
//                        }
//                    }
//                }
//            }
//        }
//
//
//        SunlightUtils.propagateSunlight(repropQueue, affectedChunks);
//
//
//        return true;
//    }
//
//    final int SIZE = 32;
//
//    public boolean setBlock(Block item, final CursorRay ray, boolean isCreationMode) {
//        HashSet<Chunk> affectedChunks = new HashSet<>();
//        WCCi wcc = new WCCi().set(ray.getHitPosPlusNormal());
//        Chunk startChunk = Client.world.getChunk(wcc.chunk);
//
//        if (startChunk == null) return true;
//
//        if (resetChunksMode) {
//            for (int x = -1; x <= 1; x++) {
//                for (int z = -1; z <= 1; z++) {
//                    Chunk chunk = Client.world.getChunk(new Vector3i(
//                            startChunk.position.x + x,
//                            startChunk.position.y,
//                            startChunk.position.z + z));
//                    if (chunk != null)
//                        fixSunlightPillar(affectedChunks, chunk.pillarInformation.getTopPillar());
//                }
//            }
//        } else {
//            ArrayList<ChunkNode> filledPropagator = new ArrayList<>();
//            ArrayList<ChunkNode> emptyPropagator = new ArrayList<>();
//
//            for (int x = -SIZE / 2; x < SIZE; x++) {
//                for (int z = -SIZE / 2; z < SIZE; z++) {
//                    filledPropagator.add(new ChunkNode(new WCCi().set(
//                            ray.getHitPosPlusNormal().x + x,
//                            World.WORLD_TOP_Y + 1,
//                            ray.getHitPosPlusNormal().z + z), Client.world));
//                }
//            }
//
//            SunlightUtils.updateFromQueue(filledPropagator, emptyPropagator, affectedChunks, null);
//            SunlightUtils.updateFromQueue(emptyPropagator, filledPropagator, affectedChunks, null);
//        }
//
//
////        for (Chunk chunk : affectedChunks) {
////            chunk.generateMesh(true);
////            chunk.markAsModified();
////        }
//        return true;
//    }
//
//    public void changeMode() {
//        resetChunksMode = !resetChunksMode;
//    }
//
//    @Override
//    public boolean activationKey(int key, int scancode, int action, int mods) {
//        return false;
//    }
//
//    @Override
//    public void activate() {
//    }
//
//    public boolean drawCursor(CursorRay ray, Matrix4f proj, Matrix4f view) {
//        return true;
//    }
//
//    @Override
//    public boolean keyEvent(int key, int scancode, int action, int mods) {
//        if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_M) {
//            changeMode();
//            return true;
//        }
//        return false;
//    }
//}
