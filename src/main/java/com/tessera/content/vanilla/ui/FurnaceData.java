package com.tessera.content.vanilla.ui;

import com.tessera.engine.server.item.ItemStack;

public class FurnaceData {
    public ItemStack inputGrid, fuelGrid, outputGrid;
    public long lastSmeltTime = 0;
    public float fuel = 0;


    public FurnaceData() {
    }

    public String toString() {
        return "inputGrid: " + inputGrid + " fuelGrid: " + fuelGrid + " outputGrid: " + outputGrid + " lastSmeltTime: " + lastSmeltTime;
    }

}
