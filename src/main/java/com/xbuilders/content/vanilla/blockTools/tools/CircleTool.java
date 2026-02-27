package com.xbuilders.content.vanilla.blockTools.tools;

import com.xbuilders.Main;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.server.block.BlockRegistry;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.common.players.localPlayer.raycasting.CursorRay;
import com.xbuilders.engine.common.resource.ResourceUtils;
import com.xbuilders.engine.common.math.AABB;
import com.xbuilders.engine.common.math.MathUtils;
import com.xbuilders.content.vanilla.blockTools.BlockTool;
import com.xbuilders.content.vanilla.blockTools.BlockTools;
import com.xbuilders.window.nuklear.components.NumberBox;
import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;

import static org.lwjgl.nuklear.Nuklear.*;

public class CircleTool extends BlockTool {
    public CircleTool(BlockTools tools, CursorRay cursorRay) {
        super("Circle", tools, cursorRay);
        hasOptions = true;
        wallThickness = new NumberBox(10);
        wallThickness.setMinValue(1);
        wallThickness.setMaxValue(3);
        wallThickness.setValueAsNumber(1);
        try {
            setIcon(ResourceUtils.resourceFile("blockTools\\circle.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    final AABB renderingAABB = new AABB();
    final AABB settingAABB = new AABB();
    int size;
    final int MAX_RADIUS = 50;
    boolean placeOnHit = false;
    boolean hollow = false;
    NumberBox wallThickness;

    @Override
    public void drawOptionsUI(MemoryStack stack, NkContext ctx, NkRect windowSize) {
        nk_layout_row_dynamic(ctx, 30, 1);
        ByteBuffer active = stack.malloc(1);
        active.put(0, hollow ? (byte) 0 : 1); //For some reason the boolean needs to be flipped
        if (nk_checkbox_label(ctx, "hollow", active)) {
            hollow = !hollow;
        }
        if (hollow) {
            nk_layout_row_dynamic(ctx, 30, 2);
            nk_label(ctx, "Wall thickness", Nuklear.NK_TEXT_LEFT);
            wallThickness.render(ctx);
        }
    }


    @Override
    public boolean activationKey(int key, int scancode, int action, int mods) {
        if (key == GLFW.GLFW_KEY_5) return true;
        return false;
    }

    @Override
    public String toolDescription() {
        return "Fill (x" + (size * 2) + ")";
    }

    @Override
    public void activate() {
        Client.userPlayer.camera.cursorRay.disableBoundaryMode();
    }


    @Override
    public boolean setBlock(Block item, CursorRay ray, boolean isCreationMode) {
        setAABB(settingAABB, ray);

        Block block = isCreationMode ? getSelectedBlock() : BlockRegistry.BLOCK_AIR;
        if (isCreationMode && block == null) return false;

        //do A BFS from the center of the box
        ArrayList<Vector3i> queue = new ArrayList<>();
        Vector3i origin = getStartingPos(ray);
        queue.add(origin);
        HashSet<Vector3i> visited = new HashSet<>();

        System.out.println("AABB: " + settingAABB.getXLength() + ", " + settingAABB.getYLength() + ", " + settingAABB.getZLength());

        while (!queue.isEmpty()) {
            Vector3i pos = queue.remove(0);
            float radius = Math.max(settingAABB.getXLength(), settingAABB.getZLength()) / 2;

            setBlock(origin, pos.x, pos.y, pos.z, block, radius);

            if (settingAABB.getXLength() > 0) {
                propagate(origin, pos.x + 1, pos.y, pos.z, block, queue, radius, visited);
                propagate(origin, pos.x - 1, pos.y, pos.z, block, queue, radius, visited);
            }
            if (settingAABB.getYLength() > 0) {
                propagate(origin, pos.x, pos.y + 1, pos.z, block, queue, radius, visited);
                propagate(origin, pos.x, pos.y - 1, pos.z, block, queue, radius, visited);
            }
            if (settingAABB.getZLength() > 0) {
                propagate(origin, pos.x, pos.y, pos.z + 1, block, queue, radius, visited);
                propagate(origin, pos.x, pos.y, pos.z - 1, block, queue, radius, visited);
            }

        }

        return true;
    }

    private void setBlock(Vector3i origin, int x, int y, int z, Block block, float radius) {
        if (!hollow || origin.distance(x, y, z) > radius - wallThickness.getValueAsNumber()) {
            Main.getServer().setBlock(block.id, x, y, z);
        }
    }


    private void propagate(Vector3i origin, int x, int y, int z, Block block,
                           ArrayList<Vector3i> queue, float radius, HashSet<Vector3i> visited) {
        if (origin.distance(x, y, z) > radius) return;
        Block b = Client.world.getBlock(x, y, z);
        if ((placeOnHit || !b.solid)
                && !visited.contains(new Vector3i(x, y, z))) {
            visited.add(new Vector3i(x, y, z));
            setBlock(origin, x, y, z, block, radius);
            queue.add(new Vector3i(x, y, z));
        }
    }

    public Vector3i getStartingPos(CursorRay ray) {
        Vector3i start = ray.getHitPosPlusNormal();
        if (placeOnHit) start = ray.getHitPos();
        return start;
    }

    private void setAABB(AABB aabb, CursorRay ray) {
        Vector3i start = getStartingPos(ray);
        Vector3i normal = ray.getHitNormalAsInt();

        aabb.min.set(start.x - size, start.y - size, start.z - size);
        aabb.max.set(start.x + size, start.y + size, start.z + size);

        if (normal.x != 0) {
            aabb.min.x = start.x;
            aabb.max.x = start.x;
        }
        if (normal.y != 0) {
            aabb.min.y = start.y;
            aabb.max.y = start.y;
        }
        if (normal.z != 0) {
            aabb.min.z = start.z;
            aabb.max.z = start.z;
        }
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


    public boolean keyEvent(int key, int scancode, int action, int mods) {
        //If K is pressed
        if (key == GLFW.GLFW_KEY_K && action == GLFW.GLFW_PRESS) {
            placeOnHit = true;
            return true;
        }//If K is released
        else if (key == GLFW.GLFW_KEY_K && action == GLFW.GLFW_RELEASE) {
            placeOnHit = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        size += (int) yoffset;
        size = MathUtils.clamp(size, 1, MAX_RADIUS);
        return true;
    }

}
