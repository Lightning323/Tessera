package com.tessera.engine.client.skin;


import com.tessera.engine.common.players.Player;

@FunctionalInterface
public interface SkinSupplier {
    public Skin get(Player player);
}


