package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.players.Player;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientEntrancePacket extends Packet {

    String name;
    int skinID;

    public ClientEntrancePacket(Player player) {
        this(player.getName(), player.getSkinID());
    }

    public ClientEntrancePacket(String name, int skinID) {
        super(AllPackets.CLIENT_ENTRANCE);
        this.name = name;
        this.skinID = skinID;
    }

    public ClientEntrancePacket() {
        super(AllPackets.CLIENT_ENTRANCE);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        ClientEntrancePacket packetInstance = (ClientEntrancePacket) packet;
        //Write a string as UTF-8 bytes with byte length prefix.
        String name = packetInstance.name == null ? "" : packetInstance.name;
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        out.writeInt(nameBytes.length);
        out.writeBytes(nameBytes);
        out.writeInt(packetInstance.skinID);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int nameLength = in.readInt();
        byte[] nameBytes = new byte[nameLength];
        in.readBytes(nameBytes);

        String name = new String(nameBytes, StandardCharsets.UTF_8);
        int skinID = in.readInt();

        out.add(new ClientEntrancePacket(name, skinID));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        System.out.println("ClientEntrancePacket");
        ClientEntrancePacket packetInstance = (ClientEntrancePacket) packet;

        if (Main.getServer() == null) return;

        //Check if maximum player count is reached
        if (Main.getServer().players.size() >= Main.getServer().maxPlayers) {
            ctx.writeAndFlush(new ServerGatekeeperPacket(false, "Maximum player count of " + Main.getServer().maxPlayers + " reached"));
            ctx.close();
            return;
        }

        //Check if the name is already in use
        for (ChannelBase channel : Main.getServer().players) {
            if (
                    channel.getPlayer() != null
                            && channel.getPlayer().getName().equalsIgnoreCase(packetInstance.name)) {
                ctx.writeAndFlush(new ServerGatekeeperPacket(false, "Your players name is already in use"));
                ctx.close();
                return;
            }
        }

        //Make the player and add it to the server
        Player player = new Player(ctx);
        player.setName(packetInstance.name);
        player.setSkin(packetInstance.skinID);
        ctx.setPlayer(player);

        ctx.writeAndFlush(new ServerGatekeeperPacket(true, ""));
        // Sync authoritative game state so the new client never reads the
        // server object directly (remote clients have no local Server).
        try {
            ctx.writeAndFlush(new GameStatePacket(Main.getServer().getGameMode(), Main.getServer().getDifficulty()));
        } catch (Exception e) {
            Main.LOGGER.warn("Failed to send game state to " + player.getName(), e);
        }
    }
}
