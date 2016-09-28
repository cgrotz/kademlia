package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.Configuration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaClient.class);

    private static SecureRandom random = new SecureRandom();

    private final Distributor distributor;
    private final Bootstrap bootstrap;
    private final Configuration config;
    private final Node localNode;
    private final NioEventLoopGroup group;

    private Codec codec = new Codec();

    public KademliaClient(Configuration config, Node localNode) throws InterruptedException {
        this.localNode = localNode;
        this.config = config;
        this.group = new NioEventLoopGroup();

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

            if (!channel.closeFuture().await(config.getNetworkTimeoutMs())) {
                LOGGER.error("request with seqId={} on node={} timed out.", seqId, localNode);
            }
        } catch (InterruptedException e) {
            throw new TimeoutException();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void sendPing(String hostname, int port, Consumer<Pong> pongConsumer) throws TimeoutException {
        long seqId = random.nextLong();
        send(hostname, port, seqId,new Ping(seqId,localNode), msg -> {
            pongConsumer.accept((Pong)msg);
        });
    }


    public void sendFindNode(String hostname, int port, Key key, Consumer<List<Node>> callback) throws TimeoutException {
        long seqId = random.nextLong();
        send(hostname, port, seqId,new FindNode(seqId,localNode, key),
                message -> {
                    NodeReply nodeReply = (NodeReply) message;
                    callback.accept(nodeReply.getNodes());
                }
        );
    }

    public void sendFindValue(String hostname, int port, Key key,
                              Consumer<NodeReply> nodeReplyConsumer, Consumer<ValueReply> valueReplyConsumer) throws TimeoutException {
        long seqId = random.nextLong();
        send(hostname, port, seqId, new FindValue(seqId,localNode, key),
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
        send(node.getAddress(), node.getPort(), seqId, new Store(seqId,localNode, key, value),
                message -> {

                }
        );
    }

    public void close() {
        group.shutdownGracefully();
    }
}
