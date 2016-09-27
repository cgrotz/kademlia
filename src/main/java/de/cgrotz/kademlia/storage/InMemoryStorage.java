package de.cgrotz.kademlia.storage;

import de.cgrotz.kademlia.node.Key;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christoph on 23.09.2016.
 */
public class InMemoryStorage implements LocalStorage {

    private final ConcurrentHashMap<Key, Value> map = new ConcurrentHashMap<>();

    @Override
    public void put(Key key, Value value) {
        map.put(key, value);
    }

    @Override
    public Value get(Key key) {
        return map.get(key);
    }

    @Override
    public boolean contains(Key key) {
        return map.containsKey(key);
    }
}
