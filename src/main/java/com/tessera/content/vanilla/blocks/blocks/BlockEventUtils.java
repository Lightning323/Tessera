package com.tessera.content.vanilla.blocks.blocks;


import com.tessera.Main;
import com.tessera.content.vanilla.Blocks;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.entity.Entity;
import com.tessera.engine.server.entity.ItemDrop;
import com.tessera.engine.server.entity.LivingEntity;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.common.players.pipeline.BlockHistory;
import com.tessera.engine.common.world.chunk.Chunk;
import com.tessera.engine.common.world.wcc.WCCi;
import com.tessera.engine.common.math.MathUtils;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BiConsumer;

/**
 * @author zipCoder933
 */
public class BlockEventUtils {

    public static void makeVerticalPairedBlock(short id_top, short id_bottom) {
        Block topBlock = Registrys.getBlock(id_top);
        Block bottomBlock = Registrys.getBlock(id_bottom);

        topBlock.setBlockEvent(false, (x, y, z) -> {
            Main.getServer().setBlock(bottomBlock.id, x, y + 1, z);
        });
        topBlock.removeBlockEvent(false, (x, y, z, history) -> {
            if (Main.getClient().world.getBlock(x, y + 1, z) == bottomBlock) {
                Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, x, y + 1, z);
            }
        });

        bottomBlock.setBlockEvent(false, (x, y, z) -> {
            Main.getServer().setBlock(topBlock.id, x, y - 1, z);
        });
        bottomBlock.removeBlockEvent(false, (x, y, z, history) -> {
            if (Main.getClient().world.getBlock(x, y - 1, z) == topBlock) {
                Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, x, y - 1, z);
            }
        });
    }


    public static void setTNTEvents(Block thisBlock, final int radius, long fuseDelay,
                                    final float maxToughness, final short... exceptions) {
        thisBlock.localChangeEvent(true,
                (BlockHistory history, Vector3i changedPosition, Vector3i thisPosition) -> {
                    if (history.newBlock.id != Blocks.BLOCK_FIRE) return;
                    int setX = thisPosition.x;
                    int setY = thisPosition.y;
                    int setZ = thisPosition.z;

                    Main.getServer().setBlock(Blocks.BLOCK_TNT_ACTIVE, setX, setY, setZ);
                    try {
                        Thread.sleep(fuseDelay);
                        if (Main.getClient().world.getBlockID(setX, setY, setZ) == Blocks.BLOCK_TNT_ACTIVE) {
                            Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, setX, setY, setZ);
                            removeEverythingWithinRadius(radius, new Vector3i(setX, setY, setZ), maxToughness, exceptions);
                            float dist = Client.userPlayer.worldPosition.distance(setX, setY, setZ);
                            if (dist < radius) {
                                Client.userPlayer.addHealth(
                                        MathUtils.mapAndClamp(dist, radius, 0, 0, -10));
                            }


                            //Move the player away
                            Vector3f direction = new Vector3f(
                                    Client.userPlayer.worldPosition.x - setX,
                                    Client.userPlayer.worldPosition.y - setY,
                                    Client.userPlayer.worldPosition.z - setZ).normalize();
                            direction = direction.mul(1f / MathUtils.dist(
                                    Client.userPlayer.worldPosition.x,
                                    Client.userPlayer.worldPosition.y,
                                    Client.userPlayer.worldPosition.z, setX, setY, setZ));
                            direction.mul(50);
                            Client.userPlayer.positionHandler.addVelocity(direction.x, direction.y, direction.z);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });
    }

    public static void radius(final int radius, Vector3i position, BiConsumer<Vector3i, Block> cons) {
        int setX = position.x;
        int setY = position.y;
        int setZ = position.z;
        for (int x = 0 - radius; x < radius; x++) {
            for (int y = 0 - radius; y < radius; y++) {
                for (int z = 0 - radius; z < radius; z++) {
                    if (MathUtils.dist(setX, setY, setZ, setX + x, setY + y, setZ + z) < radius) {
                        Block highlightedBlock = Main.getClient().world.getBlock(setX + x, setY + z, setZ + y);
                        cons.accept(new Vector3i(setX + x, setY + z, setZ + y), highlightedBlock);
                    }
                }
            }
        }
    }

    public static ArrayList<Vector3i> removeEverythingWithinRadius(
            int size, Vector3i position, float maxToughness, short... exceptions) {

        int setX = position.x;
        int setY = position.y;
        int setZ = position.z;

        HashSet<Vector3i> chunks = new HashSet<>();
        ArrayList<Vector3i> explosionList = new ArrayList<>();
        for (int x = 0 - size; x < size; x++) {
            for (int y = 0 - size; y < size; y++) {
                zLoop:
                for (int z = 0 - size; z < size; z++) {
                    if (MathUtils.dist(setX, setY, setZ, setX + x, setY + y, setZ + z) < size) {

                        Block oldBlock = Main.getClient().world.getBlock(setX + x, setY + z, setZ + y);
                        //If the block is too tough, dont do it
                        if (oldBlock.toughness > maxToughness) continue;
                        //If we are trying to delete something on the blacklist, dont do it
                        for (short exception : exceptions) {
                            if (oldBlock.id == exception) continue zLoop;
                        }

                        //Place Item Drop here
                        if (Main.getServer().getGameMode() != GameMode.FREEPLAY) {
                            Item dropItem = Registrys.getItem(oldBlock);
                            if (dropItem != null) Main.getServer().placeItemDrop(
                                    new Vector3f(setX + x, setY + z, setZ + y),
                                    new ItemStack(dropItem), false);
                        }

                        chunks.add(new WCCi().set(setX + x, setY + z, setZ + y).chunk);
                        Main.getServer().setBlock(BlockRegistry.BLOCK_AIR.id, setX + x, setY + z, setZ + y);

                    }
                }
            }
        }

        ArrayList<Entity> entitiesToDelete = new ArrayList<>();
        for (Vector3i cc : chunks) {
            Chunk chunk = Main.getClient().world.chunks.get(cc);
            if (chunk != null) {
                for (Entity e : chunk.entities.list) {
                    if (MathUtils.dist(setX, setY, setZ, e.worldPosition.x, e.worldPosition.y, e.worldPosition.z) < size
                            && !(e instanceof LivingEntity)
                            && !(e instanceof ItemDrop)) {
                        entitiesToDelete.add(e);
                    }
                }
            }
        }
        for (Entity e : entitiesToDelete) {
            e.destroy();
        }
        return explosionList;
    }

}