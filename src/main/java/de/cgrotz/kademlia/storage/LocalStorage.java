package de.cgrotz.kademlia.storage;

import de.cgrotz.kademlia.node.Key;

/**
 * Created by Christoph on 23.09.2016.
 */
public interface LocalStorage {
    void put(Key key, Value value);

    Value get(Key key);

    boolean contains(Key key);
}
