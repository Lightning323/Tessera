package com.xbuilders.content.vanilla.blockTools.tools;

import com.xbuilders.Main;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.common.players.localPlayer.raycasting.CursorRay;
import com.xbuilders.engine.common.resource.ResourceUtils;
import com.xbuilders.engine.common.math.AABB;
import com.xbuilders.engine.common.world.chunk.ChunkVoxels;
import com.xbuilders.content.vanilla.blockTools.BlockTool;
import com.xbuilders.content.vanilla.blockTools.BlockTools;
import com.xbuilders.content.vanilla.blockTools.PrefabUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;

import static com.xbuilders.Main.LOGGER;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

public class CopyTool extends BlockTool {
    public CopyTool(BlockTools tools, CursorRay cursorRay) {
        super("Copy", tools, cursorRay);
        hasOptions = true;
        try {
            setIcon(ResourceUtils.resourceFile("blockTools\\copy.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void drawOptionsUI(MemoryStack stack, NkContext ctx, NkRect windowSize) {
        nk_layout_row_dynamic(ctx, 30, 2);
        if (Nuklear.nk_button_label(ctx, "Load Prefab")) {
            Main.getClient().window.gameScene.ui.fileDialog.show(ResourceUtils.appDataFile("prefabs"),
                    false, "xbprefab", (file) -> {
                        System.out.println("LOADING " + file.getAbsolutePath());
                        try {
                            PasteTool.clipboard = PrefabUtils.loadPrefabFromFile(file);
                        } catch (IOException e) {
                            LOGGER.info("Error", e);
                        }
                        System.out.println(PasteTool.clipboard.toString());
                        PasteTool.updateMesh();
                    });
        }
        if (Nuklear.nk_button_label(ctx, "Save Prefab")) {
            Main.getClient().window.gameScene.ui.fileDialog.show(ResourceUtils.appDataFile("prefabs"),
                    true, "xbprefab", (file) -> {
                        System.out.println("SAVING " + file.getAbsolutePath());
                        try {
                            PrefabUtils.savePrefabToFile(PasteTool.clipboard, file);
                        } catch (IOException e) {
                            LOGGER.info("Error", e);
                        }
                    });
        }
    }

    @Override
    public boolean activationKey(int key, int scancode, int action, int mods) {
        //Only activate with Ctrl+C
        if (key == GLFW.GLFW_KEY_C && (mods & GLFW.GLFW_MOD_CONTROL) != 0) {
            return true;
        }
        return false;
    }

    @Override
    public void activate() {
        cursorRay.enableBoundaryMode((aabb, aBoolean) -> blockBoundarySetEvent(aabb, aBoolean));
        cursorRay.boundary_lockToPlane = false;
    }


    private void blockBoundarySetEvent(AABB aabb, boolean created) {
        PasteTool.clipboard = new ChunkVoxels(
                (int) (aabb.getXLength()),
                (int) (aabb.getYLength()),
                (int) (aabb.getZLength()));

        for (int x = (int) aabb.min.x; x < (int) aabb.max.x; x++) {
            for (int y = (int) aabb.min.y; y < (int) aabb.max.y; y++) {
                for (int z = (int) aabb.min.z; z < (int) aabb.max.z; z++) {
                    PasteTool.clipboard.setBlock(
                            (int) (x - aabb.min.x),
                            (int) (y - aabb.min.y),
                            (int) (z - aabb.min.z),
                            Client.world.getBlockID(x, y, z));
                    //Set block data
                    if (Client.world.getBlockData(x, y, z) != null) {
                        PasteTool.clipboard.setBlockData(
                                (int) (x - aabb.min.x),
                                (int) (y - aabb.min.y),
                                (int) (z - aabb.min.z),
                                Client.world.getBlockData(x, y, z));
                    }
                }
            }
        }
        PasteTool.updateMesh();
    }


    @Override
    public boolean keyEvent(int key, int scancode, int action, int mods) {
        return false;
    }
}
