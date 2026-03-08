package com.tessera.content.vanilla.blockTools.tools;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.players.localPlayer.raycasting.CursorRay;
import com.tessera.utils.resource.PathHandler;
import com.tessera.engine.common.math.AABB;
import com.tessera.content.vanilla.blockTools.BlockTool;
import com.tessera.content.vanilla.blockTools.BlockTools;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class PlaneTool extends BlockTool {
    public PlaneTool(BlockTools tools, CursorRay cursorRay) {
        super("Plane", tools, cursorRay);
        try {
            setIcon(PathHandler.resourcePath("blockTools\\plane.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//
//    public String toolDescription() {
//        return "Plane";
//    }

    @Override
    public boolean activationKey(int key, int scancode, int action, int mods) {
        if (key == GLFW.GLFW_KEY_3) return true;
        return false;
    }

    @Override
    public void activate() {
        Client.userPlayer.camera.cursorRay.enableBoundaryMode((aabb, created) -> {

            blockBoundarySetEvent(aabb, created);
        });
        Client.userPlayer.camera.cursorRay.boundary_lockToPlane = true;
    }


    private void blockBoundarySetEvent(AABB aabb, boolean created) {
        Block block = BlockRegistry.BLOCK_AIR;
        if (created) {
            block = getSelectedBlock();
        }

        for (int x = (int) aabb.min.x; x < (int) aabb.max.x; x++) {
            for (int y = (int) aabb.min.y; y < (int) aabb.max.y; y++) {
                for (int z = (int) aabb.min.z; z < (int) aabb.max.z; z++) {
                    Main.getServer().setBlock(block.id, x, y, z);
                }
            }
        }
    }

    @Override
    public boolean keyEvent(int key, int scancode, int action, int mods) {
        return false;
    }
}
