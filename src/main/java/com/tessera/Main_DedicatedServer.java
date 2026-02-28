package com.tessera;

import com.tessera.content.vanilla.Game;
import com.tessera.engine.server.Server;

public class Main_DedicatedServer {

    public static Server localServer;
    public static Game game;

    public static void main(String[] args) {
        System.out.println("Server started: " + Server.VERSION);
        game = new Game();
        localServer = new Server(game);
    }
}
