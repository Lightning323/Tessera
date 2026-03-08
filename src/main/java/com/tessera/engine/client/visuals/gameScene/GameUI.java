/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene;

/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.Client;
import com.tessera.engine.common.players.localPlayer.LocalPlayer;
import com.tessera.engine.common.world.ClientWorld;
import com.tessera.engine.server.Game;
import com.tessera.engine.client.visuals.FileDialog;
import com.tessera.engine.client.visuals.RectOverlay;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14C.glBlendEquation;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Java port of
 * <a href="https://github.com/vurtun/nuklear/blob/master/demo/calculator.c">https://github.com/vurtun/nuklear/blob/master/demo/calculator.c</a>.
 */
public class GameUI {

    NkContext ctx;
    ClientWindow window;
    Game game;
    private RectOverlay overlay;
    public FileDialog fileDialog;
    Crosshair crosshair;
    public static InfoText infoBox;
    public static UI_Hotbar hotbar;
    public static GameMenu baseMenu;
    public static HUDText hudText;
    boolean drawUI = true;

    public GameUI(Game game, NkContext ctx, Client client, LocalPlayer player, ClientWorld world) throws IOException {
        this.ctx = ctx;
        this.window = client.window;
        this.game = game;
        crosshair = new Crosshair(window.getWidth(), window.getHeight());
        infoBox = new InfoText(ctx, window, client);
        hudText = new HUDText(ctx, window);
        hotbar = new UI_Hotbar(ctx, window, player);

        baseMenu = new GameMenu(ctx, client, world);
        fileDialog = new FileDialog(ctx, window);
        overlay = new RectOverlay();
        overlay.setColor(0, 0, 0, 0);
    }

    public boolean infoBoxVisible() {
        return infoBox.isOpen();
    }

    public void newGameEvent() {
        baseMenu.newGameEvent();
        infoBox.newGameEvent();
        hotbar.newGameEvent();
    }

    public void setDevText(String text) {
        infoBox.setText(text);
    }


    public boolean anyMenuOpen() {
        return baseMenu.isOpen() || game.menusAreOpen() || infoBox.isActive() || fileDialog.isOpen();
    }

    public boolean baseMenusOpen() {
        return baseMenu.isOpen() || infoBox.isActive();
    }


    public void windowResizeEvent(int width, int height) {
        crosshair.windowResizeEvent(width, height);
        infoBox.windowResizeEvent(width, height);
    }

    public void setOverlayColor(float r, float g, float b, float a) {
        overlay.setColor(r, g, b, a);
    }

    public void draw() {
        if (drawUI) {
            GL30.glDepthMask(false);
            if (game.menusAreOpen()) {
                baseMenu.setOpen(false);
            }
            initGLForUI();
            overlay.draw();
            crosshair.draw();
            hudText.draw();

            try (MemoryStack stack = stackPush()) {
                if (baseMenu.isOpen()) {
                    baseMenu.draw(stack);
                } else if (fileDialog.isOpen()) {
                    fileDialog.draw(stack);
                } else if (game.uiDraw(stack)) {
                } else {
                    infoBox.draw(stack);
                    hotbar.draw(stack);
                }
                //Add myGame.uiDraw right here
            }
            window.NKrender();
            GL30.glDepthMask(true);
        }
    }


    private void initGLForUI() {
        //Some of these parameters allow the overlay to be seen
        // setup global state
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11C.glDisable(GL_CULL_FACE); //Disable backface culling
        glEnable(GL_SCISSOR_TEST);
        glBlendFunc(GL_ONE, GL_ZERO);
        GL11C.glDisable(GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST); //Disable depth test
        //enable transparency
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {//TODO: nuklear already has builtin scroll event, Learn to detect that and take advantage of it.
        if (infoBox.mouseScrollEvent(scroll, xoffset, yoffset)) {
            return true;
        }
        return false;
    }


    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_RELEASE) {
            switch (key) {
                case GLFW.GLFW_KEY_ESCAPE -> {  //When we hit ESC, we ALWAYS open the menu.
                    baseMenu.setOpen(!baseMenu.isOpen());
                    infoBox.escKey();
                    return true;
                }
                case GLFW.GLFW_KEY_F1 -> {
                    drawUI = !drawUI;
                    return true;
                }
            }
        }
        if (fileDialog.isOpen() && fileDialog.keyEvent(key, scancode, action, mods)) {
            printKeyConsumption(fileDialog.getClass());
            return true;
        } else if (infoBox.keyEvent(key, scancode, action, mods)) {
            printKeyConsumption(infoBox.getClass());
            return true;
        } else if (hotbar.keyEvent(key, scancode, action, mods)) {
            printKeyConsumption(hotbar.getClass());
            return true;
        } else if (baseMenusOpen()) {
            printKeyConsumption(baseMenu.getClass());
            return true;
        }
        return false;
    }

    public static void printKeyConsumption(Class aClass) {
        //System.out.println("keyEvent consumed by: " + aClass.getSimpleName());
    }

    public boolean mouseButtonEvent(int button, int action, int mods) {
        if (fileDialog.isOpen() && fileDialog.mouseButtonEvent(button, action, mods)) {
            return true;
        } else return game.uiMouseButtonEvent(button, action, mods);
    }

    public boolean releaseMouse() {
        if (anyMenuOpen()) return true;
        else if (ClientWindow.popupMessage.isShown()) return true;
        else if (infoBox.releaseMouse()) return true;
        else if (fileDialog.isOpen() && fileDialog.releaseMouse) return false;
        else return game.releaseMouse();
    }
}
