package com.tessera.engine.common.network.netty;

import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.netty.ping.PingPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Internal Netty channel handler that bridges channel events to the abstract localServer methods.
 */
 class NettyServerHandler extends ChannelInboundHandlerAdapter {

     NettyServer server;

     public NettyServerHandler(NettyServer server){
         this.server = server;
     }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        ChannelBase channel = new NettyChannel(client);
        // Decide whether to accept the new connection.
        if (!server.newClientEvent(channel)) {
            client.close();
            return;
        }
        server.clients.add(client);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        server.clients.remove(client);
        ChannelBase channel = new NettyChannel(client);
        server.clientDisconnectEvent(channel);
        super.channelInactive(ctx);
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        Channel client = ctx.channel();
//        if (msg instanceof ByteBuf) {
//            ByteBuf buf = (ByteBuf) msg;
//            byte[] receivedData = new byte[buf.readableBytes()];
//            buf.readBytes(receivedData);
//            // Handle ping/pong messages.
//            if (Arrays.equals(receivedData, NettyServer.pingMessage)) {
//                // Received a PING: send back a PONG.
//                ctx.writeAndFlush(Unpooled.wrappedBuffer(NettyServer.pongMessage));
//                System.out.println("Recieved PING from client");
//            } else if (Arrays.equals(receivedData, NettyServer.pongMessage)) {
//                // Received a PONG (could update lastPing if you track it).
//                System.out.println("Received PONG from client");
//            } else {
//                // Pass any other data to the subclass.
//                server.dataFromClientEvent(client, receivedData);
//            }
//        }
//    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // When the IdleStateHandler triggers a write idle event, send a PING.
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // If no write occurred within the ping interval, send a PING.
            if (event.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(new PingPacket());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Exception: " + cause.getMessage());
        ctx.close();
    }
}