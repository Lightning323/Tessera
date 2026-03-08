package com.tessera.engine.client.visuals.gameScene.rendering.chunk.meshers.bufferSet;

public class ResizableIntArray {
    int[] array;
    int size;

    public ResizableIntArray(int size) {
        array = new int[Math.max(100, size)];
        this.size = size;
    }

    public int size() {
        return size;
    }

    public void clear() {
        resize(0);
    }

    public int get(int index) {
        return array[index];
    }

    public void put(int index, int value) {
        try {
            array[index] = value;//buffer.put(index, value);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Index: " + index + " Size: " + size + " Buffer capacity: " + array.length);
        }
    }


    public void resize(int newSize) {
        int originalSize = array.length;
        if (originalSize < newSize) {
            int resize;
            if (newSize < originalSize * 2) {
                resize = originalSize * 2;
            } else {
                resize = newSize + 100;
            }
            int[] newArray = new int[resize];
            System.arraycopy(array, 0, newArray, 0, originalSize);
            array = newArray;
            System.out.println("Array resized from " + originalSize + " to " + array.length);
        }
        this.size = newSize;
    }

    public void add(int value) {
        size++;
        resize(size);
        put(size - 1, value);
    }

    public int[] getArray() {
        return array;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Buffer: (size: ").append(size).append(") ");
        for (int i = 0; i < size; i++) {
            sb.append(get(i)).append(" ");
        }
        return sb.toString();
    }
}
