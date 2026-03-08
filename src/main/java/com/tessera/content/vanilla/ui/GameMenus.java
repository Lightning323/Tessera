package com.tessera.content.vanilla.ui;


import com.tessera.engine.client.visuals.gameScene.UI_GameMenu;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;

public class GameMenus {

    public final ArrayList<UI_GameMenu> menus = new ArrayList<>();

    public boolean draw(MemoryStack stack) {
        for (int i = 0; i < menus.size(); i++) {
            UI_GameMenu menu = menus.get(i);
            if (menu != null && menu.isOpen()) {
                menu.draw(stack);
                return true;
            }
        }
        return false;
    }

    public boolean keyEvent(int key, int scancode, int action, int mods) {
        for (int i = 0; i < menus.size(); i++) {
            UI_GameMenu menu = menus.get(i);
            if (menu != null && menu.isOpen()) {
                return menu.keyEvent(key, scancode, action, mods);
            }
        }
        return false;
    }

    public boolean mouseButtonEvent(int button, int action, int mods) {
        for (int i = 0; i < menus.size(); i++) {
            UI_GameMenu menu = menus.get(i);
            if (menu != null && menu.isOpen()) {
                return menu.mouseButtonEvent(button, action, mods);
            }
        }
        return false;
    }

    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        for (int i = 0; i < menus.size(); i++) {
            UI_GameMenu menu = menus.get(i);
            if (menu != null && menu.isOpen()) {
                return menu.mouseScrollEvent(scroll, xoffset, yoffset);
            }
        }
        return false;
    }

    public boolean isOpen() {
        for (int i = 0; i < menus.size(); i++) {
            UI_GameMenu menu = menus.get(i);
            if (menu != null && menu.isOpen()) {
                return true;
            }
        }
        return false;
    }
}
