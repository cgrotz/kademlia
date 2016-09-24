package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.node.NodeId;
import de.cgrotz.kademlia.protocol.*;
import de.cgrotz.kademlia.routing.RoutingTable;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaClient {

    private final Distributor distributor;
    private final Bootstrap bootstrap;
    private RoutingTable routingTable;
    private Codec codec = new Codec();
    private final NodeId localNodeId;
    private final String localHostName;
    private final int localPort;

    public KademliaClient(NodeId localNodeId, RoutingTable routingTable, String localHostName, int localPort) throws InterruptedException {
        this.localNodeId = localNodeId;
        this.routingTable = routingTable;
        this.localHostName = localHostName;
        this.localPort = localPort;
        EventLoopGroup group = new NioEventLoopGroup();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            group.shutdownGracefully();
        }));

        this.bootstrap = new Bootstrap();
        distributor = new Distributor();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, false)
                .handler(distributor);
    }

    public void send(String hostname, int port, long seqId, Message msg, Consumer<Message> consumer)  {
        distributor.registerHandler(seqId, consumer);
        try {
            Channel channel = bootstrap.bind(0).sync().channel();
            channel.writeAndFlush(new DatagramPacket(
                    codec.encode(msg),
                    new InetSocketAddress(hostname, port))).sync();

            if (!channel.closeFuture().await(5000)) {
                System.err.println("request with seqId="+seqId+" on node="+localNodeId+" timed out.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void sendPing(String hostname, int port, long seqId)  {
        send(hostname, port, seqId,new Ping(seqId, this.localNodeId, this.localHostName, this.localPort),
                message -> {
                    Pong pong = (Pong)message;
                    routingTable.addNode(pong.getNodeId(), pong.getAddress(), pong.getPort());
                }
        );
    }


    public void sendFindNode(String hostname, int port, long seqId, NodeId key, Consumer<List<Node>> callback)  {
        send(hostname, port, seqId,new FindNode(seqId, key),
                message -> {
                    NodeReply nodeReply = (NodeReply) message;
                    nodeReply.getNodes().stream().forEach(node -> routingTable.addNode(node.getId(), node.getAddress(), node.getPort()));
                    callback.accept(nodeReply.getNodes());
                }
        );
    }

    public void sendFindValue(String hostname, int port, long seqId, String key,
                              Consumer<NodeReply> nodeReplyConsumer, Consumer<ValueReply> valueReplyConsumer)  {
        send(hostname, port, seqId, new FindValue(seqId, key),
                message -> {
                    if (message.getType() == MessageType.NODE_REPLY) {
                        NodeReply nodeReply = (NodeReply) message;
                        nodeReply.getNodes().stream().forEach(node -> routingTable.addNode(node.getId(), node.getAddress(), node.getPort()));
                        nodeReplyConsumer.accept(nodeReply);
                    }
                    else if (message.getType() == MessageType.VALUE_REPLY) {
                        ValueReply valueReply = (ValueReply) message;
                        valueReplyConsumer.accept(valueReply);
                    }
                }
        );
    }

    public void sendContentToNode(long seqId, Node node, String key, String value)  {
        send(node.getAddress(), node.getPort(), seqId, new Store(seqId, key, value),
                message -> {

                }
        );
    }
}
