package de.cgrotz.kademlia;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Christoph on 28.09.2016.
 */
@Data
@Builder
public class Configuration {

    private final long getTimeoutMs;
    private final long networkTimeoutMs;
    private final int kValue;

    public static Configuration buildDefault() {
        return Configuration.builder()
                .getTimeoutMs(5000)
                .networkTimeoutMs(5000)
                .kValue(20)
                .build();
    }
}
