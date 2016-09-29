package de.cgrotz.kademlia.client;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christoph on 29.09.2016.
 */
@Data
@Builder
public class Retry {

    private static final Logger LOGGER = LoggerFactory.getLogger(Retry.class);

    private final int interval;
    private final int retries;
    private final Runnable sender;

    public void execute() {
        int currentRetry = 0;

        while(currentRetry < retries) {
            try {
                sender.run();
                return;
            } catch (Exception exp) {
                LOGGER.warn("Unable to send message, failed with {}, retry number={} in interval={}ms", exp, currentRetry, interval);
                currentRetry++;
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted",e);
            }
        }
        LOGGER.error("Failed sending message after retries={}", retries);
    }
}
