package com.xbuilders.content.vanilla.blockTools.tools;

import com.xbuilders.Main;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.server.block.BlockRegistry;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.common.players.localPlayer.raycasting.CursorRay;
import com.xbuilders.utils.resource.ResourceUtils;
import com.xbuilders.engine.common.math.AABB;
import com.xbuilders.engine.common.math.MathUtils;
import com.xbuilders.content.vanilla.blockTools.BlockTool;
import com.xbuilders.content.vanilla.blockTools.BlockTools;
import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkVec2;

import java.io.IOException;
import java.util.ArrayList;

public class PaintTool extends BlockTool {
    public PaintTool(BlockTools tools, CursorRay cursorRay) {
        super("Repaint", tools, cursorRay);
        try {
            setIcon(ResourceUtils.resourceFile("blockTools\\paint.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean activationKey(int key, int scancode, int action, int mods) {
        if (key == GLFW.GLFW_KEY_6) return true;
        return false;
    }

    @Override
    public String toolDescription() {
        return "Repaint (x" + (size * 2) + ")";
    }

    @Override
    public void activate() {
        Client.userPlayer.camera.cursorRay.disableBoundaryMode();
    }


    @Override
    public boolean setBlock(Block item, CursorRay ray, boolean isCreationMode) {
        setAABB(settingAABB, ray);

        if(isCreationMode && getSelectedBlock() == null) return false;

        //Get block at cursor hit position
        Block replaceBlock = Client.world.getBlock(ray.getHitPos().x, ray.getHitPos().y, ray.getHitPos().z);
        Block newBlock = isCreationMode ? getSelectedBlock() : BlockRegistry.BLOCK_AIR;
        if (newBlock == null || newBlock == replaceBlock) return false;

        //do A BFS from the center of the box
        ArrayList<Vector3i> queue = new ArrayList<>();

        Vector3i origin = getStartingPos(ray);
        queue.add(origin);

        long start = System.currentTimeMillis();
        while (!queue.isEmpty() && System.currentTimeMillis() - start < 5000) {
            Vector3i pos = queue.remove(0);

            Main.getServer().setBlock(newBlock.id, pos.x, pos.y, pos.z);
            //MainWindow.printlnDev("Painting: " + MiscUtils.printVector(pos));

            propagate(origin, pos.x + 1, pos.y, pos.z, newBlock, replaceBlock, queue);
            propagate(origin, pos.x - 1, pos.y, pos.z, newBlock, replaceBlock, queue);
            propagate(origin, pos.x, pos.y + 1, pos.z, newBlock, replaceBlock, queue);
            propagate(origin, pos.x, pos.y - 1, pos.z, newBlock, replaceBlock, queue);
            propagate(origin, pos.x, pos.y, pos.z + 1, newBlock, replaceBlock, queue);
            propagate(origin, pos.x, pos.y, pos.z - 1, newBlock, replaceBlock, queue);
        }

        return true;
    }

    private void propagate(Vector3i origin, int x, int y, int z, Block newBlock, Block blockToReplace, ArrayList<Vector3i> queue) {
        if (x > settingAABB.max.x || x < settingAABB.min.x) return;
        if (y > settingAABB.max.y || y < settingAABB.min.y) return;
        if (z > settingAABB.max.z || z < settingAABB.min.z) return;

        float radius = settingAABB.getXLength() / 2;
        if (origin.distance(x, y, z) > radius) return;

        Block existingBlock = Client.world.getBlock(x, y, z);
        if (existingBlock.id == blockToReplace.id) {
            Main.getServer().setBlock(newBlock.id, x, y, z);

            //Check again just in case
            existingBlock = Client.world.getBlock(x, y, z);
            if (existingBlock.id == newBlock.id) {
                queue.add(new Vector3i(x, y, z));
            }
        }
    }

    public Vector3i getStartingPos(CursorRay ray) {
        Vector3i start = ray.getHitPos();
        return start;
    }

    private void setAABB(AABB aabb, CursorRay ray) {
        Vector3i start = getStartingPos(ray);

        aabb.min.set(start.x - size, start.y - size, start.z - size);
        aabb.max.set(start.x + size, start.y + size, start.z + size);
    }

    @Override
    public boolean drawCursor(CursorRay ray, Matrix4f proj, Matrix4f view) {
        if (!ray.hitTarget()) return false;

        setAABB(renderingAABB, ray);
        renderingAABB.max.add(1, 1, 1);

        ray.cursorBox.set(renderingAABB);
        ray.cursorBox.draw(proj, view);
        return true;
    }

    final AABB renderingAABB = new AABB();
    final AABB settingAABB = new AABB();
    int size;
    final int maxLength = 25;

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        return false;
    }

    @Override
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        size += (int) yoffset;
        size = MathUtils.clamp(size, 1, maxLength);
        return true;
    }

}
