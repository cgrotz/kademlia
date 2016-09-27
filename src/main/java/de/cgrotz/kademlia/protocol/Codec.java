package de.cgrotz.kademlia.protocol;

import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.node.Node;
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
            return new FindNode(Long.parseLong(parts[1]), Key.build(parts[2]));
        }
        else if(parts[0].equals(MessageType.PING.name())) {
            return new Ping(
                    Long.parseLong(parts[1]),
                    Key.build(parts[2]),
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
                nodes.add(Node.builder().id(Key.build(address[0])).address(address[1]).port(Integer.parseInt(address[2])).lastSeen(System.currentTimeMillis()).build());
            }
            return new NodeReply(Long.parseLong(parts[1]), nodes);
        }
        else if(parts[0].equals(MessageType.STORE.name())) {
            return new Store(Long.parseLong(parts[1]),
                    Key.build(parts[2]),
                    new String(decoder.decode(parts[3]), CharsetUtil.UTF_8.name()));
        }
        else if(parts[0].equals(MessageType.STORE_REPLY.name())) {
            return new StoreReply(Long.parseLong(parts[1]));
        }
        else if(parts[0].equals(MessageType.FIND_VALUE.name())) {
            return new FindValue(Long.parseLong(parts[1]), Key.build(parts[2]));
        }
        else if(parts[0].equals(MessageType.VALUE_REPLY.name())) {
            return new ValueReply(Long.parseLong(parts[1]),
                    Key.build(parts[2]), parts[3]);
        }
        else {
            System.out.println("Can't decode message_type="+parts[0]);
            throw new RuntimeException("Unknown message type="+ parts[0]+" message="+ Arrays.toString(parts));
        }
    }

    public ByteBuf encode(Ping msg) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(msg.getType().name()+"|"+ msg.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+msg.getNodeId().toString(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+msg.getAddress(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+msg.getPort(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(Pong msg) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(msg.getType().name()+"|"+ msg.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+msg.getNodeId().toString(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+msg.getAddress(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+msg.getPort(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(FindNode msg) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(msg.getType().name()+"|"+ msg.getSeqId()+"|"+msg.getLookupId().toString(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(NodeReply msg) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(msg.getType().name()+"|"+ msg.getSeqId(), CharsetUtil.UTF_8);
        for( Node node : msg.getNodes()) {
            byteBuf.writeCharSequence( "|"+ node.getId().toString()+":"+node.getAddress()+":"+node.getPort(), CharsetUtil.UTF_8);
        }
        return byteBuf;
    }

    public ByteBuf encode(Store msg) throws UnsupportedEncodingException {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(msg.getType().name()+"|"+ msg.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+msg.getKey(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+encoder.encodeToString(msg.getValue().getBytes(CharsetUtil.UTF_8.name())), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(StoreReply msg) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(msg.getType().name()+"|"+ msg.getSeqId(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(FindValue msg) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(msg.getType().name()+"|"+ msg.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ msg.getKey(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(ValueReply msg) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeCharSequence(msg.getType().name()+"|"+ msg.getSeqId(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ msg.getKey(), CharsetUtil.UTF_8);
        byteBuf.writeCharSequence("|"+ msg.getValue(), CharsetUtil.UTF_8);
        return byteBuf;
    }

    public ByteBuf encode(Message msg) throws UnsupportedEncodingException {
        if(msg instanceof ValueReply) {
            return encode((ValueReply)msg);
        }
        else if(msg instanceof FindNode) {
            return encode((FindNode) msg);
        }
        else if(msg instanceof NodeReply) {
            return encode((NodeReply) msg);
        }
        else if(msg instanceof FindValue) {
            return encode((FindValue) msg);
        }
        else if(msg instanceof Store) {
            return encode((Store) msg);
        }
        else if(msg instanceof StoreReply) {
            return encode((StoreReply) msg);
        }
        else if(msg instanceof Ping) {
            return encode((Ping) msg);
        }
        else if(msg instanceof Pong) {
            return encode((Pong) msg);
        }
        else {
            throw new RuntimeException("Unknown msg type:" + msg);
        }
    }
}
