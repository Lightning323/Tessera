package com.tessera.content.vanilla.propagation;

import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.builtinMechanics.liquid.LiquidPropagationTask;
import com.tessera.content.vanilla.Blocks;


public class WaterPropagation extends LiquidPropagationTask {
    public WaterPropagation() {
        super(Registrys.getBlock(Blocks.BLOCK_WATER), 200);
    }


//    public boolean isPenetrable(Block block) {
//        return block.isAir() || (
//                !block.solid
//                        && block.type == RenderType.SPRITE
//                        && block.toughness < 0.5f);
//    }
}