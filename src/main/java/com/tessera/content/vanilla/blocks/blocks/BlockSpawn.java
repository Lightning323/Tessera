package com.tessera.content.vanilla.blocks.blocks;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.server.entity.LivingEntity;

public class BlockSpawn extends Block {
    public BlockSpawn(short id) {
        super(id, "tessera:spawn_block",
                new BlockTexture(
                        "symbols/destination top",
                        "symbols/destination top",
                        "symbols/destination",
                        "symbols/destination",
                        "symbols/destination",
                        "symbols/destination"));

        clickEvent(false, (x, y, z) -> {
            Client.userPlayer.setSpawnPoint(x, y, z);

            if (Client.userPlayer.worldPosition.distance(x, y, z) < 3) {
                if (isDarkOutside()) {
                    //Check for nearby hostile mobs
                    for (Entity e : Client.world.allEntities.values()) {
                        if (e instanceof LivingEntity) {
                            LivingEntity le = (LivingEntity) e;
                            if (le.isHostile() && le.worldPosition.distance(x, y, z) < 20) {
                                Main.getClient().consoleOut("You cannot sleep here, there are enemies nearby!");
                                return;
                            }
                        }
                    }
                    Main.getClient().consoleOut("Time set to day");
                    Main.getServer().setTimeOfDay(0);
                } else {
                    Main.getClient().consoleOut("You can only sleep at night");
                }
            } else Main.getClient().consoleOut("You are too far from spawn block to sleep");
        });

        torchlightStartingValue = 10;
        solid = false;
        opaque = false;
    }


    public boolean isDarkOutside() {
        return Main.getServer().getTimeOfDay() > 0.36
                && Main.getServer().getTimeOfDay() < 0.62;
    }

}
