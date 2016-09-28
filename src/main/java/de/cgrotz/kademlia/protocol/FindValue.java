package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * Same as FIND_NODE, but if the recipient of the request has the requested key in its store, it will return the corresponding value.
 *
 * Created by Christoph on 22.09.2016.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FindValue extends Message {

    private final Key key;

    public FindValue(long seqId, Node origin, Key key) {
        super(MessageType.FIND_VALUE, seqId, origin);
        this.key = key;
    }
}
