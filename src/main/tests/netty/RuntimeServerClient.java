package netty;

import com.tessera.engine.common.network.ChannelBase;
import com.tessera.engine.common.network.netty.NettyClient;
import com.tessera.engine.common.network.netty.NettyServer;
import com.tessera.engine.common.network.packet.Packet;
import com.tessera.engine.common.packets.MessagePacket;
import com.tessera.engine.common.network.netty.ping.PingPacket;
import com.tessera.engine.common.network.netty.ping.PongPacket;

public class RuntimeServerClient {

    public static void main(String[] args) throws Exception {

        Packet.register( new MessagePacket());
        Packet.register( new PingPacket());
        Packet.register( new PongPacket());

        NettyServer server = new NettyServer(8080) {
            @Override
            public boolean newClientEvent(ChannelBase client) {
                System.out.println("New client: " + client.remoteAddress());
              //  System.out.println("All clients: " + clients.toString());
                client.writeAndFlush(new MessagePacket("Hello from server!"));
                return true;
            }

            @Override
            public void clientDisconnectEvent(ChannelBase client) {
                System.out.println("Client disconnected: " + client.remoteAddress());
            }
        };


        Thread.sleep(10000);
        NettyClient client = new NettyClient("localhost", 8080) {
            public void onConnected(boolean success, Throwable cause, ChannelBase channel) {
                if (success) {
                    System.out.println("Successfully connected to the localServer!");

                    //Schedule this on another thread
                    new Thread(() -> {
                        while (true) {
                            if (channel.isActive()) {
                                try {
                                    channel.writeAndFlush(new MessagePacket("Hello World!"));
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }).start();


                } else {
                    // This block will be executed if the connection fails
                    System.err.println("Failed to connect to the localServer: " + cause);
                }
            }
        };


    }
}
