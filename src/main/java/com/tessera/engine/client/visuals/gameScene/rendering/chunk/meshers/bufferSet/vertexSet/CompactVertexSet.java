/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet.vertexSet;

import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.client.visuals.gameScene.rendering.VertexSet;
import com.tessera.engine.client.visuals.gameScene.rendering.chunk.mesh.CompactMesh;
import com.tessera.engine.common.world.chunk.Chunk;
import org.joml.Vector3f;

/**
 * @author zipCoder933
 */
public abstract class CompactVertexSet extends VertexSet<CompactMesh> {
    //<editor-fold defaultstate="collapsed" desc="Vertex packing">


    /**
     * The vertex are packed and unpacked as follows:
     * We add 1 to value, before converting. This changes the range FROM (0 to 31) TO (-1 to 30)
     * In order to allow the vertex positions to go above 32, we subtract from maxMult12bits
     * maxMult12bits = maxMult12bits - X;
     * <p>
     * float val = 5.234f; //(our vertex)
     * int converted = (int) ((val + 1) * maxMult12bits);
     * float unconverted = ((float) (converted) / maxMult12bits) - 1;
     */
    public final static float maxMult10bits = (float) ((Math.pow(2, 10) / (Chunk.WIDTH)) - 1);//Note that ANYTHING that goes outside this range will cause mesh artifacts
    public final static float maxMult12bits = (float) ((Math.pow(2, 12) / (Chunk.WIDTH)) - 6);


    public final static int MAX_BLOCK_ANIMATION_LENGTH = (int) (Math.pow(2,5) - 1); //Block animation can only have up to 31 frames
    public static final int VECTOR_ELEMENTS = 3;

    public static int packFirstInt(float vertX, float vertY, byte normals, byte animation) {
        // Scale and convert vertX and vertY to 12-bit integers
        int a = (int) ((vertX + 1) * maxMult12bits); // A and b are 12 bits (0-4095)
        int b = (int) ((vertY + 1) * maxMult12bits);

        // Pack the values into a single integer
        // VertX is shifted to the left by 20 bits, VertY by 8 bits, Normals by 5 bits, and Animation by 0 bits
        //Animation should allow up to 32 animation frames
        // Normals take up 3 bits, and animation takes up 5 bits
        return (a << 20) | (b << 8) | ((normals & 0b111) << 5) | (animation & 0b11111);
    }

    public static int packSecondInt(float vertZ, float u, float v) {
        int a = (int) ((vertZ + 1) * maxMult12bits); //12 bits
        int b = (int) (u * maxMult10bits); //10 bits
        int c = (int) (v * maxMult10bits); //10 bits

        //a = 2^12 (0-4095), b and c = 2^10 (0-1023)
        return (a << 20) | (b << 10) | c;
    }

    public static int packThirdInt(int texture, byte light) {
        // First 16 bits for texture
        int textureBits = (texture & 0xFFFF) << 16;
        //Remaining 16 bits for light
        int lightBits = (light & 0xFFFF);

        return textureBits | lightBits;
    }

    /**
     * Packs three integer coordinates into a single integer.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     * @return The packed integer.
     */
    public static int packCoords(int x, int y, int z) {
        // Each value is reduced to 10 bits, this integer is only wasting 2 bits
        return z | (x << 10) | (y << 20);
    }

    /**
     * Unpacks an integer into a Vector3f representing coordinates.
     *
     * @param vec    The Vector3f to store the unpacked coordinates.
     * @param packed The packed integer containing x, y, and z coordinates.
     */
    public static void unpackCoords(Vector3f vec, int packed) {
        float y = (packed >> 20);
        float x = (packed >> 10) & 0x3FF;  // Use bitmask to getVert 10 bits
        float z = packed & 0x3FF;  // Use bitmask to getVert 10 bits
        vec.set(x, y, z);
    }

    //</editor-fold>

    public CompactVertexSet() {
    }

    public abstract int size();

    /**
     * Free any buffers and make it ready for the next round
     */
    public abstract void reset();

    //    public abstract void makeVertexSet();
    public abstract void sendToMesh(CompactMesh mesh);

    public abstract void vertex(int layer, int a, int b, int c);

    public void vertex(float x, float y, float z,
                       float uvX, float uvY, byte normal,
                       BlockTexture.FaceTexture texture, byte light) {
        vertex(0, packFirstInt(x, y, normal, texture.getAnimationFrames()),
                packSecondInt(z, uvX, uvY),
                packThirdInt(texture.zLayer, light));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY, int normal,
                       BlockTexture.FaceTexture texture, byte light) {
        vertex(0, packFirstInt(x, y, (byte) normal, texture.getAnimationFrames()),
                packSecondInt(z, uvX, uvY),
                packThirdInt(texture.zLayer, light));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY,
                       BlockTexture.FaceTexture texture, byte light) {
        vertex(0, packFirstInt(x, y, (byte) 0, texture.getAnimationFrames()),
                packSecondInt(z, uvX, uvY),
                packThirdInt(texture.zLayer, light));
    }

    public void vertex(float x, float y, float z,
                       float uvX, float uvY, byte normal,
                       int texture, byte animationLength, byte light) {
        vertex(0, packFirstInt(x, y, normal, animationLength),
                packSecondInt(z, uvX, uvY),
                packThirdInt(texture, light));
    }

}
