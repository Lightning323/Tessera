package com.tessera.engine.client.visuals.gameScene.items;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.client.visuals.gameScene.UI_GameMenu;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.window.NKWindow;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public abstract class UI_ItemWindow extends UI_GameMenu {
    //    static WidgetSizeMeasurement buttonSize = new WidgetSizeMeasurement(0);
    private static final int ITEM_WIDTH = 52;

    public static final int getItemSize() {
        return ITEM_WIDTH;
    }

    public UI_ItemWindow(NkContext ctx, NKWindow window, String title) {
        super(ctx, window);
        this.title = title;
    }

    public ItemStack draggingItem = null;
    public Vector2i menuDimensions = new Vector2i(642, 645);
    public final int maxColumns = 11;
    private boolean isOpen = false;
    public final int windowFlags = NK_WINDOW_TITLE | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_CLOSABLE;
    public final String title;
    private final NkRect windowDims = NkRect.create();
    boolean allowInput = false;

    public boolean isOpen() {
        return isOpen;
    }

    public void onOpenEvent() {
    }

    public void onCloseEvent() {
    }

    public void setOpen(boolean open) {
        if (open) {
            nk_window_show(ctx, title, windowFlags);
            isOpen = true;
            allowInput = false;
            onOpenEvent();
        } else {
            isOpen = false;
            onCloseEvent();
        }
    }


    public boolean allowInput() {
        return allowInput;
    }

    @Override
    public final void draw(MemoryStack stack) {
        if (isOpen) {

            GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);

            Theme.resetEntireButtonStyle(ctx);
            Theme.resetWindowColor(ctx);
            Theme.resetWindowPadding(ctx);
            nk_style_set_font(ctx, Theme.font_10);

            nk_rect(window.getWidth() / 2 - (menuDimensions.x / 2), window.getHeight() / 2 - (menuDimensions.y / 2), menuDimensions.x, menuDimensions.y, windowDims);

            if (nk_begin(
                    ctx, title, windowDims, windowFlags | (allowInput() ? NK_WINDOW_NO_INPUT : 0)
            )) {
//              calibrate(stack, ctx);
                drawWindow(stack, windowDims);
                if (draggingItem != null) drawItemAtCursor(window, stack, ctx, draggingItem);
            }
            nk_end(ctx);

            if (draggingItem != null) {
                drawOutOfBoundsStackAtCursor(window, stack, ctx, draggingItem);
            }
        }
        if (nk_window_is_hidden(ctx, title)) {
            if (isOpen) onCloseEvent();
            isOpen = false;
        }
        if (!window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)
                && !window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            allowInput = true;
        }


    }

    public boolean mouseButtonEvent(int button, int action, int mods) {
        if (action == GLFW.GLFW_RELEASE) {
            if (!inBounds(windowDims) && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
                if (draggingItem != null) {
                    Client.userPlayer.dropItem(draggingItem);
                    draggingItem = null;
                } else setOpen(false);
                return true;
            }
        }
        return false;
    }

    public abstract void drawWindow(MemoryStack stack, NkRect windowDims2);

    final static NkColor white = Theme.createColor(255, 255, 255, 255);
    final static NkColor green = Theme.createColor(0, 255, 0, 255);
    final static NkColor black = Theme.createColor(0, 0, 0, 255);
    final static int padding = 5;
    //    public final static WidgetSizeMeasurement itemWidth = new WidgetSizeMeasurement(0);
    final static String empty = "";

    public static boolean drawItemStackButton(MemoryStack stack, NkContext ctx, ItemStack itemStack) {
        return drawItemStackButton(stack, ctx, itemStack, null);
    }

    public static boolean drawItemStackButton(MemoryStack stack, NkContext ctx, ItemStack itemStack, NkRect buttonBounds) {
        NkRect widgetBounds = NkRect.calloc(stack); //Get the bounds of the widget
        Nuklear.nk_widget_bounds(ctx, widgetBounds);

        //Draw an empty button
        boolean pressed = nk_button_label(ctx, empty);

        //Set button bounds if the user wants to know them
        if (buttonBounds != null) buttonBounds.set(widgetBounds);

        //We will be reusing widgetBounds to draw the image
        drawItemStack(stack, ctx, itemStack, widgetBounds);
        return pressed;
    }

    private static void drawItemStack(MemoryStack stack, NkContext ctx, ItemStack itemStack, NkRect buttonBounds) {
        NkCommandBuffer canvas = Nuklear.nk_window_get_canvas(ctx); // Get the current drawing canvas
        NkImage bgImage = itemStack.item.getNKIcon();

        NkRect bounds = NkRect.calloc(stack).set(buttonBounds);
        bounds.x(buttonBounds.x() + padding).y(buttonBounds.y() + padding).w(buttonBounds.w() - padding - padding).h(buttonBounds.h() - padding - padding);
        nk_draw_image(canvas, bounds, bgImage, white);

        //draw quantity
        if (!(Main.getServer().getGameMode() == GameMode.FREEPLAY && itemStack.stackSize == 1) && itemStack.item.maxStackSize > 1) {
            bounds.set(buttonBounds);
            bounds.x(buttonBounds.x() + 5).y(buttonBounds.y() + buttonBounds.w() - 16);
            Nuklear.nk_draw_text(canvas, bounds, "" + itemStack.stackSize, Theme.font_10, white, black);
            bounds.y(bounds.y() - 1).x(bounds.x() - 1);
            Nuklear.nk_draw_text(canvas, bounds, "" + itemStack.stackSize, Theme.font_10, black, white);
        }

        //draw durability
        if (itemStack.item.maxDurability > 0 && itemStack.durability < itemStack.item.maxDurability) {
            bounds.set(buttonBounds);
            bounds.y(bounds.y() + bounds.h() - 3 - 4);
            bounds.x(bounds.x() + 2);
            bounds.w(MathUtils.map((float) itemStack.durability / itemStack.item.maxDurability, 0, 1, 0, bounds.w() - 4));
            bounds.h(3);
            Nuklear.nk_fill_rect(canvas, bounds, 0.0f, green); // 0.0f for no rounding
        }

    }

    public void drawItemAtCursor(NKWindow window, MemoryStack stack, NkContext ctx, ItemStack itemStack) {
        NkRect rect = NkRect.malloc(stack);
        Vector2d cursor = window.getCursorVector();
        rect.set((float) cursor.x - (getItemSize() / 2), (float) cursor.y - (getItemSize() / 2), getItemSize(), getItemSize());
        nk_layout_row_dynamic(ctx, getItemSize(), 1);
        drawItemStack(stack, ctx, itemStack, rect);
    }

    public void drawOutOfBoundsStackAtCursor(NKWindow window, MemoryStack stack, NkContext ctx, ItemStack itemStack) {
        NkRect rect = NkRect.malloc(stack);
        Vector2d cursor = window.getCursorVector();
        rect.set((float) cursor.x - (getItemSize() / 2), (float) cursor.y - (getItemSize() / 2), getItemSize(), getItemSize());

        Theme.setWindowStyle(ctx, Theme.color_transparent, Theme.color_transparent);
        if (nk_begin(ctx, "cursor_stack", rect, NK_WINDOW_NO_INPUT | NK_WINDOW_BACKGROUND | NK_WINDOW_NO_SCROLLBAR)) {
            nk_layout_row_dynamic(ctx, getItemSize(), 1);
            drawItemStack(stack, ctx, itemStack, rect);
        }
        Theme.resetWindowColor(ctx);
        nk_end(ctx);
    }
}
