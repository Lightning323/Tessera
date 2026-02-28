package com.xbuilders;

import com.xbuilders.content.vanilla.XbuildersGame;
import com.xbuilders.engine.server.Server;

public class Main_DedicatedServer {

    public static Server localServer;
    public static XbuildersGame game;

    public static void main(String[] args) {
        System.out.println("Server started: " + Server.VERSION);
        game = new XbuildersGame();
        localServer = new Server(game);
    }
}
