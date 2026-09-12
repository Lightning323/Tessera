package com.tessera.engine.common.packets;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.players.Player;
import com.tessera.engine.common.progress.ProgressData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServerGatekeeperPacket extends Packet {

    String reason;
    boolean allowedIn = false;

    public ServerGatekeeperPacket(boolean allowedIn, String reason) {
        super(AllPackets.SERVER_GATEKEEPER);
        this.allowedIn = allowedIn;
        this.reason = reason;
    }

    public ServerGatekeeperPacket() {
        super(AllPackets.SERVER_GATEKEEPER);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        ServerGatekeeperPacket packetInstance = (ServerGatekeeperPacket) packet;
        out.writeBoolean(packetInstance.allowedIn);

        String reason = packetInstance.reason == null ? "" : packetInstance.reason;
        byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);
        out.writeInt(reasonBytes.length);
        out.writeBytes(reasonBytes);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        boolean allowedIn = in.readBoolean();
        int reasonLength = in.readInt();
        byte[] reasonBytes = new byte[reasonLength];
        in.readBytes(reasonBytes);
        String reason = new String(reasonBytes, StandardCharsets.UTF_8);

        out.add(new ServerGatekeeperPacket(allowedIn, reason));
    }

    @Override
    public void handleClientSide(ChannelBase ctx, Packet packet) {
        ServerGatekeeperPacket packetInstance = (ServerGatekeeperPacket) packet;
        // Join progress + popup UI live on the render thread; never mutate
        // them from a network thread.
        try {
            if (Main.getClient() != null) {
                Main.getClient().runOnMainThread(() -> {
                    try {
                        ProgressData prog = Main.getClient().getJoinProgressData();
                        if (prog == null) return;
                        if (packetInstance.allowedIn) {
                            prog.stage++;
                        } else {
                            prog.abort(packetInstance.reason);
                        }
                    } catch (Exception e) {
                        Main.LOGGER.warn("Gatekeeper handling failed", e);
                    }
                });
            }
        } catch (Exception e) {
            Main.LOGGER.warn("Failed to enqueue gatekeeper packet", e);
        }
    }

    @Override
    public void handleServerSide(ChannelBase ctx, Packet packet) {
    }
}
