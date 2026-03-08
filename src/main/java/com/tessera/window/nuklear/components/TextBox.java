/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.nuklear.components;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkPluginFilter;
import org.lwjgl.nuklear.NkPluginFilterI;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * @author Patron
 */
public class TextBox {

    /**
     * @param onSelectEvent the onSelectEvent to set
     */
    public void setOnSelectEvent(Runnable onSelectEvent) {
        this.onSelectEvent = onSelectEvent;
    }

    /**
     * @param onChangeEvent the onChangeEvent to set
     */
    public void setOnChangeEvent(Runnable onChangeEvent) {
        this.onChangeEvent = onChangeEvent;
    }

    protected ByteBuffer buffer; // a buffer (list in memory) of bytes
    protected IntBuffer len; //a buffer (list in memory) of integers
    public NkPluginFilterI filter = null; // Restrict what the user can typeReference
    private Runnable onSelectEvent, onChangeEvent = null;
    boolean focused = false;

    /**
     * @param maxLength the maximum number of characters allowed
     */
    public TextBox(int maxLength) {
        buffer = BufferUtils.createByteBuffer(maxLength + 1); // Adjust the size as needed
        len = BufferUtils.createIntBuffer(1); // BufferUtils from LWJGL
        filter = NkPluginFilter.create(Nuklear::nnk_filter_ascii);
    }

    public void setValueAsBytes(byte[] bytes) {
        int length = Math.min(buffer.capacity() - 1, bytes.length);
        for (int i = 0; i < length; i++) {
            buffer.put(i, bytes[i]);
        }
        // Set the length in len buffer
        len.put(0, length);
    }

    public byte[] getValueAsBytes() {
        byte[] bytes = new byte[len.get(0)];
        buffer.mark();
        buffer.get(bytes, 0, len.get(0));
        buffer.reset();
        return bytes;
    }

    public String getValueAsString() {
        return new String(getValueAsBytes(), StandardCharsets.UTF_8);
    }

    public void setValueAsString(String string) {
        setValueAsBytes(string.getBytes()); //NOTE: calls the instantiated method if it is overriden
    }

    public void render(NkContext ctx) {
//        if (Nuklear.nk_widget_is_mouse_clicked(ctx, NK_BUTTON_LEFT)) {
//            Nuklear.nk_edit_focus(ctx, NK_EDIT_DEFAULT);
//        }
        int action = nk_edit_string(ctx,
                Nuklear.NK_EDIT_SIMPLE | Nuklear.NK_EDIT_SIG_ENTER
                        | //Edit flags (options)
                        NK_EDIT_FIELD | NK_EDIT_GOTO_END_ON_ACTIVATE,
                buffer, len, buffer.capacity(),
                filter);//Filter

//· NK_EDIT_ACTIVE - The text field is currently focused
//· NK_EDIT_INACTIVE - The text field is not focused
//· NK_EDIT_ACTIVATED - The text field has just received focus
//· NK_EDIT_DEACTIVATED - The text field has just lost focus
//· NK_EDIT_COMMITED - The user pressed Enter to submit the text in the field
        if ((action & (Nuklear.NK_EDIT_COMMITED | Nuklear.NK_EDIT_DEACTIVATED)) > 0) { //Box has changed
            onChangeEvent();
            if (onChangeEvent != null) {
                onChangeEvent.run();
            }
        } else if ((action & (Nuklear.NK_EDIT_ACTIVATED)) > 0) { //Box has been focused
            if (onSelectEvent != null) {
                onSelectEvent.run();
            }
            focused = true;
        }else if ((action & Nuklear.NK_EDIT_INACTIVE) > 0) { //Box has been unfocused
            focused = false;
        }
    }

    public boolean isFocused(){
        return focused;
    }

    public void deconstruct() {
        //used when closing
        MemoryUtil.memFree(buffer);
        MemoryUtil.memFree(len);
    }

    protected void onChangeEvent() {
    }
}
