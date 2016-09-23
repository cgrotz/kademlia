package de.cgrotz.kademlia;

import de.cgrotz.kademlia.client.KademliaClient;
import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import de.cgrotz.kademlia.protocol.Codec;
import de.cgrotz.kademlia.protocol.Connect;
import de.cgrotz.kademlia.protocol.FindNode;
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

    private final int kValue;

    public Kademlia(NodeId nodeId, String hostname, int port) {
        this.nodeId = nodeId;
        this.hostname = hostname;
        this.port = port;
        this.kValue = 20;

        this.routingTable = new RoutingTable(kValue, nodeId);

        this.client = new KademliaClient(routingTable);
        this.server = new KademliaServer(port, kValue, routingTable,
                Node.builder().id(nodeId).address(hostname).port(port).build());
    }

    public void bootstrap(String hostname, int port) throws InterruptedException {
        client.send(hostname, port, codec.encode(new Connect(seqId.incrementAndGet(), nodeId, this.hostname, this.port)));

        // FIND_NODE with own IDs to find nearby nodes
        client.send(hostname, port,
            codec.encode(new FindNode(seqId.incrementAndGet(), nodeId))
        );

        // Refresh buckets
        for(int i = 1; i < NodeId.ID_LENGTH; i++) {
            // Construct a NodeId that is i bits away from the current node Id
            final NodeId current = this.nodeId.generateNodeIdByDistance(i);

            routingTable.getBucketStream()
                    .flatMap(bucket -> bucket.getNodes().stream())
                    .forEach(node -> {
                        try {
                            client.send(node.getAddress(), node.getPort(),
                                    codec.encode(new FindNode(seqId.incrementAndGet(), current))
                            );
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });

        }
    }
}
