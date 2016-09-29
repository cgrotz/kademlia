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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Christoph on 21.09.2016.
 */
public class Kademlia {
    private static final Logger LOGGER = LoggerFactory.getLogger(Kademlia.class);

    protected final RoutingTable routingTable;
    protected final KademliaClient client;
    protected final KademliaServer server;

    protected final LocalStorage localStorage;
    protected final Node localNode;

    protected final Configuration config;

    public Kademlia(Key nodeId, String hostname, int port) {
        this(Configuration.defaults()
                .nodeId(nodeId)
                .bindingAddress(hostname).bindingPort(port)
                .advertisingAddress(hostname).advertisingPort(port)
                .build());
    }

    public Kademlia(Configuration config){
        this.config = config;
        this.localNode = Node.builder().id(config.getNodeId())
                .address(config.getAdvertisingAddress())
                .port(config.getAdvertisingPort())
                .build();

        this.client = new KademliaClient(config, localNode);

        this.routingTable = new RoutingTable(config.getKValue(), config.getNodeId(), client);
        this.localStorage =  new InMemoryStorage();
        this.server = new KademliaServer(config.getBindingAddress(), config.getBindingPort(),
                config.getKValue(), routingTable, localStorage, localNode);
    }

    public void bootstrap(String hostname, int port) {
        LOGGER.info("bootstrapping node={}", localNode);
        client.sendPing(hostname, port, pong -> {
            LOGGER.info("bootstrapping node={}, ping from remote={}:{} received", localNode, hostname, port);
            routingTable.addNode(pong.getOrigin().getId(), pong.getOrigin().getAddress(), pong.getOrigin().getPort());
        });

        // FIND_NODE with own IDs to find nearby nodes
        client.sendFindNode(hostname, port, localNode.getId(), nodes -> {
            LOGGER.info("bootstrapping node={}, sendFind node from remote={}:{} received, nodes={}", localNode, hostname, port, nodes.size());
            nodes.stream().forEach(node -> routingTable.addNode(node.getId(), node.getAddress(), node.getPort()));
        });

        LOGGER.info("bootstrapping node={}, refreshing buckets", localNode);
        refreshBuckets();
    }

    /**
     * Put or Update the value in the DHT
     *
     * @param key
     * @param value
     */
    public void put(Key key, String value) {
        client.sendFindNode(localNode.getAddress(), localNode.getPort(), key, nodes -> {
            nodes.stream().forEach(node -> {
                client.sendContentToNode( node, key ,value);
            });
        });
    }

    /**
     *
     * Retrieve the Value associated with the Key
     *
     * @param key
     * @return
     */
    public String get(Key key) {
        CompletableFuture<String> future = new CompletableFuture<>();
        new Thread(() -> {
            get(key, valueReply -> {
                future.complete(valueReply.getValue());
            });
        }).start();
        try {
            return future.get(config.getGetTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new de.cgrotz.kademlia.exception.TimeoutException(e);
        }
    }


    public void get(Key key, Consumer<ValueReply> valueReplyConsumer) {
        if(localStorage.contains(key)) {
            valueReplyConsumer.accept(new ValueReply(-1,localNode, key, localStorage.get(key).getContent()));
        }
        else {
            ConcurrentSet<Node> alreadyCheckedNodes = new ConcurrentSet<>();
            AtomicBoolean found = new AtomicBoolean(false);
            List<Node> nodes = routingTable.getBucketStream()
                    .flatMap(bucket -> bucket.getNodes().stream())
                    .sorted((node1, node2) -> node1.getId().getKey().xor(key.getKey()).abs()
                            .compareTo(node2.getId().getKey().xor(key.getKey()).abs()))
                    .collect(Collectors.toList());

            get(found, key, nodes, alreadyCheckedNodes, valueReply -> {
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

    /**
     * Execute key republishing
     *
     * Iterate over all keys that weren't updated within the last hour and republish
     * to the other k-nodes that are closest to the associated keys
     */
    public void republishKeys() {
        KeyRepublishing.builder()
                .kademliaClient(client)
                .localStorage(localStorage)
                .routingTable(routingTable)
                .k(config.getKValue())
                .build().execute();
    }

    public void refreshBuckets() {
        // Refresh buckets
        for (int i = 1; i < Key.ID_LENGTH; i++) {
            // Construct a Key that is i bits away from the current node Id
            final Key current = this.localNode.getId().generateNodeIdByDistance(i);

            routingTable.getBucketStream()
                    .flatMap(bucket -> bucket.getNodes().stream())
                    .forEach(node -> {
                        client.sendFindNode(node.getAddress(), node.getPort(), current, nodes -> {
                            nodes.stream().forEach(newNode -> routingTable.addNode(newNode.getId(), newNode.getAddress(), newNode.getPort()));
                        });
                    });

        }
    }

    public void close() {
        server.close();
        client.close();
    }
}
