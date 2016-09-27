package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * Response to Ping
 *
 * Created by Christoph on 22.09.2016.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Pong extends Message {
    private final Key nodeId;
    private final String address;
    private final int port;

    public Pong(long seqId, String nodeId, String address, int port) {
        super(MessageType.PONG, seqId);
        this.nodeId = Key.build(nodeId);
        this.address = address;
        this.port = port;
    }
}
