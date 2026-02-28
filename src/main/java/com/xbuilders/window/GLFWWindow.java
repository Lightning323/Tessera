/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.window;

import com.xbuilders.window.utils.ValueSmoother;
import com.xbuilders.window.utils.IOUtil;
import com.xbuilders.window.utils.texture.TextureUtils;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2d;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;

import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SEVERITY_LOW_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_SOURCE_API_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.GL_DEBUG_TYPE_OTHER_ARB;
import static org.lwjgl.opengl.ARBDebugOutput.glDebugMessageControlARB;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.system.MemoryStack.stackPush;

import org.lwjgl.system.MemoryUtil;

import org.lwjgl.glfw.GLFWImage;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.system.Platform;

/**
 * @author Patron
 */
public abstract class GLFWWindow {

    protected int width, height, display_width, display_height;
    protected Vector2d cursor;
    protected long window;
    private long monitor;
    private int[] wndPos = {0, 0};
    private int[] wndSize = {0, 0};
    private int[] vpSize = {0, 0};
    private int[] monitorSize = {0, 0};
    private boolean terminate = false;

    protected GLCapabilities capabilities;
    private static Callback debugProc;

    public void setIcon(String icon16Path, String icon32Path, String icon256Path) throws Exception {
        ByteBuffer icon16, icon32, icon256;
        try {
            icon16 = IOUtil.ioResourceToByteBuffer(icon16Path, 2048);
            icon32 = IOUtil.ioResourceToByteBuffer(icon32Path, 4096);
            icon256 = IOUtil.ioResourceToByteBuffer(icon256Path, 1262144);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setIcon(icon16, icon32, icon256);
    }

    public void setIcon(InputStream icon16Path, InputStream icon32Path, InputStream icon256Path) throws Exception {
        ByteBuffer icon16, icon32, icon256;
        try {
            icon16 = IOUtil.inputStreamToByteBuffer(icon16Path, 2048);
            icon32 = IOUtil.inputStreamToByteBuffer(icon32Path, 4096);
            icon256 = IOUtil.inputStreamToByteBuffer(icon256Path, 1262144);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setIcon(icon16, icon32, icon256);
    }

    /**
     * from https://gamedev.stackexchange.com/questions/105555/setting-window-icon-using-glfw-lwjgl-3
     */
    public void setIcon(ByteBuffer icon16, ByteBuffer icon32, ByteBuffer icon256) throws Exception {
        IntBuffer w = memAllocInt(1);
        IntBuffer h = memAllocInt(1);
        IntBuffer comp = memAllocInt(1);

        try (GLFWImage.Buffer icons = GLFWImage.malloc(2)) {
            ByteBuffer pixels16 = stbi_load_from_memory(icon16, w, h, comp, 4);
            icons.position(0)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels16);

            ByteBuffer pixels32 = stbi_load_from_memory(icon32, w, h, comp, 4);
            icons.position(1)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels32);

            ByteBuffer pixels256 = stbi_load_from_memory(icon256, w, h, comp, 4);
            icons.position(2)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels256);

            icons.position(0);
            GLFW.glfwSetWindowIcon(window, icons);

            stbi_image_free(pixels32);
            stbi_image_free(pixels16);
        }
        // Display.setIcon(new ByteBuffer[] {
        // new ImageIOImageData().imageToByteBuffer(ImageIO.read(new
        // File("res/game/gameIcon.png")), false, false, null),
        // new ImageIOImageData().imageToByteBuffer(ImageIO.read(new
        // File("res/game/gameIcon.png")), false, false, null)
        // });

        // IntBuffer w = MemoryUtil.memAllocInt(1);
        // IntBuffer h = MemoryUtil.memAllocInt(1);
        // IntBuffer comp = MemoryUtil.memAllocInt(1);

        // File image = new File(path);
        // if(!image.exists()) {
        // throw new RuntimeException("File not found: " + path);
        // }

        // // Icons
        // {
        // ByteBuffer icon16;
        // ByteBuffer icon32;
        // try {
        // icon16 = MiscUtils.ioResourceToByteBuffer(path, 2048);
        // icon32 = MiscUtils.ioResourceToByteBuffer(path, 4096);
        // } catch (Exception e) {
        // throw new RuntimeException(e);
        // }

        // try (GLFWImage.Buffer icons = GLFWImage.malloc(2)) {
        // ByteBuffer pixels16 = STBImage.stbi_load_from_memory(icon16, w, h, comp, 4);
        // icons
        // .position(0)
        // .width(w.get(0))
        // .height(h.get(0))
        // .pixels(pixels16);

