package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Node;
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

    public Ping(long seqId, Node origin) {
        super(MessageType.PING, seqId, origin);
    }
}
