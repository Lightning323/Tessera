package com.tessera.engine.common.network;

public abstract class ClientBase {

    public abstract ChannelBase getChannel();

    public abstract void onConnected(boolean success, Throwable cause, ChannelBase channel);

    public abstract void close();
}
