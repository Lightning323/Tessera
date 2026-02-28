package netty;

import com.xbuilders.engine.common.network.ChannelBase;
import com.xbuilders.engine.common.network.fake.FakeClient;
import com.xbuilders.engine.common.network.fake.FakeServer;
import com.xbuilders.engine.common.network.packet.Packet;
import com.xbuilders.engine.common.packets.MessagePacket;
import com.xbuilders.engine.common.network.netty.ping.PingPacket;
import com.xbuilders.engine.common.network.netty.ping.PongPacket;

public class SimulatedServerClient {

    public static void main(String[] args) throws InterruptedException {
        Packet.register(new MessagePacket());
        Packet.register(new PingPacket());
        Packet.register(new PongPacket());

        FakeServer server = new FakeServer() {
            @Override
            public boolean newClientEvent(ChannelBase client) {
                System.out.println("New client: " + client.remoteAddress());
                client.writeAndFlush(new MessagePacket("Hello from server!"));
                return true;
            }

            @Override
            public void clientDisconnectEvent(ChannelBase client) {
                System.out.println("Client disconnected: " + client.remoteAddress());
            }
        };

        Thread.sleep(1000); // Give the server a second

        FakeClient client = new FakeClient(server) {
            @Override
            public void onConnected(boolean success, Throwable cause, ChannelBase channel) {
                System.out.println("Successfully connected to the FakeServer!");
                new Thread(() -> {
                    while (isConnected()) {
                        try {
                            channel.writeAndFlush(new MessagePacket("Hello World!"));
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
        };

    }
}
