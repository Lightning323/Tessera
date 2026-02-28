/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.topMenu.multiplayer;

/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */

import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.common.players.localPlayer.LocalPlayer;
import com.tessera.engine.client.visuals.topMenu.LoadWorld;
import com.tessera.engine.client.visuals.topMenu.MenuPage;
import com.tessera.engine.client.visuals.topMenu.TopMenu;
import com.tessera.engine.common.network.NetworkJoinRequest;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.client.visuals.Page;
import com.tessera.window.nuklear.components.NumberBox;
import com.tessera.window.nuklear.components.TextBox;

import java.nio.IntBuffer;

import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * Java portBox of
 * <a href="https://github.com/vurtun/nuklear/blob/master/demo/calculator.c">https://github.com/vurtun/nuklear/blob/master/demo/calculator.c</a>.
 */
public class Multiplayer implements MenuPage {


    final String ipAdress;
    LoadWorld loadWorld;
    Client localClient;


    public Multiplayer(NkContext ctx, Client localClient, TopMenu menu,
                       LocalPlayer player, boolean hosting,
                       String ipAdress, LoadWorld loadWorld) {
        this.loadWorld = loadWorld;
        this.ipAdress = ipAdress;
        this.ctx = ctx;
        this.localClient = localClient;
        this.window = localClient.window;
        this.menu = menu;
        portBox = new NumberBox(4, 0);
//        fromPortBox = new NumberBox(4, 0);
        ipAdressBox = new TextBox(20);
        ipAdressBox.setValueAsString("192.168.0.");
        presetBox = new TextBox(20);

//        fromPortBox.setOnSelectEvent(() -> {
//            selectedServerPreset = null;
//        });
        portBox.setOnSelectEvent(() -> {
            selectedServerPreset = null;
        });
        ipAdressBox.setOnSelectEvent(() -> {
            selectedServerPreset = null;
        });
        presetBox.setOnSelectEvent(() -> {
            selectedServerPreset = null;
        });


//        fromPortBox.setValueAsNumber(8080);
        portBox.setValueAsNumber(8080);

//        if (Client.DEV_MODE) {
//            if (hosting) {
//                fromPortBox.setValueAsNumber(8081);
//            } else {
//                portBox.setValueAsNumber(8081);
//            }
//        }

        this.player = player;
        this.hosting = hosting;
    }

    boolean hosting;
    LocalPlayer player;
    NkContext ctx;
    TopMenu menu;
    ClientWindow window;
    NumberBox portBox;
    TextBox ipAdressBox, presetBox;

    final int BOX_DEFAULT_HEIGHT = menu.HEIGHT_3;

    @Override
    public void layout(MemoryStack stack, NkRect windowDims, IntBuffer titleYEnd) {
        nk_style_set_font(ctx, Theme.font_12);

        int boxWidth = menu.WIDTH_3;
        if (!hosting) boxWidth = menu.WIDTH_4;

        nk_rect((window.getWidth() / 2) - (boxWidth / 2), titleYEnd.get(0),
                boxWidth, BOX_DEFAULT_HEIGHT, windowDims);

        if (nk_begin(ctx, (hosting ? "Host" : "Join") + " Server", windowDims, NK_WINDOW_BORDER | NK_WINDOW_TITLE)) {
            nk_style_set_font(ctx, Theme.font_10);
            if (hosting) {
                nk_layout_row_static(ctx, 20, 1, 1);
                addressGroup(ctx, true);
            } else {
                //Include the address menu and presets
                nk_layout_row_dynamic(ctx, BOX_DEFAULT_HEIGHT - 50, 2); // Adjust height as needed
                nk_group_begin(ctx, "Presets", NK_WINDOW_TITLE);
                presetGroup(ctx);
                nk_group_end(ctx);

                nk_group_begin(ctx, "Address", NK_WINDOW_TITLE);
                addressGroup(ctx, false);
                nk_group_end(ctx);
            }

            nk_end(ctx);
        }
    }

    ServerEntry selectedServerPreset = null;

