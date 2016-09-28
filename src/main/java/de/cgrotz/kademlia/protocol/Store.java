package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * Stores a (key, value) pair in one node.
 *
 * Created by Christoph on 22.09.2016.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Store extends Message {
    private final String value;
    private final Key key;

    public Store(long seqId, Node origin, Key key, String value) {
        super(MessageType.STORE, seqId, origin);
        this.key = key;
        this.value = value;
    }
}
