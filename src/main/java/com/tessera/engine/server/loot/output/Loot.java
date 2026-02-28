package com.tessera.engine.server.loot.output;

public class Loot {
    public String item;
    public float chance;
    public int maxItems = 1;

    public Loot(String item, float chance) {
        this.item = item;
        this.chance = chance;
        this.maxItems = 1;
    }

    public Loot(String item, float chance, int maxItems) {
        this.item = item;
        this.chance = chance;
        this.maxItems = maxItems;
    }
}
