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
        if(message.getType() == MessageType.NODE_REPLY) {
            NodeReply nodeReply = (NodeReply)message;
            nodeReply.getNodes().stream().forEach(node -> routingTable.addNode(node.getId(), node.getAddress(), node.getPort()));
        }
        else if(message.getType() == MessageType.STORE_REPLY) {

        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
