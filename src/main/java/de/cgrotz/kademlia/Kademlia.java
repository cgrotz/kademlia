package de.cgrotz.kademlia;

import de.cgrotz.kademlia.client.KademliaClient;
import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.protocol.Codec;
import de.cgrotz.kademlia.protocol.ValueReply;
import de.cgrotz.kademlia.routing.RoutingTable;
import de.cgrotz.kademlia.server.KademliaServer;
import de.cgrotz.kademlia.storage.InMemoryStorage;
import de.cgrotz.kademlia.storage.LocalStorage;
import io.netty.util.internal.ConcurrentSet;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
public class Kademlia {
    private final RoutingTable routingTable;
    private final Key nodeId;
    private final String hostname;
    private final int port;
    private final KademliaClient client;
    private final KademliaServer server;

    private final Codec codec = new Codec();

    private final int kValue;
    private final LocalStorage localStorage;
    private final Node localNode;

    public Kademlia(Key nodeId, String hostname, int port) throws InterruptedException {
        this.nodeId = nodeId;
        this.hostname = hostname;
        this.port = port;
        this.kValue = 20;
        this.localNode = Node.builder().id(nodeId).address(hostname).port(port).build();

        this.client = new KademliaClient(nodeId, hostname, port);

        this.routingTable = new RoutingTable(kValue, nodeId, client);
        this.localStorage =  new InMemoryStorage();
        this.server = new KademliaServer(port, kValue, routingTable, localStorage,
                Node.builder().id(nodeId).address(hostname).port(port).build());
    }

    public void bootstrap(String hostname, int port) throws InterruptedException {
        client.sendPing(hostname, port, pong -> {
            routingTable.addNode(pong.getNodeId(), pong.getAddress(), pong.getPort());
        });

        // FIND_NODE with own IDs to find nearby nodes
        client.sendFindNode(hostname, port, nodeId, nodes -> {
            nodes.stream().forEach(node -> routingTable.addNode(node.getId(), node.getAddress(), node.getPort()));
        });

        // Refresh buckets
        for (int i = 1; i < Key.ID_LENGTH; i++) {
            // Construct a Key that is i bits away from the current node Id
            final Key current = this.nodeId.generateNodeIdByDistance(i);

            routingTable.getBucketStream()
                    .flatMap(bucket -> bucket.getNodes().stream())
                    .forEach(node -> {
                        client.sendFindNode(node.getAddress(), node.getPort(), current, nodes -> {
                            nodes.stream().forEach(newNode -> routingTable.addNode(newNode.getId(), newNode.getAddress(), newNode.getPort()));
                        });
                    });

        }
    }

    /**
     * Put or Update the value in the DHT
     *
     * @param key
     * @param value
     */
    public void put(Key key, String value) throws InterruptedException {
        int id = key.hashCode();
        client.sendFindNode(hostname, port, new Key(id), nodes -> {
                    nodes.stream().forEach(node -> {
                        client.sendContentToNode( node, key ,value);
                    });
                });
    }

    public String get(Key key) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        new Thread(() -> {
            get(key, valueReply -> {
                future.complete(valueReply.getValue());
            });
        }).start();
        return future.get();
    }


    public void get(Key key, Consumer<ValueReply> valueReplyConsumer) {
        if(localStorage.contains(key)) {
            valueReplyConsumer.accept(new ValueReply(-1,key, localStorage.get(key).getContent()));
        }
        else {
            ConcurrentSet<Node> alreadyCheckedNodes = new ConcurrentSet<>();
            AtomicBoolean found = new AtomicBoolean(false);
            get(found, key, routingTable.getBucketStream()
                    .flatMap(bucket -> bucket.getNodes().stream())
                    .sorted((node1, node2) -> node1.getId().getKey().xor(key.getKey()).abs()
                            .compareTo(node2.getId().getKey().xor(key.getKey()).abs()))
                    .collect(Collectors.toList()), alreadyCheckedNodes, valueReply -> {
                        if(!found.getAndSet(true)) {
                            valueReplyConsumer.accept(valueReply);
                        }
                    });
        }
    }

    private void get(AtomicBoolean found, Key key, List<Node> nodes, ConcurrentSet<Node> alreadyCheckedNodes, Consumer<ValueReply> valueReplyConsumer) {
        for( Node node : nodes) {
            if(!alreadyCheckedNodes.contains(node) && !found.get()) {
                client.sendFindValue(node.getAddress(), node.getPort(),
                        key, nodeReply -> {
                            nodeReply.getNodes().stream().forEach(newNode -> routingTable.addNode(newNode.getId(), newNode.getAddress(), newNode.getPort()));
                            get(found, key, nodeReply.getNodes(), alreadyCheckedNodes, valueReplyConsumer);
                        }, valueReplyConsumer);

                alreadyCheckedNodes.add(node);
            }
        }
    }

    public Node getLocalNode() {
        return localNode;
    }
}
