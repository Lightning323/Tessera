package com.tessera.engine.common.network.packet;

import com.tessera.engine.common.network.netty.NettyChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PacketHandler extends SimpleChannelInboundHandler<Packet> {

    final boolean isClientSide;

    public PacketHandler(boolean isClientSide) {
        this.isClientSide = isClientSide;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        packet.handleClientSide(new NettyChannel(ctx.channel()), packet);
    }
}