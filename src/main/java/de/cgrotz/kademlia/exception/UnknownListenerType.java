package de.cgrotz.kademlia.exception;

/**
 * Created by Christoph on 30.09.2016.
 */
public class UnknownListenerType extends RuntimeException{
    public UnknownListenerType(String url) {
        super("Can't parse url="+url);

    }
}
