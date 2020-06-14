package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.Configuration;
import de.cgrotz.kademlia.config.ListenerType;
import de.cgrotz.kademlia.config.UdpListener;
import de.cgrotz.kademlia.exception.NoMatchingListener;
import de.cgrotz.kademlia.exception.KademliaTimeoutException;
import de.cgrotz.kademlia.node.Key;
import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaClient {

    private final Logger LOGGER;

    private static SecureRandom random = new SecureRandom();

    private final KademliaClientHandler kademliaClientHandler;
    private final Configuration config;
    private final Node localNode;
    private final DatagramSocket socket;

    private Codec codec = new Codec();
    private final Duration timeout = Duration.ofSeconds(30);
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public KademliaClient(DatagramSocket socket, Configuration config, Node localNode) {
        this.LOGGER =  LoggerFactory.getLogger(KademliaClient.class.getSimpleName() + " " + localNode.getId().toString());
        this.localNode = localNode;
        this.config = config;
        this.socket = socket;
        kademliaClientHandler = new KademliaClientHandler();
    }

    private void send(Node node, long seqId, Message msg, Consumer<Message> consumer) throws KademliaTimeoutException, NoMatchingListener {
        kademliaClientHandler.registerHandler(seqId, consumer);

        UdpListener udpListener = node.getAdvertisedListeners().stream()
                .filter(listener -> listener.getType() == ListenerType.UDP)
                .map(listener -> (UdpListener) listener)
                .findFirst().orElseThrow(() -> new NoMatchingListener());

        Retry.builder()
                .interval(1000)
                .retries(3)
                .sender(() -> {
                    try {
                        byte[] payload = codec.encode(msg);
                        socket.send(new DatagramPacket(
                                payload,
                                payload.length,
                                new InetSocketAddress(udpListener.getHost(), udpListener.getPort())));

                        final Future future = executor.submit((Callable) () -> {
                            LOGGER.debug("sending message via udp to node={} msg={}",node.getId(), msg);
                            byte[] receiveData = new byte[1024];
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            socket.receive(receivePacket);
                            kademliaClientHandler.handle(receiveData, receivePacket);
                            return null;
                        });

                        try {
                            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                        }
                        catch (java.util.concurrent.TimeoutException e) {
                            LOGGER.warn("Server didn't respond in time to msg {}", msg);
                            future.cancel(true);
                        }
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error("unsupported encoding for encoding msg {}",msg, e);
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        LOGGER.error("Unhandled error while sending command {} to node {}", msg, node.getId(), e);
                    }
                }).build().execute();
    }

    public void sendPing(Node node, Consumer<Pong> pongConsumer) throws KademliaTimeoutException {
        long seqId = random.nextLong();
        send(node, seqId,new Ping(seqId,localNode), msg -> {
            pongConsumer.accept((Pong)msg);
        });
    }


    public void sendFindNode(Node node, Key key, Consumer<List<Node>> callback) throws KademliaTimeoutException {
        long seqId = random.nextLong();
        send(node, seqId,new FindNode(seqId,localNode, key),
                message -> {
                    NodeReply nodeReply = (NodeReply) message;
                    callback.accept(nodeReply.getNodes());
                }
        );
    }

    public void sendFindValue(Node node, Key key,
                              Consumer<NodeReply> nodeReplyConsumer, Consumer<ValueReply> valueReplyConsumer) throws KademliaTimeoutException {
        long seqId = random.nextLong();
        send(node, seqId, new FindValue(seqId,localNode, key),
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

    public void sendContentToNode(Node node, Key key, String value) throws KademliaTimeoutException {
        final long seqId = random.nextLong();
        send(node, seqId, new Store(seqId,localNode, key, value),
                message -> {

                }
        );
    }

    public void close() {
        executor.shutdownNow();
        socket.close();
    }
}
