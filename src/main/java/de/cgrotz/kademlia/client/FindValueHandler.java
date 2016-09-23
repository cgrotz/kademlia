package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Christoph on 23.09.2016.
 */
public class FindValueHandler  extends SimpleChannelInboundHandler<DatagramPacket> {
    private final Consumer<NodeReply> nodeReplyConsumer;
    private final Consumer<ValueReply> valueReplyConsumer;
    private Codec codec = new Codec();

    private RoutingTable routingTable;

    public FindValueHandler(RoutingTable routingTable, Consumer<NodeReply> nodeReplyConsumer, Consumer<ValueReply> valueReplyConsumer) {
        this.routingTable = routingTable;
        this.nodeReplyConsumer = nodeReplyConsumer;
        this.valueReplyConsumer = valueReplyConsumer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message message = codec.decode(packet.content());
        if (message.getType() == MessageType.NODE_REPLY) {
            NodeReply nodeReply = (NodeReply) message;
            nodeReply.getNodes().stream().forEach(node -> routingTable.addNode(node.getId(), node.getAddress(), node.getPort()));
            nodeReplyConsumer.accept(nodeReply);
        }
        else if (message.getType() == MessageType.VALUE_REPLY) {
            ValueReply valueReply = (ValueReply) message;
            valueReplyConsumer.accept(valueReply);
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
