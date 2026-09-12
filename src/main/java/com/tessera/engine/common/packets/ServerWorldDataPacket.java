package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.client.Client;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.world.WorldData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Server -&gt; client snapshot of the authoritative world the server is hosting
 * (name, seed, terrain, spawn point, game mode...).
 *
 * <p>Sent during the entrance handshake right after the gatekeeper. A client
 * joining a remote server has no local world file, so without this the client
 * world has no {@code WorldData} and the join would NPE on {@code
 * world.getData()}. The client applies it to its (empty) world shell and then
 * proceeds with the join. Local worlds (singleplayer/host) already have their
 * data set and ignore it.
 */
public class ServerWorldDataPacket extends Packet {

    public String name;
    public String dataJson;

    public ServerWorldDataPacket() {
        super(AllPackets.SERVER_WORLD_DATA);
    }

    public ServerWorldDataPacket(String name, String dataJson) {
        super(AllPackets.SERVER_WORLD_DATA);
        this.name = name;
        this.dataJson = dataJson;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        ServerWorldDataPacket packetInstance = (ServerWorldDataPacket) packet;
        byte[] nameBytes = packetInstance.name == null ? new byte[0] : packetInstance.name.getBytes(StandardCharsets.UTF_8);
        out.writeInt(nameBytes.length);
        out.writeBytes(nameBytes);
        byte[] jsonBytes = packetInstance.dataJson == null ? new byte[0] : packetInstance.dataJson.getBytes(StandardCharsets.UTF_8);
        out.writeInt(jsonBytes.length);
        out.writeBytes(jsonBytes);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int nameLength = in.readInt();
        byte[] nameBytes = new byte[nameLength];
        in.readBytes(nameBytes);
        int jsonLength = in.readInt();
        byte[] jsonBytes = new byte[jsonLength];
        in.readBytes(jsonBytes);

        out.add(new ServerWorldDataPacket(
                new String(nameBytes, StandardCharsets.UTF_8),
                new String(jsonBytes, StandardCharsets.UTF_8)));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        ServerWorldDataPacket packetInstance = (ServerWorldDataPacket) packet;
        Client client = Main.getClient();
        if (client == null || packetInstance.name == null || packetInstance.dataJson == null) return;
        try {
            WorldData serverData = new WorldData();
            serverData.makeNew(packetInstance.name, packetInstance.dataJson);
            // setData clears the world's chunk maps, so it must run on the
            // window thread and never clobber a local world that is already
            // loading (singleplayer/host). A pure join has no data yet, so
            // this is what makes the join proceed past the entrance handshake.
            client.runOnMainThread(() -> {
                try {
                    if (client.world.getData() != null) return;
                    client.world.setData(serverData);
                    if (serverData.data != null) {
                        if (serverData.data.gameMode != null) client.cachedGameMode = serverData.data.gameMode;
                        if (serverData.data.difficulty != null) client.cachedDifficulty = serverData.data.difficulty;
                    }
                } catch (Exception e) {
                    Main.LOGGER.warn("Failed to apply server world data", e);
                }
            });
        } catch (Exception e) {
            Main.LOGGER.warn("World data packet malformed", e);
        }
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
    }
}