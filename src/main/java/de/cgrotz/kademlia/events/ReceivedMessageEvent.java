package de.cgrotz.kademlia.events;

import de.cgrotz.kademlia.protocol.Message;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Christoph on 06.10.2016.
 */
@Data
@Builder
public class ReceivedMessageEvent extends Event {
    private Message message;
}
