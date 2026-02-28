package com.tessera.content.vanilla.entities.animal.quadPedal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.Main;
import com.tessera.content.vanilla.Blocks;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.client.Client;
import com.tessera.engine.server.GameMode;
import com.tessera.engine.server.block.Block;
import com.tessera.engine.server.entity.EntitySupplier;
import com.tessera.engine.common.players.Player;

import java.io.IOException;


public class Dog extends QuadPedalLandAnimal {
    public Dog(long uniqueIdentifier, ClientWindow window) {
        super(uniqueIdentifier, window, false);
        isHostile = true;
    }

    static QuadPedalLandAnimal_StaticData staticData;

    @Override
    public QuadPedalLandAnimal_StaticData getStaticData() throws IOException {
        if (staticData == null) {
            staticData = new QuadPedalLandAnimal_StaticData(
                    "/assets/tessera/entities/animal\\dog\\large\\body.obj",
                    "/assets/tessera/entities/animal\\dog\\large\\sitting.obj",
                    "/assets/tessera/entities/animal\\dog\\large\\leg.obj",
                    null,
                    "/assets/tessera/entities/animal\\dog\\textures");
        }
        return staticData;
    }

    @Override
    public void initSupplier(EntitySupplier entitySupplier) {
        super.initSupplier(entitySupplier);
        entitySupplier.spawnLikelyhood = () -> {
            switch (Main.getServer().getDifficulty()) {
                case EASY -> {
                    return 0.0f;
                }
                case NORMAL -> {
                    return 0.000018f;
                }
                default -> {
                    return 0.0005f;
                }
            }
        };
        entitySupplier.spawnCondition = (x, y, z) -> {
            if (Main.getServer().getLightLevel(x, y, z) > 6) return false; //If it's too bright, don't spawn

            Block floor = Client.world.getBlock(x, (int) (y + Math.ceil(aabb.box.getYLength())), z);
            if (floor.solid && Client.world.getBlockID(x, y, z) == Blocks.BLOCK_AIR) return true;
            return false;
        };
        //TODO: There is no way to club dogs, so we have to tame them, however when they are tamed they dont despawn, so we have to despawn ALL dogs
        entitySupplier.despawnCondition = (e) -> {
            return true;
        };
        entitySupplier.isAutonomous = true;
    }

    Player playerWithLowestDist = Client.userPlayer;
    long lastPlayerCheckTime;


    public void animal_move() {
        if (tamed || Main.getServer().getGameMode() != GameMode.ADVENTURE || health <= 0) super.animal_move();
        else {
//            if (Server.localServer.isPlayingMultiplayer()) {
//                if (playerWithLowestDist == null || System.currentTimeMillis() - lastPlayerCheckTime > 1000) {
//                    lastPlayerCheckTime = System.currentTimeMillis();
//                    float lowestDist = Float.MAX_VALUE;
//                    for (Player player : Server.localServer.clients) {
//                        float dist = player.worldPosition.distance(worldPosition);
//                        if (dist < lowestDist) {
//                            lowestDist = dist;
//                            playerWithLowestDist = player;
//                        }
//                    }
//                    System.out.println("Lowest dist: " + lowestDist + " Player: " + playerWithLowestDist);
//                }
//            } else
            playerWithLowestDist = Client.userPlayer;

            //If the player is too close, the dog will start to attack
            if (distToPlayer < 3) {
                Client.userPlayer.addHealth(-0.1f);
            }

            if (playerWithLowestDist != null) {
                setRotationYDeg((float) Math.toDegrees(getYDirectionToPlayer(playerWithLowestDist)) + random.noise(2f, -3, 3));
                if (worldPosition.distance(playerWithLowestDist.worldPosition) > 4) goForward(0.17f, jumpOverBlocks);
                else goForward(0.1f, jumpOverBlocks);
            }
        }
    }

    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);

        setActivity(0.7f);
        //Z is the direciton of the animal
        legXSpacing = 0.30f * SCALE;
        legZSpacing = 0.82f * SCALE;
        legYSpacing = -1.15f * SCALE; //negative=higher, positive=lower
    }


}
