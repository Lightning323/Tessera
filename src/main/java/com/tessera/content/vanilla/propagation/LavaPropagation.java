package com.tessera.content.vanilla.propagation;

import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.builtinMechanics.liquid.LiquidPropagationTask;
import com.tessera.content.vanilla.Blocks;

public class LavaPropagation extends LiquidPropagationTask {

    public LavaPropagation() {
        super(Registrys.getBlock(Blocks.BLOCK_LAVA), 900);
    }

}
