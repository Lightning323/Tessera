package com.tessera.engine.client.visuals;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.gameScene.UI_GameMenu;
import com.tessera.engine.client.visuals.topMenu.PopupMessage;
import com.tessera.window.WindowEvents;
import com.tessera.window.nuklear.components.TextBox;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static org.lwjgl.nuklear.Nuklear.*;

public class FileDialog extends UI_GameMenu implements WindowEvents {

    public FileDialog(NkContext ctx, ClientWindow window) {
        super(ctx, window);
        releaseMouse = true;
        prompt = new PopupMessage(ctx, window);
        fileNameBox = new TextBox(100);
        oh = new OpenHandler(ctx, WINDOW_NAME_ID, windowFlags);
    }

    final String WINDOW_NAME_ID = "FileDialog";
    final int windowFlags = Nuklear.NK_WINDOW_TITLE | Nuklear.NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_CLOSABLE;

    String prefferedFileExtension;
    TextBox fileNameBox;
    public final int width = 550;
    public final int height = 420;

    PopupMessage prompt;
    Consumer<File> onSelectCallback;
    File baseDir, navDir, selectedFile;
    boolean saveMode;
    OpenHandler oh;

    @Override
    public boolean isOpen() {
        return oh.isOpen();
    }

    public void show(File baseDir,
                     boolean saveMode,
                     String prefferedFileExtension,
                     Consumer<File> onSelectCallback) {
        this.onSelectCallback = onSelectCallback;
        this.baseDir = baseDir;

        this.prefferedFileExtension = prefferedFileExtension.toLowerCase();
        if (!this.prefferedFileExtension.startsWith(".")) {
            this.prefferedFileExtension = "." + this.prefferedFileExtension;
        }
        selectedFile = null;
        oh.setOpen(true);
        fileNameBox.setValueAsString("");
        this.navDir = baseDir;
        this.saveMode = saveMode;
        nk_window_show(ctx, WINDOW_NAME_ID, windowFlags);
    }

    public void hide() {
        baseDir = null;
        oh.setOpen(false);
    }

    @Override
    public void draw(MemoryStack stack) {
        if (isOpen()) {
            NkRect windowDims = NkRect.malloc(stack);
            Theme.resetWindowColor(ctx);
            Theme.resetWindowPadding(ctx);

            nk_rect(
                    window.getWidth() / 2 - (width / 2),
                    window.getHeight() / 2 - (height / 2), width, height, windowDims);

            String title = (saveMode ? "Save File" : "Open File");

            if (nk_begin_titled(ctx, WINDOW_NAME_ID, title, windowDims, windowFlags)) {
                Theme.resetEntireButtonStyle(ctx);
                oh.autoClose();
                nk_style_set_font(ctx, Theme.font_10);


                Nuklear.nk_layout_row_dynamic(ctx, 25, 1);

                String dir = navDir.getPath();
                //Remove base dir from path
                dir = dir.substring(baseDir.getPath().length());
                Nuklear.nk_label(ctx, dir, NK_TEXT_RIGHT);


                if (!canGoUpOneDir()) {
                    Nuklear.nk_style_push_color(ctx, ctx.style().button().active().data().color(), Theme.color_gray);
                    Nuklear.nk_style_push_color(ctx, ctx.style().button().hover().data().color(), Theme.color_gray);
                    Nuklear.nk_style_push_color(ctx, ctx.style().button().border_color(), Theme.color_lightGray);
                }
                if (Nuklear.nk_button_label(ctx, "^- Up Directory")) {
                    if (canGoUpOneDir()) {
                        navigateToFolder(navDir.getParentFile());
                    }
                }
                if (!canGoUpOneDir()) {
                    Nuklear.nk_style_pop_color(ctx);
                    Nuklear.nk_style_pop_color(ctx);
                    Nuklear.nk_style_pop_color(ctx);
                }


                Nuklear.nk_style_push_color(ctx, ctx.style().window().background(), Theme.color_gray);
                Nuklear.nk_style_push_color(ctx, ctx.style().button().hover().data().color(), Theme.color_blue);
                float border = ctx.style().button().border(); //Get border size
                ctx.style().button().border(0);
                filesGroup();
                Nuklear.nk_style_pop_color(ctx);
                Nuklear.nk_style_pop_color(ctx);
                ctx.style().button().border(border);


                Nuklear.nk_layout_row_dynamic(ctx, 30, 1);
                if (saveMode) fileNameBox.render(ctx);

                Nuklear.nk_layout_row_dynamic(ctx, 30, 3);
                if (Nuklear.nk_button_label(ctx, "New Folder")) {
                    if (navDir != null && !fileNameBox.getValueAsString().isBlank()) {
                        File newDir = new File(navDir, fileNameBox.getValueAsString());
                        newDir.mkdirs();
                        navigateToFolder(newDir);
                        fileNameBox.setValueAsString("");
                    }
                }
                if (Nuklear.nk_button_label(ctx, "Delete")) {
                    deleteSelectedFile();
                }
                if (saveMode) {
                    if (Nuklear.nk_button_label(ctx, "Save")) {
                        saveFile(fileNameBox.getValueAsString());
                        fileNameBox.setValueAsString("");
                    }
                } else {
                    if (Nuklear.nk_button_label(ctx, "Open")) {
                        if (selectedFile != null && !selectedFile.isDirectory()) {
                            onSelectCallback.accept(selectedFile);
                            hide();
                        }
                    }
                }
                Nuklear.nk_end(ctx);
            } else {
                oh.setOpen(false);
            }
            oh.autoClose();
        }
        prompt.draw();
        oh.autoClose();
    }

