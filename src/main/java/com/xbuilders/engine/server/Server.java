/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.xbuilders.Main;
import com.xbuilders.engine.client.visuals.gameScene.GameScene;
import com.xbuilders.engine.common.json.JsonManager;
import com.xbuilders.engine.common.network.ChannelBase;
import com.xbuilders.engine.common.network.ServerBase;
import com.xbuilders.engine.common.network.fake.FakeServer;
import com.xbuilders.engine.common.network.netty.NettyServer;
import com.xbuilders.engine.common.network.packet.Packet;
import com.xbuilders.engine.common.packets.AllPackets;
import com.xbuilders.engine.common.players.Player;
import com.xbuilders.engine.common.players.pipeline.BlockEventPipeline;
import com.xbuilders.engine.common.players.pipeline.BlockHistory;
import com.xbuilders.utils.bytes.ByteUtils;
import com.xbuilders.engine.common.world.ServerWorld;
import com.xbuilders.engine.common.world.chunk.BlockData;
import com.xbuilders.engine.common.world.chunk.Chunk;
import com.xbuilders.engine.common.world.wcc.WCCf;
import com.xbuilders.engine.common.world.wcc.WCCi;
import com.xbuilders.engine.server.commands.Command;
import com.xbuilders.engine.server.commands.CommandRegistry;
import com.xbuilders.engine.server.commands.GiveCommand;
import com.xbuilders.engine.server.entity.Entity;
import com.xbuilders.engine.server.entity.EntityRegistry;
import com.xbuilders.engine.server.entity.EntitySupplier;
import com.xbuilders.engine.server.entity.ItemDrop;
import com.xbuilders.engine.server.item.ItemStack;
import org.joml.Vector3f;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import static com.xbuilders.Main.LOGGER;
import static com.xbuilders.Main.versionStringToNumber;

public class Server {
    public final static long VERSION = 0;
    public final static int SERVER_PLAYER_COMMUNICATION_RANGE = 2000;

    public final LivePropagationHandler livePropagationHandler = new LivePropagationHandler();
    public final ServerWorld world;
    private final Game game;
    public BlockEventPipeline eventPipeline;
    public LogicThread tickThread;
    public static final CommandRegistry commandRegistry = new CommandRegistry();
    public ArrayList<ChannelBase> players = new ArrayList<>();
    public final ServerBase endpoint;
    public int maxPlayers = 10;
    private boolean alive = true;
    private int port = -1;

    public void writeAndFlushToAllPlayers(Packet p) {
        for (ChannelBase player : players) {
            player.writeAndFlush(p);
        }
    }

    public void writeAndFlushToAllPlayers(Packet p, int centerX, int centerY, int centerZ) {
        //Players that are not within the range wont get the message
        for (ChannelBase player : players) {
            if (player.getPlayer().worldPosition.distance(centerX, centerY, centerZ) < SERVER_PLAYER_COMMUNICATION_RANGE) {
                player.writeAndFlush(p);
            }
        }
    }

    static {
        AllPackets.registerPackets();
    }

