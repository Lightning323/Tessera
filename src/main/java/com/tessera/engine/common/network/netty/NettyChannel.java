package com.tessera.engine.common.network.netty;

import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.players.Player;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

public class NettyChannel extends ChannelBase {
    private final Channel channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void writeAndFlush(Packet packet) {
        channel.writeAndFlush(packet);
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public void close() {
        channel.close();
    }

    //Attributes
    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return channel.attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return channel.hasAttr(key);
    }


    //Custom methods
    public void setPlayer(Player player){
        channel.attr(PLAYER_KEY).set(player);
    }

    public Player getPlayer(){
        return channel.attr(PLAYER_KEY).get();
    }
}
