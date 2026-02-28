package com.xbuilders.content.vanilla.items;

import com.xbuilders.engine.server.item.Item;
import com.xbuilders.utils.MiscUtils;

public class Axe extends Item {

    public Axe(String material, int durability, float miningSpeedMultiplier) {
        super("xbuilders:"+ material+"_axe", MiscUtils.capitalizeWords(material) + " Axe");
        setIcon("pp\\"+material + "_axe.png");
        maxStackSize = 1;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        tags.add("tool");
        tags.add("axe");
        //tags.add(material);
        maxDurability = durability;
    }
}
