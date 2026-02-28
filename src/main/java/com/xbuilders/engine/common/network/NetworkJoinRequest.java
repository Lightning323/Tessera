package com.xbuilders.engine.common.network;

public class NetworkJoinRequest {
    public boolean hosting;
    public int port;
    public String playerName;
    public String address;

    public NetworkJoinRequest(boolean hosting, int portVal, String playerName, String ipAdress) {
        this.hosting = hosting;
        this.port = portVal;
        this.playerName = playerName;
        this.address = ipAdress;
    }

    @Override
    public String toString() {
        return "NetworkJoin{" +
                "hosting=" + hosting +
                ", portVal=" + port +
                ", playerName='" + playerName + '\'' +
                ", ipAdress='" + address + '\'' +
                '}';
    }
}
