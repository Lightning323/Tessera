///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.tessera.tests;
//
//import com.tessera.engine.game.model.Game;
//import com.tessera.engine.game.model.items.ItemList;
//import com.tessera.engine.game.model.items.item.blockIconRendering.BlockIconRenderer;
//import com.tessera.engine.client.visuals.localServer.GameUI;
//import com.tessera.engine.utils.resource.ResourceUtils;
//import com.tessera.game.MyGame;
//import com.tessera.window.GLFWWindow;
//import com.tessera.window.NKWindow;
//import com.tessera.window.developmentTools.MemoryProfiler;
//import org.lwjgl.nuklear.NkVec2;
//
//import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import static org.lwjgl.opengl.GL11C.*;
//
///**
// *
// * @author zipCoder933
// */
//public class UITester extends NKWindow {
//
//    GameUI ui;
//    public static BlockIconRenderer iconRenderer;
//
//    public UITester() throws IOException, Exception {
//        super();
//        GLFWWindow.initGLFW();
//        startWindow("NUKLEAR TEST", false, 800, 600);
//
//        ResourceUtils.initialize(true);
//        Game game = new MyGame();
//
//        ItemList blocks = new ItemList();
////        iconRenderer = new BlockIconRenderer(blocks, blocks.textures, ResourceUtils.resource("items\\blocks\\icons")) {
////            @Override
////            public boolean shouldMakeIcon(Block block) {
////                return game.includeBlockIcon(block);
////            }
////        };
////        iconRenderer.saveAllIcons();
//
//        ui = new GameUI(game, ctx, this);
//        ui.init();
//
//        showWindow();
//        while (!windowShouldClose()) {
//            /* Input */
//            startFrame();
//            glClearColor(0, .5f, .5f, 1f);
//            glClear(GL_COLOR_BUFFER_BIT);
//            render();
//
//            MemoryProfiler.update();
//            endFrame();
//        }
//        terminate();
//    }
//
//    @Override
//    public void onMPFUpdate() {
//        setTitle("Test 2     Memory: " + MemoryProfiler.getMemoryUsageAsString());
//    }
//
//    public static void main(String[] args) {
//        try {
//            new UITester();
//        } catch (Exception ex) {
//            Logger.getLogger(UITester.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    @Override
//    public void disposeEvent() {
//    }
//
//    @Override
//    public void keyEvent(int key, int scancode, int action, int mods) {
//        ui.keyEvent(key, scancode, action, mods);
//    }
//
//    @Override
//    public void mouseButtonEvent(int button, int action, int mods) {
//        ui.mouseButtonEvent(button, action, mods);
//    }
//
//    @Override
//    public void mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
//        ui.mouseScrollEvent(scroll, xoffset, yoffset);
//    }
//
//    @Override
//    public void windowResizeEvent(int width, int height) {
//        ui.windowResizeEvent(width, height);
//    }
//
//    private void render() {
//        ui.draw();
//    }
//
//}
