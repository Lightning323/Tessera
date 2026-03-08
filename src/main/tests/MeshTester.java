///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.tessera.tests;
//
//import com.tessera.engine.client.visuals.localServer.rendering.entity.EntityMesh;
//import com.tessera.engine.client.visuals.localServer.rendering.entity.EntityShader;
//import com.tessera.engine.utils.resource.ResourceUtils;
//import com.tessera.window.developmentTools.MemoryProfiler;
//import com.tessera.window.Window;
//import com.tessera.window.demos.CameraNavigator;
//import com.tessera.window.render.MVP;
//import com.tessera.window.utils.obj.OBJLoader;
//import com.tessera.window.utils.texture.TextureUtils;
//
//import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import org.joml.Matrix4f;
//import org.lwjgl.opengl.GL11;
//
//import static org.lwjgl.opengl.GL11.GL_BACK;
//import static org.lwjgl.opengl.GL11.GL_BLEND;
//import static org.lwjgl.opengl.GL11.GL_CCW;
//import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
//import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
//import static org.lwjgl.opengl.GL11.GL_LESS;
//import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
//import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
//import static org.lwjgl.opengl.GL11.glBlendFunc;
//import static org.lwjgl.opengl.GL11.glCullFace;
//import static org.lwjgl.opengl.GL11.glDepthFunc;
//import static org.lwjgl.opengl.GL11.glEnable;
//import static org.lwjgl.opengl.GL11.glFrontFace;
//
//import org.lwjgl.opengl.GL11C;
//
///**
// * @author zipCoder933
// */
//public class MeshTester extends Window {
//
//    final float g_vertex_buffer_data[] = {
//            -1.0f, -1.0f, -1.0f, // triangle 1 : begin
//            -1.0f, -1.0f, 1.0f, //each line is a vertex
//            -1.0f, 1.0f, 1.0f, // triangle 1 : end
//            1.0f, 1.0f, -1.0f, // triangle 2 : begin
//            -1.0f, -1.0f, -1.0f,
//            -1.0f, 1.0f, -1.0f, // triangle 2 : end
//            1.0f, -1.0f, 1.0f,
//            -1.0f, -1.0f, -1.0f,
//            1.0f, -1.0f, -1.0f,
//            1.0f, 1.0f, -1.0f,
//            1.0f, -1.0f, -1.0f,
//            -1.0f, -1.0f, -1.0f,
//            -1.0f, -1.0f, -1.0f,
//            -1.0f, 1.0f, 1.0f,
//            -1.0f, 1.0f, -1.0f,
//            1.0f, -1.0f, 1.0f,
//            -1.0f, -1.0f, 1.0f,
//            -1.0f, -1.0f, -1.0f,
//            -1.0f, 1.0f, 1.0f,
//            -1.0f, -1.0f, 1.0f,
//            1.0f, -1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f,
//            1.0f, -1.0f, -1.0f,
//            1.0f, 1.0f, -1.0f,
//            1.0f, -1.0f, -1.0f,
//            1.0f, 1.0f, 1.0f,
//            1.0f, -1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f,
//            1.0f, 1.0f, -1.0f,
//            -1.0f, 1.0f, -1.0f,
//            1.0f, 1.0f, 1.0f,
//            -1.0f, 1.0f, -1.0f,
//            -1.0f, 1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f,
//            -1.0f, 1.0f, 1.0f,
//            1.0f, -1.0f, 1.0f
//    };
//
//    EntityMesh mesh;
//    EntityShader shader;
//    MVP mvp;
//    Matrix4f projection, view, model;
//    CameraNavigator nav;
//    int texture;
//
//    private void init() {
//        projection = new Matrix4f().perspective(
//                (float) Math.toRadians(35.0f), //Fov
//                (float) getWidth() / (float) getHeight(), //screen ratio
//                0.1f, 100.0f); //display range
//
//        nav = new CameraNavigator(this);
//        view = nav.getViewMatrix();
//        model = new Matrix4f();
//
//        try {
//            mesh = new EntityMesh();
//            shader = new EntityShader();
//            mvp = new MVP();
//
//            texture = TextureUtils.loadTexture(
//                    ResourceUtils.RESOURCE_DIR.getAbsolutePath() + "\\items\\entity\\animal\\fox\\red.png", false).id;
//            mesh.setTextureID(texture);
//            mesh.loadFromOBJ(OBJLoader.loadModel(ResourceUtils.resource("\\items\\entity\\animal\\fox\\body.obj")));
//        } catch (IOException ex) {
//            Logger.getLogger(MeshTester.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
////<editor-fold defaultstate="collapsed" desc="opengl hints">
//        glEnable(GL_DEPTH_TEST);   // Enable depth test
//        glDepthFunc(GL_LESS); // Accept fragment if it closer to the camera than the former one
//        glEnable(GL_CULL_FACE); // enable face culling
//        glFrontFace(GL_CCW);// specify the winding order of frontRay-facing triangles
//        glCullFace(GL_BACK);// specify which faces to cull
//        glEnable(GL_BLEND); //Enable transparency
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); //Enable transparency
////</editor-fold>
//    }
//
//    private void render() {
//        nav.update();
//        shader.bind();
//
//        model.identity().setTranslation(0, 0, 0);
//        mvp.update(projection, view, model);
//        mvp.sendToShader(shader.getID(), shader.uniform_modelMatrix);
//        mesh.draw(true);
//
//        model.identity().setTranslation(2, 0, 0);
//        mvp.update(projection, view, model);
//        mvp.sendToShader(shader.getID(), shader.uniform_modelMatrix);
//        mesh.draw(true);
//    }
//
//    public MeshTester() {
//        ResourceUtils.initialize(true, null);
//        createWindow("3D WINDOW", 800, 600);
//        init();
//        showWindow();
//        while (!windowShouldClose()) {
//            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); //Clear not only the color but the depth buffer
//            GL11C.glClearColor(0.5f, 0.5f, 1.0f, 1.0f); //Set the background color
//            MemoryProfiler.update();
//            render();
//            newFrame();
//        }
//        terminate();
//    }
//
//    public static void main(String[] args) {
//        new MeshTester();
//    }
//
//    @Override
//    public void windowResizeEvent(int width, int height) {
//        projection.identity().perspective((float) Math.toRadians(35.0f),
//                (float) width / (float) height, 0.1f, 100.0f);
//    }
//
//}