    private void presetGroup(NkContext ctx) {
        nk_style_set_font(ctx, Theme.font_10);
        nk_layout_row_dynamic(ctx, 30, 1);
        presetBox.render(ctx);
        nk_layout_row_dynamic(ctx, 30, 2);//this sets the height of the subsequent elements
        if (nk_button_label(ctx, "+ Add")) {
            if (!presetBox.getValueAsString().isEmpty()) {
                ClientWindow.settings.internal_serverList.add(
                        new ServerEntry(
                                presetBox.getValueAsString(),
                                ipAdressBox.getValueAsString(),
                                (int) portBox.getValueAsNumber()));
                presetBox.setValueAsString("");
                ClientWindow.settings.save();
            }
        }
        if (nk_button_label(ctx, "- Delete")) {
            if (selectedServerPreset != null) {
                ClientWindow.settings.internal_serverList.remove(selectedServerPreset);
                selectedServerPreset = null;
                ClientWindow.settings.save();
            }
        }
        nk_layout_row_static(ctx, 10, 1, 1);
        nk_layout_row_dynamic(ctx, BOX_DEFAULT_HEIGHT - (200), 1);
        nk_group_begin(ctx, "Presets scroll", 0);
        nk_layout_row_dynamic(ctx, 30, 1);
        ctx.style().button().text_alignment(NK_TEXT_ALIGN_LEFT);
        for (ServerEntry server : ClientWindow.settings.internal_serverList) { // Adjust the number of buttons
            if (selectedServerPreset == server) {
                ctx.style().button().normal().data().color().set(Theme.color_blue);
                ctx.style().button().hover().data().color().set(Theme.color_blue);
            } else {
                ctx.style().button().normal().data().color().set(Theme.color_buttonColor);
                ctx.style().button().hover().data().color().set(Theme.color_buttonHover);
            }
            if (nk_button_label(ctx, server.name)) {
                selectedServerPreset = server;
                ipAdressBox.setValueAsString(server.address);
                portBox.setValueAsNumber(server.port);
            }
        }
        ctx.style().button().normal().data().color().set(Theme.color_buttonColor);
        ctx.style().button().hover().data().color().set(Theme.color_buttonHover);

        nk_group_end(ctx);
    }


    @Override
    public void onOpen(Page lastPage) {
        selectedServerPreset = null;
        if (hosting) {
            ipAdressBox.setValueAsString(ipAdress);
        } else {
            if (Client.DEV_MODE) {
                ipAdressBox.setValueAsString(ipAdress);
            }
        }
    }

    public static void row(NkContext ctx, String text, int columns) {
        if (columns == 2) nk_layout_row_dynamic(ctx, 30, columns);
        else {
            nk_layout_row_static(ctx, 8, 1, 1);
            nk_layout_row_dynamic(ctx, 10, columns);
        }
        nk_label(ctx, text, NK_TEXT_ALIGN_LEFT);
    }

    private void addressGroup(NkContext ctx, boolean column2) {
        ctx.style().button().text_alignment(NK_TEXT_ALIGN_CENTERED | NK_TEXT_ALIGN_MIDDLE);
        nk_style_set_font(ctx, Theme.font_10);
        row(ctx, "IP Address:", column2 ? 2 : 1);
        if (!column2) nk_layout_row_dynamic(ctx, 30, 1);
        if (hosting) {
            nk_label(ctx, ipAdressBox.getValueAsString(), NK_TEXT_ALIGN_LEFT);
        } else {
            ipAdressBox.render(ctx);
        }

//        if (Client.DEV_MODE) {
//            row(ctx, "From Port:", column2 ? 2 : 1);
//            if (!column2) nk_layout_row_dynamic(ctx, 30, 1);
//            fromPortBox.render(ctx);
//        }

        row(ctx, "Port:", column2 ? 2 : 1);
        if (!column2) nk_layout_row_dynamic(ctx, 30, 1);
        portBox.render(ctx);

        TopMenu.divider(ctx);
        nk_layout_row_dynamic(ctx, 40, 1);
        if (nk_button_label(ctx, "Customize Player")) {
            menu.setPage(Page.CUSTOMIZE_PLAYER);
        }

        TopMenu.divider(ctx);
        nk_layout_row_dynamic(ctx, 40, 2);

        if (nk_button_label(ctx, "BACK")) {
            if (hosting) {
                menu.setPage(Page.LOAD_WORLD);
            } else menu.setPage(Page.HOME);
        }
        if (nk_button_label(ctx, "CONTINUE")) {
//            int fromPortVal = (int) fromPortBox.getValueAsNumber();
            int portVal = (int) portBox.getValueAsNumber();
//            if (!Client.DEV_MODE) fromPortVal = portVal;
            String ipAdress = this.ipAdressBox.getValueAsString();
            NetworkJoinRequest req = new NetworkJoinRequest(hosting, portVal, player.getName(), ipAdress);
            System.out.println(req.toString());
            localClient.loadWorld(loadWorld.currentWorld, req);
        }
    }


}


