package com.tessera.engine.client.visuals.gameScene.items;

import com.tessera.engine.server.item.Item;
import com.tessera.engine.client.visuals.Theme;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nuklear.Nuklear.*;

public class UI_ItemGrid {
    final String title;
    String hoveredItem;
    public final List<Item> items;
    int rowCount = 0;
    final int flags;
    long lastTimeShown = 0;


    public UI_ItemGrid(String title, boolean showTitle) {
        this.title = title;
        this.items = new ArrayList<>();

        if (showTitle) flags = NK_WINDOW_TITLE | NK_WINDOW_NO_SCROLLBAR;
        else flags = NK_WINDOW_NO_SCROLLBAR;
    }

    public void draw(MemoryStack stack, NkContext ctx, int maxColumns) {
        if (nk_group_begin(ctx, title, flags)) {
            try {
                lastTimeShown = System.currentTimeMillis();

                //Set up
                nk_style_set_font(ctx, Theme.font_10);
                hoveredItem = null;

                //Draw item grid
                rowCount = 0;
                int index = 0;
                rows:
                while (true) {
                    nk_layout_row_static(ctx, UI_ItemWindow.getItemSize(), UI_ItemWindow.getItemSize(), maxColumns);

                    rowCount++;
                    cols:
                    for (int i = 0; i < maxColumns; i++) {
                        if (index >= items.size()) {
                            break rows;
                        }
                        Item item = items.get(index);
                        ctx.style().button().border_color().set(Theme.color_blue);

                        if (item != null) {
                            if (nk_widget_is_hovered(ctx)) {
                                hoveredItem = item.name;
                            }
                            if (nk_button_image(ctx, item.getNKIcon())) {
                            }
                        } else {
                            if (nk_button_text(ctx, "")) {
                            }
                        }
                        index++;
                    }
                }
                if (hoveredItem != null) Nuklear.nk_tooltip(ctx, " " + hoveredItem + "");
            } finally {
                nk_group_end(ctx);
            }
        }
    }
}