    private void registerCommands() {
//        commandRegistry.registerCommand(new Command("tickrate", "Sets the random tick likelihood. Usage: tickrate <ticks>")
//                .requiresOP(true).executesServerSide((parts, player) -> {
//                    if (parts.length >= 1) {
//                        Chunk.randomTickLikelyhoodMultiplier = (float) Double.parseDouble(parts[0]);
//                        return "Tick rate changed to: " + Chunk.randomTickLikelyhoodMultiplier;
//                    }
//                    return "Tick rate is " + Chunk.randomTickLikelyhoodMultiplier;
//                }));

        commandRegistry.registerCommand(new Command("mode",
                "Usage (to get the current mode): mode\n" +
                        "Usage (to change mode): mode <mode> <all (optional)>")
                .requiresOP(true).executesServerSide((parts, player) -> {
                    if (parts.length >= 1) {
                        String mode = parts[0].toUpperCase().trim().replace(" ", "_");

                        boolean sendToAll = (parts.length >= 2 && parts[1].equalsIgnoreCase("all"));
                        try {
                            Main.getServer().setGameMode(GameMode.valueOf(mode.toUpperCase()));
                            return "Game mode changed to: " + Main.getServer().getGameMode();
                        } catch (IllegalArgumentException e) {
                            return "No game mode \"" + mode + "\" Valid game modes are "
                                    + Arrays.toString(GameMode.values());
                        } catch (IOException e) {
                            return "Error: " + e;
                        }
                    } else {
                        return "Game mode: " + Main.getServer().getGameMode();
                    }
                }));

//        commandRegistry.registerCommand(new Command("die",
//                "Kills the current player")
//                .requiresOP(false).executesServerSide((parts,player) -> {
//                    player.die();
//                    return "Player " + player.getName() + " has died";
//                }));
//
//        commandRegistry.registerCommand(new Command("setSpawn",
//                "Set spawnpoint for the current player")
//                .requiresOP(false).executesServerSide((parts,player) -> {
//                    LocalClient.userPlayer.setSpawnPoint(
//                            LocalClient.userPlayer.worldPosition.x,
//                            LocalClient.userPlayer.worldPosition.y,
//                            LocalClient.userPlayer.worldPosition.z);
//                    return "Set spawn point for " + LocalClient.userPlayer.getName() + " to current position";
//                }));

        commandRegistry.registerCommand(new Command("op", "Usage: op <player> <true/false>")
                .requiresOP(true).executesServerSide((parts, player) -> {
                    if (parts.length >= 2) {
                        boolean operator = Boolean.parseBoolean(parts[1]);
                        Player target = getPlayerByName(parts[0]);
                        if (target != null) {
                            target.setOperator(operator);
                            return "Player " + target.toString() + " has been " + (operator ? "given" : "removed") + " operator privileges";
                        } else {
                            return "Player not found";
                        }
                    }
                    return null;
                }));

        commandRegistry.registerCommand(new Command("msg",
                "Usage: msg <player/all> <message>").executesServerSide((parts, player) -> {
            if (parts.length >= 2) {
                // return Main.getServer().server.sendChatMessage(parts[0], parts[1]);
            }
            return null;
        }));

        commandRegistry.registerCommand(new GiveCommand());

        commandRegistry.registerCommand(new Command("time",
                "Usage: time set <day/evening/night>\n" +
                        "Usage: time get")
                .requiresOP(false)
                .executesServerSide((parts, player) -> {
                    if (parts.length >= 1 && parts[0].equalsIgnoreCase("get")) {
                        return "Time of day: " + Main.getServer().getTimeOfDay();
                    } else if (parts.length >= 2 && parts[0].equalsIgnoreCase("set")) {
                        if (parts[1].equalsIgnoreCase("morning") || parts[1].equalsIgnoreCase("m")) {
                            Main.getServer().setTimeOfDay(0.95f);
                            return "Time of day set to: " + Main.getServer().getTimeOfDay();
                        } else if (parts[1].equalsIgnoreCase("day") || parts[1].equalsIgnoreCase("d")) {
                            Main.getServer().setTimeOfDay(0.0f);
                            return "Time of day set to: " + Main.getServer().getTimeOfDay();
                        } else if (parts[1].equalsIgnoreCase("evening") || parts[1].equalsIgnoreCase("e")) {
                            Main.getServer().setTimeOfDay(0.25f);
                            return "Time of day set to: " + Main.getServer().getTimeOfDay();
                        } else if (parts[1].equalsIgnoreCase("night") || parts[1].equalsIgnoreCase("n")) {
                            Main.getServer().setTimeOfDay(0.5f);
                            return "Time of day set to: " + Main.getServer().getTimeOfDay();
                        } else {
                            float time = Float.parseFloat(parts[1]);
                            Main.getServer().setTimeOfDay(time);
                            return "Time of day set to: " + Main.getServer().getTimeOfDay();
                        }
                    }
                    return null;
                }));

//        commandRegistry.registerCommand(new Command("alwaysDay",
//                "Usage: alwaysDay true/false")
//                .requiresOP(true)
//                .executesServerSide((parts) -> {
//                    if (parts.length >= 1) {
//                        LocalClient.world.data.data.alwaysDayMode = parts[0].equalsIgnoreCase("true");
//                        try {
//                            LocalClient.world.data.save();
//                            return "Always day mode: " + LocalClient.world.data.data.alwaysDayMode;
//                        } catch (IOException e) {
//                            return "Error: " + e;
//                        }
//                    }
//                    return null;
//                }));

//        commandRegistry.registerCommand(new Command("teleport",
//                "Usage: teleport <player>\nUsage: teleport <x> <y> <z>")
//                .requiresOP(true)
//                .executesServerSide((parts) -> {
//                    if (parts.length >= 3) {
//                        LocalClient.userPlayer.worldPosition.set(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
//                        return null;
//                    }
//
//                    if (parts.length >= 1) {
////                        Player target = Main.getServer().server.getPlayerByName(parts[0]);
////                        if (target != null) {
////                            LocalClient.userPlayer.worldPosition.set(target.worldPosition);
////                            return null;
////                        } else {
////                            return "Player not found";
////                        }
//                    }
//                    return null;
//                }));

        commandRegistry.registerCommand(new Command("difficulty",
                "Usage: difficulty <easy/normal/hard>")
                .requiresOP(true)
                .executesServerSide((parts, player) -> {
                    if (parts.length >= 1) {
                        String mode = parts[0].toUpperCase().trim().replace(" ", "_");
                        try {
                            Main.getServer().setDifficulty(Difficulty.valueOf(mode.toUpperCase()));
//                            if (Main.getServer().server.isPlayingMultiplayer())
//                                Main.getServer().server.sendToAllClients(new byte[]{
//                                        GameServer.CHANGE_DIFFICULTY, (byte) Main.getServer().getDifficulty().ordinal()});
                            return "Difficulty changed to: " + Main.getServer().getDifficulty();
                        } catch (IllegalArgumentException e) {
                            return "Invalid mode \"" + mode + "\" Valid modes are "
                                    + Arrays.toString(Difficulty.values());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return "Difficulty: " + Main.getServer().getDifficulty();
                }));

        commandRegistry.registerCommand(new Command("list",
                "Lists all connected players")
                .executesServerSide((parts, p) -> {
                    StringBuilder str = new StringBuilder(players.size() + " players:\n");
                    for (ChannelBase channel : players) {
                        Player player = channel.getPlayer();
                        if (player == null) str.append("null player").append("\n");
                        else
                            str.append(player.toString()).append(";   ").append(player.getConnectionStatus()).append("\n");
                    }
                    System.out.println("\nPLAYERS:\n" + str);
                    return str.toString();
                }));
    }

    private Player getPlayerByName(String part) {
        for (ChannelBase channel : players) {
            Player player = channel.getPlayer();
            if (player != null && player.getName().equalsIgnoreCase(part)) return player;
        }
        return null;
    }

    public final boolean runningLocally() {
        return port == -1;
    }

    public Server(Game game, ServerWorld world) {
        LOGGER.info("Server v{} started", VERSION);
        this.game = game;
        this.world = world;
        endpoint = new FakeServer() {
            @Override
            public boolean newClientEvent(ChannelBase client) {
                return Server.this.newClientEvent(client);
            }

            @Override
            public void clientDisconnectEvent(ChannelBase client) {
                System.out.println("Client disconnected: " + client.remoteAddress());
                Server.this.clientDisconnectEvent(client);
            }

            @Override
            public void close() {
                Server.this.stop();
            }
        };
    }

    public Server(Game game, ServerWorld world, int port) throws InterruptedException {
        this.world = world;
        this.game = game;
        this.port = port;
        endpoint = new NettyServer(port) {
            @Override
            public boolean newClientEvent(ChannelBase client) {
                return Server.this.newClientEvent(client);
            }

            @Override
            public void clientDisconnectEvent(ChannelBase client) {
                Server.this.clientDisconnectEvent(client);
            }

            @Override
            public void close() {
                Server.this.stop();
            }
        };
    }


    //Server events
    public boolean newClientEvent(ChannelBase client) {
        for (ChannelBase c : players) { //Only 1 player per IP
            if (c.remoteAddress().equals(client.remoteAddress())) {
                System.out.println("Player already connected from " + c.remoteAddress());
                return false;
            }
        }
        players.add(client);
        return true;
    }


    public void clientDisconnectEvent(ChannelBase client) {
        players.remove(client);
        System.out.println("Client disconnected: " + client);
    }

    public void run() {
        try {
            registerCommands();
            eventPipeline = new BlockEventPipeline(world);

            tickThread = new LogicThread(this);
            livePropagationHandler.startGameEvent(world.getData());
            eventPipeline.startGameEvent(world.getData());
            tickThread.startGameEvent();

            //Setup the game
            game.setupServer(this);

            while (alive) {
                eventPipeline.update();
            }
        } catch (Exception e) {
            LOGGER.error( "Error", e);
        } finally {
            stop();
        }
    }


    public void stop() {
        alive = false;
        if (world.getData() != null) world.close();
        tickThread.stopGameEvent();
        eventPipeline.stopGameEvent();
        livePropagationHandler.stopGameEvent();
    }

    //Game Mode =======================================================================================================


    public Difficulty getDifficulty() {
        return world.getData().data.difficulty;
    }

    public void setDifficulty(Difficulty difficulty) throws IOException {
        if (difficulty == null) difficulty = Difficulty.NORMAL;
        world.getData().data.difficulty = difficulty;
        world.getData().save();
        Main.getClient().consoleOut("Difficulty changed to: " + getDifficulty());
    }

    public GameMode getGameMode() {
        return world.getData().data.gameMode;
    }

    public void setGameMode(GameMode gameMode) throws IOException {
        if (gameMode == null) gameMode = GameMode.ADVENTURE;
        world.getData().data.gameMode = gameMode;
        world.getData().save();
        Main.getClient().consoleOut("Game mode changed to: " + getGameMode());
    }

    // Getters
    public int getLightLevel(int worldX, int worldY, int worldZ) {
        WCCi wcc = new WCCi().set(worldX, worldY, worldZ);
        Chunk chunk = world.getChunk(wcc.chunk);
        if (chunk != null) {
            int sun = chunk.voxels.getSun(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
            int torch = chunk.voxels.getTorch(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
            sun = (int) Math.min(sun, GameScene.background.getLightness() * 15);
            return Math.max(sun, torch);
        }
        return 0;
    }

    //Set block ===============================================================================

    public void setBlock(short block, int worldX, int worldY, int worldZ) {
        setBlock(block, new WCCi().set(worldX, worldY, worldZ));
    }

    public void setBlock(short block, BlockData data, int worldX, int worldY, int worldZ) {
        setBlock(block, data, new WCCi().set(worldX, worldY, worldZ));
    }

    public void setBlockData(BlockData data, int worldX, int worldY, int worldZ) {
        setBlockData(data, new WCCi().set(worldX, worldY, worldZ));
    }

    public void setBlock(short newBlock, BlockData blockData, WCCi wcc) {
        if (!world.inYBounds((wcc.chunk.y * Chunk.WIDTH) + wcc.chunkVoxel.y)) return;
        Chunk chunk = world.getChunk(wcc.chunk);
        if (chunk != null) {
            //Get the previous block
            short previousBlock = chunk.voxels.getBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);

            //we need to set the block because some algorithms want to check to see if the block has changed immediately
            chunk.voxels.setBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, newBlock); //Important
            chunk.voxels.setBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, blockData); //Important

            BlockHistory history = new BlockHistory(previousBlock, newBlock);
            history.updateBlockData = true;
            history.newBlockData = blockData;
            eventPipeline.addEvent(wcc, history);
        }
    }

    public void setBlock(short newBlock, WCCi wcc) {
        if (!world.inYBounds((wcc.chunk.y * Chunk.WIDTH) + wcc.chunkVoxel.y)) return;
        Chunk chunk = world.getChunk(wcc.chunk);
        if (chunk != null) {
            //Get the previous block
            short previousBlock = chunk.voxels.getBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);

            //we need to set the block because some algorithms want to check to see if the block has changed immediately
            chunk.voxels.setBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, newBlock); //Important

            BlockHistory history = new BlockHistory(previousBlock, newBlock);
            eventPipeline.addEvent(wcc, history);
        }
    }

    public void setBlockData(BlockData blockData, WCCi wcc) {
        if (!world.inYBounds((wcc.chunk.y * Chunk.WIDTH) + wcc.chunkVoxel.y)) return;
        Chunk chunk = world.getChunk(wcc.chunk);
        if (chunk != null) {
            //Get the previous block
            short previousBlock = chunk.voxels.getBlock(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);

            //we need to set the block because some algorithms want to check to see if the block has changed immediately
            chunk.voxels.setBlockData(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z, blockData); //Important

            BlockHistory history = new BlockHistory(previousBlock, previousBlock);
            history.updateBlockData = true;
            history.newBlockData = blockData;
            eventPipeline.addEvent(wcc, history);
        }
    }

    //Set entity =================================================================================
    public Entity placeItemDrop(Vector3f position, ItemStack item, boolean droppedFromPlayer) {
        Entity e = placeEntity(EntityRegistry.ENTITY_ITEM_DROP, position, null);

        ItemDrop drop = (ItemDrop) e;
        drop.definitionData.stack = item;
        drop.definitionData.droppedFromPlayer = droppedFromPlayer;
        return e;
    }

    /**
     * @param entity    the entity to place
     * @param w         the world position to place the entity
     * @param simpleGen the JSON to inject into the entity
     * @return the entity that was placed
     */
    public Entity placeEntity(EntitySupplier entity, Vector3f w, JsonManager.SimpleJsonGenerator simpleGen) {
        byte[] entityBytes = null;
        if (simpleGen != null) {
            //Write the entity input as binary data
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (JsonGenerator generator = Entity.smileFactory.createGenerator(baos)) {
                generator.writeStartObject(); // Start root object
                simpleGen.write(generator);
                generator.writeEndObject(); // End root object
                generator.close();
            } catch (IOException e) {
                LOGGER.info( "Error saving container data", e);
            }
            entityBytes = baos.toByteArray();
        }

        WCCf wcc = new WCCf();
        wcc.set(w);
        Chunk chunk = world.getChunk(wcc.chunk);
        if (chunk != null) {
            chunk.markAsModified();
            Entity e = chunk.entities.placeNew(w, entity, entityBytes);
            e.sendMultiplayer = true;//Tells the chunkEntitySet to send the entity to the clients
            return e;
        }
        return null;
    }


    public void setTimeOfDay(double v) {

        System.out.println("Setting time of day to " + v);
        GameScene.background.setTimeOfDay(v);
        byte[] timeFloat = ByteUtils.floatToBytes((float) v);
        //  server.sendToAllClients(new byte[]{GameServer.SET_TIME, timeFloat[0], timeFloat[1], timeFloat[2], timeFloat[3]});

    }

    public float getTimeOfDay() {
        return GameScene.background.getTimeOfDay();
    }


}
