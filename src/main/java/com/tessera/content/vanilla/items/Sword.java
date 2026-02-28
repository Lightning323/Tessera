package com.tessera.content.vanilla.items;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.entity.LivingEntity;
import com.tessera.engine.server.item.Item;
import com.tessera.utils.MiscUtils;

public class Sword extends Item {

    float attackDamage = 0;

    public Sword(String material, int durability, float damage) {
        super("tessera:" + material + "_sword", MiscUtils.capitalizeWords(material) + " Sword");
        setIcon(material + "_sword.png");
        maxStackSize = 1;
        miningSpeedMultiplier = 0.1f;
        tags.add("tool");
        tags.add("sword");
        //tags.add(material);
        maxDurability = durability;
        this.attackDamage = damage;
        this.miningSpeedMultiplier = 1.5f;
        destroyClickEvent = (ray, itemStack) -> {
            if (ray.getEntity() != null && ray.getEntity() instanceof LivingEntity) {
                LivingEntity entity = (LivingEntity) ray.getEntity();
                if (entity.isHostile() || Client.DEV_MODE) {//only attack hostile allEntities
                    entity.damage(attackDamage);
                } else entity.damage(attackDamage / 3); //attack non-hostile allEntities, but less damage
                Main.getClient().window.gameScene.client_hudText(Math.max(0, Math.round(entity.health)) + " / " + entity.maxHealth);
                return true;
            }
            return false;
        };
    }


}