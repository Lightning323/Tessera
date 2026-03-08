/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.common.players.localPlayer.LocalPlayer;
import com.tessera.engine.common.players.localPlayer.raycasting.CursorRay;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemWindow;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.server.item.StorageSpace;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.window.NKWindow;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * @author zipCoder933
 */
public class UI_Hotbar extends UI_GameMenu {

    int menuWidth = 650;
    int menuHeight = 65 + 20;
    final int ELEMENTS = 11;
    int pushValue;
    public static StorageSpace playerStorage;
    LocalPlayer userPlayer;

    public UI_Hotbar(NkContext ctx, NKWindow window, LocalPlayer userPlayer) {
        super(ctx, window);
        this.userPlayer = userPlayer;
        playerStorage = userPlayer.inventory;
    }


    @Override
    public void draw(MemoryStack stack) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return;
        NkRect windowDims2 = NkRect.malloc(stack);

        ctx.style().window().fixed_background().data().color().set(Theme.color_transparent);
        ctx.style().button().normal().data().color().set(Theme.color_transparent);
        ctx.style().window().border_color().set(Theme.color_transparent);
        ctx.style().button().padding().set(0, 0);
        nk_style_set_font(ctx, Theme.font_10);

        int x = window.getWidth() / 2 - (menuWidth / 2);
        int y = window.getHeight() - menuHeight - 20;

        //Draw healthbars
        if (Main.getServer().getGameMode() == GameMode.ADVENTURE) {
            nk_rect(x, y - 60, menuWidth, menuHeight + 2, windowDims2);
            if (nk_begin(ctx, "health", windowDims2, NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_BORDER)) {
                nk_layout_row_dynamic(ctx, 10, 3);
                nk_text(ctx, "Health: " + (int) (userPlayer.getHealth()), NK_TEXT_ALIGN_LEFT);
                nk_text(ctx, "Hunger: " + (int) (userPlayer.getFoodLevel()), NK_TEXT_ALIGN_LEFT);
                nk_text(ctx, "Air: " + (int) (userPlayer.getOxygenLevel()), NK_TEXT_ALIGN_LEFT);
                nk_layout_row_dynamic(ctx, 20, 3);
//            ctx.style().progress().normal().data().color().set(Theme.color_red);
                nk_prog(ctx,
                        (long) (userPlayer.getHealth() * 10),
                        (int) userPlayer.MAX_HEALTH * 10, false);

                nk_prog(ctx,
                        (long) (userPlayer.getFoodLevel() * 10),
                        (int) userPlayer.MAX_FOOD * 10, false);

                nk_prog(ctx,
                        (long) (userPlayer.getOxygenLevel() * 10),
                        (int) userPlayer.MAX_OXYGEN * 10, false);
//            Theme.resetProgressBar(ctx);
            }
            nk_end(ctx);
        }

        //Draw hotbar
        nk_rect(x, y, menuWidth, menuHeight + 2, windowDims2);
        ctx.style().window().fixed_background().data().color().set(Theme.color_backgroundColor);
        ctx.style().window().padding().set(4, 4);
        if (nk_begin(ctx, "HotbarB", windowDims2, NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_BORDER)) {
            playerStorage.deleteEmptyItems();
            nk_layout_row_dynamic(ctx, 20, 1);
            if (userPlayer.getSelectedItem() != null && userPlayer.getSelectedItem().item != null)
                nk_text(ctx, userPlayer.getSelectedItem().item.name, NK_TEXT_ALIGN_CENTERED);

            nk_layout_row_dynamic(ctx, UI_ItemWindow.getItemSize(), ELEMENTS);
            int i = 0;
            for (int j = 0; j < ELEMENTS; j++) {
                i = j + pushValue;

                if (i >= playerStorage.size()) {
                    break;
                }
                ItemStack item = playerStorage.get(i);

                //if (buttonHeight.isCalibrated()) {
                if (i == userPlayer.getSelectedItemIndex()) {
                    ctx.style().button().border_color().set(Theme.color_white);
                } else {
                    ctx.style().button().border_color().set(Theme.color_blue);
                }
                //}
                if (item != null) {
                    UI_ItemWindow.drawItemStackButton(stack, ctx, item);
                } else {
                    Nuklear.nk_button_text(ctx, "");
                }
                //buttonHeight.measure(ctx, stack);
            }
        }
        nk_end(ctx);
        Theme.resetEntireButtonStyle(ctx);
        Theme.resetWindowPadding(ctx);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    protected void changeSelectedIndex(float increment) {
        userPlayer.changeSelectedIndex(increment);
        if (userPlayer.getSelectedItemIndex() >= ELEMENTS + pushValue) {
            pushValue++;
        } else if (userPlayer.getSelectedItemIndex() < pushValue) {
            pushValue--;
        }
        pushValue = MathUtils.clamp(pushValue, 0, playerStorage.size() - ELEMENTS);
    }

    public void setSelectedIndex(int index) {
        userPlayer.setSelectedIndex(index);
        pushValue = userPlayer.getSelectedItemIndex() - ELEMENTS / 2;
        pushValue = MathUtils.clamp(pushValue, 0, playerStorage.size() - ELEMENTS);
    }

    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return false;

        changeSelectedIndex(-scroll.y());
        return true;
    }

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return false;

        if (action == GLFW.GLFW_PRESS) {
            if (key == GLFW.GLFW_KEY_COMMA) {
                changeSelectedIndex(-1);
            } else if (key == GLFW.GLFW_KEY_PERIOD) {
                changeSelectedIndex(1);
            } else if (key == GLFW.GLFW_KEY_Q) {
                userPlayer.dropItem(userPlayer.getSelectedItem());
                playerStorage.set(userPlayer.getSelectedItemIndex(), null);
            }
        }
        return false;
    }

    public void pickItem(CursorRay ray, boolean allowAcquire) {
        ItemStack stack = null;
        StorageSpace storageSpace = playerStorage;
        if (ray.hitTarget()) {
            if (ray.getEntity() != null) {
                Entity entity = ray.getEntity();
                stack = new ItemStack(Registrys.getItem(entity), 1);
            } else {
                Block block = Client.world.getBlock(ray.getHitPos().x, ray.getHitPos().y, ray.getHitPos().z);
                Item item = Registrys.getItem(block);
                if (item != null) stack = new ItemStack(item, 1);
            }
        }
        if (stack == null) return;
        for (int i = 0; i < storageSpace.size(); i++) {
            ItemStack item = storageSpace.get(i);
            if (item != null && item.item == stack.item) {
                setSelectedIndex(i);
                return;
            }
        }
        if (allowAcquire) setSelectedIndex(storageSpace.acquireItem(stack));
    }
}
