package de.cgrotz.kademlia.exception;

/**
 * Created by Christoph on 27.09.2016.
 */
public class KademliaTimeoutException extends RuntimeException {

    public KademliaTimeoutException(Exception e) {
        super(e);
    }

    public KademliaTimeoutException() {

    }
}
