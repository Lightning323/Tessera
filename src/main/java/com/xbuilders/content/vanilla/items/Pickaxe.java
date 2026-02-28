package com.xbuilders.content.vanilla.items;

import com.xbuilders.engine.server.item.Item;
import com.xbuilders.utils.MiscUtils;

public class Pickaxe extends Item {

    public Pickaxe(String material, int durability, float miningSpeedMultiplier) {
        super("xbuilders:"+ material+"_pickaxe", MiscUtils.capitalizeWords(material) + " Pickaxe");
        setIcon("pp\\"+material + "_pickaxe.png");
        maxStackSize = 1;
        tags.add("tool");
        tags.add("pickaxe");
        //tags.add(material);
        maxDurability = durability;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
    }
}
