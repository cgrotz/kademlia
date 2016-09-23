package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private Codec codec = new Codec();

    private RoutingTable routingTable;

    public KademliaClientHandler(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message message = codec.decode(packet.content());
        if(message.getType() == MessageType.ACKNOWLEDGE) {
            ConnectionAcknowledge connectionAcknowledge = (ConnectionAcknowledge)message;
            routingTable.addNode(connectionAcknowledge.getNode().getId(),
                    connectionAcknowledge.getNode().getAddress(),
                    connectionAcknowledge.getNode().getPort());
        }
        else if(message.getType() == MessageType.FIND_NODE_REPLY) {
            FindNodeReply findNodeReply = (FindNodeReply)message;
            findNodeReply.getNodes().stream().forEach(node -> routingTable.addNode(node.getId(), node.getAddress(), node.getPort()));

        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
