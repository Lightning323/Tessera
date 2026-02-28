package com.tessera.engine.client.visuals.gameScene.items;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.window.NKWindow;
import com.tessera.window.nuklear.components.TextBox;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import java.util.*;
import java.util.function.Consumer;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

public class UI_ItemIndex {
    private final String title = "all_item_index";
    TextBox searchBox;
    final List<Item> items, allItems;
    UI_ItemWindow itemWindow;
    public int scrollValue = 0;
    int maxColumns = 11;
    NKWindow window;
    Item hoveredItem;

    private boolean matchesSearch(Item item, String searchCriteria) {
        if (searchCriteria == null || searchCriteria.isBlank() || item.name == null) return true;
        for (String tag : item.tags) {
            if (tag != null && tag.toLowerCase().contains(
                    searchCriteria.toLowerCase().trim())) {
                return true;
            }
        }
        return item.name.toLowerCase().contains(searchCriteria);
    }

    private void clearSearch() {
        searchBox.setValueAsString("");

        items.clear();
        items.addAll(allItems);
    }

    private void searchQueryEvent() {
        items.clear();
        for (Item item : allItems) {
            if (matchesSearch(item, searchBox.getValueAsString())) {
                items.add(item);
            }
        }
    }

    public void setItemList(Item[] itemList) {
        allItems.clear();
        allItems.addAll(Arrays.asList(itemList));
        Collections.sort(allItems, comparator);
        items.addAll(allItems);
    }

    private final Comparator<Item> comparator = (o1, o2) -> {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else {
            for (String tag : o1.getTags()) {  // If 2 items have a shared tag
                if (o2.getTags().equals(tag)) {
                    return 0;
                }
            }
            return 1;
        }
    };

    public UI_ItemIndex(UI_ItemWindow itemWindow) {
        this.itemWindow = itemWindow;
        this.window = itemWindow.window;
        this.items = new ArrayList<>();
        this.allItems = new ArrayList<>();
        searchBox = new TextBox(25);
        searchBox.setOnSelectEvent(() -> clearSearch());
        searchBox.setOnChangeEvent(() -> searchQueryEvent());
    }

    public void draw(NkContext ctx, MemoryStack stack, int Allitems_Height) {
        hoveredItem = null;
        isOver = false;

        //Search box
        nk_layout_row_dynamic(ctx, 20, 1);
        nk_label(ctx, "Search Item List", NK_TEXT_LEFT);
        nk_layout_row_dynamic(ctx, 25, 1);

        searchBox.render(ctx);

        ctx.style().button().padding().set(0, 0);
        inventoryGroup(ctx, stack, Allitems_Height);

        if (hoveredItem != null) {
            String tooltip = " " + hoveredItem.name;
            if (Client.DEV_MODE) tooltip = " " + hoveredItem.id;
            Nuklear.nk_tooltip(ctx, tooltip);
        }
    }

    boolean isOver;

    private void inventoryGroup(NkContext ctx, MemoryStack stack, int Allitems_Height) {
        nk_layout_row_dynamic(ctx, Allitems_Height, 1);
        if (nk_widget_is_hovered(ctx)) { //Widget is hovered counts for groups as well
            isOver = true;
        }
        if (nk_group_begin(ctx, title, NK_WINDOW_NO_SCROLLBAR)) {
            int maxRows = (int) (Math.floor(Allitems_Height / itemWindow.getItemSize()));
            scrollValue = MathUtils.clamp(scrollValue, 0, Math.max(0, (items.size() / maxColumns) - 1));
            int itemID = scrollValue * maxColumns;
            int rows = 0;


            rows:
            while (rows < maxRows) {
                //  nk_layout_row_dynamic(ctx, getItemSize(), maxColumns);// row
                nk_layout_row_static(ctx, itemWindow.getItemSize(), itemWindow.getItemSize(), maxColumns);
                rows++;

                for (int column = 0; column < maxColumns; ) {
                    if (itemID >= items.size()) {
                        break rows;
                    }

                    Item item = items.get(itemID);
                    if (item == null) {
                        if (nk_widget_is_hovered(ctx)) {
                            hoveredItem = null;
                            isOver = true;
                        }
                        nk_button_label(ctx, "");
                    } else {
                        if (nk_widget_is_hovered(ctx)) {
                            hoveredItem = item;
                            isOver = true;
                        }
                        if (nk_button_image(ctx, item.getNKIcon())) {
                            if (itemClickEvent != null) itemClickEvent.accept(item);
                        }
                    }
                    column++;
                    itemID++;
                }
            }
        }
        nk_group_end(ctx);

    }

    public Consumer<Item> itemClickEvent;

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (searchBox.isFocused()) {
            searchQueryEvent();
            return true;
        }
        return false;
    }

    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        if (isOver) scrollValue -= yoffset;
        return true;
    }

}
