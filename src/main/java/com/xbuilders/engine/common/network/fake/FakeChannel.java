package com.xbuilders.engine.common.network.fake;

import com.xbuilders.Main;
import com.xbuilders.engine.common.network.ChannelBase;
import com.xbuilders.engine.common.network.packet.Packet;
import com.xbuilders.engine.common.players.Player;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.DefaultAttributeMap;

import java.net.SocketAddress;
import java.util.HashMap;
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
                        if (sendMessagesToServer) {
                            server.receive(reverseChannel, packet);
                        } else {
                            client.receive(packet);
                        }
                    } catch (Exception e) {
                        Main.LOGGER.warn("Failed to receive fake packet", e);
                    }
                }
            } catch (InterruptedException ignored) {
                com.xbuilders.Main.LOGGER.error("FakeChannel interrupted!", ignored);
            }
        });
        processingThread.start();
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
