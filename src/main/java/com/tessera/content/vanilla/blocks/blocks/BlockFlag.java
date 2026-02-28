package com.tessera.content.vanilla.blocks.blocks;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.construction.BlockTexture;
import com.tessera.engine.server.item.StorageSpace;
import com.tessera.engine.common.world.chunk.BlockData;
import org.joml.Vector3f;

import java.io.IOException;

import static com.tessera.Main.LOGGER;

public class BlockFlag extends Block {
    public BlockFlag(short id) {
        super(id, "tessera:flag_block",
                new BlockTexture(
                        "symbols/flag top",
                        "symbols/flag top",
                        "symbols/flag",
                        "symbols/flag",
                        "symbols/flag",
                        "symbols/flag"));

        easierMiningTool_tag = null;
        solid = true;
        opaque = true;
        toughness = 0.05f;
        torchlightStartingValue = 15;

        setBlockEvent(false, (x, y, z) -> {
            try {
                byte[] bytes = Client.userPlayer.inventory.writeToJson();
                BlockData data = new BlockData(bytes);
                System.out.println("Flag placed " + new String(bytes));
                Main.getServer().setBlockData(data, x, y, z);
                Client.userPlayer.inventory.clear();
            } catch (IOException e) {
                LOGGER.info("Error", e);
            }
        });

        removeBlockEvent(false, (x, y, z, hist) -> {
            try {
                BlockData data = hist.previousBlockData;
                if (data == null || data.toByteArray() == null || data.toByteArray().length == 0) {
                    System.out.println("Flag is empty");
                    return;
                }
                System.out.println("Flag removed " + new String(data.toByteArray()));
                StorageSpace storage = new StorageSpace(Client.userPlayer.inventory.size());
                storage.loadFromJson(data.toByteArray());


                //Add items to inventory.
                for (int i = 0; i < storage.size(); i++) {
                    if (storage.get(i) != null) {
                        if (Client.userPlayer.inventory.acquireItem(storage.get(i)) == -1) {
                            System.out.println("\tDropped " + storage.get(i));
                            //Drop item if inventory is full.
                            Main.getServer().placeItemDrop(new Vector3f(x, y, z), storage.get(i), false);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.info("Error", e);
            }
        });
    }

}
