package com.tessera.engine.common.option;


import com.google.gson.annotations.SerializedName;

public class BoundedFloat {

    @SerializedName("value")
    public float value;

    @SerializedName("min")
    public float min;

    @SerializedName("max")
    public float max;

    public BoundedFloat() {
    }

    public BoundedFloat(float value) {
        this.value = value;
    }

    public BoundedFloat(float value, float min, float max) {
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public void setBounds(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public void clamp() {
        if (value < min) value = min;
        if (value > max) value = max;
    }
}
