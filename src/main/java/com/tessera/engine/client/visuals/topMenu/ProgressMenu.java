/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.topMenu;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.Page;
import com.tessera.engine.client.visuals.Theme;
import com.tessera.engine.common.progress.ProgressData;

import java.nio.IntBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import static org.lwjgl.nuklear.Nuklear.NK_TEXT_ALIGN_LEFT;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_BORDER;
import static org.lwjgl.nuklear.Nuklear.NK_WINDOW_TITLE;
import static org.lwjgl.nuklear.Nuklear.nk_begin;
import static org.lwjgl.nuklear.Nuklear.nk_button_label;
import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_label;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_static;
import static org.lwjgl.nuklear.Nuklear.nk_rect;
import static org.lwjgl.nuklear.Nuklear.nk_style_set_font;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author zipCoder933
 */
public class ProgressMenu implements MenuPage {

    ProgressData prog;
    TopMenu menu;
    NkContext ctx;
    ClientWindow window;

    public void enable(ProgressData prog, Runnable update, Runnable finishedTask, Runnable canceledTask) {
        this.prog = prog;
        menu.setPage(Page.PROGRESS);
        this.canceledTask = canceledTask;
        this.update = update;
        this.finishedTask = finishedTask;
    }

    public void enableOnSeparateThread(ProgressData prog, Thread thread) {
        this.prog = prog;
        menu.setPage(Page.PROGRESS);
        this.canceledTask = null;
        this.update = null;
        this.finishedTask = null;
        thread.start();
    }

    public ProgressMenu(NkContext ctx, ClientWindow window, TopMenu menu) {
        this.menu = menu;
        this.ctx = ctx;
        this.window = window;
        progress = PointerBuffer.allocateDirect(1);
    }

    @Override
    public void onOpen(Page lastPage) {
    }

    final int boxWidth = 550;
    final int boxHeight = 250;
    Runnable canceledTask, update, finishedTask;
    PointerBuffer progress;

    @Override
    public void layout(MemoryStack stack, NkRect windowDims, IntBuffer titleYEnd) {
        if (prog.isFinished()) {
            if (finishedTask != null) {
                finishedTask.run();
            }
            finishedTask = null;
        }else if(prog.isAborted()){
            if (canceledTask != null) {
                canceledTask.run();
            }
            canceledTask = null;
        } else {
            if (update != null) {
                update.run();
            }
            nk_style_set_font(ctx, Theme.font_12);
            nk_rect((window.getWidth() / 2) - (boxWidth / 2), titleYEnd.get(0),
                    boxWidth, boxHeight, windowDims);

            if (nk_begin(ctx, prog.title, windowDims, NK_WINDOW_BORDER | NK_WINDOW_TITLE)) {
                nk_style_set_font(ctx, Theme.font_10);
                nk_layout_row_static(ctx, 20, 1, 1);
                nk_layout_row_dynamic(ctx, 20, 1);
                nk_label(ctx, prog.getTask(), NK_TEXT_ALIGN_LEFT);

                // Draw progress bar
                nk_layout_row_dynamic(ctx, 40, 1);

                progress.put(0, prog.bar.getIncrements());
                Nuklear.nk_progress(ctx, progress, prog.bar.getMax(), true);

                nk_layout_row_static(ctx, 20, 1, 1);
                nk_layout_row_dynamic(ctx, 40, 1);
                if (nk_button_label(ctx, "CANCEL")) {
                    if (canceledTask != null) {
                        canceledTask.run();
                    }
                    menu.goBack();
                }
            }
            nk_end(ctx);
        }
    }

}