        // ByteBuffer pixels32 = STBImage.stbi_load_from_memory(icon32, w, h, comp, 4);
        // icons
        // .position(1)
        // .width(w.get(0))
        // .height(h.get(0))
        // .pixels(pixels32);

        // icons.position(0);
        // GLFW.glfwSetWindowIcon(id, icons);

        // STBImage.stbi_image_free(pixels32);
        // STBImage.stbi_image_free(pixels16);
        // }
        // }

        // MemoryUtil.memFree(comp);
        // MemoryUtil.memFree(h);
        // MemoryUtil.memFree(w);
    }

    public boolean windowIsFocused() {
        return glfwGetWindowAttrib(window, GLFW_FOCUSED) == GLFW_TRUE;
    }

    public BufferedImage readPixelsOfWindow() {
        int WIDTH = getWidth();
        int HEIGHT = getHeight();

        ByteBuffer buffer = BufferUtils.createByteBuffer(WIDTH * HEIGHT * 4);

        // Read the pixel data from the framebuffer
        GL11.glReadPixels(0, 0, WIDTH, HEIGHT, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Convert the raw pixel data to an image
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int i = (x + WIDTH * y) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, HEIGHT - y - 1, rgb); // Flip the image vertically
            }
        }
        return image;
    }

    public GLFWWindow() {
        cursor = new Vector2d();
        initGLFW();
    }

    // <editor-fold defaultstate="collapsed" desc="glfw implentations / random
    // methods">
    public void setTitle(String title) {
        GLFW.glfwSetWindowTitle(getWindow(), title);
    }

    public void setWindowPos(final int x, final int y) {
        GLFW.glfwSetWindowPos(getWindow(), x, y);
    }

    public void getWindowPos(IntBuffer xpos, IntBuffer ypos) {
        GLFW.glfwGetWindowPos(getWindow(), xpos, ypos);
    }

    public void centerWindow() {
        // vidmode gets the info about the monitor
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(glfwGetPrimaryMonitor());
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(getWindow(), pWidth, pHeight);
            GLFW.glfwSetWindowPos(getWindow(),
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2);
        }
    }

    /**
     * @param key A key ID from the org.lwjgl.glfw.GLFW package
     * @return
     */
    public boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(getWindow(), key) == GLFW.GLFW_PRESS;
    }

    /**
     * @param key A key ID from the org.lwjgl.glfw.GLFW package
     * @return
     */
    public boolean isMouseButtonPressed(int key) {
        return GLFW.glfwGetMouseButton(getWindow(), key) == GLFW.GLFW_PRESS;
    }

    public void showWindow() {
        GLFW.glfwShowWindow(getWindow());
    }

    public void hideWindow() {
        GLFW.glfwHideWindow(getWindow());
    }

    public boolean windowShouldClose() {
        return GLFW.glfwWindowShouldClose(getWindow()) || terminate;
    }
    // </editor-fold>

    public void dispose() {
        terminate = true;
    }

    // //<editor-fold defaultstate="collapsed" desc="variables">

    /**
     * @return the id
     */
    public long getWindow() {
        return window;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the display_width
     */
    public int getDisplay_width() {
        return display_width;
    }

    /**
     * @return the display_height
     */
    public int getDisplay_height() {
        return display_height;
    }

    private void setWindowSizeVariables() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            glfwGetWindowSize(getWindow(), w, h);
            width = w.get(0);
            height = h.get(0);

            glfwGetFramebufferSize(getWindow(), w, h);
            display_width = w.get(0);
            display_height = h.get(0);
        }
    }

    public double getCursorPosX() {
        return cursor.x;
    }

    public double getCursorPosY() {
        return cursor.y;
    }

    public Vector2d getCursorVector() {
        return cursor;
    }

    /// / </editor-fold>

    public static final Object windowCreateLock = new Object();
    private static boolean isGLFWInitialized = false;

    public static final void initGLFW() {
        if (!isGLFWInitialized) { //Initialize GLFW if it hasn't been initialized yet
            isGLFWInitialized = true;
            if (!glfwInit()) throw new IllegalStateException("Unable to initialize glfw");
        }
    }

    /**
     * Closes and cleans up the current window
     */
    public static final void endGLFW() {
        GLFW.glfwSetErrorCallback(null).free();
        if (GLFWWindow.debugProc != null) {
            debugProc.free();
        }
        TextureUtils.deleteAllTextures();
        GLFW.glfwTerminate();
    }


    public boolean isFullscreen() {
        return GLFW.glfwGetWindowMonitor(window) != MemoryUtil.NULL;
    }

    public void disableFullscreen() {
        if (!isFullscreen()) return;
        // Restore windowed mode
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
        GLFW.glfwSetWindowMonitor(window, MemoryUtil.NULL, wndPos[0], wndPos[1], wndSize[0], wndSize[1], vidMode.refreshRate());
    }

    public void enableFullscreen(float resolutionScale) {
        if (!isFullscreen()) {
            // Store the window's position and size before entering fullscreen
            IntBuffer posX = BufferUtils.createIntBuffer(1);
            IntBuffer posY = BufferUtils.createIntBuffer(1);
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);

            GLFW.glfwGetWindowPos(window, posX, posY);
            GLFW.glfwGetWindowSize(window, width, height);

            wndPos[0] = posX.get(0);
            wndPos[1] = posY.get(0);
            wndSize[0] = width.get(0);
            wndSize[1] = height.get(0);

            // Backup window position and size
            GLFW.glfwGetWindowPos(window, intBuffer(wndPos[0]), intBuffer(wndPos[1]));
            GLFW.glfwGetWindowSize(window, intBuffer(wndSize[0]), intBuffer(wndSize[1]));

            // Backup monitor size
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
            monitorSize[0] = vidMode.width();
            monitorSize[1] = vidMode.height();
        }

        if (resolutionScale > 1f) resolutionScale = 1f;
        else if (resolutionScale < 0f) resolutionScale = 0f;
        int fullscreenWidth = (int) (monitorSize[0] * resolutionScale);
        int fullscreenHeight = (int) (monitorSize[1] * resolutionScale);

        if (isFullscreen()) {
            GLFW.glfwSetWindowSize(window, fullscreenWidth, fullscreenHeight);
        } else {
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
            GLFW.glfwSetWindowMonitor(window, monitor, 0, 0, fullscreenWidth, fullscreenHeight, vidMode.refreshRate());
        }
    }

    public void startFrame() {
    }

    public void endFrame() {
        GLFW.glfwSwapBuffers(getWindow());
        GLFW.glfwPollEvents();
        tickMPF();
    }

    private IntBuffer intBuffer(int value) {
        return BufferUtils.createIntBuffer(1).put(0, value);
    }

    public void createWindow(String title, int width, int height) {
        initGLFW();
        windowHints();

        synchronized (windowCreateLock) {
            window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
            if (window == MemoryUtil.NULL) {
                GLFW.glfwTerminate();
                throw new RuntimeException("Error creating the window");
            }
        }

        GLFW.glfwMakeContextCurrent(window);
        capabilities = GL.createCapabilities();
        monitor = GLFW.glfwGetPrimaryMonitor();
        GLFW.glfwGetWindowSize(window, intBuffer(wndSize[0]), intBuffer(wndSize[1]));
        GLFW.glfwGetWindowPos(window, intBuffer(wndPos[0]), intBuffer(wndPos[1]));

        GLFW.glfwSwapInterval(1);// to disable vsync GLFW.glfwSwapInterval(0); (1 = on, 0 = off)
        centerWindow();
        setWindowSizeVariables();
        initDebugs();
        initCallbacks();
        startMPF();
    }

    public void destroyWindow() {
        try {//java.lang.NoClassDefFoundError: org/lwjgl/glfw/Callbacks
            Callbacks.glfwFreeCallbacks(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GLFW.glfwDestroyWindow(window);
    }

    private void initDebugs() {
        if (debugProc == null) {
            debugProc = GLUtil.setupDebugMessageCallback();
            if (capabilities.OpenGL43) {
                GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_API, GL43.GL_DEBUG_TYPE_OTHER,
                        GL43.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null, false);
            } else if (capabilities.GL_KHR_debug) {
                KHRDebug.glDebugMessageControl(
                        KHRDebug.GL_DEBUG_SOURCE_API,
                        KHRDebug.GL_DEBUG_TYPE_OTHER,
                        KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                        (IntBuffer) null,
                        false);
            } else if (capabilities.GL_ARB_debug_output) {
                glDebugMessageControlARB(GL_DEBUG_SOURCE_API_ARB, GL_DEBUG_TYPE_OTHER_ARB, GL_DEBUG_SEVERITY_LOW_ARB,
                        (IntBuffer) null, false);
            }
            GL43.glDebugMessageCallback(GLFWWindow::debugCallback, NULL);
        }
    }

    private void windowHints() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE); //Important hint for fullscreen resolution
        glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        // Set opengl version (You can change this to your desired opengl version)
        glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);

        glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        if (Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        }
        glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
    }

    static boolean enableDebugMessages = false;

    /**
     * @param enabled if we should print debug messages
     */
    public static void printDebugsEnabled(boolean enabled) {
        enableDebugMessages = enabled;
    }

    private static void debugCallback(int source, int type, int id, int severity, int length, long message,
                                      long userParam) {
        if (enableDebugMessages) {
            ByteBuffer buffer = MemoryUtil.memByteBuffer(message, length);
            String debugMessage = MemoryUtil.memASCII(buffer);

            System.err.println("OpenGL Debug Message:");
            System.err.println("  Source: " + source);
            System.err.println("  Type: " + type);
            System.err.println("  ID: " + id);
            System.err.println("  Severity: " + severity);
            System.err.println("  Message: " + debugMessage);
        }
    }

    private void initCallbacks() {
        GLFW.glfwSetFramebufferSizeCallback(getWindow(), new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, final int width, final int height) {
                glfwMakeContextCurrent(window);
                setWindowSizeVariables();
                glViewport(0, 0, width, height);
                framebufferResizeEvent(width, height);
            }
        });
    }

    /**
     * called when the window is resized
     *
     * @param width
     * @param height
     */
    public abstract void framebufferResizeEvent(final int width, final int height);

    // <editor-fold defaultstate="collapsed" desc="MPF">
    private double lastTime;
    private int nbFrames;
    private double updateIntervalSec = 1.0;
    private int updateIntervalMS = 1000;
    private double msPerFrame;
    long timer = System.nanoTime();


    public void setMpfUpdateInterval(int milliseconds) {
        updateIntervalMS = milliseconds;
        updateIntervalSec = (double) updateIntervalMS / 1000;
    }

    private void startMPF() {
        lastTime = glfwGetTime();
        nbFrames = 0;
        timer = System.nanoTime();
        frameDeltaSec = 1f;
    }

    /**
     * @return the amount of time it takes (in ms) to render 1 frame
     */
    public double getMsPerFrame() {
        return msPerFrame;
    }


    public void onMPFUpdate() {
        // System.out.println(getMsPerFrame() + " ms/frame\n");
    }

    /**
     * This code displays the time, in milliseconds, needed to draw a frame
     * instead of how many frame were drawn in the last second. This is actually
     * much better. Don’t rely on FPS. Never.
     * <br>
     * <br>
     * this is an inverse relationship, and we humans suck at understanding this
     * kind of relationship. Let’s take an example. You write a great rendering
     * function that runs at 1000 FPS ( 1ms/frame ). But you forgot a little
     * computation in a shader, which adds an extra cost of 0.1ms. And bam,
     * 1/0.0011 = 900. You just lost 100FPS. Morality : never use FPS for
     * performance analysis. If you intend to make a 60fps game, your target
     * will be 16.6666ms ; If you intend to make a 30fps game, your target will
     * be 33.3333ms. That’s all you need to know.
     */

    public float frameDeltaSec = 1f;
    public float smoothFrameDeltaSec = 1f;

    private ValueSmoother smoothed = new ValueSmoother(20);

    /**
     * Delta smoothign helps the movement maintain a constant speed
     * Here's a basic example in Java:
     * <p>
     * Copy
     * public class GameLoop {
     * <p>
     * private static final int SMOOTHING_FACTOR = 5; // Number of frame times to consider for smoothing
     * private static List<Double> frameTimes = new ArrayList<>();
     * <p>
     * public static void main(String[] args) {
     * while (true) {
     * long currentTime = System.nanoTime();
     * double frameTime = (currentTime - previousTime) / 1_000_000_000.0;
     * previousTime = currentTime;
     * <p>
     * frameTimes.add(frameTime);
     * if (frameTimes.size() > SMOOTHING_FACTOR) {
     * frameTimes.remove(0); // Keep only the last N frame times
     * }
     * <p>
     * double smoothedDeltaTime = calculateSmoothedDeltaTime();
     * update(smoothedDeltaTime);
     * render();
     * }
     * }
     * <p>
     * private static double calculateSmoothedDeltaTime() {
     * double sum = 0;
     * for (double time : frameTimes) {
     * sum += time;
     * }
     * return sum / frameTimes.size();
     * }
     */

    protected void tickMPF() {
        frameDeltaSec = (System.nanoTime() - timer) / 1000000000f;
        timer = System.nanoTime();
        smoothed.add(frameDeltaSec);
        smoothFrameDeltaSec = smoothed.getAverage();

        // do {// Measure speed
        double currentTime = glfwGetTime();
        nbFrames++;
        if (currentTime - lastTime >= updateIntervalSec) {// If last prinf() was more than 1 sec ago
            // printf and reset timer
            msPerFrame = updateIntervalMS / ((double) nbFrames);
            onMPFUpdate();
            nbFrames = 0;
            lastTime += updateIntervalSec;
        }
        // }
    }
    // </editor-fold>


}
