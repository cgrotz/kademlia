package de.cgrotz.kademlia;

import de.cgrotz.kademlia.client.KademliaClient;
import de.cgrotz.kademlia.node.NodeId;
import de.cgrotz.kademlia.protocol.Codec;
import de.cgrotz.kademlia.protocol.Connect;
import de.cgrotz.kademlia.routing.RoutingTable;
import de.cgrotz.kademlia.server.KademliaServer;
import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
public class Kademlia {
    private final RoutingTable routingTable;
    private AtomicLong seqId = new AtomicLong();
    private final NodeId nodeId;
    private final String hostname;
    private final int port;
    private final KademliaClient client;
    private final KademliaServer server;

    private final Codec codec = new Codec();

    public Kademlia(NodeId nodeId, String hostname, int port) {
        this.nodeId = nodeId;
        this.hostname = hostname;
        this.port = port;
        this.routingTable = new RoutingTable(nodeId);

        this.client = new KademliaClient();
        this.server = new KademliaServer(port, routingTable, nodeId);
    }

    public void connect(String hostname, int port) throws InterruptedException {
        this.client.send(hostname, port, codec.encode(new Connect(seqId.incrementAndGet(), nodeId, this.hostname, this.port)));
    }
}
