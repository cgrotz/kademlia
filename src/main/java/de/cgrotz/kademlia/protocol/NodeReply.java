package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * Created by Christoph on 23.09.2016.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NodeReply extends Message {

    private final List<Node> nodes;

    public NodeReply(long seqId, Node origin, List<Node> nodes) {
        super(MessageType.NODE_REPLY, seqId, origin);
        this.nodes = nodes;
    }
}
