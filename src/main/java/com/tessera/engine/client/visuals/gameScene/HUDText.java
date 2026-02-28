package com.tessera.engine.client.visuals.gameScene;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.window.nuklear.NKUtils;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;

import static org.lwjgl.nuklear.Nuklear.*;

public class HUDText {
    ClientWindow window;
    NkContext ctx;
    final String windowTitle = "hudText";
    String text = null;
    long lastMessage = 0;
    NkRect rect;

    public HUDText(NkContext ctx, ClientWindow window) {
        this.window = window;
        this.ctx = ctx;
        rect = NkRect.create().h(100);
    }

    public void setText(String text) {
        this.text = text;
        lastMessage = System.currentTimeMillis();
    }

    public void draw() {

        if (text == null || text.isBlank()) return;
        if (System.currentTimeMillis() - lastMessage > 3000) {
            text = null;
        }

        nk_style_set_font(ctx, Theme.font_10);
        rect.x(0);
        rect.y(window.getHeight() / 2);
        rect.w(window.getWidth());
        ctx.style().window().fixed_background().data().color().set(Theme.color_transparent);
        if (nk_begin(ctx, windowTitle, rect, NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_NO_INPUT)) {
            Nuklear.nk_layout_row_dynamic(ctx, 10, 1);
            if (text != null) NKUtils.text(ctx, text, 10, NK_TEXT_CENTERED);
            Nuklear.nk_layout_row_static(ctx, 20, window.getWidth(), 1);
        }
        nk_end(ctx);
    }
}
