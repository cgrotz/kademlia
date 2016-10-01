package de.cgrotz.kademlia.node;

import de.cgrotz.kademlia.config.Listener;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;

import java.util.List;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
@EqualsAndHashCode(of={"id"})
@Builder
public class Node implements Comparable<Node>{
    private Key id;
    @Singular
    private final List<Listener> advertisedListeners;
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
