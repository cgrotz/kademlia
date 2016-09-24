package de.cgrotz.kademlia.server;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
import de.cgrotz.kademlia.storage.LocalStorage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.List;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final RoutingTable routingTable;
    private final int kValue;
    private Codec codec = new Codec();
    private final Node localNode;
    private final LocalStorage localStorage;

    public KademliaServerHandler(RoutingTable routingTable, LocalStorage localStorage, Node localNode, int kValue) {
        this.routingTable = routingTable;
        this.localNode = localNode;
        this.kValue = kValue;
        this.localStorage = localStorage;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message message = codec.decode(packet.content());
        if(message.getType() == MessageType.PING) {
            Ping ping = (Ping)message;
            routingTable.addNode(ping.getNodeId(), ping.getAddress(), ping.getPort());
            ctx.writeAndFlush(new DatagramPacket(codec.encode(new Pong(message.getSeqId(),
                    localNode.getId().toString(),localNode.getAddress(), localNode.getPort()
                    )), packet.sender()));
        }
        else if(message.getType() == MessageType.FIND_NODE) {
            FindNode findNode = (FindNode) message;
            List<Node> closest = routingTable.findClosest(findNode.getLookupId(), kValue);
            ctx.writeAndFlush(new DatagramPacket(codec.encode(new NodeReply(message.getSeqId(), closest)), packet.sender()));
        }
        else if(message.getType() == MessageType.FIND_VALUE) {
            FindValue findValue = (FindValue) message;

            // query local store
            if(localStorage.contains(findValue.getKey())) {
                ctx.writeAndFlush(new DatagramPacket(codec.encode(new ValueReply(message.getSeqId(), findValue.getKey(), localStorage.get(findValue.getKey()))), packet.sender()));
            }
            else {
                // Else send list of closest nodes
                List<Node> closest = routingTable.findClosest(new NodeId(findValue.getKey().hashCode()), kValue);
                ctx.writeAndFlush(new DatagramPacket(codec.encode(new NodeReply(message.getSeqId(), closest)), packet.sender()));
            }
        }
        else if(message.getType() == MessageType.STORE) {
            Store store = (Store) message;
            localStorage.put(store.getKey(), store.getValue());
            ctx.writeAndFlush(new DatagramPacket(codec.encode(new StoreReply(message.getSeqId())), packet.sender()));
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
