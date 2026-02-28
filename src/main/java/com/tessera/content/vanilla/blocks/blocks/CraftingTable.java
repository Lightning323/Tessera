package com.tessera.content.vanilla.blocks.blocks;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.common.world.chunk.BlockData;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;

public class CraftingTable extends Block {
    public CraftingTable(short id) {
        super(id, "tessera:crafting_table",new BlockTexture(
                "crafting_table_top.png",
                "crafting_table_top.png",
                "crafting_table_side.png",
                "crafting_table_side.png",
                "crafting_table_side.png",
                "crafting_table_front.png"));
        easierMiningTool_tag = "axe";

        clickEvent(false, (x, y, z) -> {
            BlockData data = Client.world.getBlockData(x, y, z);
            if (data == null) {
                data = new BlockData(0);
                Client.world.setBlockData(data, x, y, z);
            }
            WCCi wcc = new WCCi().set(x, y, z);
            Chunk chunk = Client.world.getChunk(wcc.chunk);
            Main.game.craftingUI.setOpen(true);
        });
    }
}
