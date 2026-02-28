package com.tessera.content.vanilla.items;

import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.item.Item;

public class Camera extends Item {

    public Camera() {
        super("tessera:camera", "Camera");
        setIcon("camera.png");
        tags.add("tool");
        this.createClickEvent = (ray,stack) -> {
            ClientWindow.takeScreenshot();
            return true;
        };
    }

}