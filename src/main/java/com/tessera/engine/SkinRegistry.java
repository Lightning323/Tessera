package com.tessera.engine;

import com.tessera.content.vanilla.skins.FoxSkin;
import com.tessera.engine.client.skin.SkinSupplier;

import java.util.HashMap;

public class SkinRegistry {
    public final HashMap<Integer, SkinSupplier> availableSkins = new HashMap<>();

    public int size() {
        return availableSkins.size();
    }

    public SkinSupplier get(int id) {
        return availableSkins.get(id);
    }

    public SkinRegistry() {
        /**
         * Skins
         */
        availableSkins.put(0, (p) -> new FoxSkin(p, "red"));
        availableSkins.put(1, (p) -> new FoxSkin(p, "yellow"));
        availableSkins.put(2, (p) -> new FoxSkin(p, "blue"));
        availableSkins.put(3, (p) -> new FoxSkin(p, "green"));
        availableSkins.put(4, (p) -> new FoxSkin(p, "magenta"));
    }
}
