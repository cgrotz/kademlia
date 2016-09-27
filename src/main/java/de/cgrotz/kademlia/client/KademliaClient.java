package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.exception.TimeoutException;
import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.protocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaClient {

    private final Distributor distributor;
    private final Bootstrap bootstrap;
    private Codec codec = new Codec();
    private final Key localNodeId;
    private final String localHostName;
    private final int localPort;
    private static SecureRandom random = new SecureRandom();

    public KademliaClient(Key localNodeId, String localHostName, int localPort) throws InterruptedException {
        this.localNodeId = localNodeId;
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

    private void send(String hostname, int port, long seqId, Message msg, Consumer<Message> consumer) throws TimeoutException {
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
            throw new TimeoutException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void sendPing(String hostname, int port, Consumer<Pong> pongConsumer) throws TimeoutException {
        long seqId = random.nextLong();
        send(hostname, port, seqId,new Ping(seqId, this.localNodeId, this.localHostName, this.localPort), msg -> {
            pongConsumer.accept((Pong)msg);
        });
    }


    public void sendFindNode(String hostname, int port, Key key, Consumer<List<Node>> callback) throws TimeoutException {
        long seqId = random.nextLong();
        send(hostname, port, seqId,new FindNode(seqId, key),
                message -> {
                    NodeReply nodeReply = (NodeReply) message;
                    callback.accept(nodeReply.getNodes());
                }
        );
    }

    public void sendFindValue(String hostname, int port, Key key,
                              Consumer<NodeReply> nodeReplyConsumer, Consumer<ValueReply> valueReplyConsumer) throws TimeoutException {
        long seqId = random.nextLong();
        send(hostname, port, seqId, new FindValue(seqId, key),
                message -> {
                    if (message.getType() == MessageType.NODE_REPLY) {
                        NodeReply nodeReply = (NodeReply) message;
                         nodeReplyConsumer.accept(nodeReply);
                    }
                    else if (message.getType() == MessageType.VALUE_REPLY) {
                        ValueReply valueReply = (ValueReply) message;
                        valueReplyConsumer.accept(valueReply);
                    }
                }
        );
    }

    public void sendContentToNode(Node node, Key key, String value) throws TimeoutException {
        final long seqId = random.nextLong();
        send(node.getAddress(), node.getPort(), seqId, new Store(seqId, key, value),
                message -> {

                }
        );
    }
}
