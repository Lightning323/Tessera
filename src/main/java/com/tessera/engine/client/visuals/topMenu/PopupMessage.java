/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.topMenu;

/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.window.nuklear.NKUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * Java port of
 * <a href="https://github.com/vurtun/nuklear/blob/master/demo/calculator.c">https://github.com/vurtun/nuklear/blob/master/demo/calculator.c</a>.
 */
public class PopupMessage {

    public PopupMessage(NkContext ctx, ClientWindow window) {
        this.ctx = ctx;
        windowDims = NkRect.create();
        this.window = window;
        windowDims.set(0, 0, boxWidth, boxHeight);
        //message("Title", "Body");
    }

    NkContext ctx;
    ClientWindow window;

    Runnable confirmationCallback, closeCallback;
    boolean visible = false;
    float boxHeight = 400;
    float boxWidth = 500;

    NkRect windowDims;
    String title, body;
    private long shownTime;
    boolean buttonCanClose = false;
    boolean unfocusCanClose = false;

    public boolean isShown() {
        return visible;
    }

//    private String maxCharsPerLine(String text, int maxCharactersPerLine) {
//        String[] words = text.split("[\\s&&[^\\n]]+");
//        String newText = "";
//        int characterCount = 0;
//        for (int i = 0; i < words.length; i++) {
//            characterCount += words[i].length() + 1;
//            if (words[i].endsWith("\n")) characterCount = 0;
//            else if (characterCount > maxCharactersPerLine) {
//                newText += "\n";
//                characterCount = 0;
//            }
//            newText += words[i] + " ";
//        }
//        return newText;
//    }

    public void message(String title, String body) {
        this.title = title;
        this.body = body;
        resetShownTime();
        this.confirmationCallback = null;
        this.closeCallback = null;
        visible = true;
    }

    public void message(String title, String body, Runnable closeCallback) {
        this.title = title;
        this.body = body;
        resetShownTime();
        this.confirmationCallback = null;
        this.closeCallback = closeCallback;
        visible = true;
    }

    public void confirmation(String title, String body, Runnable confirmationCallback) {
        this.title = title;
        this.body = body;
        resetShownTime();
        this.confirmationCallback = confirmationCallback;
        this.closeCallback = null;
        visible = true;
    }

    public void confirmation(String title, String body, Runnable confirmationCallback, Runnable closeCallback) {
        this.title = title;
        this.body = body;
        resetShownTime();
        this.confirmationCallback = confirmationCallback;
        this.closeCallback = closeCallback;
        visible = true;
    }

    private void resetShownTime() {
        if (!visible) {
            shownTime = System.currentTimeMillis();
        }
    }

    private final static String WINDOW_ID = "Popup_window";


    private void closeWindow(boolean confirmation) {
        System.out.println("Closing popup, confirmation: " + confirmation);
        if (confirmation) {
            if (confirmationCallback != null) {
                confirmationCallback.run();
            }
        } else if (closeCallback != null) {
            closeCallback.run();
        }
        closeCallback = null;
        confirmationCallback = null;
        visible = false;
    }


    public void draw() {
        if (!visible) {
            return;
        }


        int timeSinceShown = (int) (System.currentTimeMillis() - shownTime);
        buttonCanClose = timeSinceShown > 500;
        unfocusCanClose = timeSinceShown > 1000;

        float wrapWidth = boxWidth - 20;
        boxHeight = (int) (NKUtils.calculateWrappedTextHeight(Theme.font_10, body, wrapWidth) + 180);
        boxHeight = MathUtils.clamp(boxHeight, 160, 400);


        nk_style_set_font(ctx, Theme.font_12);
        nk_rect((window.getWidth() / 2) - (boxWidth / 2), (window.getHeight() / 2) - (boxHeight / 2), boxWidth, boxHeight, windowDims);
        if (nk_begin_titled(ctx, WINDOW_ID, title, windowDims, NK_WINDOW_BORDER | NK_WINDOW_TITLE)) {

            //Detect if the window is in focus
            if (!nk_window_has_focus(ctx)) {
                if (unfocusCanClose) {
                    closeWindow(false);
                } else {
                    System.out.println("Refocusing popup\t\t can close: " + unfocusCanClose + " Button can close: " + buttonCanClose + " Time since shown: " + timeSinceShown);
                    nk_window_set_focus(ctx, WINDOW_ID);
                }
            }


            nk_style_set_font(ctx, Theme.font_10);
            nk_layout_row_dynamic(ctx, 5, 1);

            NKUtils.wrapText(ctx, body, wrapWidth);

            nk_style_set_font(ctx, Theme.font_12);
            nk_layout_row_static(ctx, 20, 1, 1);

            if (confirmationCallback != null) {
                nk_layout_row_dynamic(ctx, 40, 2);

                if (nk_widget_is_hovered(ctx)) {
                    //System.out.println("Hovered click " + window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT));
                    if (buttonCanClose && window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                        closeWindow(true);
                    }
                }
                if (nk_button_label(ctx, "OK")) {
                    if (buttonCanClose) {
                        closeWindow(true);
                    }
                }

                if (nk_widget_is_hovered(ctx)) {
                    //System.out.println("Hovered click " + window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT));
                    if (buttonCanClose && window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                        closeWindow(false);
                    }
                }
                if (nk_button_label(ctx, "Cancel")) {
                    if (buttonCanClose) {
                        closeWindow(false);
                    }
                }
            } else {
                nk_layout_row_dynamic(ctx, 40, 1);

                if (nk_widget_is_hovered(ctx)) {
                    //System.out.println("Hovered click " + window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT));
                    if (buttonCanClose && window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                        closeWindow(false);
                    }
                }
                if (nk_button_label(ctx, "OK")) {
                    if (buttonCanClose) {
                        closeWindow(false);
                    }
                }
            }
        }
        nk_end(ctx);

    }


}
