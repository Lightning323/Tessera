package com.tessera.window.utils;

import java.util.ArrayList;
import java.util.List;

public class ValueSmoother {
    private List<Float> values = new ArrayList<>();

    float average;
    final int maxRecords;

    public ValueSmoother(int maxRecords) {
        this.maxRecords = maxRecords;
    }

    public float getAverage() {
        float sum = 0.0f;
        for (int i = 0; i < values.size(); i++) {
            sum += values.get(i);
        }
        average = sum / values.size();
        return average;
    }

    public void add(float newValue) {
        values.add(newValue);
        if (values.size() > maxRecords) {
            values.remove(0);
        }
    }
}
