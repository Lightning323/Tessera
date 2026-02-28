package com.tessera.engine.common.network.fake;

import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.ServerBase;
import com.tessera.engine.common.network.packet.Packet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class FakeServer extends ServerBase {
    private final List<FakeChannel> clients = new CopyOnWriteArrayList<>();

    //Whenever we receive a connection from a client
    protected FakeChannel connect(FakeChannel channel) {
        clients.add(channel);
        boolean accepted = newClientEvent(channel);
        if (!accepted) {
            channel.close();
        }
        return channel;
    }

    public abstract boolean newClientEvent(ChannelBase client);

    public abstract void clientDisconnectEvent(ChannelBase client);

    protected void receive(FakeChannel client, Packet packet) {
        //System.out.println("Server received: " + packet);
        packet.handleServerSide(client, packet);
    }

    public void close(){
        for (FakeChannel client : clients) {
            client.close();
        }
    }
}
