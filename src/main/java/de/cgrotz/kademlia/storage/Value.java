package de.cgrotz.kademlia.storage;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Christoph on 27.09.2016.
 */
@Builder
@Data
public class Value {
    private long lastPublished;
    private String content;
}
