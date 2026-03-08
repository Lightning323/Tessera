package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class ResizableIntBuffer {
    IntBuffer buffer;
    int size;

    public ResizableIntBuffer(int size) {
        buffer = MemoryUtil.memAllocInt(Math.max(100, size));
        this.size = size;
    }

    public int size() {
        return size;
    }

    public void clear() {
        resize(0);
    }

    public void get(int index) {
        buffer.get(index);
    }

    public void put(int index, int value) {
        try {
            buffer.put(index, value);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Index: " + index + " Size: " + size + " Buffer capacity: " + buffer.capacity());
        }
    }


    public void resize(int newSize) {
        int originalSize = buffer.capacity();
        if (originalSize < newSize) {
            int resize;
            if (newSize < originalSize * 2) {
                resize = originalSize * 2;
            } else {
                resize = newSize + 100;
            }
            buffer = MemoryUtil.memRealloc(buffer, resize);
            System.out.println("Buffer resized from " + originalSize + " to " + buffer.capacity());
        }
        this.size = newSize;
    }

    public void add(int value) {
        size++;
        resize(size);
        put(size - 1, value);
    }

    public IntBuffer getBuffer() {
        return buffer;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Buffer: (size: ").append(size).append(") ");
        for (int i = 0; i < size; i++) {
            sb.append(buffer.get(i)).append(" ");
        }
        return sb.toString();
    }
}
