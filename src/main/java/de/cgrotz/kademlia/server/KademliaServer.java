package de.cgrotz.kademlia.server;

import de.cgrotz.kademlia.node.NodeId;
import de.cgrotz.kademlia.routing.RoutingTable;
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
    private final NodeId localNodeId;

    public KademliaServer(int port, RoutingTable routingTable, NodeId localNodeId) {
        this.routingTable = routingTable;
        this.localNodeId = localNodeId;

        this.group = new NioEventLoopGroup();
        new Thread(() -> {
            try {
                Bootstrap b = new Bootstrap();

                b.group(group)
                        .channel(NioDatagramChannel.class)
                        .option(ChannelOption.SO_BROADCAST, false)
                        .handler(new KademliaServerHandler(this.routingTable, this.localNodeId));

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
