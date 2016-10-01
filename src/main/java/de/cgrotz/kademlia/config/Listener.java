package de.cgrotz.kademlia.config;

import de.cgrotz.kademlia.exception.UnknownListenerType;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Christoph on 30.09.2016.
 */
@Data
@Builder
public class Listener {
    private ListenerType type;

    public static Listener fromUrl(String url) {
        if(url.startsWith(ListenerType.UDP.prefix())) {
            return UdpListener.from(url);
        }
        else {
            throw new UnknownListenerType(url);
        }
    }
}
