package com.tessera.engine.common.packets;

import com.tessera.engine.common.network.packet.Packet;

public class AllPackets {
    //Ping and pong packets take up 0 and 1
    public static final int MESSAGE = 2;
    public static final int CLIENT_ENTRANCE = 3;
    public static final int SERVER_GATEKEEPER = 4;
    public static final int CHUNK_DATA = 5;
    public static final int CHUNK_REQUEST = 6;
    // Server-authoritative block editing. Client -> server requests, server -> client broadcast.
    public static final int BLOCK_BREAK_REQUEST = 7;
    public static final int BLOCK_PLACE_REQUEST = 8;
    public static final int BLOCK_UPDATE = 9;
    // Server -> client authoritative game-state snapshot (game mode, etc.).
    public static final int GAME_STATE = 10;
    // Server -> client authoritative world data (seed, terrain, spawn point).
    public static final int SERVER_WORLD_DATA = 11;

    public static void registerPackets() {
        Packet.register(new ClientEntrancePacket());
        Packet.register(new ServerGatekeeperPacket());
        Packet.register(new MessagePacket());
        Packet.register(new ChunkDataPacket());
        Packet.register(new ChunkRequestPacket());
        Packet.register(new BlockBreakRequestPacket());
        Packet.register(new BlockPlaceRequestPacket());
        Packet.register(new BlockUpdatePacket());
        Packet.register(new GameStatePacket());
        Packet.register(new ServerWorldDataPacket());
    }
}
