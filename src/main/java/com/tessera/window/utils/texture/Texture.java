/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.texture;

import java.nio.ByteBuffer;

/**
 *
 * @author Patron
 */
public class Texture {

    public Texture(int id, int w, int h) {
        this.id = id;
        this.width = w;
        this.height = h;
    }

    public Texture(ByteBuffer buffer, int id, int w, int h) {
        this.buffer = buffer;
        this.id = id;
        this.width = w;
        this.height = h;
    }

    public ByteBuffer buffer;
    public final int id, width, height;

    public String toString() {
        return "Texture: " + id + " " + width + "x" + height;
    }
}
