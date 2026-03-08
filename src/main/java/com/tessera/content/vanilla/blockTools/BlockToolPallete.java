/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.blockTools;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.window.nuklear.WidgetSizeMeasurement;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import java.util.*;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * @author zipCoder933
 */
public class BlockToolPallete {

    ClientWindow window;
    NkContext ctx;
    WidgetSizeMeasurement buttonWidth;
    BlockTools tools;
    boolean wasOpen = false;

    int palleteMaxColumns = 10;

    int menuWidth = 460;
    int toolMenuHeight = 120;
    int optionsMenuHeight = 150;

    List<BlockTool> toolsList;


    public BlockToolPallete(NkContext ctx, ClientWindow window, List<BlockTool> toolsList, BlockTools tools) {
        this.toolsList = toolsList;
        this.tools = tools;
        this.ctx = ctx;
        this.window = window;
        buttonWidth = new WidgetSizeMeasurement(0);
    }

    public void draw(MemoryStack stack) {
        if (isOpen()) {
            if (!wasOpen) {
                onOpenEvent();
            }
            GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            NkRect windowSize = NkRect.malloc(stack);
            Theme.resetEntireButtonStyle(ctx);
            Theme.resetWindowColor(ctx);
            Theme.resetWindowPadding(ctx);
            nk_style_set_font(ctx, Theme.font_10);


            BlockTool selectedTool = tools.getSelectedTool();
            boolean showOptions = selectedTool.hasOptions;

            float menu1X = window.getWidth() / 2 - (menuWidth / 2);
            float menu1Y = window.getHeight() / 2 - (toolMenuHeight / 2) - toolMenuHeight;
            float menuHeight = toolMenuHeight;
            if (showOptions) menuHeight += optionsMenuHeight;

            nk_rect(menu1X, menu1Y,
                    menuWidth, menuHeight,
                    windowSize);

            //Set window background color
//            ctx.style().window().background().set(Theme.transparent);


            if (nk_begin(ctx, "Pallete", windowSize, Nuklear.NK_WINDOW_NO_SCROLLBAR)) {
                nk_layout_row_dynamic(ctx, 15, 1);

                Theme.resetTextColor(ctx);
                Nuklear.nk_text(ctx, selectedTool.toolDescription(), Nuklear.NK_TEXT_ALIGN_CENTERED);

                if (!nk_window_has_focus(ctx)) {
                    enabled = false;
                }

                ctx.style().button().padding().set(0, 0);

                int itemID = 0;
                int totalRows = 0;
                rows:
                while (true) {
                    boolean firstColunm = true;
                    totalRows++;
                    cols:
                    for (int i = 0; i < palleteMaxColumns; i++) {
                        if (itemID >= toolsList.size()) {
                            break rows;
                        }
                        BlockTool tool = toolsList.get(itemID);

                        if (itemID == tools.selectedTool) {
                            ctx.style().button().border_color().set(Theme.color_white);
                        } else {
                            ctx.style().button().border_color().set(Theme.color_blue);
                        }

                        if (tool != null && tool.getNKIcon() != null) {
                            if (firstColunm) {
                                nk_layout_row_dynamic(ctx, buttonWidth.width, palleteMaxColumns);
                                firstColunm = false;
                            }
//                            if (Nuklear.nk_widget_is_hovered(ctx)) {
//                                hoveredTool = tool;
//                            }
                            if (Nuklear.nk_button_image(ctx, tool.getNKIcon())) {
                                tools.selectTool(itemID);
                            }
                        }

                        itemID++;
                    }
                }
                buttonWidth.measure(ctx, stack);
                toolMenuHeight = (int) ((totalRows * buttonWidth.width) - 1) + 20;
                Theme.resetEntireButtonStyle(ctx);

                if (showOptions) optionsGroup(stack, windowSize, selectedTool);
            }
            nk_end(ctx);
        } else if (wasOpen) {
            onCloseEvent();
        }
        wasOpen = false;
    }



    private void onCloseEvent() {
    }

    private void onOpenEvent() {
    }

    private void optionsGroup(MemoryStack stack, NkRect windowSize, BlockTool tool) {
        nk_layout_row_dynamic(ctx, optionsMenuHeight + 20, 1);//Super important
        if (Nuklear.nk_group_begin(ctx, "Options", Nuklear.NK_WINDOW_TITLE)) {
            tool.drawOptionsUI(stack, ctx, windowSize);
            Nuklear.nk_group_end(ctx);
        }
    }

    public boolean isOpen() {
        return enabled;
    }

    boolean enabled = false;

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_LEFT_ALT) {
            enabled = !enabled;
            return true;
        } else if (action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_ESCAPE) {
            enabled = false;
            return true;
        }
        return enabled;
    }
}
