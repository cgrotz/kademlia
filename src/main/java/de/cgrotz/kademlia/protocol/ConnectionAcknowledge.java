package de.cgrotz.kademlia.protocol;

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

    private String nodeId;

    public ConnectionAcknowledge(long seqId, NodeId nodeId) {
        super(MessageType.CONNECT, seqId);
        this.nodeId = nodeId.toString();
    }
}
