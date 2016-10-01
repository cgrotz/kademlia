package de.cgrotz.kademlia;

import de.cgrotz.kademlia.config.Listener;
import de.cgrotz.kademlia.config.ListenerType;
import de.cgrotz.kademlia.config.UdpListener;
import de.cgrotz.kademlia.node.Key;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * Created by Christoph on 28.09.2016.
 */
@Data
@Builder
public class Configuration {

    private final Key nodeId;

    private final long getTimeoutMs;
    private final long networkTimeoutMs;
    private final int kValue;

    @Singular
    private final List<Listener> listeners;
    @Singular
    private final List<Listener> advertisedListeners;

    public static ConfigurationBuilder defaults() {
        return Configuration.builder()
                .getTimeoutMs(5000)
                .networkTimeoutMs(5000)
                .kValue(20);
                //.listener(new UdpListener("0.0.0.0", 9000))
                //.advertisedListener(new UdpListener("127.0.0.1", 9000));
    }
}
