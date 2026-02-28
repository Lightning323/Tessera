package com.tessera.engine.client.visuals.topMenu.multiplayer;

public class ServerEntry {
    public String name;
    public String address;
    public int port;

    public ServerEntry(String name, String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }
}
