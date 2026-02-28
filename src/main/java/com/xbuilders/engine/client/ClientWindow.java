/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.client;

import com.xbuilders.Main;
import com.xbuilders.engine.client.settings.ClientSettings;
import com.xbuilders.engine.client.visuals.Theme;
import com.xbuilders.engine.client.visuals.gameScene.GameScene;
import com.xbuilders.engine.client.visuals.topMenu.PopupMessage;
import com.xbuilders.engine.client.visuals.topMenu.TopMenu;
import com.xbuilders.utils.resource.ResourceLoader;
import com.xbuilders.utils.resource.ResourceUtils;
import com.xbuilders.engine.common.world.ClientWorld;
import com.xbuilders.engine.server.Game;
import com.xbuilders.engine.server.Registrys;
import com.xbuilders.window.GLFWWindow;
import com.xbuilders.window.NKWindow;
import com.xbuilders.window.developmentTools.MemoryProfiler;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.nuklear.NkVec2;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.xbuilders.Main.LOGGER;


public class ClientWindow extends NKWindow {
    public static int frameCount = 0;
    Client client;

    //==================================================================================================================
    //==================================================================================================================
    //==================================================================================================================
    //==================================================================================================================
    //==================================================================================================================
    //==================================================================================================================


    public static void printlnDev(String message) {
        if (Client.DEV_MODE) {
            System.out.println(message);
        }
    }


    // We can still have static variables, but we want to use dependency injection,
    // and make all classes ask for the object instead of directly acsessing it
    // In summary, we want ALL classes to be easily seprable, to make the code more
    // flexible.
    public void goToGamePage() {
        isGameMode = true;
        gameScene.newGameEvent();
    }

    public void goToMenuPage() {
        isGameMode = false;
        client.stopGame(); //Close the entire game
    }

    public static boolean isInGamePage() {
        return isGameMode;
    }

    public static ClientSettings settings;

    public void saveAndApplySettings() {
        settings.save();
        //Set vsync
        if (settings.video_vsync) {
            GLFW.glfwSwapInterval(1);
        } else {
            GLFW.glfwSwapInterval(0);
        }
        if (settings.video_fullscreen) {
            enableFullscreen(settings.video_fullscreenSize.value);
        } else disableFullscreen();

//        Theme.setUIScale(settings.video_largerUI);
    }

    private static boolean isGameMode = false;

    public TopMenu topMenu;
    public GameScene gameScene;

    public static PopupMessage popupMessage;
    public static final ResourceLoader resourceLoader = new ResourceLoader();

    String title;

    public ClientWindow(String title, Client client) {
        super();
        this.client = client;
        this.title = title;
    }


    public void startWindowThread() throws IOException {
        while (!windowShouldClose()) {
            /* Input */
            beginScreenshot(); //If we want the frameTester to capture the entire frame length, we need to include startFrame() and endFrame()
            startFrame();
            Client.frameTester.__startFrame();
            render();
            MemoryProfiler.update();
            if (Client.memoryGraph != null) Client.memoryGraph.update();
            Client.frameTester.__endFrame();

            endFrame();//EndFrame takes the most time, becuase we have vsync turned on
            endScreenshot();
        }
    }

