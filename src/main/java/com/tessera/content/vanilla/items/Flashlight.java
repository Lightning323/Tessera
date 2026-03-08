package com.tessera.content.vanilla.items;

import com.tessera.engine.client.Client;
import com.tessera.engine.server.item.Item;

public class Flashlight extends Item {

    final float distance = 20f;
    boolean on = false;

    public Flashlight() {
        super("tessera:flashlight", "Flashlight");
        setIcon("flashlight.png");
        tags.add("tool");
        maxStackSize = 1;
        this.createClickEvent = (ray, stack) -> {
            Client.userPlayer.setFlashlight(on ? 0 : distance);
            on = !on;
            return true;
        };
    }


}