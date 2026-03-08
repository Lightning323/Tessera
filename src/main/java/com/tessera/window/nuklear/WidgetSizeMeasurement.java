/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.nuklear;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

/**
 *
 * @author zipCoder933
 */
public class WidgetSizeMeasurement {

    public float width;
    public float height;
    private int calibrations;
    private final int maxCalibrations = 1;

    public WidgetSizeMeasurement(float initialValue) {
        width = initialValue;
        height = initialValue;
        calibrations = 0;
    }

    public boolean isCalibrated() {
        return calibrations >= maxCalibrations;
    }

    public void recalibrate() {
        calibrations = 0;
    }

    public void measure(NkContext ctx, MemoryStack stack) {
        if (calibrations < maxCalibrations) {
            calibrations++;
            NkRect bounds = NkRect.malloc(stack);
            NkRect nk_widget_bounds = Nuklear.nk_widget_bounds(ctx, bounds);

            width = nk_widget_bounds.w();
            height = nk_widget_bounds.h();
        }
    }

//    Another way to measure width:    
//// Begin a temporary layout space to measure the bounds
//                Nuklear.nk_layout_space_begin(ctx, Nuklear.NK_STATIC, 0, 0);
//
//                nk_layout_row_dynamic(ctx, 52, 11);
//                Nuklear.nk_button_image(ctx, iconList[0]);
//// Get the bounds after measuring
//                NkRect bounds = NkRect.malloc(stack);
//                Nuklear.nk_widget_bounds(ctx, bounds);
//                System.out.println("Width: " + bounds.w());
//// End the temporary layout space
//                Nuklear.nk_layout_space_end(ctx)
}
