package de.cgrotz.kademlia.storage;

/**
 * Created by Christoph on 23.09.2016.
 */
public interface LocalStorage {
    void put(String key, String value);

    String get(String key);

    boolean contains(String key);
}
