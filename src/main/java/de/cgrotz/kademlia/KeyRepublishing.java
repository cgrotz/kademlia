package de.cgrotz.kademlia;

import de.cgrotz.kademlia.client.KademliaClient;
import de.cgrotz.kademlia.exception.KademliaTimeoutException;
import de.cgrotz.kademlia.routing.RoutingTable;
import de.cgrotz.kademlia.storage.LocalStorage;
import de.cgrotz.kademlia.storage.Value;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Christoph on 27.09.2016.
 */
@Data
@Builder
public class KeyRepublishing {
    private final LocalStorage localStorage;
    private final KademliaClient kademliaClient;
    private final RoutingTable routingTable;
    private final int k;

    public void execute() {
        localStorage.getKeysBeforeTimestamp(System.currentTimeMillis() - 3600*1000)
        .stream().forEach( key -> {
            Value value = localStorage.get(key);
            routingTable.findClosest(key, k).stream().forEach( node -> {
                try {
                    kademliaClient.sendContentToNode(node, key, value.getContent());
                }
                catch (KademliaTimeoutException exp) {
                    routingTable.retireNode(node);
                }
            });
            localStorage.updateLastPublished(key, System.currentTimeMillis());
        });
    }
}
