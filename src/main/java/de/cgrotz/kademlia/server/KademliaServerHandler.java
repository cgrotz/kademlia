package de.cgrotz.kademlia.server;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
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

    public KademliaServerHandler(RoutingTable routingTable, Node localNode, int kValue) {
        this.routingTable = routingTable;
        this.localNode = localNode;
        this.kValue = kValue;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message message = codec.decode(packet.content());
        if(message.getType() == MessageType.CONNECT) {
            Connect connect = (Connect)message;
            routingTable.addNode(NodeId.build(connect.getNodeId()), connect.getHost(), connect.getPort());
            ctx.writeAndFlush(new DatagramPacket(codec.encode(new ConnectionAcknowledge(message.getSeqId(),
                    localNode.getId(),localNode.getAddress(), localNode.getPort()
                    )), packet.sender()));
        }
        else if(message.getType() == MessageType.FIND_NODE) {
            FindNode findNode = (FindNode) message;
            List<Node> closest = routingTable.findClosest(findNode.getLookupId(), kValue);
            ctx.writeAndFlush(new DatagramPacket(codec.encode(new FindNodeReply(message.getSeqId(), closest)), packet.sender()));
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
