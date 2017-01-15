package de.cgrotz.kademlia.server;

import de.cgrotz.kademlia.events.Event;
import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.routing.RoutingTable;
import de.cgrotz.kademlia.storage.LocalStorage;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
public class KademliaServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaServer.class);

    private final RoutingTable routingTable;
    private final Node localNode;
    private final int kValue;
    private final LocalStorage localStorage;
    private final Map<String, Consumer<Event>> eventConsumers;
    private final DatagramSocket socket;
    private final Thread serverThread;

    public KademliaServer(String bindingAddress, int port, int kValue, RoutingTable routingTable, LocalStorage localStorage, Node localNode, Map<String, Consumer<Event>> eventConsumers) throws SocketException {
        this.routingTable = routingTable;
        this.localNode = localNode;
        this.kValue = kValue;
        this.localStorage = localStorage;
        this.eventConsumers = eventConsumers;

        this.socket = new DatagramSocket(port);
        KademliaServerHandler serverHandler = new KademliaServerHandler(this.routingTable, this.localStorage, this.localNode, this.kValue, this.eventConsumers, socket);
        this.serverThread = new Thread(() -> {
            try {
                byte[] receiveData = new byte[1024];
                while(true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    serverHandler.channelRead(receivePacket, receiveData);
                    Arrays.fill(receiveData, (byte)0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        LOGGER.info("Kademlia Listener started");
    }

    public void close() {
        serverThread.stop();
        socket.close();
    }
}
