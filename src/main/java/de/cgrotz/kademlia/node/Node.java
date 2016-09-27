package de.cgrotz.kademlia.node;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
@EqualsAndHashCode(of={"id"})
@Builder
public class Node implements Comparable<Node>{
    private Key id;
    private String address;
    private int port;
    private long lastSeen = System.currentTimeMillis();

    @Override
    public int compareTo(Node o) {
        if (this.equals(o))
        {
            return 0;
        }

        return (this.lastSeen > o.lastSeen) ? 1 : -1;
    }
}
