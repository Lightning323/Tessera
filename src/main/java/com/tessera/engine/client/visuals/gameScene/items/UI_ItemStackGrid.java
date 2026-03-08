package com.tessera.engine.client.visuals.gameScene.items;

import com.tessera.Main;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.server.item.StorageSpace;
import com.tessera.window.NKWindow;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkInput;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import java.util.function.Predicate;

import static org.lwjgl.nuklear.Nuklear.*;

public class UI_ItemStackGrid {
    final String title;
    public Predicate<ItemStack> itemFilter;

    String hoveredItem;
    public final StorageSpace storageSpace;
    UI_ItemWindow box;
    NKWindow window;
    int rowCount = 0;
    final int flags;
    public boolean showButtons = true;
    long lastTimeShown = 0;

    private boolean allowInput() {
        return box.allowInput();
    }

    public UI_ItemStackGrid(NKWindow window, String title, StorageSpace storageSpace, UI_ItemWindow box, boolean showTitle) {
        this.title = title;
        this.storageSpace = storageSpace;
        this.box = box;
        this.window = window;

        if (showTitle) flags = NK_WINDOW_TITLE | NK_WINDOW_NO_SCROLLBAR;
        else flags = NK_WINDOW_NO_SCROLLBAR;
    }

    public void draw(MemoryStack stack, NkContext ctx, int maxColumns) {
        if (nk_group_begin(ctx, title, flags)) {
            lastTimeShown = System.currentTimeMillis();

            NkRect buttonBounds = NkRect.calloc(stack);
            NkInput input = ctx.input();

            storageSpace.deleteEmptyItems();
            //Set up
            nk_style_set_font(ctx, Theme.font_10);
            hoveredItem = null;


            //Draw buttons
            if (showButtons) {
                nk_layout_row_dynamic(ctx, 20, Main.getServer().getGameMode() == GameMode.FREEPLAY ? 2 : 1);
                if (nk_button_label(ctx, "Sort")) {
                    storageSpace.organize();
                } else if (Main.getServer().getGameMode() == GameMode.FREEPLAY && nk_button_label(ctx, "Clear")) {
                    for (int i = 0; i < storageSpace.size(); i++) {
                        storageSpace.set(i, null);
                    }
                }
            }

            //Draw item grid
            rowCount = 0;
            int index = 0;
            rows:
            while (true) {
                nk_layout_row_static(ctx, UI_ItemWindow.getItemSize(), UI_ItemWindow.getItemSize(), maxColumns);

//                if (buttonSize.isCalibrated()) {
//                    nk_layout_row_static(ctx, buttonSize.width, (int) buttonSize.width, maxColumns);
//                } else nk_layout_row_dynamic(ctx, buttonSize.width, maxColumns);

                rowCount++;
                cols:
                for (int i = 0; i < maxColumns; i++) {
                    if (index >= storageSpace.size()) {
                        break rows;
                    }
                    ItemStack item = storageSpace.get(index);
                    ctx.style().button().border_color().set(Theme.color_blue);


                    if (item != null) {
                        if (nk_widget_is_hovered(ctx)) {
                            hoveredItem = itemTooltip(item);
                        }
                        if (UI_ItemWindow.drawItemStackButton(stack, ctx, item, buttonBounds)) {//Left click
                            if (allowInput()) itemClickEvent(item, index, false);
                        } else if (Nuklear.nk_input_is_mouse_click_in_rect(input, NK_BUTTON_RIGHT, buttonBounds)) {//Right click
                            if (allowInput()) itemClickEvent(item, index, true);
                        }
                    } else {
                        Nuklear.nk_widget_bounds(ctx, buttonBounds);
                        if (nk_button_text(ctx, "")) {//Left click
                            if (allowInput()) itemClickEvent(item, index, false);
                        } else if (Nuklear.nk_input_is_mouse_click_in_rect(input, NK_BUTTON_RIGHT, buttonBounds)) {//Right click
                            if (allowInput()) itemClickEvent(item, index, true);
                        }
                    }
                    index++;
//                    buttonSize.measure(ctx, stack);
                }
            }
            if (hoveredItem != null) Nuklear.nk_tooltip(ctx, " " + hoveredItem + "");
//            if (Nuklear.nk_tooltip_begin(ctx, 300)) {
//                UI_Inventory.drawItemStack(stack, ctx, new ItemStack(Items.TOOL_ANIMAL_FEED,4));
//            }   Nuklear.nk_tooltip_end(ctx);
        }
        nk_group_end(ctx);

    }


    private String itemTooltip(ItemStack item) {
        String str = item.item.name;
        if (item.item.maxDurability > 0) {
            str += "\n " + ((int) item.durability) + " / " + item.item.maxDurability;
        }
        return str;
    }

    @FunctionalInterface
    public interface DragEvent {
        void onDrag(ItemStack item, int index, boolean rightClick);
    }

    public DragEvent dragFromEvent, dragToEvent;

    /**
     * When the box is clicked
     *
     * @param clickedItem
     * @param index
     */
    private void itemClickEvent(ItemStack clickedItem, int index, boolean rightClick) {

        if (!allowInput()) return;

        if (box.draggingItem != null && (itemFilter == null || itemFilter.test(box.draggingItem))) {
            if (rightClick) {
                if (box.draggingItem.stackSize > 0) {
                    if (clickedItem == null) {
                        box.draggingItem.stackSize--;
                        ItemStack to = new ItemStack(box.draggingItem.item, 1);
                        storageSpace.set(index, to);
                        if (dragToEvent != null) dragToEvent.onDrag(to, index, rightClick);
                    } else if (clickedItem.item.equals(box.draggingItem.item) && clickedItem.stackSize < clickedItem.item.maxStackSize) {
                        box.draggingItem.stackSize--;
                        clickedItem.stackSize++;
                        if (dragToEvent != null) dragToEvent.onDrag(clickedItem, index, rightClick);
                    }
                }
                if (box.draggingItem.stackSize <= 0) {
                    ItemStack item = box.draggingItem;
                    box.draggingItem = null;
                    if (dragToEvent != null) dragToEvent.onDrag(item, index, rightClick);
                }
            } else if (clickedItem != null && box.draggingItem.item.equals(clickedItem.item)
                    && clickedItem.item.maxStackSize > 1 && box.draggingItem.stackSize < clickedItem.item.maxStackSize) {
                ItemStack thisStack = storageSpace.get(index);

                int totalStackSize = thisStack.stackSize + box.draggingItem.stackSize;
                if (totalStackSize > thisStack.item.maxStackSize) {
                    box.draggingItem.stackSize = (totalStackSize - thisStack.item.maxStackSize);
                    thisStack.stackSize = thisStack.item.maxStackSize;
                } else {
                    thisStack.stackSize += box.draggingItem.stackSize;
                    box.draggingItem = null;
                    if (dragToEvent != null) dragToEvent.onDrag(thisStack, index, rightClick);
                }
            } else {
                ItemStack replaceStack = storageSpace.get(index);
                ItemStack originalDraggingItem = box.draggingItem;
                storageSpace.set(index, box.draggingItem);
                box.draggingItem = replaceStack;

                if (dragToEvent != null) dragToEvent.onDrag(originalDraggingItem, index, rightClick);
                if (dragFromEvent != null && replaceStack != null)
                    dragFromEvent.onDrag(replaceStack, index, rightClick);
            }
        } else if (clickedItem != null) {
            box.draggingItem = clickedItem;
            storageSpace.set(index, null);
            if (dragFromEvent != null) dragFromEvent.onDrag(box.draggingItem, index, rightClick);
        }
        storageSpace.changeEvent();
    }
}
