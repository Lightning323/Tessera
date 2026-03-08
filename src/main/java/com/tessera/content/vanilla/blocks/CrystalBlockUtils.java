package com.tessera.content.vanilla.blocks;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;

import static com.tessera.content.vanilla.Blocks.*;

public class CrystalBlockUtils {

    private static Block.RandomTickEvent randomTickEvent(short crystal) {
        return (x, y, z) -> {
            Block above = Client.world.getBlock(x, y - 1, z);
            if (above.isAir() || above.isLiquid()) {
                Main.getServer().setBlock(crystal, x, y - 1, z);
                return true;
            }
            return false;
        };
    }

    public static void init() {
        //Crystals
        Block crystalBlock = Registrys.getBlock("tessera:amethyst_block");
        if (crystalBlock != null) crystalBlock.randomTickEvent = randomTickEvent(BLOCK_AMETHYST_CRYSTAL);

        crystalBlock = Registrys.getBlock("tessera:aquamarine_block");
        if (crystalBlock != null) crystalBlock.randomTickEvent = randomTickEvent(BLOCK_AQUAMARINE_CRYSTAL);

        crystalBlock = Registrys.getBlock("tessera:jade_block");
        if (crystalBlock != null) crystalBlock.randomTickEvent = randomTickEvent(BLOCK_JADE_CRYSTAL);

        crystalBlock = Registrys.getBlock("tessera:ruby_block");
        if (crystalBlock != null) crystalBlock.randomTickEvent = randomTickEvent(BLOCK_RUBY_CRYSTAL);
    }
}
