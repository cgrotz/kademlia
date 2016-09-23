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
public class FindNodeReply extends Message {

    private final List<Node> nodes;

    public FindNodeReply(long seqId, List<Node> nodes) {
        super(MessageType.FIND_NODE_REPLY, seqId);
        this.nodes = nodes;
    }
}
