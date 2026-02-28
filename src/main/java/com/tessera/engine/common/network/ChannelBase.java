package com.tessera.engine.common.network;

import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.players.Player;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;

import java.net.SocketAddress;

public abstract class ChannelBase implements AttributeMap {
    public abstract void writeAndFlush(Packet packet);

    public abstract boolean isActive();

    public SocketAddress remoteAddress() {
        return null;
    }

    public abstract void close();


    //Custom methods
    public static final AttributeKey<Player> PLAYER_KEY = AttributeKey.valueOf("player");

    public abstract void setPlayer(Player player);

    public abstract Player getPlayer();
}
