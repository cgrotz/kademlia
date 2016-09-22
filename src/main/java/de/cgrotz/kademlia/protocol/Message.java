package de.cgrotz.kademlia.protocol;

import lombok.Data;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
public abstract class Message {
    private final MessageType type;
    private final long seqId;
}
