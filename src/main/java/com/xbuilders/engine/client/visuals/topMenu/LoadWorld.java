/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.client.visuals.topMenu;

/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */

import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.client.ClientWindow;
import com.xbuilders.engine.common.world.WorldData;
import com.xbuilders.engine.common.world.WorldsHandler;
import com.xbuilders.engine.client.visuals.Page;
import com.xbuilders.engine.client.visuals.Theme;
import com.xbuilders.window.nuklear.NKUtils;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static com.xbuilders.Main.LOGGER;
import static org.lwjgl.nuklear.Nuklear.*;

/**
 * Java port of
 * <a href="https://github.com/vurtun/nuklear/blob/master/demo/calculator.c">https://github.com/vurtun/nuklear/blob/master/demo/calculator.c</a>.
 */
public class LoadWorld implements MenuPage {

    public LoadWorld(NkContext ctx, Client localClient, TopMenu menu) throws IOException {
        this.ctx = ctx;
        this.localClient = localClient;
        this.window = localClient.window;
        this.menu = menu;
        worlds = new ArrayList<>();


//        Texture texture = TextureUtils.loadTexture(ResourceUtils.RESOURCE_DIR + "\\icon.png", false);
//        image = new NkImage(texture.buffer);
//       image = nk_image_id(texture.id, image);
    }

    // Texture texture;
    NkContext ctx;
    TopMenu menu;
    Client localClient;
    ClientWindow window;
    final int BOX_DEFAULT_WIDTH = TopMenu.WIDTH_4;
    final int BOX_DEFAULT_HEIGHT = TopMenu.HEIGHT_4;
    ArrayList<WorldData> worlds;
    public WorldData currentWorld;
//    NkImage image;

    @Override
    public void onOpen(Page lastPage) {
        try {
            WorldsHandler.listWorlds(worlds);
            if (worlds.size() > 0) {
                currentWorld = worlds.get(0);
            }
        } catch (IOException ex) {
            com.xbuilders.Main.LOGGER.error("Unhandled exception in LoadWorld", ex);
        }
    }

    @Override
    public void layout(MemoryStack stack, NkRect windowDims, IntBuffer titleYEnd) {
        int boxWidth = (int) (BOX_DEFAULT_WIDTH * Theme.getScale());
        int boxHeight = (int) (BOX_DEFAULT_HEIGHT * Theme.getScale());

        nk_rect((window.getWidth() / 2) - (boxWidth / 2), titleYEnd.get(0), boxWidth, boxHeight, windowDims);
        nk_style_set_font(ctx, Theme.font_12);

        if (nk_begin(ctx, "Load World", windowDims, NK_WINDOW_BORDER | NK_WINDOW_TITLE)) {

            nk_layout_row_dynamic(ctx, BOX_DEFAULT_HEIGHT - 50, 2); // Adjust height as needed
            nk_group_begin(ctx, "Worlds", NK_WINDOW_TITLE);
            nk_style_set_font(ctx, Theme.font_10);
            nk_layout_row_dynamic(ctx, 30, 1);//this sets the height of the subsequent elements
            ctx.style().button().text_alignment(NK_TEXT_ALIGN_LEFT);

            for (int i = 0; i < worlds.size(); i++) { // Adjust the number of buttons
                if (nk_button_label(ctx, worlds.get(i).getName())) {
                    currentWorld = worlds.get(i);
                }
            }

            Theme.resetEntireButtonStyle(ctx);
            nk_group_end(ctx);

            nk_style_set_font(ctx, Theme.font_12);
            nk_group_begin(ctx, "Details", NK_WINDOW_TITLE);

            if (currentWorld != null) {
                nk_style_set_font(ctx, Theme.font_10);
                NKUtils.text(ctx, currentWorld.getDetails(), 10, NK_TEXT_ALIGN_LEFT);
                nk_layout_row_static(ctx, 40, 1, 1);
                nk_layout_row_dynamic(ctx, 40, 1);

                nk_style_set_font(ctx, Theme.font_12);

                if (!currentWorld.data.isJoinedMultiplayerWorld) {
                    if (nk_button_label(ctx, "LOAD WORLD")) {
                        localClient.loadWorld(currentWorld, null);
                    }
                    if (nk_button_label(ctx, "HOST SERVER")) {
                        menu.setPage(Page.HOST_MULTIPLAYER);
                    }
                }

                if (nk_button_label(ctx, "DELETE WORLD")) {
                    deleteCurrentWorld();
                }
            }

            nk_layout_row_dynamic(ctx, 40, 1);
            if (nk_button_label(ctx, "BACK")) {
                menu.setPage(Page.HOME);
            }
            nk_group_end(ctx);
        }
        nk_end(ctx);

    }

    private void deleteCurrentWorld() {
        ClientWindow.popupMessage.confirmation("Delete World", "Are you sure you want to delete " + currentWorld.getName() + "?", () -> {

            try {
                WorldsHandler.deleteWorld(currentWorld);
            } catch (IOException ex) {
                ClientWindow.popupMessage.message("Error Deleting World", ex.getMessage());
            }
            if (currentWorld.getDirectory().exists()) {
                ClientWindow.popupMessage.message("Error Deleting World", "Failed to delete world");
            }
            try {
                WorldsHandler.listWorlds(worlds);
            } catch (IOException e) {
                LOGGER.info("error", e);
            }
            currentWorld = null;

        });
    }
}
