package de.cgrotz.kademlia;

import de.cgrotz.kademlia.node.NodeId;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaNodeTest {

    @Test
    public void simpleTest() throws InterruptedException, ExecutionException {
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

        kad2.bootstrap("127.0.0.1", 9001);
        kad3.bootstrap("127.0.0.1", 9001);

        Thread.sleep(1000);

        kad1.put("Key", "Value");
        kad3.put("Key2", "Value2");

        kad1.get("Key", System.out::println);
        kad2.get("Key", System.out::println);
        kad3.get("Key", System.out::println);

        assertThat(kad1.get("Key"), equalTo("Value"));
        assertThat(kad2.get("Key"), equalTo("Value"));
        assertThat(kad3.get("Key"), equalTo("Value"));

        assertThat(kad1.get("Key2"), equalTo("Value2"));
        assertThat(kad2.get("Key2"), equalTo("Value2"));
        assertThat(kad3.get("Key2"), equalTo("Value2"));
    }
}
