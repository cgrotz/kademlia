package de.cgrotz.kademlia;

import de.cgrotz.kademlia.node.Key;
import lombok.Builder;
import lombok.Data;

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

    private final String bindingAddress;
    private final int bindingPort;

    private final String advertisingAddress;
    private final int advertisingPort;

    public static ConfigurationBuilder defaults() {
        return Configuration.builder()
                .getTimeoutMs(5000)
                .networkTimeoutMs(5000)
                .kValue(20)
                .bindingAddress("0.0.0.0")
                .bindingPort(9000)
                .advertisingAddress("127.0.0.1")
                .advertisingPort(9000);
    }
}
