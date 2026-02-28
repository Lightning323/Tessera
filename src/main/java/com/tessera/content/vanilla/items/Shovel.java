package com.tessera.content.vanilla.items;

import com.tessera.engine.server.item.Item;
import com.tessera.utils.MiscUtils;

public class Shovel extends Item {

    public Shovel(String material, int durability, float miningSpeedMultiplier) {
        super("tessera:" + material + "_shovel", MiscUtils.capitalizeWords(material) + " Shovel");
        setIcon("pp\\" + material + "_shovel.png");
        maxStackSize = 1;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        tags.add("shovel");
        tags.add("tool");
        //tags.add(material);
        maxDurability = durability;
    }
}
