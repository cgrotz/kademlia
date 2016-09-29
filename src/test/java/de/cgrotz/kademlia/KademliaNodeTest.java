package de.cgrotz.kademlia;

import de.cgrotz.kademlia.node.Key;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by Christoph on 21.09.2016.
 */
public class KademliaNodeTest {

    @Test
    public void bootstrapTest() throws InterruptedException, ExecutionException {
        Kademlia kad1 = new Kademlia(
                Key.build("-292490753721043471326861150471687022870500507356"),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                Key.build("-352183435137046830118902193008042623829670945730"),
                "127.0.0.1", 9002
        );
        kad2.bootstrap("127.0.0.1", 9001);

        assertThat(kad1.routingTable.getBuckets()[155].getNodes(), contains(kad2.localNode));
        assertThat(kad2.routingTable.getBuckets()[155].getNodes(), contains(kad1.localNode));

        kad1.close();
        kad2.close();
    }

    @Test
    public void storeAndRetrieveTest() throws InterruptedException, ExecutionException {

        Kademlia kad1 = new Kademlia(
                Key.build("-292490753721043471326861150471687022870500507356"),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                Key.build("-352183435137046830118902193008042623829670945730"),
                "127.0.0.1", 9002
        );

        kad2.bootstrap("127.0.0.1", 9001);

        assertThat(kad1.routingTable.getBuckets()[155].getNodes(), contains(kad2.localNode));
        assertThat(kad2.routingTable.getBuckets()[155].getNodes(), contains(kad1.localNode));

        Key key1 = Key.build("590079237527231438500678240732481547146451535223");

        kad1.put(key1, "Value");
        assertThat(kad1.get(key1), equalTo("Value"));

        kad1.close();
        kad2.close();
    }

    @Test
    public void simpleTest() throws InterruptedException, ExecutionException {
        Kademlia kad1 = new Kademlia(
                Key.build("-292490753721043471326861150471687022870500507356"),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                Key.build("-352183435137046830118902193008042623829670945730"),
                "127.0.0.1", 9002
        );

        Kademlia kad3 = new Kademlia(
                Key.build("590079237527231438500678240732481547146451535223"),
                "127.0.0.1", 9003
        );

        kad2.bootstrap("127.0.0.1", 9001);
        kad3.bootstrap("127.0.0.1", 9001);

        Thread.sleep(10000);

        assertThat(kad1.routingTable.getBuckets()[155].getNodes(), contains(kad2.localNode));
        assertThat(kad1.routingTable.getBuckets()[159].getNodes(), contains(kad3.localNode));

        assertThat(kad2.routingTable.getBuckets()[155].getNodes(), contains(kad1.localNode));

        assertThat(kad3.routingTable.getBuckets()[159].getNodes(), containsInAnyOrder(kad1.localNode, kad2.localNode));

        Key key1 = Key.random();
        Key key2 = Key.random();
        
        kad1.put(key1, "Value");
        kad3.put(key2, "Value2");

        assertThat(kad1.get(key1), equalTo("Value"));
        assertThat(kad2.get(key1), equalTo("Value"));
        assertThat(kad3.get(key1), equalTo("Value"));

        assertThat(kad1.get(key2), equalTo("Value2"));
        assertThat(kad2.get(key2), equalTo("Value2"));
        assertThat(kad3.get(key2), equalTo("Value2"));

        kad1.close();
        kad2.close();
        kad3.close();
    }

    @Test
    public void complexRoutingTest() throws InterruptedException, ExecutionException {
        Kademlia kad1 = new Kademlia(
                Key.build("-352183435137046830118902193008042623829670945730"),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                Key.build("-292490753721043471326861150471687022870500507356"),
                "127.0.0.1", 9002
        );

        Kademlia kad3 = new Kademlia(
                Key.build("590079237527231438500678240732481547146451535223"),
                "127.0.0.1", 9003
        );

        Kademlia kad4 = new Kademlia(
                Key.build("-528381461527837381681463930629914464494078737316"),
                "127.0.0.1", 9004
        );

        kad2.bootstrap("127.0.0.1", 9001);
        kad3.bootstrap("127.0.0.1", 9002);
        kad4.bootstrap("127.0.0.1", 9003);

        Thread.sleep(10000);

        Key key1 = Key.random();
        kad4.put(key1, "Value");

        assertThat(kad1.get(key1), equalTo("Value"));

        kad1.close();
        kad2.close();
        kad3.close();
        kad4.close();
    }
}
