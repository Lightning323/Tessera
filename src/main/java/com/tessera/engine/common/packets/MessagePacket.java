package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.fake.FakeChannel;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.players.Player;
import com.tessera.engine.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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
        //Write a string
        out.writeInt(packetInstance.message.length());
        out.writeBytes(packetInstance.message.getBytes());
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        MessagePacket packetInstance = (MessagePacket) packet;
        Main.getClient().consoleOut("server: " + packetInstance.message);
        System.out.println("Message from server: " + packetInstance.message);
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        Player player = ctx.getPlayer();  //Get the player by its channel
        System.out.println("Player asking: " + player.toString());
//        if (player == null) {
//            ctx.writeAndFlush(new MessagePacket("Who is this player?"));
//            return;
//        }

        MessagePacket packetInstance = (MessagePacket) packet;
        System.out.println("Command from client: " + packetInstance.message + " Player asking: " + player.toString());
        String out = Server.commandRegistry.handleCommand(packetInstance.message, player);

        if (out != null) {
            System.out.println("Sending response to the client: " + out + ", \t\t SEND to server: " + ((FakeChannel) ctx).sendMessagesToServer);
            ctx.writeAndFlush(new MessagePacket(out));
        }
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int length = in.readInt();
        byte[] messageBytes = new byte[length];
        in.readBytes(messageBytes);
        String msg = new String(messageBytes);
        out.add(new MessagePacket(msg));
    }
}
