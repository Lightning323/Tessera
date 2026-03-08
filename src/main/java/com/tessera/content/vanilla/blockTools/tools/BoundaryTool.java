package com.tessera.content.vanilla.blockTools.tools;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.players.localPlayer.raycasting.CursorRay;
import com.tessera.utils.resource.PathHandler;
import com.tessera.engine.common.math.AABB;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;
import com.tessera.content.vanilla.blockTools.BlockTool;
import com.tessera.content.vanilla.blockTools.BlockTools;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.HashSet;

public class BoundaryTool extends BlockTool {
    public BoundaryTool(BlockTools tools, CursorRay cursorRay) {
        super("Boundary", tools, cursorRay);
        try {
            setIcon(PathHandler.resourcePath("blockTools\\boundary.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


//
//    public String toolDescription() {
//        return name + (GameScene.player.camera.cursorRay.boundary_lockToPlane ? " Plane" : "");
//    }

    @Override
    public boolean activationKey(int key, int scancode, int action, int mods) {
        if (key == GLFW.GLFW_KEY_2) return true;
        return false;
    }

    @Override
    public void activate() {
        Client.userPlayer.camera.cursorRay.enableBoundaryMode((aabb, created) -> {
            blockBoundarySetEvent(aabb, created);
        });
        Client.userPlayer.camera.cursorRay.boundary_lockToPlane = false;
    }


    private void blockBoundarySetEvent(AABB aabb, boolean created) {
        Block block = BlockRegistry.BLOCK_AIR;
        if (created) {
            if (getSelectedBlock() == null) return;
            block = getSelectedBlock();
        }

        HashSet<Chunk> foundChunks = new HashSet<Chunk>();
        WCCi wcc = new WCCi();

        for (int x = (int) aabb.min.x; x < (int) aabb.max.x; x++) {
            for (int y = (int) aabb.min.y; y < (int) aabb.max.y; y++) {
                for (int z = (int) aabb.min.z; z < (int) aabb.max.z; z++) {
                    Main.getServer().setBlock(block.id, x, y, z);
                    foundChunks.add(wcc.set(x, y, z).getChunk(Main.getClient().world));
                }
            }
        }
        //Deleate all allEntities within aabb
        //We should delete allEntities within boundary wether we are creating or not
//        for (Chunk chunk : foundChunks) { //TODO: Fix this
//            for (Entity entity : chunk.allEntities.entities) {
//                if (entity.worldPosition.x >= aabb.min.x && entity.worldPosition.x <= aabb.max.x
//                        && entity.worldPosition.y >= aabb.min.y && entity.worldPosition.y <= aabb.max.y
//                        && entity.worldPosition.z >= aabb.min.z && entity.worldPosition.z <= aabb.max.z) {
//                    entity.destroy();
//                }
//            }
//        }
    }

    @Override
    public boolean keyEvent(int key, int scancode, int action, int mods) {
        return false;
    }
}
