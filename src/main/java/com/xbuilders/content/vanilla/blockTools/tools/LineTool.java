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

public class LineTool extends BlockTool {

    int length;
    final int maxLength = 20;
    final AABB aabb = new AABB();
    final Vector3i start = new Vector3i();
    final Vector3i end = new Vector3i();

    @Override
    public boolean activationKey(int key, int scancode, int action, int mods) {
        if (key == GLFW.GLFW_KEY_4) return true;
        return false;
    }

    public LineTool(BlockTools tools, CursorRay cursorRay) {
        super("Line", tools, cursorRay);
        try {
            setIcon(ResourceUtils.resourceFile("blockTools\\line.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toolDescription() {
        return super.toolDescription() + " (x" + length + ")";
    }

    @Override
    public boolean setBlock(Block item, CursorRay ray, boolean isCreationMode) {
        Vector3i pos = new Vector3i(ray.getHitPos());
        if (length >= 0) pos.add(ray.getHitNormalAsInt());

        if(isCreationMode && getSelectedBlock() == null) return false;

        for (int i = 0; i <= Math.abs(length); i++) {

            if (isCreationMode) Main.getServer().setBlock(item.id, pos.x, pos.y, pos.z);
            else Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, pos.x, pos.y, pos.z);

            if (length < 0) pos.sub(ray.getHitNormalAsInt());
            else {
                pos.add(ray.getHitNormalAsInt());
                if (isCreationMode && Client.world.getBlock(pos.x, pos.y, pos.z).solid) break;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        length += (int) yoffset;
        length = MathUtils.clamp(length, -maxLength, maxLength);
        return true;
    }

    public boolean drawCursor(CursorRay ray, Matrix4f proj, Matrix4f view) {
        if (!ray.hitTarget()) return false;

        start.set(ray.getHitPos());
        if (length >= 0) start.add(ray.getHitNormalAsInt());

        end.set(ray.getHitNormalAsInt()).mul(length).add(start);
        aabb.min.set(
                Math.min(start.x, end.x),
                Math.min(start.y, end.y),
                Math.min(start.z, end.z));

        aabb.max.set(
                Math.max(start.x + 1, end.x + 1),
                Math.max(start.y + 1, end.y + 1),
                Math.max(start.z + 1, end.z + 1));

        ray.cursorBox.set(aabb);
        ray.cursorBox.draw(proj, view);
        return true;
    }


    @Override
    public void activate() {
        cursorRay.disableBoundaryMode();
    }


}
