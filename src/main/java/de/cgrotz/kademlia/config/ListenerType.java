package de.cgrotz.kademlia.config;

/**
 * Created by Christoph on 30.09.2016.
 */
public enum ListenerType {
    UDP("udp");

    private final String prefix;

    ListenerType(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}
