package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.routing.RoutingTable;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaClient {

    private RoutingTable routingTable;

    public KademliaClient(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    public void send(String address, int port, ByteBuf buf) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .handler(new KademliaClientHandler(routingTable));

            Channel channel = b.bind(0).sync().channel();
            channel.writeAndFlush(new DatagramPacket(
                    buf,
                    new InetSocketAddress(address, port))).sync();

            if (!channel.closeFuture().await(5000)) {
                System.err.println("QOTM request timed out.");
            }
        } finally {
            group.shutdownGracefully();
        }
    }

}
