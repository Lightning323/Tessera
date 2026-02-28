/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.server.item.blockIconRendering;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockAir;
import com.tessera.engine.server.block.BlockArrayTexture;
import com.tessera.engine.server.block.construction.BlockType;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.IconGenShader;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.mesh.CompactMesh;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet.TraditionalVertexSet;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.window.GLFWWindow;
import com.tessera.window.render.MVP;
import com.tessera.window.utils.preformance.SimpleWaitLock;
import com.tessera.window.utils.texture.TextureUtils;

import java.io.File;
import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CCW;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFrontFace;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

/**
 * IF THE RESULTING IMAGES ARE EMPTY, THE MOST LIKELY CULPRIT IS THAT THE
 * ICON.RTT.VS WAS NOT UPDATED TO MATCH CHUNK.VS
 * 
 * @author zipCoder933
 */
public class BlockIconRenderer {

    Thread thread1;
    long window1 = 0;
    CompactMesh mesh;
    IconGenShader shader;
    Matrix4f projection, view, model;
    MVP blockMVP;
    int framebuffer;
    final int imageSize = 128;
    final float viewProj = 0.75f;
    public int renderedTexture;
    SimpleWaitLock lock;

    public BlockIconRenderer(BlockArrayTexture textures, Block[] list, File exportDirectory) {
        lock = new SimpleWaitLock();
        thread1 = new Thread(() -> {
            System.out.println("Generating icons... Image size: " + imageSize + "x" + imageSize);
            // <editor-fold defaultstate="collapsed" desc="initialize">
            synchronized (GLFWWindow.windowCreateLock) {
                // Create first window
                window1 = glfwCreateWindow(imageSize, imageSize, "Icon Generator", NULL, NULL);
                if (window1 == 0) {
                    throw new IllegalStateException("Failed to create icon gnerator window");
                }
            }
            glfwMakeContextCurrent(window1);
            GL.createCapabilities();
            GLFW.glfwHideWindow(window1);

            framebuffer = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer); // Bind the framebuffer as a framebuffer

            int depthRenderBuffer = GL30.glGenRenderbuffers();
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthRenderBuffer);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, imageSize, imageSize);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER,
                    depthRenderBuffer);

            renderedTexture = makeBlankTexture(imageSize, imageSize);
            GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, renderedTexture, 0);

            /**
             * Set the list of draw buffers.
             */
            int[] drawBuffers = { GL30.GL_COLOR_ATTACHMENT0 };
            GL20.glDrawBuffers(drawBuffers);

            glEnable(GL_DEPTH_TEST); // Enable depth test
            glDepthFunc(GL_LESS); // Accept fragment if it closer to the camera than the former one
            glEnable(GL_CULL_FACE); // enable face culling
            glFrontFace(GL_CCW);// specify the winding order of front-facing triangles
            glCullFace(GL_BACK);// specify which faces to cull
            glEnable(GL_BLEND); // Enable transparency
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // Enable transparency

            try {
                shader = new IconGenShader(textures.layerCount);
                shader.bind();
                projection = new Matrix4f().ortho(-viewProj, viewProj, -viewProj, viewProj, 0.1f, 100f);
                view = calculateOrbitingViewMatrix((float) Math.toRadians(45), (float) Math.toRadians(-25), 1);
                model = new Matrix4f().translate(-0.5f, -0.5f, -0.5f);
                blockMVP = new MVP();
                blockMVP.update(projection, view, model);
                blockMVP.sendToShader(shader.getID(), shader.mvpUniform);
                mesh = new CompactMesh();
                mesh.setTextureID(textures.createNewArrayTexture());
                // </editor-fold>

                exportDirectory.mkdirs();
                for (int i = 0; !glfwWindowShouldClose(window1); i++) {
                    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
                    GL11.glViewport(0, 0, imageSize, imageSize);
                    GL11C.glClearColor(0, 0, 0, 0); // Set the background color
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                    if (i >= list.length) {
                        break;
                    } else {
                        Block block = list[i];
                        generateAndSaveIcon(block, exportDirectory, renderedTexture);
                    }
                }
                GLFW.glfwDestroyWindow(window1);
                lock.unlock();
            } catch (Exception ex) {
                com.tessera.Main.LOGGER.error("Unhandled exception in BlockIconRenderer", ex);
            }
        });
    }

    public void saveAllIcons() {
        System.out.println("\n\n\nGENERATING ALL BLOCK ICONS");
        thread1.start();
        try {
            lock.lock();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean shouldMakeIcon(Block block) {
        BlockType type = Registrys.blocks.getBlockType(block.type);
        return type != null && type.generate3DIcon && block.texture != null;
    }

    private void generateAndSaveIcon(Block block, File baseFile, int renderedTexture) throws IOException {
        if (block.texture != null && shouldMakeIcon(block)) {
            if (makeBlockMesh(block)) {
                ClientWindow.printlnDev("\tblock: " + block.id + " (" + block.alias + ")");
                shader.bind();
                mesh.draw(false);
                File outFile = new File(baseFile, block.id + ".png");
                TextureUtils.saveTextureAsPNG(renderedTexture, outFile);
            }
        }
    }

    public final static Block BLOCK_AIR = new BlockAir();

    private boolean makeBlockMesh(Block block) {
        TraditionalVertexSet buffers = new TraditionalVertexSet();
        BlockType type = Registrys.blocks.getBlockType(block.type);
        if (type == null) {
            return false;
        }
        Block[] blockNeghbors = new Block[] { BLOCK_AIR,
                BLOCK_AIR,
                BLOCK_AIR,
                BLOCK_AIR,
                BLOCK_AIR,
                BLOCK_AIR };
        byte[] lightNeghbors = new byte[] { 15, 15, 15, 15, 15, 15 };
        BlockData[] neighborData = new BlockData[6];

        type.constructBlock(buffers, block, null,
                blockNeghbors,
                neighborData,
                lightNeghbors,
                null, 0, 0, 0,
                false);

        buffers.makeVertexSet();
        buffers.sendToMesh(mesh);
        buffers.reset();
        return true;
    }

    public static int makeBlankTexture(int width, int height) {
        int tex = GL11.glGenTextures(); // Generate the texture and return its serverExecute
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);// "Bind" the newly created texture: all future texture
        // functions will modify this texture

        // specifies the storage and format of the texture image.
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
                0);// Give an empty image to OpenGL (the last "0")

        // Sepcify nearest textel blending (as opposed to linear filtering, nearest
        // makes the image look pixelated up close)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        return tex;
    }

    // Set up the camera target and up vector
    private final static Vector3f target = new Vector3f(0.0f, 0.0f, 0.0f);
    private final static Vector3f up = new Vector3f(0.0f, -1.0f, 0.0f);

    /**
     * @param horizontalOrbit in radians
     * @param verticalOrbit   in radians
     * @param distance
     * @return the view matrix
     */
    private Matrix4f calculateOrbitingViewMatrix(float horizontalOrbit, float verticalOrbit, float distance) {
        // Calculate the camera position based on horizontal and vertical orbits and
        // distance
        float x = distance * (float) Math.cos(verticalOrbit) * (float) Math.sin(horizontalOrbit);
        float y = distance * (float) Math.sin(verticalOrbit);
        float z = distance * (float) Math.cos(verticalOrbit) * (float) Math.cos(horizontalOrbit);

        Vector3f cameraPosition = new Vector3f(x, y, z);

        // Calculate the view matrix
        return new Matrix4f().lookAt(
                cameraPosition,
                target,
                up);
    }

}
