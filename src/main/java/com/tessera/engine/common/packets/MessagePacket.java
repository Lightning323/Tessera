package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.players.Player;
import com.tessera.engine.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessagePacket extends Packet {
    String message;

    public MessagePacket(String message) {
        super(AllPackets.MESSAGE);
        this.message = message;
    }

    public MessagePacket() {
        super(AllPackets.MESSAGE);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        MessagePacket packetInstance = (MessagePacket) packet;
        //Write a string as UTF-8 with byte length (not char length, so
        // non-ASCII chat does not corrupt the stream).
        String msg = packetInstance.message == null ? "" : packetInstance.message;
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        MessagePacket packetInstance = (MessagePacket) packet;
        // consoleOut mutates Nuklear UI state iterated on the render thread;
        // enqueue it instead of touching UI from the network thread.
        try {
            if (Main.getClient() != null) {
                Main.getClient().runOnMainThread(() -> {
                    try {
                        Main.getClient().consoleOut("server: " + packetInstance.message);
                    } catch (Exception e) {
                        Main.LOGGER.warn("consoleOut failed", e);
                    }
                });
            }
        } catch (Exception e) {
            Main.LOGGER.warn("Failed to enqueue chat message", e);
        }
        System.out.println("Message from server: " + packetInstance.message);
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        Player player = ctx.getPlayer();  //Get the player by its channel
        if (player == null) {
            ctx.writeAndFlush(new MessagePacket("Who is this player? (join handshake incomplete)"));
            return;
        }
        System.out.println("Player asking: " + player);

        MessagePacket packetInstance = (MessagePacket) packet;
        System.out.println("Command from client: " + packetInstance.message + " Player asking: " + player);
        String out = Server.commandRegistry.handleCommand(packetInstance.message, player);

        if (out != null) {
            System.out.println("Sending response to the client: " + out);
            ctx.writeAndFlush(new MessagePacket(out));
        }
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int length = in.readInt();
        byte[] messageBytes = new byte[length];
        in.readBytes(messageBytes);
        String msg = new String(messageBytes, StandardCharsets.UTF_8);
        out.add(new MessagePacket(msg));
    }
}
