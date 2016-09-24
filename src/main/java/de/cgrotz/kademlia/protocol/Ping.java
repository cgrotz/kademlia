package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.NodeId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * Used to verify that a node is still alive.
 *
 * Created by Christoph on 22.09.2016.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Ping extends Message {
    private final NodeId nodeId;
    private final String address;
    private final int port;

    public Ping(long seqId, NodeId nodeId, String address, int port) {
        super(MessageType.PING, seqId);
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
    }
}