    private void deleteSelectedFile() {
        if (selectedFile != null) {
            if (selectedFile.isDirectory() && selectedFile.listFiles().length > 0) {
                prompt.message("Can't delete folder",
                        "Can't delete folders with files in them");
                return;
            }

            prompt.confirmation("Delete?",
                    "Are you sure you want to delete " + selectedFile.getName() + "?",
                    () -> {
                        System.out.println("Deleting " + selectedFile.getPath());
                        selectedFile.delete();
                    });
        }
    }

    private boolean canGoUpOneDir() {
        return !navDir.equals(baseDir) && navDir.getParentFile() != null;
    }

    private void navigateToFolder(File newDir) {
        if (newDir != null) {
            navDir = newDir;
            selectedFile = null;
        }
    }

    private void filesGroup() {
        Nuklear.nk_layout_row_dynamic(ctx, height - 145 - 30, 1);//very important
        if (Nuklear.nk_group_begin(ctx, "Files", 0)) {
            Nuklear.nk_layout_row_dynamic(ctx, 25, 1);
            if (navDir.listFiles() != null) { //If there are no files, it will be null
                for (File f : navDir.listFiles()) {
                    if (!hasFileExtension(f) && !f.isDirectory()) continue;
                    boolean selected = selectedFile != null && selectedFile.equals(f);

                    if (selected) {
                        Nuklear.nk_style_push_color(ctx, ctx.style().button().normal().data().color(), Theme.color_blue);
                    }
                    if (Nuklear.nk_button_label(ctx, f.getName() + (f.isDirectory() ? "\\" : ""))) {
                        if (f.isDirectory() && selected) {
                            navDir = f;
                        } else {
                            selectedFile = f;
                            fileNameBox.setValueAsString(f.getName());
                        }
                    }
                    if (selected) {
                        Nuklear.nk_style_pop_color(ctx);
                    }
                }
            }

            Nuklear.nk_group_end(ctx);
        }
    }


    private void saveFile(String filename) {
        if (filename.isBlank()) return;

        if (!filename.toLowerCase().endsWith(prefferedFileExtension)) {
            if (filename.contains(".")) {
                prompt.message("Can't Save", "Invalid File Extension");
                return;
            }
            filename = filename + prefferedFileExtension;
        }

        File newFile = new File(navDir, filename);
        if (!isFilenameValid(newFile)) {
            prompt.message("Can't Save", "Invalid File Name");
            return;
        }


        if (newFile.exists()) {
            if (newFile.isDirectory()) return;

            prompt.confirmation("File Exists",
                    "Do you want to overwrite " + newFile.getName() + "?",
                    () -> {
                        System.out.println("Overwriting " + newFile.getName());
                        acceptFile(newFile);
                    });
        } else {
            System.out.println("Creating " + newFile.getName());
            acceptFile(newFile);
        }
    }


    public boolean hasFileExtension(File file) {
        if (prefferedFileExtension == null) return true;
        return file.getName().toLowerCase().endsWith(prefferedFileExtension);
    }

    private void acceptFile(File file) {
        if (!hasFileExtension(file))
            prompt.message("Invalid File Type",
                    "File typeReference must be " + prefferedFileExtension);

        onSelectCallback.accept(file);
        hide();
    }

    public static boolean isFilenameValid(File f) {
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    @Override
    public void windowResizeEvent(int width, int height) {

    }

    @Override
    public boolean keyEvent(int key, int scancode, int action, int mods) {
        if(action == GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_ESCAPE) {
            hide();
            return true;
        }
        return fileNameBox.isFocused();
    }

    @Override
    public boolean mouseButtonEvent(int button, int action, int mods) {
        return false;
    }

    @Override
    public boolean mouseScrollEvent(NkVec2 scroll, double xoffset, double yoffset) {
        return false;
    }
}
