package com.tessera.content.vanilla.blocks.blocks;

import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;

public class BlockBarrel extends Block {
    public BlockBarrel(int id, String name) {
        super(id, name, new BlockTexture("barrel_top.png", "barrel_top.png", "barrel_side.png"));
    }


}
