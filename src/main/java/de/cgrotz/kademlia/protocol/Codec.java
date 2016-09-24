package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Created by Christoph on 21.09.2016.
 */
public class Codec {
    private final Base64.Decoder decoder = Base64.getDecoder();
    private final Base64.Encoder encoder = Base64.getEncoder();

    public Message decode(ByteBuf buffer) throws UnsupportedEncodingException {
        String message = buffer.toString(CharsetUtil.UTF_8);
        String[] parts = message.split("\\|");
        if(parts[0].equals(MessageType.FIND_NODE.name())) {
            return new FindNode(Long.parseLong(parts[1]),NodeId.build(parts[2]));
        }
        else if(parts[0].equals(MessageType.PING.name())) {
            return new Ping(
                    Long.parseLong(parts[1]),
                    NodeId.build(parts[2]),
                    parts[3],
                    Integer.parseInt(parts[4])
                    );
        }
        else if(parts[0].equals(MessageType.PONG.name())) {
            return new Pong(
                    Long.parseLong(parts[1]),
                    parts[2],
                    parts[3],
                    Integer.parseInt(parts[4])
            );
        }
        else if(parts[0].equals(MessageType.NODE_REPLY.name())) {
            List<Node> nodes = new ArrayList<>();
            for(int i = 2; i< parts.length; i++) {
                String[] address = parts[i].split(":");
                nodes.add(Node.builder().id(NodeId.build(address[0])).address(address[1]).port(Integer.parseInt(address[2])).lastSeen(System.currentTimeMillis()).build());
            }
            return new NodeReply(Long.parseLong(parts[1]), nodes);
        }
        else if(parts[0].equals(MessageType.STORE.name())) {
            return new Store(Long.parseLong(parts[1]),
                    new String(decoder.decode(parts[2]), CharsetUtil.UTF_8.name()),
                    new String(decoder.decode(parts[3]), CharsetUtil.UTF_8.name()));
        }
        else if(parts[0].equals(MessageType.STORE_REPLY.name())) {
            return new StoreReply(Long.parseLong(parts[1]));
        }
        else if(parts[0].equals(MessageType.FIND_VALUE.name())) {
            return new FindValue(Long.parseLong(parts[1]), parts[2]);
        }
        else if(parts[0].equals(MessageType.VALUE_REPLY.name())) {
            return new ValueReply(Long.parseLong(parts[1]),
                    parts[2], parts[3]);
        }
        else {
            throw new RuntimeException("Unknown message type="+ parts[0]+" message="+ Arrays.toString(parts));
        }
    }

    public ByteBuf encode(Ping ping) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("PING|"+ ping.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ping.getNodeId().toString(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ping.getAddress(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ping.getPort(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(Pong pong) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("PONG|"+ pong.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+pong.getNodeId().toString(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+pong.getAddress(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+pong.getPort(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(FindNode findNode) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("FIND_NODE|"+ findNode.getSeqId()+"|"+findNode.getLookupId().toString(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(NodeReply nodeReply) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("NODE_REPLY|"+ nodeReply.getSeqId(), CharsetUtil.UTF_8);
        for( Node node : nodeReply.getNodes()) {
            byteBuf.writeCharSequence( "|"+ node.getId().toString()+":"+node.getAddress()+":"+node.getPort(), CharsetUtil.UTF_8);
        }
        return byteBuf;
    }

    public ByteBuf encode(Store store) throws UnsupportedEncodingException {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("STORE|"+ store.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+encoder.encodeToString(store.getKey().getBytes(CharsetUtil.UTF_8.name())), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+encoder.encodeToString(store.getValue().getBytes(CharsetUtil.UTF_8.name())), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(StoreReply storeReply) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("STORE_REPLY|"+ storeReply.getSeqId(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(FindValue findValue) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("FIND_VALUE|"+ findValue.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ findValue.getKey(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(ValueReply valueReply) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence("VALUE_REPLY|"+ valueReply.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ valueReply.getKey(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ valueReply.getValue(), CharsetUtil.UTF_8);
        return byteBuf;
    }
}
