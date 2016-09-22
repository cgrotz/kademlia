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
public class Connect extends Message{

    private final String nodeId;
    private final String host;
    private final int port;

    public Connect(long seqId, NodeId nodeId, String host, int port) {
        super(MessageType.CONNECT, seqId);
        this.nodeId = nodeId.toString();
        this.host = host;
        this.port = port;
    }
}
