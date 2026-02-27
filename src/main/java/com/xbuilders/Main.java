package com.xbuilders;

import com.xbuilders.content.vanilla.XbuildersGame;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.SkinRegistry;
import com.xbuilders.engine.server.Server;
import com.xbuilders.engine.common.utils.LoggingUtils;
import com.xbuilders.engine.common.resource.ResourceLister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The client contains everything ONLY on the client
 * The server contains everything ONLY on the server
 * the Main class contains everything shared by both
 */
public class Main {


    private static Client client;
    private static Server localServer;

    public static XbuildersGame game;
    public static SkinRegistry skins;

    public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);


    public static Client getClient() {
        return client;
    }

    public static Server getServer() {
        return localServer; //TODO: If on a dedicated server, use dedicated server object instead
    }

    public static void setServer(Server server) {
        localServer = server;
    }


    public static void main(String[] args) throws Exception {
        ResourceLister.init();//This takes almost 10s, so it mind as well come first

        skins = new SkinRegistry();
        game = new XbuildersGame();

        client = new Client(args, game);


        try {
            getClient().window.startWindowThread();
        } catch (Exception e) {
            LOGGER.error("Window Thread Failed", e);
        } finally {
            getClient().window.destroyWindow();
        }

    }


    public static long versionStringToNumber(String version) {
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = Integer.parseInt(parts[2]);
        // Combine parts into a single number by shifting bits or scaling by powers of 1000.
        return (major * 1_000_000L) + (minor * 1_000L) + patch;
    }

}
