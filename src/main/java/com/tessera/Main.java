package com.tessera;

import com.tessera.content.vanilla.TesseraGame;
import com.tessera.engine.client.Client;
import com.tessera.engine.SkinRegistry;
import com.tessera.engine.server.Server;
import com.tessera.engine.utils.ErrorHandler;
import com.tessera.engine.utils.resource.ResourceLister;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * The client contains everything ONLY on the client
 * The server contains everything ONLY on the server
 * the Main class contains everything shared by both
 */
public class Main {


    public static final String TITLE = "Tessera";
    public static final String VERSION = "1.0.0";

    private static Client localClient;
    public static TesseraGame game;
    public static SkinRegistry skins;
    public static Logger LOGGER = Logger.getLogger(Main.class.getName());


    public static Client getClient() {
        return localClient;
    }

    public static Server getServer() {
        return Client.localServer;
    }


    public static void main(String[] args) throws Exception {
        System.out.println("Client started: " + VERSION);
        ResourceLister.init();//This takes almost 10s, so it mind as well come first

        skins = new SkinRegistry();
        game = new TesseraGame();

        localClient = new Client(args, VERSION, game);



        try {
            getClient().window.startWindowThread();
        } catch (Exception e) {
            ErrorHandler.createPopupWindow(
                    getClient().title + " has crashed",
                    getClient().title + " has crashed: \"" + (e.getMessage() != null ? e.getMessage() : "unknown error") + "\"\n\n" +
                            "Stack trace:\n" +
                            String.join("\n", Arrays.toString(e.getStackTrace()).split(",")) +
                            "\n\n Log saved to clipboard.");
            ErrorHandler.log(e, "Fatal Error");
        } finally {
            getClient().window.destroyWindow();
        }

    }

}
