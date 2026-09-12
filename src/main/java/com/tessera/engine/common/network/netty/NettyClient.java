package com.tessera.engine.common.network.netty;

import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.ClientBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.network.packet.PacketDecoder;
import com.tessera.engine.common.network.packet.PacketEncoder;
import com.tessera.engine.common.network.packet.PacketHandler;
import com.tessera.engine.common.network.netty.ping.PingPacket;
import com.tessera.engine.common.network.netty.ping.PongPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.concurrent.TimeUnit;

import static com.tessera.engine.common.network.netty.NettyServer.MAX_FRAME_SIZE;

public abstract class NettyClient extends ClientBase {

    private final Channel channel;
    private final ChannelBase channelBase;
    private final EventLoopGroup group;
    private final ChannelFuture future;

    /**
     * Optional test hook: invoked on the event-loop thread after each inbound
     * packet has been dispatched ({@code handleClientSide}). Lets headless
     * connectivity tests observe real packets without a window/GUI. Null by
     * default (no overhead, no behavior change).
     */
    public java.util.function.Consumer<Packet> inboundObserver;

    /**
     * Register the ping and pong packets
     */
    static{
        Packet.register(new PingPacket());
        Packet.register(new PongPacket());
    }

    public ChannelBase getChannel() {
        return channelBase;
    }

    public NettyClient(String host, int port) throws InterruptedException {
        this(host, port, null);
    }

    public NettyClient(String host, int port, java.util.function.Consumer<Packet> inboundObserver) throws InterruptedException {
        this.inboundObserver = inboundObserver;
        System.out.println("Connecting to " + host + ":" + port);
        group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new NettyClientHandler(NettyClient.this));

                        /**
                         * Add  the decoder
                         * 1. The LengthFieldBasedFrameDecoder decodes the length of the packet and strips the length field
                         * 2. The PacketDecoder decodes the packet
                         */
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                                MAX_FRAME_SIZE, // Max frame size (8 MB, must fit gzipped chunks)
                                0,    // Length field offset (starts at byte 0)
                                4,    // Length field length (4 bytes for int)
                                0,    // No length adjustment
                                4     // Strip the length field from the output
                        ));
                        ch.pipeline().addLast(new PacketDecoder(NettyClient.this));

                        if (inboundObserver != null) {
                            // PacketHandler is a SimpleChannelInboundHandler and does
                            // not propagate reads downstream, so the observer MUST sit
                            // before it to see decoded packets.
                            ch.pipeline().addLast((ChannelInboundHandler) new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext c, Object msg) {
                                    inboundObserver.accept((Packet) msg);
                                    c.fireChannelRead(msg);
                                }
                            });
                        }

                        ch.pipeline().addLast(new PacketEncoder());
                        ch.pipeline().addLast(new PacketHandler(true));
                    }
                });

        // Connect to localServer
        try {
            future = bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            group.shutdownGracefully();
            throw e;
        } catch (Exception e) {
            // Refused / unreachable host: shut the event-loop group down so a
            // failed attempt doesn't leak threads, then surface the error to the
            // caller (UI popup or test retry) instead of a bare thread leak.
            System.out.println("Failed to connect to " + host + ":" + port + ": " + e.getMessage());
            group.shutdownGracefully();
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Failed to connect to " + host + ":" + port, e);
        }
        channel = future.channel();
        channelBase = new NettyChannel(channel);

        // Schedule periodic ping
        schedulePing();

        // Add a listener to the future to serverExecute when the connection is successful
        future.addListener((ChannelFutureListener) this::nettyServerConnectEvent);
    }

//    public void waitUntilChannelIsClosed() {
//        // Wait until connection is closed
//        try {
//            future.channel().closeFuture().sync();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            group.shutdownGracefully();
//        }
//    }

    private void nettyServerConnectEvent(ChannelFuture channelFuture) {
        onConnected(
                channelFuture.isSuccess(),
                channelFuture.cause(),
                new NettyChannel(channelFuture.channel()));
    }

    public abstract void onConnected(boolean success, Throwable cause, ChannelBase channel);

    private void schedulePing() {
        channel.eventLoop().scheduleAtFixedRate(() -> {
            if (channel.isActive()) {
                channel.writeAndFlush(new PingPacket());
            }
        }, 10, NettyServer.PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

//    public void sendData(byte[] data) {
//        if (channel != null && channel.isActive()) {
//            channel.writeAndFlush(Unpooled.wrappedBuffer(data));
//        }
//    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
