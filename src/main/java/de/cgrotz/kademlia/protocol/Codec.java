package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.NodeId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * Created by Christoph on 21.09.2016.
 */
public class Codec {

    public Message decode(ByteBuf buffer) {
        String message = buffer.toString(CharsetUtil.UTF_8);
        if(message.startsWith(MessageType.CONNECT.name())) {
            String[] parts = message.split("\\|");
            return new Connect(Long.parseLong(parts[1]), NodeId.build(parts[2]), parts[3], Integer.parseInt(parts[4]));
        }
        else if(message.startsWith(MessageType.ACKNOWLEDGE.name())) {
            String[] parts = message.split("\\|");
            return new ConnectionAcknowledge(Long.parseLong(parts[1]),NodeId.build(parts[2]));
        }
        else {
            throw new RuntimeException("Unknown message");
        }
    }

    public ByteBuf encode(Connect connect) {
        return Unpooled.copiedBuffer("CONNECT|"+connect.getSeqId()+"|"+connect.getNodeId()+"|"+connect.getHost()+"|"+connect.getPort(), CharsetUtil.UTF_8);
    }

    public ByteBuf encode(ConnectionAcknowledge connectionAcknowledge) {
        return Unpooled.copiedBuffer("ACKNOWLEDGE|"+ connectionAcknowledge.getSeqId()+"|"+connectionAcknowledge.getNodeId(), CharsetUtil.UTF_8);
    }
}
