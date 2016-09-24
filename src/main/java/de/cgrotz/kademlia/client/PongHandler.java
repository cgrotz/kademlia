package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.function.Consumer;

/**
 * Created by Christoph on 23.09.2016.
 */
public class PongHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final RoutingTable routingTable;
    private final Consumer<Pong> pongReply;
    private Codec codec = new Codec();

    public PongHandler(RoutingTable routingTable, Consumer<Pong> pongReply) {
        this.routingTable = routingTable;
        this.pongReply = pongReply;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        Message message = codec.decode(msg.content());
        if (message.getType() == MessageType.PONG) {
            Pong pong = (Pong) message;
            this.pongReply.accept(pong);
        }
    }
}
