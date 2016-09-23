package de.cgrotz.kademlia.protocol;

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
public class ValueReply extends Message {
    private final String value;
    private final String key;

    public ValueReply(long seqId, String key, String value) {
        super(MessageType.VALUE_REPLY, seqId);
        this.key = key;
        this.value = value;
    }
}
