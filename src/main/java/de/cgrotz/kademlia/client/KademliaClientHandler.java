package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.protocol.Codec;
import de.cgrotz.kademlia.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private Codec codec = new Codec();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message message = codec.decode(packet.content());
        System.out.println("Response: " + message);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
