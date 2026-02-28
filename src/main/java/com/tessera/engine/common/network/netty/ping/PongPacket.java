package com.tessera.engine.common.network.netty.ping;

import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.netty.NettyServer;
import com.tessera.engine.common.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class PongPacket extends Packet {

    public PongPacket() {
        super(NettyServer.PONG_PACKET);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        System.out.println("Received pong");
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
        handleClientSide(ctx, packet);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        out.add(new PongPacket());
    }
}
