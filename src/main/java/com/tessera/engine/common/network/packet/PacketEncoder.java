package com.tessera.engine.common.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * There can only be one decoder,handler and encoder per channel.
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        out.writeInt(0); // Placeholder for length, updated later
        int startIndex = out.writerIndex(); // Mark position

        out.writeByte(packet.id);  // Write packet ID
        packet.encode(ctx, packet, out); // Encode packet

        int endIndex = out.writerIndex();
        out.setInt(0, endIndex - startIndex); // Update length at the beginning
    }
}