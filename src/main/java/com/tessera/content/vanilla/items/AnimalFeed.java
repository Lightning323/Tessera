package com.tessera.content.vanilla.items;

import com.tessera.Main;
import com.tessera.engine.server.entity.LivingEntity;
import com.tessera.engine.server.item.Item;

public class AnimalFeed extends Item {
    public AnimalFeed() {
        super("tessera:animal_feed", "Animal Feed");
        setIcon("animal_feed.png");
        tags.add("tool");

        OnClickEvent event = (ray, itemStack) -> {
            System.out.println("Animal Feed clicked");
            if (ray.getEntity() != null && ray.getEntity() instanceof LivingEntity animal) {
                itemStack.stackSize--;
                animal.tamed = true;
                Main.getClient().consoleOut("You have tamed an animal!");
                animal.markAsModifiedByUser();
            }
            return true;
        };

        createClickEvent = event;
        destroyClickEvent = event;
    }
}
