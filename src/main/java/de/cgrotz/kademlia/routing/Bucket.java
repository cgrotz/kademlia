package de.cgrotz.kademlia.routing;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import io.netty.util.internal.ConcurrentSet;
import lombok.Data;

import java.util.TreeSet;

/**
 * Created by Christoph on 22.09.2016.
 */
@Data
public class Bucket {
    private final int bucketId;

    private final TreeSet<Node> nodes = new TreeSet<>();
    private final TreeSet<Node> replacementNodes = new TreeSet<>();
    private final int k;

    public Bucket(int k, int bucketId) {
        this.k = k;
        this.bucketId = bucketId;
    }

    public void addNode(NodeId nodeId, String host, int port) {
        Node node = Node.builder().id(nodeId).address(host).port(port).build();
        if(nodes.size() < k) {
            if(nodes.contains(node)) {
                nodes.remove(node);
                nodes.add(node);
            }
            else {
                nodes.add(node);
            }
        }
        else {
            if(replacementNodes.contains(node)) {
                replacementNodes.remove(node);
                replacementNodes.add(node);
            }
            else {
                replacementNodes.add(node);
            }
        }
    }

    public TreeSet<Node> getNodes() {
        TreeSet<Node> set = new TreeSet<>();
        set.addAll(nodes);
        return set;
    }
}
