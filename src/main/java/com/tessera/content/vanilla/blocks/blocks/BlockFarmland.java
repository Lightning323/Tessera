package com.tessera.content.vanilla.blocks.blocks;

import com.tessera.content.vanilla.Blocks;
import com.tessera.content.vanilla.blocks.RenderType;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;

public class BlockFarmland extends Block {

    public BlockFarmland(short id) {
        super(id, "tessera:farmland",
                new BlockTexture(
                        "farmland",
                        "farmland",
                        "dirt",
                        "dirt",
                        "dirt",
                        "dirt"));

        randomTickEvent = (x, y, z) -> {

            //If we are directly touching water
            if (Client.world.getBlockID(x, y + 1, z) == Blocks.BLOCK_WATER ||
                    //
                    Client.world.getBlockID(x + 1, y, z) == Blocks.BLOCK_WATER ||
                    Client.world.getBlockID(x - 1, y, z) == Blocks.BLOCK_WATER ||
                    Client.world.getBlockID(x, y, z + 1) == Blocks.BLOCK_WATER ||
                    Client.world.getBlockID(x, y, z - 1) == Blocks.BLOCK_WATER) {

                Client.world.setBlock(Blocks.BLOCK_WET_FARMLAND, x, y, z);

                // Server.world.setBlockData(new BlockData(new byte[]{0}), x, y, z); //Set the data to 0 since we are touching water
                return true;
            }

            Block above = Client.world.getBlock(x, y - 1, z);
            //Decay the farmland if there is something solid on top or if there are no crops
            if (above.solid || above.type != RenderType.SPRITE) {
                Client.world.setBlock(Blocks.BLOCK_DIRT, x, y, z);
            }

            return false;
        };
    }
}
