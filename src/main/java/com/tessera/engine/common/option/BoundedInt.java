package com.tessera.engine.common.option;

public class BoundedInt {

    public int value;
    public int min;
    public int max;

    public BoundedInt(int value) {
        this.value = value;
    }

    public BoundedInt(int value, int min, int max) {
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public void setBounds(int min, int max) {
        this.min = min;
        this.max = max;
    }

}
