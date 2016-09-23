package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaClient {

    private RoutingTable routingTable;
    private Codec codec = new Codec();

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
                System.err.println("request timed out.");
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendFindNode(String hostname, int port, long seqId, NodeId key, Consumer<List<Node>> callback) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .handler(new FindNodeHandler(routingTable, callback));

            Channel channel = b.bind(0).sync().channel();
            channel.writeAndFlush(new DatagramPacket(
                    codec.encode(new FindNode(seqId, key)),
                    new InetSocketAddress(hostname, port))).sync();

            if (!channel.closeFuture().await(5000)) {
                System.err.println("request with seqId="+seqId+" timed out.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendFindValue(String hostname, int port, long seqId, String key,
                              Consumer<NodeReply> nodeReplyConsumer, Consumer<ValueReply> valueReplyConsumer) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .handler(new FindValueHandler(routingTable, nodeReplyConsumer, valueReplyConsumer));

            Channel channel = b.bind(0).sync().channel();
            channel.writeAndFlush(new DatagramPacket(
                    codec.encode(new FindValue(seqId, key)),
                    new InetSocketAddress(hostname, port))).sync();

            if (!channel.closeFuture().await(5000)) {
                System.err.println("request with seqId="+seqId+" timed out.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendContentToNode(long seqId, Node node,  String key, String value) throws UnsupportedEncodingException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .handler(new SendContentHandler(routingTable));

            Channel channel = b.bind(0).sync().channel();
            channel.writeAndFlush(new DatagramPacket(
                    codec.encode(new Store(seqId, key, value)),
                    new InetSocketAddress(node.getAddress(), node.getPort()))).sync();

            if (!channel.closeFuture().await(5000)) {
                System.err.println("request with seqId="+seqId+" timed out.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
