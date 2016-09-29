package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.protocol.Codec;
import de.cgrotz.kademlia.protocol.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Christoph on 24.09.2016.
 */
@ChannelHandler.Sharable
public class KademliaClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final static Logger LOGGER = LoggerFactory.getLogger(KademliaClientHandler.class);

    private final Codec codec = new Codec();

    private Map<Long, Consumer<Message>>  handlers = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        Message message = codec.decode(packet.content());
        LOGGER.info("receiving response seqId={} msg={} from host={}:{}", message.getSeqId(), message, packet.sender().getHostName(), packet.sender().getPort());
        handlers.get(message.getSeqId()).accept(message);
        handlers.remove(message.getSeqId());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void registerHandler(long seqId, Consumer<Message> consumer) {
        this.handlers.put(seqId, consumer);
    }
}
