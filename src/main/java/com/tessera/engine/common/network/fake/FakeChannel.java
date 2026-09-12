package com.tessera.engine.common.network.fake;

import com.tessera.Main;
import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.network.packet.PacketDecoder;
import com.tessera.engine.common.players.Player;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.DefaultAttributeMap;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FakeChannel extends ChannelBase {
    private final FakeServer server;
    private final FakeClient client;
    public final boolean sendMessagesToServer;
    private final BlockingQueue<Object> incoming = new LinkedBlockingQueue<>();
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final SocketAddress fakeAddress = new SocketAddress() {
        public String toString() {
            return "FakeAddress-" + hashCode();
        }
    };
    private Thread processingThread;
    FakeChannel reverseChannel;
    //Attribute map (based on netty attribute map)
    DefaultAttributeMap attributeMap = new DefaultAttributeMap();

    public FakeChannel(FakeServer server, FakeClient client, boolean sendMessagesToServer) {
        this.server = server;
        this.client = client;
        this.sendMessagesToServer = sendMessagesToServer;
        startProcessing();
    }

    /**
     * The reverse channel is basically the server channel, It is used for the server to communicate to the client
     * The reverse channel should not share attributes with the client as that would cross client/server barriers
     */
    public void makeReverseChannel() {
        reverseChannel = new FakeChannel(server, client, !sendMessagesToServer);
        //reverseChannel.reverseChannel = this;
        //reverseChannel.attributeMap = this.attributeMap; //We MUST share attribute map
    }

    private void startProcessing() {
        processingThread = new Thread(() -> {
            System.out.println("FakeChannel started " + this);
            try {
                while (active.get()) {
                    Packet packet = (Packet) incoming.take();
                    try {
                        // Round-trip through encode/decode so singleplayer exercises
                        // the exact same packet bytes as Netty multiplayer. This
                        // catches encode/decode asymmetry early and prevents
                        // shared-mutable-packet bugs across the client/server
                        // boundary. Falls back to the original instance if the
                        // codec fails so a test packet never silently vanishes.
                        Packet wirePacket = roundTrip(packet);
                        if (sendMessagesToServer) {
                            server.receive(reverseChannel, wirePacket);
                        } else {
                            client.receive(wirePacket);
                        }
                    } catch (Exception e) {
                        Main.LOGGER.warn("Failed to receive fake packet", e);
                    }
                }
            } catch (InterruptedException ignored) {
                com.tessera.Main.LOGGER.error("FakeChannel interrupted!", ignored);
            }
        });
        processingThread.start();
    }

    /**
     * Serializes a packet to bytes and deserializes it again, mimicking the
     * Netty PacketEncoder + LengthFieldBasedFrameDecoder + PacketDecoder path
     * (minus the 4-byte length prefix, which is framing only).
     */
    private static Packet roundTrip(Packet packet) {
        ByteBuf buf = Unpooled.buffer();
        try {
            buf.writeByte(packet.id);
            packet.encode(null, packet, buf);
            byte id = buf.readByte();
            Packet prototype = PacketDecoder.PACKET_REGISTRY.get(id);
            if (prototype == null) {
                System.out.println("FakeChannel: unknown packet id " + id + ", passing through");
                return packet;
            }
            List<Object> out = new ArrayList<>(1);
            prototype.decode(null, buf, out);
            if (out.isEmpty() || !(out.get(0) instanceof Packet decoded)) {
                System.out.println("FakeChannel: decode produced no packet for id " + id + ", passing through");
                return packet;
            }
            if (buf.isReadable()) {
                System.out.println("FakeChannel: " + buf.readableBytes() + " trailing bytes after decoding packet id " + id);
            }
            return decoded;
        } catch (Exception e) {
            System.out.println("FakeChannel: roundtrip failed for " + packet.getClass().getSimpleName() + ": " + e + ", passing through");
            return packet;
        } finally {
            buf.release();
        }
    }

    public void writeAndFlush(Packet packet) {
        incoming.offer(packet);
    }

    public boolean isActive() {
        return active.get();
    }

    public void close() {
        System.out.println("FakeChannel closed " + this);
        active.set(false);
        server.clientDisconnectEvent(this);
        processingThread.interrupt();
    }

    public SocketAddress remoteAddress() {
        return fakeAddress;
    }

    public String toString() {
        return "FakeChannel{" +
                "server=" + server +
                ", client=" + client +
                "sendMessagesToServer=" + sendMessagesToServer +
                '}';
    }

    //Attributes
    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return attributeMap.attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return attributeMap.hasAttr(key);
    }


    //Custom methods
    //TODO: Replace this with a real variable if we need performance boost
    public void setPlayer(Player player) {
        attr(PLAYER_KEY).set(player);
    }

    public Player getPlayer() {
        return attr(PLAYER_KEY).get();
    }
}
