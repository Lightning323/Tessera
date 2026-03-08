/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.common.progress;

/**
 *
 * @author zipCoder933
 */
public class ProgressBar {

    /**
     * @param max the max to set
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * @return the max
     */
    public int getMax() {
        return max;
    }

    /**
     * @return the value
     */
    public int getIncrements() {
        return increments;
    }

    private double progress;
    private int increments;
    private int max;

    public boolean isComplete() {
        return getProgress() >= 0.9999;
    }

    public synchronized void setProgress(int increments, int max) {
        this.increments = increments;
        this.setMax(max);
        progress = (double) increments / max;
    }

    public synchronized void setProgress(int increments) {
        this.increments = increments;
        progress = (double) increments / max;
    }

    /**
     * @return the progress
     */
    public double getProgress() {
        return progress;
    }
}
