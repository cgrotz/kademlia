package de.cgrotz.kademlia.server;

import de.cgrotz.kademlia.node.NodeId;
import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final RoutingTable routingTable;
    private Codec codec = new Codec();
    private final NodeId localNodeId;

    public KademliaServerHandler(RoutingTable routingTable, NodeId localNodeId) {
        this.routingTable = routingTable;
        this.localNodeId = localNodeId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message message = codec.decode(packet.content());
        System.out.println("received "+packet.sender().getHostName()+" "+packet.sender().getPort()+" "+message);
        if(message.getType() == MessageType.CONNECT) {
            Connect connect = (Connect)message;
            routingTable.addNode(NodeId.build(connect.getNodeId()), connect.getHost(), connect.getPort());
            ctx.write(new DatagramPacket(codec.encode(new ConnectionAcknowledge(message.getSeqId(), localNodeId)), packet.sender()));
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
