package com.tessera.engine.common.network.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private final NettyClient client;

    public NettyClientHandler(NettyClient client) {
        this.client = client;
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        ByteBuf buf = (ByteBuf) msg;
//        byte[] receivedData = new byte[buf.readableBytes()];
//        buf.readBytes(receivedData);
//        buf.release();
//
//        System.out.println("Received data: " + Arrays.toString(receivedData));
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}