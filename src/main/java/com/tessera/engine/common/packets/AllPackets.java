package com.tessera.engine.common.packets;

import com.tessera.engine.common.network.packet.Packet;

public class AllPackets {
    //Ping and pong packets take up 0 and 1
    public static final int MESSAGE = 2;
    public static final int CLIENT_ENTRANCE = 3;
    public static final int SERVER_GATEKEEPER = 4;
    public static final int CHUNK_DATA = 5;
    public static final int CHUNK_REQUEST = 6;

    public static void registerPackets() {
        Packet.register(new ClientEntrancePacket());
        Packet.register(new ServerGatekeeperPacket());
        Packet.register(new MessagePacket());
        Packet.register(new ChunkDataPacket());
        Packet.register(new ChunkRequestPacket());
    }
}
