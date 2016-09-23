package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.routing.RoutingTable;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by Christoph on 23.09.2016.
 */
public class SendContentHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    public SendContentHandler(RoutingTable routingTable) {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {

    }
}
