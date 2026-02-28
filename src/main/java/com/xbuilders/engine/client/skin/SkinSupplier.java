package com.xbuilders.engine.client.skin;


import com.xbuilders.engine.common.players.Player;

@FunctionalInterface
public interface SkinSupplier {
    public Skin get(Player player);
}


