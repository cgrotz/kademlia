package de.cgrotz.kademlia.storage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christoph on 23.09.2016.
 */
public class InMemoryStorage implements LocalStorage {

    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    @Override
    public void put(String key, String value) {
        map.put(key, value);
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public boolean contains(String key) {
        return map.containsKey(key);
    }
}
