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

import static org.lwjgl.nuklear.Nuklear.*;

public class SphereTool extends BlockTool {
    public SphereTool(BlockTools tools, CursorRay cursorRay) {
        super("Sphere", tools, cursorRay);
        hasOptions = true;
        wallThickness = new NumberBox(10);
        wallThickness.setValueAsNumber(1);
        wallThickness.setMinValue(1);
        wallThickness.setMaxValue(10);

        try {
            setIcon(ResourceUtils.resourceFile("blockTools\\sphere.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    boolean hollow = false;
    NumberBox wallThickness;

    @Override
    public void drawOptionsUI(MemoryStack stack, NkContext ctx, NkRect windowSize) {
        nk_layout_row_dynamic(ctx, 30, 1);
        ByteBuffer active = stack.malloc(1);
        active.put(0, hollow ? (byte) 0 : 1); //For some reason the boolean needs to be flipped
        if (nk_checkbox_label(ctx, "hollow sphere", active)) {
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
        if (key == GLFW.GLFW_KEY_7) return true;
        return false;
    }

    @Override
    public String toolDescription() {
        return "Sphere (x" + (size * 2) + ")";
    }

    @Override
    public void activate() {
        Client.userPlayer.camera.cursorRay.disableBoundaryMode();
    }


    @Override
    public boolean setBlock(Block item, CursorRay ray, boolean isCreationMode) {
        setAABB(aabb, ray);

        if(isCreationMode && getSelectedBlock() == null) return false;

        //Get block at cursor hit position
        Vector3i origin = getStartingPos(ray);
        Block newBlock = isCreationMode ? getSelectedBlock() : BlockRegistry.BLOCK_AIR;
        if (newBlock == null) return false;

        for (int x = (int) aabb.min.x; x < (int) aabb.max.x; x++) {
            for (int y = (int) aabb.min.y; y < (int) aabb.max.y; y++) {
                for (int z = (int) aabb.min.z; z < (int) aabb.max.z; z++) {
                    set(x, y, z, origin, newBlock);
                }
            }
        }

        return true;
    }

    private void set(int x, int y, int z, Vector3i origin, Block newBlock) {
        float radius = aabb.getXLength() / 2;
        if (origin.distance(x, y, z) > radius) return;
        if (!newBlock.isAir() && hollow &&
                origin.distance(x, y, z) < radius - wallThickness.getValueAsNumber()) return; //Make the sphere hollow

        Block prevBlock = Client.world.getBlock(x, y, z);
        if (prevBlock != newBlock && (!prevBlock.solid || newBlock.isAir())) {
            Main.getServer().setBlock(newBlock.id, x, y, z);
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
    final AABB aabb = new AABB();
    int size;
    final int MAX_RADIUS = 40;

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        return false;
    }

    @Override
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        size += (int) yoffset;
        size = MathUtils.clamp(size, 1, MAX_RADIUS);
        return true;
    }

}
