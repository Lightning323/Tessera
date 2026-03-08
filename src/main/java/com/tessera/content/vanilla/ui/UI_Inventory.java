/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.ui;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.client.visuals.gameScene.UI_Hotbar;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemIndex;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemStackGrid;
import com.tessera.engine.client.visuals.gameScene.items.UI_ItemWindow;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.window.NKWindow;
import com.tessera.window.WindowEvents;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * @author zipCoder933
 */
public class UI_Inventory extends UI_ItemWindow implements WindowEvents {

    public static final int KEY_OPEN_INVENTORY = GLFW.GLFW_KEY_E;

    public UI_Inventory(NkContext ctx, Item[] itemList, NKWindow window, UI_Hotbar hotbar) {
        super(ctx, window, "Item List");
        this.hotbar = hotbar;
        allItems = new UI_ItemIndex(this);
        allItems.setItemList(itemList);
        allItems.itemClickEvent = (item) -> {

            if (window.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
                draggingItem = new ItemStack(item, item.maxStackSize);
            } else draggingItem = new ItemStack(item, 1);

        };


        craftingGrid = new CraftingUI_Base(ctx, window, this, Client.userPlayer.inventory, 4);
        playerInventory = new UI_ItemStackGrid(window, "Inventory", Client.userPlayer.inventory, this, true);
        // We have to create the window initially
        nk_begin(ctx, title, NkRect.create(), windowFlags);
        nk_end(ctx);
        setOpen(false);
    }


    CraftingUI_Base craftingGrid;
    UI_ItemStackGrid playerInventory;

    int Allitems_Height = 250; //total item list window size
    int craftingGrid_Height = 180; //total item list window size
    final int playerInv_height = 350; //player inventory window size
    UI_Hotbar hotbar;
    UI_ItemIndex allItems;


    public void onOpenEvent() {
        craftingGrid.onCloseEvent();

        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) setOpen(false);
        if (drawAllInventory()) menuDimensions.y = Allitems_Height + playerInv_height;
        else menuDimensions.y = playerInv_height;
    }

    public void onCloseEvent() {
        craftingGrid.onCloseEvent();
    }

    @Override
    public void drawWindow(MemoryStack stack, NkRect windowDims2) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) {
            setOpen(false);
        }

        if (drawAllInventory()) {
            menuDimensions.y = Allitems_Height + playerInv_height;
            allItems.draw(ctx, stack, Allitems_Height);
        } else {
            menuDimensions.y = craftingGrid_Height + playerInv_height;
            craftingGrid.draw(stack, craftingGrid.inputGrid.storageSpace.size());
        }

        nk_layout_row_dynamic(ctx, playerInv_height, 1);
        playerInventory.draw(stack, ctx, maxColumns);

        Theme.resetEntireButtonStyle(ctx);
    }

    private boolean drawAllInventory() {
        return Main.getServer().getGameMode() == GameMode.FREEPLAY;
    }


    @Override
    public void windowResizeEvent(int width, int height) {

    }

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return false;

        if (allItems.keyEvent(key, scancode, action, mods)) return true;
        if (action == GLFW.GLFW_RELEASE && key == KEY_OPEN_INVENTORY) {
            setOpen(!isOpen());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        if (Main.getServer().getGameMode() == GameMode.SPECTATOR) return false;
        allItems.mouseScrollEvent(scroll, xoffset, yoffset);
        return true;
    }

}
