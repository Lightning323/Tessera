package com.tessera.engine.client.visuals;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.nk_end;

public class OpenHandler {

    private final String WINDOW_TITLE;
    private final int windowFlags;
    private boolean isOpen = false;
    NkContext ctx;

    public OpenHandler(NkContext ctx, String windowTitle, int windowFlags) {
        this.ctx = ctx;
        this.WINDOW_TITLE = windowTitle;
        this.windowFlags = windowFlags;

        // We have to create the window initially
        nk_begin(ctx, WINDOW_TITLE, NkRect.create(), windowFlags);
        nk_end(ctx);
        nk_window_show(ctx, WINDOW_TITLE, windowFlags);
        isOpen = false;
    }


    public void autoClose() {
        if (nk_window_is_hidden(ctx, WINDOW_TITLE)) {
            System.out.println("HIDE");
            setOpen(false);
        }
    }

    public void setOpen(boolean open) {
        isOpen = open;
        if (open) {
            nk_window_show(ctx, WINDOW_TITLE, windowFlags);
        }
    }

    public boolean isOpen() {
        return isOpen;
    }
}
