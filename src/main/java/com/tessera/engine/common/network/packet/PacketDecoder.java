package com.tessera.engine.common.network.packet;

import com.tessera.engine.common.network.netty.NettyClient;
import com.tessera.engine.common.network.netty.NettyServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    public final static HashMap<Byte, Packet> PACKET_REGISTRY = new HashMap<>();

    NettyServer ns = null;
    NettyClient nc = null;

    public PacketDecoder(NettyServer ns) {
        this.ns = ns;
    }

    public PacketDecoder(NettyClient ns) {
        this.nc = ns;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        /**
         * Preview the packet
         */
        // previewPacket(in);

        if (!in.isReadable()) {
            return;
        }

        /**
         * Packet ID
         */
        // LengthFieldBasedFrameDecoder guarantees one full frame per call,
        // but be defensive: a truncated frame must not consume the reader.
        in.markReaderIndex();
        byte packetId = in.readByte();

        //Get the message ID and decode it
        Packet packetInstance = PACKET_REGISTRY.get((byte) packetId);
        if (packetInstance != null) {
            try {
                packetInstance.decode(ctx, in, out);
            } catch (Exception e) {
                System.out.println("Failed to decode packet id " + packetId + ": " + e);
                in.resetReaderIndex();
            }
        } else {
            System.out.println("Unknown packet: " + packetId + " (dropping " + in.readableBytes() + " remaining bytes of this frame)");
            // Drop the rest of this frame so a bad id cannot desync the stream.
            // The frame itself is discarded after this method returns.
            in.skipBytes(in.readableBytes());
        }
    }

    private void previewPacket(ByteBuf in) {
        in.markReaderIndex(); // Mark reader index
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        System.out.print("Decoding Packet: \"" + new String(bytes) + "\"; " + Arrays.toString(bytes) + "; l=" + bytes.length);
        in.resetReaderIndex();
        in.markReaderIndex(); // Mark reader index
        //System.out.println("Packet length: " + in.readInt());
        System.out.print("; Packet id: " + in.readByte());
        in.resetReaderIndex();
        System.out.println();
    }
}
