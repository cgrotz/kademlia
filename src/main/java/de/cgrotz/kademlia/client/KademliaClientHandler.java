package de.cgrotz.kademlia.client;

import de.cgrotz.kademlia.protocol.Codec;
import de.cgrotz.kademlia.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Christoph on 24.09.2016.
 */
public class KademliaClientHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(KademliaClientHandler.class);

    private final Codec codec = new Codec();

    private Map<Long, Consumer<Message>>  handlers = new HashMap<>();

    protected void handle(byte[] payload, DatagramPacket packet) throws Exception {
        Message message = codec.decode(payload);
        LOGGER.debug("receiving response seqId={} msg={} from host={}:{}", message.getSeqId(), message, packet.getAddress().getHostName(), packet.getPort());
        handlers.get(message.getSeqId()).accept(message);
        handlers.remove(message.getSeqId());
    }

    public void registerHandler(long seqId, Consumer<Message> consumer) {
        this.handlers.put(seqId, consumer);
    }
}
