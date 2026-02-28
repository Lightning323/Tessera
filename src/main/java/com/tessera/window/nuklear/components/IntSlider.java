/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.nuklear.components;

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkContext;
import static org.lwjgl.nuklear.Nuklear.nk_property_int;

/**
 *
 * @author zipCoder933
 */
public class IntSlider {

    private final IntBuffer value;
    public int min, max, step, inc_per_pixel;
    public String label;
    NkContext ctx;

    public IntSlider(NkContext ctx, String label) {
        this.label = label;
        min = 0;
        max = 100;
        step = 1;
        inc_per_pixel = 1;
        value = BufferUtils.createIntBuffer(1); //a 1 value int buffer
        value.put(0, 0);
        this.ctx = ctx;
    }

    public void setValue(int val) {
        value.put(0, val);
    }

    public int getValue() {
        return value.get(0);
    }

    public void render() {
        nk_property_int(ctx, label, min, value, max, step, inc_per_pixel);
    }
}
