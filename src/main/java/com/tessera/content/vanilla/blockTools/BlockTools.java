package com.tessera.content.vanilla.blockTools;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.players.localPlayer.raycasting.CursorRay;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.client.visuals.gameScene.UI_GameMenu;
import com.tessera.content.vanilla.blockTools.tools.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nuklear.Nuklear.*;

public class BlockTools extends UI_GameMenu {

    public BlockTools(NkContext ctx, ClientWindow window, CursorRay cursorRay) {
        super(ctx, window);
        tools.add(new DefaultTool(this, cursorRay));
        tools.add(new PlaneTool(this, cursorRay));
        tools.add(new BoundaryTool(this, cursorRay));
        tools.add(new LineTool(this, cursorRay));
        tools.add(new CircleTool(this, cursorRay));
        tools.add(new SphereTool(this, cursorRay));
        tools.add(new PaintTool(this, cursorRay));
        tools.add(new CopyTool(this, cursorRay));
        tools.add(new PasteTool(this, cursorRay));
//        tools.add(new LightFixTool(this, cursorRay));
        pallete = new BlockToolPallete(ctx, window, tools, this);
    }

    public final List<BlockTool> tools = new ArrayList<BlockTool>();
    BlockToolPallete pallete;
    int selectedTool;

    public void addTool(BlockTool tool) {
        tools.add(tool);
    }


    int menuWidth = 400;
    int menuHeight = 30;

    @Override
    public void draw(MemoryStack stack) {
        if (Main.getServer().getGameMode() == GameMode.FREEPLAY) {
            NkRect windowDims = NkRect.malloc(stack);

            Theme.resetEntireButtonStyle(ctx);
            Theme.resetWindowColor(ctx);
            Theme.resetWindowPadding(ctx);
            nk_style_set_font(ctx, Theme.font_10);

            nk_rect(window.getWidth() / 2 - (menuWidth / 2),
                    0,
                    menuWidth, menuHeight, windowDims);

            if (nk_begin(ctx, "Block Tools", windowDims, Nuklear.NK_WINDOW_NO_SCROLLBAR)) {
                nk_layout_row_dynamic(ctx, menuHeight, 1);
                Nuklear.nk_text(ctx, tools.get(selectedTool).toolDescription(), Nuklear.NK_TEXT_ALIGN_CENTERED);
            }
            nk_end(ctx);
            pallete.draw(stack);
        }
    }

    @Override
    public boolean isOpen() {
        return pallete.isOpen();
    }

    /**
     * @param scroll
     * @param xoffset
     * @param yoffset
     * @return true if the event was consumed
     */
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        boolean allowToolParameters = true;
        if (allowToolParameters) {
            return tools.get(selectedTool).mouseScrollEvent(scroll, xoffset, yoffset);
        }
        autoRevert();
        return false;
    }

    /**
     * @param key
     * @param scancode
     * @param action
     * @param mods
     * @return true if the event was consumed
     */
    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (Main.getServer().getGameMode() == GameMode.FREEPLAY) {
            if (pallete.keyEvent(key, scancode, action, mods)) {
            } else {
                if (action == GLFW.GLFW_RELEASE || action == GLFW.GLFW_PRESS) {
                    for (int i = 0; i < tools.size(); i++) {
                        if (tools.get(i).activationKey(key, scancode, action, mods)) {
                            selectTool(i);
                            return true;
                        }
                    }
                }
            }
            autoRevert();
            return tools.get(selectedTool).keyEvent(key, scancode, action, mods);
        }
        return false;
    }

    public final int DEFAULT_TOOL_INDEX = 0;

    public void selectTool(int i) {
        tools.get(selectedTool).deactivate();
        selectedTool = i;
        Client.userPlayer.camera.cursorRay.disableBoundaryMode();
        tools.get(selectedTool).activate();
        autoRevert();
    }

    public void selectDefaultTool() {
        tools.get(selectedTool).deactivate();
        selectedTool = DEFAULT_TOOL_INDEX;
        Client.userPlayer.camera.cursorRay.disableBoundaryMode();
        tools.get(selectedTool).activate();
    }

    public void autoRevert() {
//        if (selectedTool != DEFAULT_TOOL_INDEX && !BlockTool.hasBlock()) {
//            selectDefaultTool();
//        }
    }

    public boolean clickEvent(CursorRay ray, boolean isCreationMode) {
        autoRevert();
        Block block = BlockTool.getSelectedBlock();
        if (Main.getServer().getGameMode() != GameMode.FREEPLAY || block == null) return false;
        return getSelectedTool().setBlock(block, ray, isCreationMode);
    }

    public boolean UIMouseButtonEvent(int button, int action, int mods) {
        if (Main.getServer().getGameMode() != GameMode.FREEPLAY) return false;
        return tools.get(selectedTool).mouseButtonEvent(button, action, mods);
    }

    public BlockTool getSelectedTool() {
        if (tools.get(selectedTool) == null) return tools.get(0);
        return tools.get(selectedTool);
    }

    public boolean releaseMouse() {
        return pallete.isOpen();
    }

}
