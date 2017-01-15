package de.cgrotz.kademlia.config;

/**
 * Created by Christoph on 30.09.2016.
 */
public enum ListenerType {
    UDP("udp", UdpListener.class);

    private final String prefix;
    private final Class listenerConfigClass;

    ListenerType(String prefix, Class<UdpListener> listenerConfigClass) {
        this.prefix = prefix;
        this.listenerConfigClass = listenerConfigClass;
    }

    public String prefix() {
        return prefix;
    }

    public Class getListenerConfigClass() {
        return listenerConfigClass;
    }
}
