/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.world.chunk;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zipCoder933
 */
public class BlockData {

    //The buffer must always be private, we want the flexibility of changing the data structure
    //    private final ByteBuffer buff;
    private byte[] buff;

    public BlockData(int size) {
//        buff = MemoryUtil.memAlloc(size);
        buff = new byte[size];
    }

    public BlockData(BlockData blockData) {
        //Create a copy of the data
        buff = new byte[blockData.toByteArray().length];
        System.arraycopy(blockData.toByteArray(), 0, buff, 0, blockData.toByteArray().length);
    }

    public BlockData(byte[] bytes) {
//        buff = ByteBuffer.allocate(bytes.length);
//        buff.put(bytes);
        buff = bytes;
    }

    public BlockData(List<Byte> bytes) {
//        buff = ByteBuffer.allocate(bytes.size());
        buff = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
//            buff.put(i, bytes.get(i));
            buff[i] = bytes.get(i);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; //if they are the same reference
        if (!(o instanceof BlockData blockData)) return false; //if they are not the same typeReference
        return Arrays.equals(buff, blockData.buff); //if they have the same data
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(buff);
    }

    /**
     * The size of block data is designed to be a fixed value. If another
     * block replaces this block data, it must be replaced with a new
     * blockData
     * <p>
     * Unlike dynamic data structures (e.g., ArrayList), ByteBuffer doesn’t inherently support resizing. Once you allocate a ByteBuffer, its capacity remains fixed.
     * If you need to resize a ByteBuffer, you typically create a new one with a larger capacity and copy the data from the old buffer to the new one.
     * <p>
     */
//    public void setSize(int size) {
//        buff = Arrays.copyOf(buff, size);

    /// /        MemoryUtil.memRealloc(buff, size);
//    }
    public byte get(int i) {
//        return buff.get(i);
        if(buff.length <= i) {
            return 0;
        }
        return buff[i];
    }

    public void set(int i, byte val) {
//        buff.put(i, val);
        buff[i] = val;
    }

    public int size() {
//        return buff.capacity();
        return buff.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("[");
        for (int i = 0; i < buff.length; i++) {
            sb.append(buff[i]);
            if (i < buff.length - 1) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    public void write(final OutputStream out) throws IOException {
        out.write(buff);
    }

    public byte[] toByteArray() {
        return Objects.requireNonNull(buff);
    }

    public void setByteArray(byte[] byteArray) {
        buff = Objects.requireNonNull(byteArray);
    }
}
