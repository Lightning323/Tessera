package com.tessera.engine.common.network.fake;

import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.ClientBase;
import com.tessera.engine.common.network.packet.Packet;

public abstract class FakeClient extends ClientBase {

    private final FakeServer server;
    private FakeChannel channel;

    public FakeClient(FakeServer server) {
        this.server = server;
        connect();
    }

    public ChannelBase getChannel() {
        return channel;
    }

    private void connect() {
        this.channel = new FakeChannel(server, this, true); // client-side channel
        this.channel.makeReverseChannel(); //A reverse channel so that the server can send us packets

        //Get the server where send message to server is FALSE
        FakeChannel serverChannel = channel.reverseChannel;
        server.connect(serverChannel); //Make a server-side channel that the server can send us packets

        onConnected(true, null, channel);
    }

    public abstract void onConnected(boolean success, Throwable cause, ChannelBase channel);

    protected void receive(Packet packet) {
        //System.out.println("Client received: " + packet);
        packet.handleClientSide(channel, packet);
    }

    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    public void close() {
        channel.close();
    }
}
