package org.gengine.messaging.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gengine.messaging.amqp.AmqpDirectEndpoint;
import org.gengine.messaging.amqp.AmqpNodeBootstrapUtils;

/**
 * Boostrap which creates an {@link AmqpDirectEndpoint} with a {@link BenchmarkConsumer}
 * to measure the throughput of a broker.
 */
public class Bootstrap
{
    private static final Log logger = LogFactory.getLog(Bootstrap.class);

    protected static final String DEFAULT_QUEUE = "cheninfo.test.benchmark";
    protected static final String USAGE_MESSAGE = "USAGE: brokerUrl numMessages [queue]";
    protected static final int LOG_AFTER_SENDING_NUM_MESSAGES = 1000;

    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            throw new IllegalArgumentException(USAGE_MESSAGE);
        }

        String brokerUrl = args[0];

        int numMessages = Integer.valueOf(args[1]);

        String queue = DEFAULT_QUEUE;
        if (args.length > 2)
        {
            queue = args[2];
        }

        BenchmarkConsumer messageConsumer = new BenchmarkConsumer();

        AmqpDirectEndpoint endpoint =
                AmqpNodeBootstrapUtils.createEndpoint(messageConsumer, brokerUrl, null, null, queue, queue);

        // Start listener
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(endpoint.getListener());

        // Wait for listener initialization
        while (!endpoint.isInitialized())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
        }

        logger.debug("Sending " + numMessages + " messages...");

        long start = (new Date()).getTime();

        // Send the messages
        for (int i = 0; i < numMessages; i++)
        {
            BenchmarkMessage message = BenchmarkMessage.createInstance();
            endpoint.send(message);
            if (i > 0 && i % LOG_AFTER_SENDING_NUM_MESSAGES == 0)
            {
                logger.debug("Sent " + i + " messages...");
            }
        }
        long endSend = (new Date()).getTime();
        long sendTime = endSend - start;
        logger.info("Sent " + numMessages + " messages in " + sendTime + "ms");

        // Wait for consumer to dequeue all messages
        while (messageConsumer.getMessageCount() < numMessages)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
        }
        long end = (new Date()).getTime();
        long receiveTime = end - start;
        double messagesPerSecond = numMessages / (receiveTime / 1000.0);


        logger.info("Processed " + numMessages + " messages in " + receiveTime + "ms. Throughput: "
                + "" + Math.round(messagesPerSecond) + " messages/second");
        System.exit(0);
    }

}
