package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConnectionAcknowledge extends Message {

    private Node node;

    public ConnectionAcknowledge(long seqId, NodeId nodeId, String address, int port) {
        super(MessageType.CONNECT, seqId);
        this.node = Node.builder().id(nodeId).address(address).port(port).lastSeen(System.currentTimeMillis()).build();
    }
}
