///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// */
//
//package com.xbuilders.tests.rectangle;
//
//import com.xbuilders.engine.client.visuals.RectOverlay;
//import com.xbuilders.window.Window;
//import com.xbuilders.window.utils.texture.TextureUtils;
//import org.lwjgl.opengl.GL11;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//
//
///**
// * @author sampw
// */
//public class RectangleDemo extends Window {
//    int texture;
//    RectOverlay rect;
//    final String TEXTURE_PATH = "C:\\Users\\Samuel.Walker-1\\OneDrive\\Code Projects\\Java\\Projects\\LwjglOpenglTutorials\\res\\windows.png";
//
//    public RectangleDemo() throws IOException {
//        super();
//        createWindow("Rectangle Demo", false, 400, 400);
//        showWindow();
//
//        texture = TextureUtils.loadTexture(TEXTURE_PATH, false).id;
//        BufferedImage textureImage = ImageIO.read(new File(TEXTURE_PATH));
//        rect = new RectOverlay();
////        rect.setTextureID(texture);
//        rect.setColor(0,0,1,1);
//
//        while (!windowShouldClose()) { //loop until should close
//            render();
//            newFrame();
//        }
//        terminate();
//
//    }
//
//    public void render() {
//        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//        rect.draw();
//    }
//
//    public static void main(String[] args) throws IOException {
//        RectangleDemo boot = new RectangleDemo();
//    }
//
//    @Override
//    public void windowResizeEvent(int width, int height) {
//
//    }
//}
