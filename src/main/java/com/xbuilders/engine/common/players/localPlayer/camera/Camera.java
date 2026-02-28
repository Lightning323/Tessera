package com.xbuilders.engine.common.players.localPlayer.camera;

import com.xbuilders.engine.client.ClientWindow;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.common.players.localPlayer.LocalPlayer;
import com.xbuilders.engine.server.block.BlockRegistry;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.engine.server.block.Block;
import com.xbuilders.engine.common.players.localPlayer.raycasting.CursorRay;
import com.xbuilders.engine.common.players.localPlayer.raycasting.Ray;
import com.xbuilders.engine.common.players.localPlayer.raycasting.RayCasting;
import com.xbuilders.utils.MiscUtils;
import com.xbuilders.engine.common.math.MathUtils;
import com.xbuilders.engine.common.world.chunk.BlockData;

import java.awt.*;
import java.lang.Math;
import java.nio.IntBuffer;

import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import static com.xbuilders.Main.LOGGER;

public class Camera {

    public final Vector2i simplifiedPanTilt;
    public final Vector3f right, look, target, position;
    public final Vector3f centeredViewNegatedLook = new Vector3f();
    public final Vector3f cursorRaycastLook = new Vector3f();
    public final Vector3f cameraRaycast = new Vector3f();
    public final Vector3f cameraForward = new Vector3f();
    public final Vector3f identity = new Vector3f(0f, 0f, 0f);

    public float tilt, pan, normalizedPan;
    public final Ray cameraViewRay;
    private float thirdPersonDist = 0;
    public final static FrustumCullingTester frustum = new FrustumCullingTester();
    public static final double HALF_PI = Math.PI / 2;
    public static final double TWO_PI = Math.PI * 2;
    public final static Vector3f up = new Vector3f(0f, -1f, 0f);
    private final float sensitivity = 0.15f;
    private Point mouse = new Point(0, 0);
    private final IntBuffer windowX, windowY;
    private Robot robot;
    private final LocalPlayer player;
    private final ClientWindow window;

    public final Matrix4f view, centeredView, projection;


    private void calculateCameraOrientation() {
        normalizedPan = (float) ((float) pan / MathUtils.TWO_PI);
        if (normalizedPan < 0) {
            normalizedPan += ((int) -normalizedPan) + 1;
        } else {
            normalizedPan -= (int) normalizedPan;
        }
        simplifiedPanTilt.x = Math.round(normalizedPan * 4);
        if (simplifiedPanTilt.x == 4) {
            simplifiedPanTilt.x = 0;
        }
        simplifiedPanTilt.y = Math.round(tilt);
        //Add 1 to simplifiedPanTilt.x but make it still wrap around to 0
        switch (simplifiedPanTilt.x) {
            case 0 -> simplifiedPanTilt.x = 1;
            case 1 -> simplifiedPanTilt.x = 2;
            case 2 -> simplifiedPanTilt.x = 3;
            default -> simplifiedPanTilt.x = 0;
        }
    }

    public BlockData simplifiedPanTiltAsBlockData(BlockData data) {
        if (data.size() != 2) {
            data = new BlockData(2);
        }
        data.set(0, (byte) simplifiedPanTilt.x);
        data.set(1, (byte) simplifiedPanTilt.y);
        return data;
    }

    /**
     * @return the thirdPersonDist
     */
    public float getThirdPersonDist() {
        return thirdPersonDist;
    }

    /**
     * @param thirdPersonDist the thirdPersonDist to set
     */
    public void setThirdPersonDist(float thirdPersonDist) {
        this.thirdPersonDist = MathUtils.clamp(thirdPersonDist, -20, 20);
    }

    public void cycleToNextView(int dist) {
        if (getThirdPersonDist() == 0) {
            setThirdPersonDist(dist);
        } else if (getThirdPersonDist() > 0) {
            setThirdPersonDist(-dist);
        } else {
            setThirdPersonDist(0);
        }
    }


    public Camera(LocalPlayer player,
                  ClientWindow window,
                  Matrix4f projection, Matrix4f view, Matrix4f centeredView) {
        cursorRay = new CursorRay(this,window);
        this.view = view;
        this.centeredView = centeredView;
        this.projection = projection;
        this.window = window;
        this.player = player;
        windowX = MemoryUtil.memAllocInt(1);
        windowY = MemoryUtil.memAllocInt(1);
        simplifiedPanTilt = new Vector2i();
        cameraViewRay = new Ray();

        try {
            robot = new Robot();
        } catch (AWTException e) {
            LOGGER.info("error", e);
        }

        target = new Vector3f();
        position = new Vector3f();
        right = new Vector3f(1f, 0f, 0f);
        look = new Vector3f(0f, 0.5f, 1f);
        pan = 0;
        tilt = 0f;
    }

