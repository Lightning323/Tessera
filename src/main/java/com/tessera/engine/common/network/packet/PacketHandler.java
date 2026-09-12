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
        // Dispatch to the correct side. Previously this always called
        // handleClientSide, which meant server-bound packets (ChunkRequest,
        // ClientEntrance, Message server logic, block edits, ...) never ran
        // on a real Netty server. FakeChannel was unaffected because it calls
        // the handlers directly, which is why singleplayer worked and
        // multiplayer silently broke.
        if (isClientSide) {
            packet.handleClientSide(new NettyChannel(ctx.channel()), packet);
        } else {
            packet.handleServerSide(new NettyChannel(ctx.channel()), packet);
        }
    }
}