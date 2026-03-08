package com.tessera.engine.common.network.netty;

import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.ServerBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.network.packet.PacketDecoder;
import com.tessera.engine.common.network.packet.PacketEncoder;
import com.tessera.engine.common.network.packet.PacketHandler;
import com.tessera.engine.common.network.netty.ping.PingPacket;
import com.tessera.engine.common.network.netty.ping.PongPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.TimeUnit;

/**
 * Abstract Netty-based localServer optimized for game servers.
 * <p>
 * This class replaces the old blocking ServerSocket implementation.
 * It uses Netty’s ServerBootstrap and pipeline:
 * <ul>
 *   <li>A {@code ChannelGroup} holds all active client channels.</li>
 *   <li>An {@code IdleStateHandler} sends PING messages if no write has occurred within the given interval.</li>
 *   <li>The inner {@code ServerHandler} takes care of connection, disconnection, ping/pong, and data events.</li>
 * </ul>
 * Subclasses should implement the abstract {@code newClientEvent(Channel)} and
 * {@code dataFromClientEvent(Channel, byte[])} methods.
 */
public abstract class NettyServer extends ServerBase {

   protected final ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // The idle interval (in seconds) for sending pings.
    public static final long PING_INTERVAL_SECONDS = 120;
    public static final int MAX_FRAME_SIZE = 2048;

    protected final EventLoopGroup bossGroup;
    protected final EventLoopGroup workerGroup;
    protected final Channel channel;
    private final ChannelFuture future;

    public Channel getChannel() {
        return channel;
    }

    /**
     * Register the ping and pong packets
     */
    public static final int PING_PACKET = 0;
    public static final int PONG_PACKET = 1;
    static{
        Packet.register(new PingPacket());
        Packet.register(new PongPacket());
    }

    /**
     * Starts the Netty localServer on the given port.
     *
     * @param port the port (must be >= 1024)
     * @throws InterruptedException if the binding is interrupted
     */
    public NettyServer(int port) throws InterruptedException {
        if (port < 1024) {
            throw new IllegalArgumentException("Port number must be higher than 1024");
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        schedulePing(ch);
                        ch.pipeline().addLast(new NettyServerHandler(NettyServer.this));

                        /**
                         * Add  the decoder
                         * 1. The LengthFieldBasedFrameDecoder decodes the length of the packet and strips the length field
                         * 2. The PacketDecoder decodes the packet
                         */
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                                MAX_FRAME_SIZE, // Max frame size (1 KB)
                                0,    // Length field offset (starts at byte 0)
                                4,    // Length field length (4 bytes for int)
                                0,    // No length adjustment
                                4     // Strip the length field from the output
                        ));
                        ch.pipeline().addLast(new PacketDecoder(NettyServer.this));
                        ch.pipeline().addLast(new PacketEncoder());
                        ch.pipeline().addLast(new PacketHandler(false));
                    }
                });

        future = bootstrap.bind(port).sync();
        channel = future.channel();
        System.out.println("Server started at " + port);
    }

    /**
     * Closes the localServer and all client connections.
     */
    public void close() {
        if (channel != null) {
            channel.close();
        }
        clients.close();

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    private void schedulePing(Channel ch) {
        // 1. IdleStateHandler triggers an IdleStateEvent if no write for PING_INTERVAL_SECONDS.
        ch.pipeline().addLast(new IdleStateHandler(PING_INTERVAL_SECONDS, PING_INTERVAL_SECONDS, 0, TimeUnit.SECONDS));
    }

//    /**
//     * Sends data to all connected clients.
//     *
//     * @param data the data to send
//     */
//    public void sendToAllClients(byte[] data) {
//        ByteBuf buf = Unpooled.wrappedBuffer(data);
//        clients.writeAndFlush(buf);
//    }

    /**
     * Called when a new client connects.
     * <p>
     * Return {@code true} to accept the connection, or {@code false} to immediately close it.
     *
     * @param client the client's channel
     * @return whether to accept the client
     */
    public abstract boolean newClientEvent(ChannelBase client);

    /**
     * Called when a client disconnects.
     *
     * @param client the client's channel
     */
    public abstract void clientDisconnectEvent(ChannelBase client);
}
