package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * The recipient of the request will return the k nodes in his own buckets that are the closest ones to the requested key.
 *
 * Created by Christoph on 22.09.2016.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FindNode extends Message {

    private final Key lookupId;

    public FindNode(long seqId, Node origin, Key lookupId) {
        super(MessageType.FIND_NODE, seqId, origin);
        this.lookupId = lookupId;
    }
}
