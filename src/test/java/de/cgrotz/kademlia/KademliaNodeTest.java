package de.cgrotz.kademlia;

import de.cgrotz.kademlia.node.NodeId;
import org.junit.Test;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaNodeTest {

    @Test
    public void simpleTest() throws InterruptedException {
        Kademlia kad1 = new Kademlia(
                NodeId.random(),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                NodeId.random(),
                "127.0.0.1", 9002
        );

        Kademlia kad3 = new Kademlia(
                NodeId.random(),
                "127.0.0.1", 9003
        );

        //kad2.getClient().send("localhost",9001, Unpooled.copiedBuffer("Hallo Welt", Charset.forName("UTF-8")));
        kad2.bootstrap("127.0.0.1", 9001);
        kad3.bootstrap("127.0.0.1", 9001);
        Thread.sleep(1000);

    }
}
