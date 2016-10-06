package de.cgrotz.kademlia.server;

import de.cgrotz.kademlia.events.Event;
import de.cgrotz.kademlia.events.ReceivedMessageEvent;
import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
import de.cgrotz.kademlia.storage.LocalStorage;
import de.cgrotz.kademlia.storage.Value;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaServerHandler.class);

    private final RoutingTable routingTable;
    private final int kValue;
    private final Map<String, Consumer<Event>> eventConsumers;
    private Codec codec = new Codec();
    private final Node localNode;
    private final LocalStorage localStorage;

    public KademliaServerHandler(RoutingTable routingTable, LocalStorage localStorage, Node localNode, int kValue, Map<String, Consumer<Event>> eventConsumers) {
        this.routingTable = routingTable;
        this.localNode = localNode;
        this.kValue = kValue;
        this.localStorage = localStorage;
        this.eventConsumers = eventConsumers;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message message = codec.decode(packet.content());
        routingTable.addNode(message.getOrigin());
        eventConsumers.forEach((s, eventConsumer) -> {
            eventConsumer.accept(ReceivedMessageEvent.builder().message(message).build());
        });

        LOGGER.debug("Received message type={},seqId={} from node={}", message.getType(),message.getSeqId(), message.getOrigin());
        if(message.getType() == MessageType.PING) {
            Ping ping = (Ping)message;
            routingTable.addNode(ping.getOrigin());
            ctx.writeAndFlush(new DatagramPacket(codec.encode(new Pong(message.getSeqId(),localNode)), packet.sender()));
        }
        else if(message.getType() == MessageType.FIND_NODE) {
            FindNode findNode = (FindNode) message;
            List<Node> closest = routingTable.findClosest(findNode.getLookupId(), kValue);
            ctx.writeAndFlush(new DatagramPacket(codec.encode(new NodeReply(message.getSeqId(),localNode, closest)), packet.sender()));
        }
        else if(message.getType() == MessageType.FIND_VALUE) {
            FindValue findValue = (FindValue) message;

            // query local store
            if(localStorage.contains(findValue.getKey())) {
                ctx.writeAndFlush(new DatagramPacket(codec.encode(new ValueReply(message.getSeqId(),localNode, findValue.getKey(), localStorage.get(findValue.getKey()).getContent())), packet.sender()));
            }
            else {
                // Else send list of closest nodes
                List<Node> closest = routingTable.findClosest(new Key(findValue.getKey().hashCode()), kValue);
                ctx.writeAndFlush(new DatagramPacket(codec.encode(new NodeReply(message.getSeqId(),localNode, closest)), packet.sender()));
            }
        }
        else if(message.getType() == MessageType.STORE) {
            Store store = (Store) message;
            localStorage.put(store.getKey(), Value.builder().content(store.getValue()).lastPublished(System.currentTimeMillis()).build());
            ctx.writeAndFlush(new DatagramPacket(codec.encode(new StoreReply(message.getSeqId(),localNode)), packet.sender()));
        }
        else {
            System.out.println("Unknown message type="+message.getType());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }
}
