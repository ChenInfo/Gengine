package org.gengine.messaging.benchmark;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.DataFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.ConnectionFactory;

import org.gengine.messaging.MessageConsumer;
import org.gengine.messaging.MessageProducer;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;
import org.gengine.messaging.amqp.AmqpNodeBootstrapUtils;
import org.gengine.messaging.camel.CamelMessageProducer;
import org.gengine.messaging.jackson.ObjectMapperFactory;

/**
 * Boostrap which creates an {@link AmqpDirectEndpoint} or Camel-based endpoint
 * (depending on the <code>brokerUrl</code> given) with a {@link BenchmarkConsumer}
 * to measure the throughput of a broker.
 * <p>
 * <code>brokerUrl</code> beginning with:
 * <ul>
 *  <li><code>tcp</code>: creates a Camel-based endpoint using JSON object marshaling/unmarshaling</li>
 *  <li><code>amqp</code>: creates an {@link AmqpDirectEndpoint} without object marshaling</li>
 * </ul>
 */
public class Bootstrap
{
    private static final Log logger = LogFactory.getLog(Bootstrap.class);

    protected static final String DEFAULT_QUEUE = "gengine.test.benchmark";
    protected static final String USAGE_MESSAGE = "USAGE: brokerUrl numMessages [queue]";
    protected static final String LOG_SEPERATOR = "--------------------------------------------------\n";

    private static final long CHECK_CONSUMER_COMPLETE_PERIOD_MS = 100;

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

        try
        {
            runBenchmark(brokerUrl, queue, numMessages);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    protected static void runBenchmark(final String brokerUrl, final String queue, int numMessages) throws Exception
    {

        BenchmarkConsumer messageConsumer = new BenchmarkConsumer();
        MessageProducer endpoint = null;

        if (brokerUrl.startsWith("tcp") || brokerUrl.startsWith("failover"))
        {
            logger.debug("Initializing Camel Endpoint");
            endpoint = initializeCamelEndpoint(brokerUrl, queue, messageConsumer);
        }
        else if (brokerUrl.startsWith("amqp"))
        {
            logger.debug("Initializing AmqpDirect Endpoint");
            endpoint = initializeAmqpDirectEndpoint(brokerUrl, queue, messageConsumer);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported transport in " + brokerUrl);
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
                logger.debug("Sent " + (i + 1) + " messages...");
            }
            else
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Sent " + (i + 1) + " messages...");
                }
            }
        }
        long endSend = (new Date()).getTime();
        long sendTime = endSend - start;

        // Wait for consumer to dequeue all messages
        while (messageConsumer.getMessageCount() < numMessages)
        {
            try
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Consumer still working, sleeping " + CHECK_CONSUMER_COMPLETE_PERIOD_MS + "ms");
                }
                Thread.sleep(CHECK_CONSUMER_COMPLETE_PERIOD_MS);
            }
            catch (InterruptedException e)
            {
            }
        }
        long end = (new Date()).getTime();
        long receiveTime = end - start;

        logStatistics(endpoint, numMessages, sendTime, receiveTime);
        System.exit(0);
    }

    /**
     * Initializes a Camel context and configures routes and object marshaling with the given
     * brokerUrl, queue, and messageConsumer.
     *
     * @param brokerUrl
     * @param queue
     * @param messageConsumer
     * @return the Gengine message producer
     * @throws Exception
     */
    protected static MessageProducer initializeCamelEndpoint(
            final String brokerUrl, final String queue, final MessageConsumer messageConsumer) throws Exception
    {
        CamelContext context = new DefaultCamelContext();

        ConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(brokerUrl);
        JmsComponent component = AMQPComponent.jmsComponent();
        component.setConnectionFactory(connectionFactory);
        context.addComponent("amqp", component);

        final DataFormat dataFormat = new JacksonDataFormat(
                ObjectMapperFactory.createInstance(), Object.class);

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("amqp:queue:" + queue).unmarshal(dataFormat).bean(messageConsumer, "onReceive");
                from("direct:benchmark.test").marshal(dataFormat).to("amqp:queue:" + queue);
            }
        });

        CamelMessageProducer messageProducer = new CamelMessageProducer();
        messageProducer.setProducer(context.createProducerTemplate());
        messageProducer.setEndpoint("direct:benchmark.test");

        context.start();

        return messageProducer;
    }

    /**
     * Initializes a Qpid-based AMQP endpoint with no object marshaling with given
     * brokerUrl, queue, and messageConsumer.
     *
     * @param brokerUrl
     * @param queue
     * @param messageConsumer
     * @return the Gengine message producer
     */
    protected static MessageProducer initializeAmqpDirectEndpoint(
            final String brokerUrl, final String queue, final MessageConsumer messageConsumer)
    {
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
        return endpoint;
    }

    /**
     * Logs the start of the benchmark to sys out
     *
     * @param numMessages
     * @param brokerUrl
     * @param queue
     */
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

    /**
     * Logs the results of the benchmark to sys out
     *
     * @param endpoint
     * @param numMessages
     * @param sendTime
     * @param receiveTime
     */
    protected static void logStatistics(
            MessageProducer endpoint, int numMessages, long sendTime, long receiveTime)
    {
        double messagesPerSecond = numMessages / (receiveTime / 1000.0);
        System.out.println("\n"
                + LOG_SEPERATOR
                + "BENCHMARK RESULTS\n"
                + LOG_SEPERATOR
                + "MessageProducer: " + endpoint.getClass().getSimpleName() + "\n"
                + "Sent:            " + numMessages + " messages in " + formatMillis(sendTime) + "\n"
                + "Received:        " + numMessages + " messages in " + formatMillis(receiveTime) + "\n"
                + "Throughput:      " + Math.round(messagesPerSecond) + " messages/second\n"
                + LOG_SEPERATOR + "\n"
                + "Note that results include time taken for factors\n"
                + "like marshalling/unmarshalling of messages, network\n"
                + "latency, etc., and is not a direct measure of the\n"
                + "broker's performance.\n");
    }

    /**
     * Formats a millisecond value for rounded seconds if sufficientaly large
     *
     * @param milliseconds
     * @return the string representation of the given time
     */
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
