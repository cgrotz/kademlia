package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christoph on 21.09.2016.
 */
public class Codec {

    public Message decode(ByteBuf buffer) {
        String message = buffer.toString(CharsetUtil.UTF_8);
        String[] parts = message.split("\\|");
        if(parts[0].equals(MessageType.CONNECT.name())) {
            return new Connect(Long.parseLong(parts[1]), NodeId.build(parts[2]), parts[3], Integer.parseInt(parts[4]));
        }
        else if(parts[0].equals(MessageType.ACKNOWLEDGE.name())) {
            return new ConnectionAcknowledge(Long.parseLong(parts[1]),
                    NodeId.build(parts[2]),
                    parts[3],
                    Integer.parseInt(parts[4]));
        }
        else if(parts[0].equals(MessageType.FIND_NODE.name())) {
            return new FindNode(Long.parseLong(parts[1]),NodeId.build(parts[2]));
        }
        else if(parts[0].equals(MessageType.FIND_NODE_REPLY.name())) {
            List<Node> nodes = new ArrayList<>();
            for(int i = 2; i< parts.length; i++) {
                String[] address = parts[i].split(":");
                nodes.add(Node.builder().id(NodeId.build(address[0])).address(address[1]).port(Integer.parseInt(address[2])).lastSeen(System.currentTimeMillis()).build());
            }
            return new FindNodeReply(Long.parseLong(parts[1]), nodes);
        }
        else {
            throw new RuntimeException("Unknown message");
        }
    }

    public ByteBuf encode(Connect connect) {
        return Unpooled.copiedBuffer("CONNECT|"+connect.getSeqId()+"|"+connect.getNodeId()+"|"+connect.getHost()+"|"+connect.getPort(), CharsetUtil.UTF_8);
    }

    public ByteBuf encode(ConnectionAcknowledge connectionAcknowledge) {
        return Unpooled.copiedBuffer("ACKNOWLEDGE|"+ connectionAcknowledge.getSeqId()+
                "|"+connectionAcknowledge.getNode().getId() +
                "|"+connectionAcknowledge.getNode().getAddress() +
                "|"+connectionAcknowledge.getNode().getPort()
                , CharsetUtil.UTF_8);
    }

    public ByteBuf encode(FindNode findNode) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("FIND_NODE|"+ findNode.getSeqId()+"|"+findNode.getLookupId().toString(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(FindNodeReply findNodeReply) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("FIND_NODE_REPLY|"+ findNodeReply.getSeqId(), CharsetUtil.UTF_8);
        for( Node node : findNodeReply.getNodes()) {
            byteBuf.writeCharSequence( "|"+ node.getId().toString()+":"+node.getAddress()+":"+node.getPort(), CharsetUtil.UTF_8);
        }
        return byteBuf;
    }
}
