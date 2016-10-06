package de.cgrotz.kademlia.server;

import de.cgrotz.kademlia.events.Event;
import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.routing.RoutingTable;
import de.cgrotz.kademlia.storage.LocalStorage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
public class KademliaServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KademliaServer.class);

    private final NioEventLoopGroup group;
    private final RoutingTable routingTable;
    private final Node localNode;
    private final int kValue;
    private final LocalStorage localStorage;
    private final Map<String, Consumer<Event>> eventConsumers;

    public KademliaServer(String bindingAddress, int port, int kValue, RoutingTable routingTable, LocalStorage localStorage, Node localNode, Map<String, Consumer<Event>> eventConsumers) {
        this.routingTable = routingTable;
        this.localNode = localNode;
        this.kValue = kValue;
        this.localStorage = localStorage;
        this.eventConsumers = eventConsumers;

        this.group = new NioEventLoopGroup();
        new Thread(() -> {
            try {
                Bootstrap b = new Bootstrap();

                b.group(group)
                        .channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST, false)
                        .handler(new KademliaServerHandler(this.routingTable, this.localStorage, this.localNode, this.kValue, this.eventConsumers));

                b.bind(bindingAddress, port).sync().channel().closeFuture().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        }).start();

        LOGGER.info("Kademlia Listener started");
    }

    public void close() {
        try {
            group.shutdownGracefully().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
