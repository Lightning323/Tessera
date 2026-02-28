package com.xbuilders.utils;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BooleanBuffer {
    private final ByteBuffer byteBuffer;
    private final int bufferSize;

    public BooleanBuffer(int size, MemoryStack stack) {
        this.byteBuffer = stack.malloc((size + 7) / 8);
        this.bufferSize = size;
    }

    public void put(int index, boolean value) {
        if (index < 0 || index >= capacity()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + capacity());
        }

        int byteIndex = index / 8;
        int bitIndex = index % 8;

        if (value) {
            byteBuffer.put(byteIndex, (byte) (byteBuffer.get(byteIndex) | (1 << bitIndex)));
        } else {
            byteBuffer.put(byteIndex, (byte) (byteBuffer.get(byteIndex) & ~(1 << bitIndex)));
        }
    }

    public int capacity() {
        //We want to count number of bits in the buffer
        return bufferSize;
    }

    public boolean get(int index) {
        if (index < 0 || index >= capacity()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + capacity());
        }

        int byteIndex = index / 8;
        int bitIndex = index % 8;

        return (byteBuffer.get(byteIndex) & (1 << bitIndex)) != 0;
    }

    public long getAddress() {
        return MemoryUtil.memAddress(byteBuffer);
    }

    public void free() {
        MemoryUtil.memFree(byteBuffer);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" ");
        sb.append("capacity=").append(capacity()).append(" bits=");
        for (int i = 0; i < capacity(); i++) {
            sb.append(get(i) ? "1" : "0");
        }
        return sb.toString();
    }
}
