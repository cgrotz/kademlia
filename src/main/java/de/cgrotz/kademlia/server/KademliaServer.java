package de.cgrotz.kademlia.server;

import de.cgrotz.kademlia.node.Node;
import de.cgrotz.kademlia.routing.RoutingTable;
import de.cgrotz.kademlia.storage.LocalStorage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Data;

/**
 * Created by Christoph on 21.09.2016.
 */
@Data
public class KademliaServer {
    private final NioEventLoopGroup group;
    private final RoutingTable routingTable;
    private final Node localNode;
    private final int kValue;
    private final LocalStorage localStorage;

    public KademliaServer(int port, int kValue, RoutingTable routingTable, LocalStorage localStorage, Node localNode) {
        this.routingTable = routingTable;
        this.localNode = localNode;
        this.kValue = kValue;
        this.localStorage = localStorage;

        this.group = new NioEventLoopGroup();
        new Thread(() -> {
            try {
                Bootstrap b = new Bootstrap();

                b.group(group)
                        .channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST, false)
                        .handler(new KademliaServerHandler(this.routingTable, this.localStorage, this.localNode, this.kValue));

                b.bind(port).sync().channel().closeFuture().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        }).start();

    }

    public void close() {
        try {
            group.shutdownGracefully().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
