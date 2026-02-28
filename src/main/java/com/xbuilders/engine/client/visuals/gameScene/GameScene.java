package com.xbuilders.engine.client.visuals.gameScene;

import com.xbuilders.Main;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.client.ClientWindow;
import com.xbuilders.engine.common.players.localPlayer.LocalPlayer;
import com.xbuilders.engine.client.visuals.skybox.SkyBackground;
import com.xbuilders.engine.common.world.ClientWorld;
import com.xbuilders.engine.common.world.chunk.ClientChunk;
import com.xbuilders.engine.server.Game;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.server.entity.Entity;
import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.common.world.wcc.WCCf;
import com.xbuilders.engine.common.world.wcc.WCCi;
import com.xbuilders.utils.MiscUtils;
import com.xbuilders.window.WindowEvents;
import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class GameScene implements WindowEvents {
    public final static Matrix4f projection = new Matrix4f();
    public final static Matrix4f view = new Matrix4f();
    public final static Matrix4f centeredView = new Matrix4f();
    public static boolean drawWireframe;
    public static boolean drawBoundingBoxes;
    public static SkyBackground background;
    public final Client client;
    public boolean holdMouse;
    public static boolean specialMode;
    public GameUI ui;
    private Game game;
    ClientWorld world;
    public boolean writeDebugText = false;


    public GameScene(Client client, Game game, ClientWorld world) throws Exception {
        this.client = client;
        this.game = game;
        this.world = world;
        setProjection();

        Client.userPlayer = new LocalPlayer(client.window, GameScene.projection, GameScene.view, GameScene.centeredView);
        Client.userPlayer.initGL();

        background = new SkyBackground(client.window, world);

        ui = new GameUI(game, client.window.ctx, client, Client.userPlayer, world);
    }

    public void client_hudText(String s) {
        ui.hudText.setText(s);
    }


    public void newGameEvent() {
        if (Client.DEV_MODE) writeDebugText = true;
        ui.newGameEvent();
    }

    public void render() {
        Client.frameTester.startProcess();
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); //Clear not only the color but the depth buffer
//        GL11C.glClearColor(backgroundColor.x, backgroundColor.y, backgroundColor.z, 1.0f); //Set the background color
        background.draw(GameScene.projection, GameScene.centeredView);   //Draw the background BEFORE ANYTHING ELSE! (Anything drawn before will be overridden)

        if (ClientWindow.frameCount % 10 == 0) {
            GameScene.background.update();
        }

        holdMouse = !ui.releaseMouse() && client.window.windowIsFocused();
        Client.frameTester.endProcess("Clearing buffer");

        glEnable(GL_DEPTH_TEST);   // Enable depth test
        glDepthFunc(GL_LESS); // Accept fragment if it closer to the camera than the former one

        //The user player is one thing that the client has full control over
        //The client will check into the Main.getServer() occasionally to see if the Main.getServer() has any updates for the player
        Client.userPlayer.update();
        Client.userPlayer.render(holdMouse);

        enableBackfaceCulling();
        Client.frameTester.startProcess();

        glEnable(GL_BLEND); //Enable transparency
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        world.client_updateAndRenderChunks(GameScene.projection, GameScene.view, Client.userPlayer.worldPosition);
        client.players.forEach((p) -> {
            p.render(projection, view);
        });

        Client.frameTester.endProcess("Drawing chunks");


        setInfoText();
        ui.draw();
    }

    public void windowUnfocusEvent() {
        if (client.window.isFullscreen()) ui.baseMenu.setOpen(true);
        else if (!ui.anyMenuOpen()) {
            ui.baseMenu.setOpen(true);
        }
        holdMouse = false;
    }

    public void windowResizeEvent(int width, int height) {
        setProjection();
        ui.windowResizeEvent(width, height);
    }


    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if (ui.keyEvent(key, scancode, action, mods)) {
        } else if (game.keyEvent(key, scancode, action, mods)) {
        } else {
            Client.userPlayer.keyEvent(key, scancode, action, mods);
        }
        if (action == GLFW.GLFW_RELEASE) {
            switch (key) {
                case GLFW.GLFW_KEY_F3 ->
                        Main.getClient().window.gameScene.writeDebugText = !Main.getClient().window.gameScene.writeDebugText;
                case GLFW.GLFW_KEY_F5 -> specialMode = !specialMode;
                case GLFW.GLFW_KEY_F6 -> drawWireframe = !drawWireframe;
                case GLFW.GLFW_KEY_F7 -> drawBoundingBoxes = !drawBoundingBoxes;
            }
        }
        return true;
    }


    public boolean mouseButtonEvent(int button, int action, int mods) {
        ui.mouseButtonEvent(button, action, mods);
        if (!ui.anyMenuOpen()) {
            Client.userPlayer.mouseButtonEvent(button, action, mods);
        }
        return true;
    }

    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        if (ui.anyMenuOpen() && ui.mouseScrollEvent(scroll, xoffset, yoffset)) {
        } else if (Client.userPlayer.mouseScrollEvent(scroll, xoffset, yoffset)) {
        } else if (game.uiMouseScrollEvent(scroll, xoffset, yoffset)) {
        } else {
            ui.hotbar.mouseScrollEvent(scroll, xoffset, yoffset);
        }
        return true;
    }


    public static void enableBackfaceCulling() {
        //If backface culling is not working, it means that another process has probably disabled it, after init3D.
        glEnable(GL_CULL_FACE); // enable face culling
        glFrontFace(GL_CCW);// specify the winding order of frontRay-facing triangles
        glCullFace(GL_BACK);// specify which faces to cull
    }

    public void setProjection() {
        projection.identity().perspective((float) Math.toRadians(70.0f), //Fov
                (float) client.window.getWidth() / (float) client.window.getHeight(), //screen ratio
                0.1f, 10000.0f); //display range (clipping planes)
    }


    private WCCi rayWCC = new WCCi();
    private Vector3i rayWorldPos = new Vector3i();

    private void setInfoText() {
        if (writeDebugText) {
            String text = "";
            try {
                WCCf playerWCC = new WCCf();
                playerWCC.set(Client.userPlayer.worldPosition);
                text += ClientWindow.mfpAndMemory + "   smoothDelta=" + client.window.smoothFrameDeltaSec + "\n";
                text += "Saved " + world.getTimeSinceLastSave() + "ms ago\n";
                text += "PLAYER pos: " +
                        ((int) Client.userPlayer.worldPosition.x) + ", " +
                        ((int) Client.userPlayer.worldPosition.y) + ", " +
                        ((int) Client.userPlayer.worldPosition.z) +
                        "    velocity: " + MiscUtils.printVec(Client.userPlayer.positionHandler.getVelocity());
                text += "\n\tcamera: " + Client.userPlayer.camera.toString();

                if (Client.userPlayer.camera.cursorRay.hitTarget() || Client.userPlayer.camera.cursorRay.angelPlacementMode) {
                    if (client.window.isKeyPressed(GLFW.GLFW_KEY_Q)) {
                        rayWCC.set(Client.userPlayer.camera.cursorRay.getHitPosPlusNormal());
                        rayWorldPos.set(Client.userPlayer.camera.cursorRay.getHitPosPlusNormal());
                        text += "\nRAY (+normal) (Q): \n\t" + Client.userPlayer.camera.cursorRay.toString() + "\n\t" + rayWCC.toString() + "\n";
                    } else {
                        rayWCC.set(Client.userPlayer.camera.cursorRay.getHitPos());
                        rayWorldPos.set(Client.userPlayer.camera.cursorRay.getHitPos());
                        text += "\nRAY (hit) (Q): \n\t" + Client.userPlayer.camera.cursorRay.toString() + "\n\t" + rayWCC.toString() + "\n";
                    }



                    ClientChunk chunk = world.getChunk(rayWCC.chunk);
                    text += "\n\nchunk ("+chunk.position.x+", "+chunk.position.y+", "+chunk.position.z+"): ";
                    if (chunk == null) {
                        text += "null";
                    } else {
                        chunk.neghbors.cacheNeighbors();
                        text += "\nneighbors: " + chunk.neghbors.toString();
                        text += "\nmesh: visible:" + chunk.getMeshBundle().opaqueMesh.isVisible();
                        text += "\nmesh: " + chunk.getMeshBundle();

                        Block block = Registrys.getBlock(chunk.voxels.getBlock(rayWCC.chunkVoxel.x, rayWCC.chunkVoxel.y, rayWCC.chunkVoxel.z));
                        BlockData data = chunk.voxels.getBlockData(rayWCC.chunkVoxel.x, rayWCC.chunkVoxel.y, rayWCC.chunkVoxel.z);

                        byte sun = chunk.voxels.getSun(rayWCC.chunkVoxel.x, rayWCC.chunkVoxel.y, rayWCC.chunkVoxel.z);
                        text += "\n" + block + " data: " + printBlockData(data) + " typeReference: " + Registrys.blocks.getBlockType(block.type);
                        text += "\nlight=" + Main.getServer().getLightLevel(rayWorldPos.x, rayWorldPos.y, rayWorldPos.z)
                                + "  sun=" + (sun)
                                + "  torch=" + chunk.voxels.getTorch(rayWCC.chunkVoxel.x, rayWCC.chunkVoxel.y, rayWCC.chunkVoxel.z);
                    }

                }

                text += "\nSpecial Mode: " + specialMode;
                text += "\nAny Menu Open: " + ui.anyMenuOpen();
                text += "\nBase Menus Open: " + ui.baseMenusOpen();

            } catch (Exception ex) {
                text = "Error: " + ex.getMessage();
                ex.printStackTrace();
            }
            ui.setDevText(text);
        } else ui.setDevText(null);
    }


    private String printBlockData(BlockData data) {
        if (data == null) return "null";
        else if (data.size() > 20)
            return "l=" + data.size() + "   \"" + new String(data.toByteArray())
                    .replaceAll("\n", "").replaceAll("\\s+", "") + "\"";
        else return data.toString();
    }


}
