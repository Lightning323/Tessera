/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.topMenu;

/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.Page;
import com.tessera.engine.client.visuals.Theme;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * Java port of
 * <a href="https://github.com/vurtun/nuklear/blob/master/demo/calculator.c">https://github.com/vurtun/nuklear/blob/master/demo/calculator.c</a>.
 */
public class MenuHome implements MenuPage {

    public MenuHome(NkContext ctx, ClientWindow window, TopMenu menu) {
        this.ctx = ctx;
        this.window = window;
        this.menu = menu;
    }

    NkContext ctx;
    TopMenu menu;
    ClientWindow window;
    final int boxWidth = TopMenu.WIDTH_1;
    final int boxHeight = 460;
    final int titleHeight = 50;

    @Override
    public void layout(MemoryStack stack, NkRect rect, IntBuffer titleYEnd) {
        nk_style_set_font(ctx, Theme.font_12);
        nk_rect((window.getWidth() / 2) - (boxWidth / 2),
                titleYEnd.get(0),
                boxWidth, boxHeight, rect);

        if (nk_begin(ctx, "Home", rect, NK_WINDOW_BORDER | NK_WINDOW_TITLE)) {
            nk_style_set_font(ctx, Theme.font_9);
            nk_layout_row_dynamic(ctx, 30, 1);
            nk_label(ctx, "\n\n" + (
                    Client.DEV_MODE ?
                            "DEV MODE" :
                            ("v" + Main.VERSION)
            ) + "\n\n", NK_TEXT_CENTERED);

            nk_style_set_font(ctx, Theme.font_12);
            if (button(ctx, "NEW WORLD")) {
                menu.setPage(Page.NEW_WORLD);
            }
            if (button(ctx, "LOAD WORLD")) {
                menu.setPage(Page.LOAD_WORLD);
            }
            if (button(ctx, "JOIN MULTIPLAYER")) {
                menu.setPage(Page.JOIN_MULTIPLAYER);
            }
            if (button(ctx, "CUSTOMIZE PLAYER")) {
                menu.setPage(Page.CUSTOMIZE_PLAYER);
            }

            nk_layout_row_static(ctx, 30, 1, 1);
            if (button(ctx, "SETTINGS")) {
                menu.setPage(Page.SETTINGS);
            }
            if (button(ctx, "QUIT")) {
                System.out.println("Quit button pressed");
                Main.getClient().window.close();
            }

        }
        nk_end(ctx);
    }

    private boolean button(NkContext ctx, String text) {
        nk_layout_row_static(ctx, 5, 1, 1);
        nk_layout_row_dynamic(ctx, 40, 1);
        return nk_button_label(ctx, text);
    }

    @Override
    public void onOpen(Page lastPage) {
    }

}
