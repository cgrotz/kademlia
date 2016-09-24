package de.cgrotz.kademlia.routing;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Christoph on 21.09.2016.
 */
@ToString
@EqualsAndHashCode
public class RoutingTable {

    private final NodeId localNodeId;

    private final Bucket[] buckets;

    public RoutingTable(int k, NodeId localNodeId) {
        this.localNodeId = localNodeId;
        buckets = new Bucket[NodeId.ID_LENGTH];
        for (int i = 0; i < NodeId.ID_LENGTH; i++)
        {
            buckets[i] = new Bucket(k, i);
        }
    }

    /**
     * Compute the bucket ID in which a given node should be placed; the bucketId is computed based on how far the node is away from the Local Node.
     *
     * @param nid The NodeId for which we want to find which bucket it belong to
     *
     * @return Integer The bucket ID in which the given node should be placed.
     */
    public final int getBucketId(NodeId nid)
    {
        int bId = this.localNodeId.getDistance(nid) - 1;

        /* If we are trying to insert a node into it's own routing table, then the bucket ID will be -1, so let's just keep it in bucket 0 */
        return bId < 0 ? 0 : bId;
    }

    public void addNode(NodeId nodeId, String host, int port) {
        if(!nodeId.equals(localNodeId)) {
            buckets[getBucketId(nodeId)].addNode(nodeId, host, port);
        }
        else {
            System.out.println("Routing table of node="+nodeId+" can't contain itself. (localNodeId="+localNodeId+")");
        }
    }

    public Bucket[] getBuckets() {
        return buckets;
    }

    public Stream<Bucket> getBucketStream() {
        return Arrays.stream(buckets);
    }

    public List<Node> findClosest(NodeId lookupId, int numberOfRequiredNodes) {
        return getBucketStream().flatMap(bucket -> bucket.getNodes().stream())
                .sorted((node1,node2) -> node1.getId().getKey().xor(lookupId.getKey()).abs()
                        .compareTo( node2.getId().getKey().xor(lookupId.getKey()).abs() ))
                .limit(numberOfRequiredNodes).collect(Collectors.toList());
    }
}
