package com.tessera.engine.common.players.pipeline;

import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.common.world.chunk.BlockData;

public class BlockHistory {
    public Block previousBlock;
    public final Block newBlock;//we guarantee that the new block will never be null

    public BlockData previousBlockData;
    public BlockData newBlockData;

    public boolean updateBlockData = false; //If we should set the block data (if we want to set the block data to null, we set this to true, and set data to null)
    public boolean fromNetwork = false;

    public BlockHistory(Block currentBlock, BlockData data) {
        this.newBlock = currentBlock;
        this.newBlockData = data;
        updateBlockData = true;
        if(newBlock ==null) throw new IllegalArgumentException("NewBlock is null");
    }

    public BlockHistory(short previousBlock, short currentBlock) {
        this.previousBlock = Registrys.getBlock(previousBlock);
        this.newBlock = Registrys.getBlock(currentBlock);
        if(newBlock ==null) throw new IllegalArgumentException("NewBlock is null");
    }

    public BlockHistory(Block previousBlock, Block currentBlock) {
        this.previousBlock = previousBlock;
        this.newBlock = currentBlock;
        if(newBlock ==null) throw new IllegalArgumentException("NewBlock is null");
    }

    public BlockHistory(Block currentBlock) {
        this.newBlock = currentBlock;
        if(newBlock ==null) throw new IllegalArgumentException("NewBlock is null");
    }


    public String toString() {
        return "BlockHistory{" +
                "previousBlock=" + previousBlock +
                ", currentBlock=" + newBlock +
                ", data=" + newBlockData +
                ", updateBlockData=" + updateBlockData +
                '}';
    }
}
