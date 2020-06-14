package de.cgrotz.kademlia.routing;

import de.cgrotz.kademlia.client.KademliaClient;
import de.cgrotz.kademlia.exception.KademliaTimeoutException;
import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.protocol.Pong;
import lombok.Data;

import java.util.TreeSet;

/**
 * Created by Christoph on 22.09.2016.
 */
@Data
public class Bucket {
    private final int bucketId;

    private final TreeSet<Node> nodes;
    private final TreeSet<Node> replacementNodes;
    private final int k;
    private final KademliaClient client;

    public Bucket(KademliaClient client, int k, int bucketId) {
        this.k = k;
        this.bucketId = bucketId;
        this.client = client;

        this.nodes = new TreeSet<>();
        this.replacementNodes = new TreeSet<>();
    }

    public void addNode(Node node) {
        if(nodes.size() < k) {
            nodes.add(node);
        }
        else {
            Node last = nodes.last();
            try {
                client.sendPing(last, message -> {
                    Pong pong = (Pong)message;
                    nodes.remove(last);
                    last.setLastSeen(System.currentTimeMillis());
                    nodes.add(last);
                    replacementNodes.add(node);
                    if(replacementNodes.size() > k) {
                        replacementNodes.remove(replacementNodes.last());
                    }
                });
            } catch (KademliaTimeoutException e) {
                nodes.remove(last);
                nodes.add(node);
                return;
            }
        }
    }

    public TreeSet<Node> getNodes() {
        TreeSet<Node> set = new TreeSet<>();
        set.addAll(nodes);
        return set;
    }

    public void refreshBucket() {
        @SuppressWarnings("unchecked") TreeSet<Node> copySet = new TreeSet(nodes);
        // Check nodes on reachability and update
        copySet.stream().forEach(node -> {
            try {
                client.sendPing(node, pong -> {
                    nodes.remove(node);
                    node.setLastSeen(System.currentTimeMillis());
                    nodes.add(node);
                });
            }
            catch(KademliaTimeoutException exp) {
                nodes.remove(node);
            }
        });

        // Fill up with reachable nodes from replacement set
        while(nodes.size() < k && !replacementNodes.isEmpty()) {
            Node node = replacementNodes.first();
            try {
                client.sendPing(node, pong -> {
                    replacementNodes.remove(node);
                    node.setLastSeen(System.currentTimeMillis());
                    nodes.add(node);
                });
            }
            catch(KademliaTimeoutException exp) {
                replacementNodes.remove(node);
            }
        }
    }

    public void retireNode(Node nodeToRetire) {
        nodes.remove(nodeToRetire);

        // Fill up with reachable nodes from replacement set
        while(nodes.size() < k && !replacementNodes.isEmpty()) {
            Node node = replacementNodes.first();
            try {
                client.sendPing(node, pong -> {
                    replacementNodes.remove(node);
                    node.setLastSeen(System.currentTimeMillis());
                    nodes.add(node);
                });
            }
            catch(KademliaTimeoutException exp) {
                replacementNodes.remove(node);
            }
        }
    }
}