    public void init(Game game, ClientWorld world) throws Exception {
        GLFWWindow.initGLFW();
        settings = ClientSettings.load();

        setMpfUpdateInterval(1000);
        MemoryProfiler.setIntervalMS(500);

        /**
         * Create the window
         */
        int windowWidth = settings.internal_smallWindow ? 680 : 920;
        int windowHeight = settings.internal_smallWindow ? 600 : 720;
        createWindow("XBuilders", windowWidth, windowHeight);
        GLFW.glfwSwapInterval(settings.video_vsync ? 1 : 0);

        //If a fullscreen window is created, we need to set the focus callback so that the user can exit fullscreen if they lose focus
        // Get the current GLFW window serverExecute
        long windowHandle = GLFW.glfwGetCurrentContext();
        // Create a new window focus callback
        GLFWWindowFocusCallback focusCallback = new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, boolean focused) {
                if (!focused) {
                    windowUnfocusEvent();
                }
            }
        };
        GLFW.glfwSetWindowFocusCallback(windowHandle, focusCallback);


        setIcon(resourceLoader.getResourceAsStream("builtin/icon16.png"),
                resourceLoader.getResourceAsStream("builtin/icon32.png"),
                resourceLoader.getResourceAsStream("builtin/icon256.png"));

        /**
         * Anything involving OpenGL must be done after the window has been created
         */

        Theme.initialize(ctx);
        gameScene = new GameScene(client, game, world);
        game.setupClient(this, ctx, gameScene.ui);
        popupMessage = new PopupMessage(ctx, this);
        topMenu = new TopMenu(client);


        //init world
        world.initGL(Registrys.blocks.textures);

        if (settings.video_fullscreen) {
            enableFullscreen(settings.video_fullscreenSize.value);
        }
        saveAndApplySettings();   //DO THIS LAST Apply settings just in case the settings were not already applied
        showWindow();
    }


    private void render() throws IOException {
        if (isGameMode) {
            gameScene.render();
        } else {
            topMenu.render();
        }
        popupMessage.draw();
        frameCount++;//It just wraps around when it reaches max value
    }

    static DecimalFormat df = new DecimalFormat("####.00");

    private static boolean screenshot = false;
    private static boolean screenShotInitialized = false;

    public static void takeScreenshot() {
        screenshot = true;
    }

    private void beginScreenshot() {
        if (screenshot) {
            screenShotInitialized = true;
        }
    }

    private void endScreenshot() {
        if (screenShotInitialized) {
            String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));
            File saveFile = ResourceUtils.appDataFile("screenshots\\" + formattedDateTime + ".png");
            Main.getClient().consoleOut("Screenshot saved to: " + saveFile.getAbsolutePath());
            try {
                saveFile.getParentFile().mkdirs();
                ImageIO.write(readPixelsOfWindow(), "png", saveFile);
            } catch (IOException e) {
                LOGGER.info("Could not save screenshot", e);
            }
            screenShotInitialized = false;
            screenshot = false;
        }
    }

    public static String mfpAndMemory = "";

    @Override
    public void onMPFUpdate() {
        // Lower MPF is better. Since we are matching the FPS to the monitors refresh
        // rate, the FPS will not exceed 60fps.
        // Our goal is to get as close to 16.666 MPF (60 FPS) as possible
        String formattedNumber = df.format(getMsPerFrame());
        mfpAndMemory = "mpf: " + formattedNumber + "    memory: " + MemoryProfiler.getMemoryUsageAsString();

        String playerName = "";
        try {
            playerName = " (" + Client.userPlayer.getName() + ") ";
        } finally {
        }
        setTitle(title + playerName + (Client.DEV_MODE ? "   " + mfpAndMemory : ""));
    }

    @Override
    public void framebufferResizeEvent(int width, int height) {
        gameScene.windowResizeEvent(width, height);
    }

    public void minimizeWindow() {
        long windowHandle = GLFW.glfwGetCurrentContext();
        if (isGameMode) {
            gameScene.windowUnfocusEvent();
        }
        if (isFullscreen()) {
            GLFW.glfwIconifyWindow(windowHandle);
        }
    }

    public static void restoreWindow() {
        long windowHandle = GLFW.glfwGetCurrentContext();
        GLFW.glfwRestoreWindow(windowHandle);
    }

    private void windowUnfocusEvent() {
        long windowHandle = GLFW.glfwGetCurrentContext();
        if (isGameMode) {
            gameScene.windowUnfocusEvent();
        }
        if (isFullscreen()) {
            GLFW.glfwIconifyWindow(windowHandle);
        }
    }

    public static boolean devkeyF3;
    public static boolean devkeyF4;
    public static boolean devkeyF1;
    public static boolean devkeyF12;

    @Override
    public void keyEvent(int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_RELEASE) {
            if (Client.DEV_MODE && key == GLFW.GLFW_KEY_F2) {
                System.out.println("System.GC()");
                System.gc();
            } else if (Client.DEV_MODE && key == GLFW.GLFW_KEY_F3) {
                devkeyF3 = !devkeyF3;
                System.out.println("Special mode (F3): " + devkeyF3);
            } else if (Client.DEV_MODE && key == GLFW.GLFW_KEY_F4) {
                devkeyF4 = !devkeyF4;
                System.out.println("Special mode (F4): " + devkeyF4);
            } else if (Client.DEV_MODE && key == GLFW.GLFW_KEY_F1) {
                devkeyF1 = !devkeyF1;
                System.out.println("Special mode (F1): " + devkeyF1);
            } else if (Client.DEV_MODE && key == GLFW.GLFW_KEY_F12) {
                devkeyF12 = !devkeyF12;
                System.out.println("Light repropagation: " + devkeyF12);
            } else if (Client.DEV_MODE && key == GLFW.GLFW_KEY_F10) {
                System.out.println("Forced devmode crash! (F10) key pressed");
                System.out.println(10 / 0);
            } else if (key == GLFW.GLFW_KEY_F11) {
                takeScreenshot();
            }
        }
        if (isGameMode) {
            gameScene.keyEvent(key, scancode, action, mods);
        } else {
            topMenu.keyEvent();
        }
    }

    @Override
    public void mouseButtonEvent(int button, int action, int mods) {
        if (isGameMode) {
            gameScene.mouseButtonEvent(button, action, mods);
        }
    }

    @Override
    public void mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        if (isGameMode) {
            gameScene.mouseScrollEvent(scroll, xoffset, yoffset);
        }
    }

}
