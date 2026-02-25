package com.tessera.engine.server.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.visuals.gameScene.GameScene;
import com.tessera.engine.server.block.BlockRegistry;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.item.ItemStack;
import com.tessera.engine.client.visuals.gameScene.rendering.wireframeBox.Box;
import com.tessera.engine.utils.ErrorHandler;
import com.tessera.engine.utils.math.MathUtils;
import org.joml.Vector3f;

import java.io.IOException;

public class ItemDrop extends Entity {
    public final static int DROP_LIVE_TIME = 9000;
    private static Box box;
    private byte seed;
    private int timeSinceDropped;
    private final Vector3f animatedPos = new Vector3f();
    private final Vector3f playerHeadPos = new Vector3f();
    boolean canGet;
    public static final String JSON_DROPPED_FROM_PLAYER = "d_p";
    public static final String JSON_ITEM_STACK = "d_s";
    public DefinitionData definitionData = new DefinitionData();


    public static class DefinitionData {
        public DefinitionData() {
        }

        public ItemStack stack;
        public boolean droppedFromPlayer;
    }

    public ItemDrop(long uniqueId) {
        super(uniqueId);
        aabb.isSolid = false;
    }


    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);//Always call super!

        canGet = false;
        seed = (byte) (Math.random() * 255);
        timeSinceDropped = 0;
        animatedPos.set(worldPosition.x, worldPosition.y + 0.5f, worldPosition.z);

        if (box == null) {
            box = new Box();
            box.setLineWidth(3);
            box.setColor(0, 0.5f, 1, 1);
            box.setSize(.2f, .2f, .2f);
        }

        if (hasData) {
            try {
                definitionData.droppedFromPlayer = node.get(JSON_DROPPED_FROM_PLAYER).asBoolean();
                if (node.has(JSON_ITEM_STACK)) {
                    JsonNode jsonNode = node.get(JSON_ITEM_STACK);
                    JsonParser parser1 = jsonNode.traverse();
                    parser1.setCodec(parser.getCodec());//This is important
                    definitionData.stack = smileObjectMapper.readValue(parser1, ItemStack.class);
                    //System.out.println("READING STACK: " + definitionData.stack.toString() + " Dropped From Player: " + definitionData.droppedFromPlayer);
                }
                if (definitionData.stack == null) destroy();
            } catch (Exception e) {
                ErrorHandler.log(e, "Error reading item stack");
            }
        }
    }

    public void serializeDefinitionData(JsonGenerator generator) throws IOException {
        generator.writeBooleanField(JSON_DROPPED_FROM_PLAYER, definitionData.droppedFromPlayer);
        generator.writeFieldName(JSON_ITEM_STACK);
        smileObjectMapper.writeValue((SmileGenerator) generator, definitionData.stack);
    }


    private boolean blockIsClear(Block camBlock, int x, int y, int z) {
        Block block = Client.world.getBlock(x, y, z);
        return block.id == BlockRegistry.BLOCK_AIR.id || !block.solid || block == camBlock;
    }

    public void server_update() {
        playerHeadPos.set(Client.userPlayer.aabb.worldPosition).add(Client.userPlayer.aabb.offset).add(0, 0.5f, 0);
        if (ClientWindow.frameCount % 20 != 0) { //Update every 20 frames
            timeSinceDropped++;
            if (timeSinceDropped > DROP_LIVE_TIME) {
                //System.out.println("TIMEOUT, DELETING ITEM DROP");
                destroy();
            } else if (definitionData.stack == null) {
                //System.out.println("STACK IS NULL, DELETING ITEM DROP");
                destroy();
            }
            canGet = (timeSinceDropped > 100 || !definitionData.droppedFromPlayer) && Client.userPlayer.inventory.hasRoomForItem(definitionData.stack);
//            if (client_distToPlayer < 5) {
//                System.out.println("item: " + stack + " DIST TO PLAYER: " + client_distToPlayer + " CAN GET: " + canGet + " TIME SINCE DROPPED: " + timeSinceDropped + " hasRoomForItem: " + GameScene.player.inventory.hasRoomForItem(stack));
//            }
            if (distToPlayer < 2 && canGet) {
                worldPosition.set(playerHeadPos);
            } else {
                //Get the block at this position
                Block camBlock = Client.userPlayer.getBlockAtCameraPos();

                int x = (int) Math.floor(worldPosition.x);
                int y = (int) Math.floor(worldPosition.y);
                int z = (int) Math.floor(worldPosition.z);

                if (Client.world.getBlock(x, y, z).enterDamage > 0.1) {
                    System.out.println("DROPPED ONTO DAMAGING SUBSTANCE, DELETING ITEM DROP");
                    destroy();
                }

                if (!blockIsClear(camBlock, x, y, z)) {
                    if (blockIsClear(camBlock, x, y - 1, z)) {
                        worldPosition.set(x, y - 1, z);
                    } else if (blockIsClear(camBlock, x - 1, y, z)) {
                        worldPosition.set(x - 1, y, z);
                    } else if (blockIsClear(camBlock, x + 1, y, z)) {
                        worldPosition.set(x + 1, y, z);
                    } else if (blockIsClear(camBlock, x, y, z - 1)) {
                        worldPosition.set(x, y, z - 1);
                    } else if (blockIsClear(camBlock, x, y, z + 1)) {
                        worldPosition.set(x, y, z + 1);
                    }
                } else if (blockIsClear(camBlock, x, y + 1, z)) {
                    worldPosition.set(x, y + 1, z);
                }
            }
        }

        if (worldPosition.distance(playerHeadPos) < 0.1 && canGet) {
            Client.userPlayer.acquireItem(definitionData.stack);
            destroy();
        }
    }

    @Override
    public void client_draw() {
        if (box == null) return;

        double sin = Math.sin((ClientWindow.frameCount * 0.1) + ((double) seed / 255));
        float bob = (float) (sin - 0.5) * 0.1f;

        float animationSpeed = 0.1f;
        if (distToPlayer < 2 && canGet) {
            animationSpeed = .35f;
        }

        animatedPos.set(
                (float) MathUtils.curve(animatedPos.x, worldPosition.x, animationSpeed),
                (float) MathUtils.curve(animatedPos.y, worldPosition.y, animationSpeed),
                (float) MathUtils.curve(animatedPos.z, worldPosition.z, animationSpeed));

        box.setPosition(
                animatedPos.x + 0.5f - (box.getSize().x / 2),
                animatedPos.y + 0.5f - (box.getSize().y / 2) + bob,
                animatedPos.z + 0.5f - (box.getSize().z / 2));
        box.getModelMatrix().rotateY((ClientWindow.frameCount * 0.01f) + seed);
        box.draw(GameScene.projection, GameScene.view);
    }
}

