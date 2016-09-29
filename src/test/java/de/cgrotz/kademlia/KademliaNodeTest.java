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
    private static final String[] KEYS = new String[] {
        "738a4793791b8a672050cf495ac15fdae8c5e171",
        "1e8f1fb41a86a828dc14f0f72a97388ecf22d0b0",
        "4e876501a5aa9bc0890aa7b2066a51f011a05bee",
        "6901145bb2f1b655f106b72b1f5351e34d71c96c",
        "6c7950726634ef8b9f0708879067aa935313cebe",
        "2e706bd3d73524e58229ab489ce106834627a6ae"
    };

    @Test
    public void bootstrapTest() throws InterruptedException, ExecutionException {
        Kademlia kad1 = new Kademlia(
                Key.build(KEYS[0]),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                Key.build(KEYS[1]),
                "127.0.0.1", 9002
        );
        kad2.bootstrap("127.0.0.1", 9001);

        assertThat(kad1.routingTable.getBuckets()[158].getNodes(), contains(kad2.localNode));
        assertThat(kad2.routingTable.getBuckets()[158].getNodes(), contains(kad1.localNode));

        kad1.close();
        kad2.close();
    }

    @Test
    public void storeAndRetrieveTest() throws InterruptedException, ExecutionException {

        Kademlia kad1 = new Kademlia(
                Key.build(KEYS[0]),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                Key.build(KEYS[1]),
                "127.0.0.1", 9002
        );

        kad2.bootstrap("127.0.0.1", 9001);

        assertThat(kad1.routingTable.getBuckets()[158].getNodes(), contains(kad2.localNode));
        assertThat(kad2.routingTable.getBuckets()[158].getNodes(), contains(kad1.localNode));

        Key key1 = Key.build(KEYS[3]);

        kad1.put(key1, "Value");
        assertThat(kad1.get(key1), equalTo("Value"));

        kad1.close();
        kad2.close();
    }

    @Test
    public void simpleTest() throws InterruptedException, ExecutionException {
        Kademlia kad1 = new Kademlia(
                Key.build(KEYS[0]),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                Key.build(KEYS[1]),
                "127.0.0.1", 9002
        );

        Kademlia kad3 = new Kademlia(
                Key.build(KEYS[2]),
                "127.0.0.1", 9003
        );

        kad2.bootstrap("127.0.0.1", 9001);
        kad3.bootstrap("127.0.0.1", 9001);

        Thread.sleep(10000);

        assertThat(kad1.routingTable.getBuckets()[155].getNodes(), contains(kad2.localNode));
        assertThat(kad1.routingTable.getBuckets()[159].getNodes(), contains(kad3.localNode));

        assertThat(kad2.routingTable.getBuckets()[155].getNodes(), contains(kad1.localNode));

        assertThat(kad3.routingTable.getBuckets()[159].getNodes(), containsInAnyOrder(kad1.localNode, kad2.localNode));

        Key key1 = Key.build(KEYS[3]);
        Key key2 = Key.build(KEYS[4]);
        
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
    public void retryTest() throws InterruptedException, ExecutionException {
        Kademlia kad1 = new Kademlia(
                Key.build(KEYS[0]),
                "127.0.0.1", 9001
        );

        kad1.client.sendPing("127.0.0.2", 9002, pong -> {

        });
        kad1.close();
    }

    @Test
    public void complexRoutingTest() throws InterruptedException, ExecutionException {
        Kademlia kad1 = new Kademlia(
                Key.build(KEYS[0]),
                "127.0.0.1", 9001
        );

        Kademlia kad2 = new Kademlia(
                Key.build(KEYS[1]),
                "127.0.0.1", 9002
        );

        Kademlia kad3 = new Kademlia(
                Key.build(KEYS[2]),
                "127.0.0.1", 9003
        );

        Kademlia kad4 = new Kademlia(
                Key.build(KEYS[3]),
                "127.0.0.1", 9004
        );

        kad2.bootstrap("127.0.0.1", 9001);
        kad3.bootstrap("127.0.0.1", 9002);
        kad4.bootstrap("127.0.0.1", 9003);

        Thread.sleep(10000);

        Key key1 = Key.build(KEYS[0]);
        kad4.put(key1, "Value");

        assertThat(kad1.get(key1), equalTo("Value"));

        kad1.close();
        kad2.close();
        kad3.close();
        kad4.close();
    }
}
