package de.cgrotz.kademlia.routing;

import de.cgrotz.kademlia.node.NodeId;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by Christoph on 21.09.2016.
 */
@ToString
@EqualsAndHashCode
public class RoutingTable {

    private final NodeId localNodeId;

    public RoutingTable(NodeId localNodeId) {
        this.localNodeId = localNodeId;
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
        System.out.println("Adding node "+nodeId+"=>"+host+":"+port);
    }
}
