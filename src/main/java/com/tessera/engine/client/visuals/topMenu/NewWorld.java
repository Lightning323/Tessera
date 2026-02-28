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
import com.tessera.engine.common.world.WorldData;
import com.tessera.engine.common.world.WorldsHandler;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.common.world.Terrain;
import com.tessera.window.nuklear.components.TextBox;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.IntBuffer;

import static com.tessera.engine.client.visuals.topMenu.TopMenu.*;
import static org.lwjgl.nuklear.Nuklear.*;

/**
 * Java port of
 * <a href="https://github.com/vurtun/nuklear/blob/master/demo/calculator.c">https://github.com/vurtun/nuklear/blob/master/demo/calculator.c</a>.
 */
public class NewWorld implements MenuPage {

    public NewWorld(NkContext ctx, Client localClient, TopMenu menu) {
        this.ctx = ctx;
        this.localClient = localClient;
        this.window = localClient.window;
        this.menu = menu;
        name = new TextBox(20);
    }

    TextBox name;
    NkContext ctx;
    TopMenu menu;
    ClientWindow window;
    Client localClient;
    TerrainSelector terrainSelector;

    final int boxWidth = WIDTH_3;
    final int boxHeight = HEIGHT_4;
    GameMode gameMode = GameMode.ADVENTURE;


    public static boolean labeledButton(NkContext ctx, String label, String button) {
        nk_layout_row_static(ctx, 20, 1, 1);
        nk_layout_row_dynamic(ctx, 10, 1);
        nk_style_set_font(ctx, Theme.font_10);
        nk_text(ctx, label, NK_TEXT_ALIGN_LEFT);
        nk_layout_row_dynamic(ctx, 40, 1);
        nk_style_set_font(ctx, Theme.font_12);
        return nk_button_label(ctx, button);
    }

    @Override
    public void layout(MemoryStack stack, NkRect windowDims, IntBuffer titleYEnd) {
        nk_style_set_font(ctx, Theme.font_12);
        nk_rect((window.getWidth() / 2) - (boxWidth / 2), titleYEnd.get(0),
                boxWidth, boxHeight, windowDims);

        if (nk_begin(ctx, "New World", windowDims, NK_WINDOW_BORDER | NK_WINDOW_TITLE)) {
            nk_style_set_font(ctx, Theme.font_10);
            nk_layout_row_static(ctx, 20, 1, 1);
            row(ctx, "World Name", 1);
            nk_layout_row_dynamic(ctx, 30, 1);
            name.render(ctx);

            if (labeledButton(ctx, "Game Mode:", gameMode.toString())) {
                //Go to the next game mode
                int index = gameMode.ordinal() + 1;
                index = index % GameMode.values().length;
                gameMode = GameMode.values()[index];
            }
            terrainSelector.draw();


            nk_layout_row_static(ctx, 20, 1, 1);


            Terrain terrain = terrainSelector.getSelectedTerrain();

            if (terrain.hasOptions()) { //Start the terrain properties
                row(ctx, "World Options", 1);
                nk_layout_row_dynamic(ctx, 15, 1);

                //Add the fields
                nk_style_set_font(ctx, Theme.font_10);
                terrainSelector.optionFields.forEach(field -> field.layout2(ctx, stack));
            }

            nk_style_set_font(ctx, Theme.font_12);
            nk_layout_row_static(ctx, 30, 1, 1);
            nk_layout_row_dynamic(ctx, 40, 2);
            if (nk_button_label(ctx, "BACK")) {
                menu.setPage(Page.HOME);
            }
            if (nk_button_label(ctx, "CREATE")) {
                if (makeNewWorld(name.getValueAsString(), 0, terrain, 0, gameMode)) {
                    menu.setPage(Page.LOAD_WORLD);
                }
            }
        }
        nk_end(ctx);
    }

    private boolean makeNewWorld(String name, int size, Terrain terrain, int seed, GameMode gameMode) {
        try {
            WorldData info = new WorldData();
            info.makeNew(name, size, terrain, seed);
            info.data.gameMode = gameMode;
            if (WorldsHandler.worldNameAlreadyExists(info.getName())) {
                ClientWindow.popupMessage.message("Error", "World name \"" + info.getName() + "\" Already exists!");
                return false;
            } else WorldsHandler.makeNewWorld(info);
        } catch (IOException ex) {
            ClientWindow.popupMessage.message("Error", ex.getMessage());
            return false;
        }
        return true;
    }



    @Override
    public void onOpen(Page lastPage) {
        name.setValueAsString("New World");
        terrainSelector = new TerrainSelector(Main.game.terrainsList, ctx);
    }



}
