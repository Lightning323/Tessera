package com.tessera.engine.common.network;

public abstract class ServerBase {

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

    /**
     * Closes the localServer and all client connections.
     */
    public abstract void close();

}
