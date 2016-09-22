package de.cgrotz.kademlia.node;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
@Builder
public class Node {
    private NodeId id;
    private String address;
    private int port;

}
