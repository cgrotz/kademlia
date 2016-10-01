package de.cgrotz.kademlia.config;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Christoph on 30.09.2016.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UdpListener extends Listener {

    public UdpListener(String host, int port) {
        super(ListenerType.UDP);
        this.host = host;
        this.port = port;
    }
    public UdpListener(String url) {
        super(ListenerType.UDP);

        this.host = url.substring(6, url.lastIndexOf(":"));
        this.port = Integer.parseInt(url.substring(url.lastIndexOf(":")+1));
    }

    private final String host;
    private final int port;


    public static UdpListener from(String url) {
        return new UdpListener(url);
    }
}
