package org.gengine.messaging.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
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

    protected static final String DEFAULT_QUEUE = "gengine.test.benchmark";
    protected static final String USAGE_MESSAGE = "USAGE: brokerUrl numMessages [queue]";
    protected static final String LOG_SEPERATOR = "----------------------------------------------\n";

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

        logStart(numMessages, brokerUrl, queue);

        long start = (new Date()).getTime();

        // Send the messages
        for (int i = 0; i < numMessages; i++)
        {
            BenchmarkMessage message = BenchmarkMessage.createInstance();
            endpoint.send(message);
            if (i > 0 && i % BenchmarkConsumer.LOG_AFTER_NUM_MESSAGES == 0)
            {
                logger.debug("Sent " + i + " messages...");
            }
        }
        long endSend = (new Date()).getTime();
        long sendTime = endSend - start;

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

        logStatistics(numMessages, sendTime, receiveTime);
        System.exit(0);
    }

    protected static void logStart(int numMessages, String brokerUrl, String queue)
    {
        System.out.println("\n\n"
                + LOG_SEPERATOR
                + "BENCHMARK START\n"
                + LOG_SEPERATOR
                + "Simultaneously sending and receiving..." + "\n\n"
                + "Number of messages: " + numMessages + "\n"
                + "Broker URL:         " + brokerUrl + "\n"
                + "Queue:              "+ queue + "\n"
                + LOG_SEPERATOR);
    }

    protected static void logStatistics(int numMessages, long sendTime, long receiveTime)
    {
        double messagesPerSecond = numMessages / (receiveTime / 1000.0);
        System.out.println("\n"
                + LOG_SEPERATOR
                + "BENCHMARK RESULTS\n"
                + LOG_SEPERATOR
                + "Sent:       " + numMessages + " messages in " + formatMillis(sendTime) + "\n"
                + "Received:   " + numMessages + " messages in " + formatMillis(receiveTime) + "\n"
                + "Throughput: " + Math.round(messagesPerSecond) + " messages/second\n"
                + LOG_SEPERATOR + "\n"
                + "Note that results include time taken for factors\n"
                + "like marshalling/unmarshalling of messages, network\n"
                + "latency, etc., and is not a direct measure of the\n"
                + "broker's performance.\n");
    }

    protected static String formatMillis(long milliseconds)
    {
        String timeString = null;
        if (milliseconds > 1000)
        {
            DecimalFormat df = new DecimalFormat("#.#");
            timeString = df.format(milliseconds / 1000.0) + " seconds";
        }
        else
        {
            timeString = milliseconds  + "ms";
        }
        return timeString;
    }

}
