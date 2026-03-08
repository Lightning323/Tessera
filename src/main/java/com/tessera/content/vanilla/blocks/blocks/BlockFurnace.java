package com.tessera.content.vanilla.blocks.blocks;

import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;

public class BlockFurnace extends Block {
    public BlockFurnace(short id) {
        super(id, "tessera:furnace", new BlockTexture(
                "furnace_top.png",
                "furnace_top.png",
                "furnace_side.png",
                "furnace_side.png",
                "furnace_side.png",
                "furnace_front.png"));
    }
}