    public void init(){
       cursorRay.init();
    }

    public final CursorRay cursorRay;

    public void hideMouse() {
        GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
    }

    public void showMouse() {
        GLFW.glfwSetInputMode(window.getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }

    public void update(boolean holdMouse) {
        if (holdMouse) {
            hideMouse();
            mouse = MouseInfo.getPointerInfo().getLocation();
        } else showMouse();

        window.getWindowPos(windowX, windowY);

        int x = windowX.get(0);
        int y = windowY.get(0);
        int w = window.getWidth();
        int h = window.getHeight();

        int middleX = w / 2 + x;
        int middleY = h / 2 + y;

        int deltaX = mouse.x - middleX;
        int deltaY = mouse.y - middleY;

        // The window Position is a little off, could be being multiplied by some factor
        if (holdMouse) robot.mouseMove(middleX, middleY); // target mouse


        if (holdMouse) {
            if (getThirdPersonDist() > 0) {
                pan += MathUtils.map(deltaX, 0, w, 0, TWO_PI) * sensitivity;
            } else {
                pan -= MathUtils.map(deltaX, 0, w, 0, TWO_PI) * sensitivity;
            }
            tilt += MathUtils.map(deltaY, 0, h, 0, Math.PI) * sensitivity;
        }


        tilt = (float) MathUtils.clamp(tilt, -Math.PI / 2.01f, Math.PI / 2.01f);
        if (tilt == HALF_PI) {
            tilt += 0.001f;
        }
        calculateCameraOrientation();

        //Update the camera position
        if (getThirdPersonDist() == 0) {
            cameraForward.set(Math.cos(pan), 0, Math.sin(pan));
            right.set(Math.cos(pan - Math.PI / 2), 0, Math.sin(pan - Math.PI / 2));
        } else {
            cameraForward.set(-Math.cos(pan), 0, -Math.sin(pan));
            right.set(-Math.cos(pan - Math.PI / 2), 0, -Math.sin(pan - Math.PI / 2));
        }

        look.set(Math.cos(pan), Math.tan(tilt), Math.sin(pan));
        right.normalize();
        look.normalize();
        cameraForward.normalize();
        target.set(player.worldPosition);
        position.set(player.worldPosition);


        if (getThirdPersonDist() == 0) {
            target.add(look);
            cursorRaycastLook.set(look);
        } else {
//            target.sub(player.aabb.offset);//Subtract the offset if in third person
//            position.sub(player.aabb.offset);

            float thirdPersonDist2 = Math.abs(thirdPersonDist);
            if (getThirdPersonDist() > 0) {
                cameraRaycast.set(look);
            } else {
                cameraRaycast.set(0).sub(look);
            }
            RayCasting.traceSimpleRay(cameraViewRay, position, cameraRaycast, (int) thirdPersonDist2 + 1,
                    ((block, forbiddenBlock, rx, ry, rz) -> {
                        Block block2 = Registrys.getBlock(block);
                        return block != BlockRegistry.BLOCK_AIR.id &&
                                block != forbiddenBlock
                                && (block2.solid || block2.opaque);
                    })
                    , Client.world);
            look.mul(MathUtils.clamp(cameraViewRay.distanceTraveled, 2, thirdPersonDist2) - 1.5f);


            if (getThirdPersonDist() > 0) {
                position.add(look);
                cursorRaycastLook.set(0).sub(look);
            } else {
                position.sub(look);
                cursorRaycastLook.set(0).add(look);
            }

        }
        cursorRay.cast(position, cursorRaycastLook, Client.world);
        view.identity().lookAt(position, target, up);

        if (getThirdPersonDist() > 0) {
            centeredViewNegatedLook.set(0).sub(look);
            centeredView.identity().lookAt(identity, centeredViewNegatedLook, up);
        } else centeredView.identity().lookAt(identity, look, up);

        //We must update the frustum AFTER we update the camera
        frustum.update(projection, view);
    }


    public String toString() {
        return "Camera: pan\\tilt:(" + MiscUtils.printVec(player.camera.simplifiedPanTilt) + "), thirdPersonDist:" + thirdPersonDist + " cursorHitAll:" + cursorRay.angelPlacementMode;
    }
}
