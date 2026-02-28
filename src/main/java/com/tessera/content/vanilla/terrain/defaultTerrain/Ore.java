package com.tessera.content.vanilla.terrain.defaultTerrain;

public class Ore {
    public int minYLevel = Integer.MIN_VALUE;
    public int maxYLevel = Integer.MAX_VALUE;

    public final String name;
    public float common; //0-1
    public short block;
    public float clusterPurity = 0.45f; //0-1
    public float amtExposedToAir = 1;  //0-1

    public Ore(String name, float common, short block) {
        this.name = name;
        this.common = common;
        this.block = block;
    }

    public String toString() {
        return "Ore "+name;
    }

}
