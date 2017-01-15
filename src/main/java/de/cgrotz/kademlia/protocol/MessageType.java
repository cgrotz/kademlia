package de.cgrotz.kademlia.protocol;

/**
 * Created by Christoph on 21.09.2016.
 */
public enum MessageType {
    PING(Ping.class), PONG(Pong.class),
    STORE(Store.class), FIND_NODE(FindNode.class),
    NODE_REPLY(NodeReply.class), STORE_REPLY(StoreReply.class),
    VALUE_REPLY(ValueReply.class), FIND_VALUE(FindValue.class);

    private final Class msgClass;

    MessageType(Class msgClass) {
        this.msgClass = msgClass;
    }

    public Class getMsgClass() {
        return msgClass;
    }
}
