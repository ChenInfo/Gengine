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
 * Runner which creates an {@link AmqpDirectEndpoint} or Camel-based endpoint
 * (depending on the <code>brokerUrl</code> given) with a {@link BenchmarkConsumer}
 * to measure the throughput of a broker.
 * <p>
 * <code>brokerUrl</code> beginning with:
 * <ul>
 *  <li><code>tcp</code>: creates a Camel-based endpoint using JSON object marshaling/unmarshaling</li>
 *  <li><code>amqp</code>: creates an {@link AmqpDirectEndpoint} without object marshaling</li>
 * </ul>
 *
 */
public class BenchmarkRunner
{
    private static final Log logger = LogFactory.getLog(BenchmarkRunner.class);

    protected static final String DEFAULT_ENDPOINT = "queue:gengine.test.benchmark";
    protected static final String LOG_SEPERATOR = "--------------------------------------------------\n";

    private static final long CHECK_CONSUMER_COMPLETE_PERIOD_MS = 100;

    protected int logAfterNumMessages = 1000;

    protected String brokerUrl;
    protected String brokerUsername;
    protected String brokerPassword;
    protected String endpointSend;
    protected String endpointReceive;
    protected int numMessages;
    protected boolean runProducer;
    protected boolean runConsumer;

    public BenchmarkRunner(String brokerUrl, String brokerUsername, String brokerPassword,
                           String endpointSend, String endpointReceive,
                           int numMessages, boolean runProducer, boolean runConsumer)
    {
        this.brokerUrl = brokerUrl;
        this.brokerUsername = brokerUsername;
        this.brokerPassword = brokerPassword;
        this.endpointSend = endpointSend;
        this.endpointReceive = endpointReceive;
        this.numMessages = numMessages;
        this.runProducer = runProducer;
        this.runConsumer = runConsumer;
    }

    protected String getDefaultReceiveEndpoint(String endpointSend)
    {
        return endpointSend;
    }

    protected Object getBenchmarkMessage(int i)
    {
        return BenchmarkMessage.createInstance();
    }

    protected BenchmarkConsumer getBenchmarkConsumer()
    {
        BenchmarkConsumer consumer = new BenchmarkConsumer();
        consumer.setLogAfterNumMessages(logAfterNumMessages);
        return consumer;
    }

    public void runBenchmark() throws Exception
    {
        if (endpointSend == null)
        {
            endpointSend = DEFAULT_ENDPOINT;
        }
        if (endpointReceive == null)
        {
            endpointReceive = getDefaultReceiveEndpoint(endpointSend);
        }
        BenchmarkConsumer messageConsumer = null;
        MessageProducer producer = null;

        if (runConsumer)
        {
            messageConsumer = getBenchmarkConsumer();
        }

        if (brokerUrl.startsWith("tcp") || brokerUrl.startsWith("failover") || brokerUrl.startsWith("ssl"))
        {
            logger.debug("Initializing Camel Endpoint: "+brokerUrl+(brokerUsername != null ? " ("+brokerUsername+")": ""));
            producer = initializeCamelEndpoint(brokerUrl, brokerUsername, brokerPassword,
                    endpointSend, endpointReceive, messageConsumer);
        }
        else if (brokerUrl.startsWith("amqp"))
        {
            // note: amqp://, amqps:// or amqp+ssl://
            logger.debug("Initializing AmqpDirect Endpoint: "+brokerUrl+(brokerUsername != null ? " ("+brokerUsername+")": ""));
            producer = initializeAmqpDirectEndpoint(brokerUrl, brokerUsername, brokerPassword,
                    endpointSend, endpointReceive, messageConsumer);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported transport in " + brokerUrl);
        }

        logStart(numMessages, brokerUrl, endpointSend, endpointReceive, runProducer, runConsumer);

        long start = (new Date()).getTime();
        long sendTime = 0;

        if (runProducer)
        {
            // Send the messages
            for (int i = 0; i < numMessages; i++)
            {
                Object message = getBenchmarkMessage(i);
                producer.send(message);

                if (i > 0 && i % logAfterNumMessages == 0)
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
            sendTime = endSend - start;
        }

        long receiveTime = 0;
        if (runConsumer)
        {
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
            receiveTime = end - start;
        }

        if (!runConsumer && (producer instanceof AmqpDirectEndpoint))
        {
            // TODO why is this needed to enqueue all messages (eg. when running AMQP "produce-only")
            // note: not currently counted in sendTime (throughput calculation)
            int delaySecs = 5;
            logger.debug("Waiting for "+delaySecs+" secs ...");
            Thread.sleep(delaySecs*1000);
        }

        logStatistics((runProducer ? producer : null), (runConsumer ? messageConsumer : null),
                getBenchmarkMessage(0), numMessages, sendTime, receiveTime);

        System.exit(0);
    }

    /**
     * Initializes a Camel context and configures routes and object marshaling with the given
     * brokerUrl, enpoint, and messageConsumer.
     *
     * @param brokerUrl
     * @param endpoint
     * @param messageConsumer
     * @return the Gengine message producer
     * @throws Exception
     */
    protected MessageProducer initializeCamelEndpoint(
            final String brokerUrl, final String brokerUsername, final String brokerPassword,
            final String endpointSend, final String endpointReceive,
            final MessageConsumer messageConsumer) throws Exception
    {
        CamelContext context = new DefaultCamelContext();

        ConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(brokerUsername, brokerPassword, brokerUrl);

        JmsComponent component = AMQPComponent.jmsComponent();
        component.setConnectionFactory(connectionFactory);
        context.addComponent("amqp", component);

        final DataFormat dataFormat = new JacksonDataFormat(
                ObjectMapperFactory.createInstance(), Object.class);

        if (messageConsumer != null)
        {
            context.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("amqp:" + endpointReceive).unmarshal(dataFormat).bean(messageConsumer, "onReceive");
                }
            });
        }

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("direct:benchmark.test").marshal(dataFormat).to("amqp:" + endpointSend);
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
     * brokerUrl, endpoint, and messageConsumer.
     *
     * @param brokerUrl
     * @param endpoint
     * @param messageConsumer - can be null
     * @return the Gengine message producer
     */
    protected MessageProducer initializeAmqpDirectEndpoint(
            final String brokerUrl, final String brokerUsername, final String brokerPassword,
            final String endpointSend, final String endpointReceive,
            final MessageConsumer messageConsumer)
    {
        AmqpDirectEndpoint amqpEndpoint =
                AmqpNodeBootstrapUtils.createEndpoint(messageConsumer, brokerUrl, brokerUsername, brokerPassword, endpointSend, endpointReceive);

        if (messageConsumer != null)
        {
            // Start listener
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.execute(amqpEndpoint.getListener());

            // Wait for listener initialization
            while (!amqpEndpoint.isInitialized())
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
        return amqpEndpoint;
    }

    /**
     * Logs the start of the benchmark to sys out
     *
     * @param numMessages
     * @param brokerUrl
     * @param endpoint
     */
    protected void logStart(int numMessages, String brokerUrl, String endpointSend, String endpointReceive,
            boolean runProducer, boolean runConsumer)
    {
        System.out.println("\n\n"
                + LOG_SEPERATOR
                + "BENCHMARK START\n"
                + LOG_SEPERATOR
                + (runProducer && runConsumer ? "Simultaneously sending and receiving..." + "\n\n": "")
                + (runProducer && !runConsumer  ? "Sending..." + "\n\n": "")
                + (runConsumer && !runProducer ? "Receiving..." + "\n\n": "")
                + "Number of Messages: " + numMessages + "\n"
                + "Broker URL:         " + brokerUrl + "\n"
                + (runProducer ? "Send Endpoint:      " + endpointSend + "\n": "")
                + (runConsumer ? "Receive Endpoint:   " + endpointReceive + "\n": "")
                + LOG_SEPERATOR);
    }

    /**
     * Logs the results of the benchmark to sys out
     *
     * @param producer
     * @param consumer
     * @param message
     * @param numMessages
     * @param sendTime
     * @param receiveTime
     */
    protected void logStatistics(MessageProducer producer, BenchmarkConsumer consumer,
            Object message, int numMessages, long sendTime, long receiveTime)
    {
        double messagesPerSecond = numMessages / ((consumer != null ? receiveTime : sendTime) / 1000.0);

        System.out.println("\n"
                + LOG_SEPERATOR
                + "BENCHMARK RESULTS\n"
                + LOG_SEPERATOR
                + (producer != null ? "MessageProducer: " + producer.getClass().getSimpleName() + "\n": "")
                + (consumer != null ? "MessageConsumer: " + consumer.getClass().getSimpleName() + "\n": "")
                + "Message Type:    " + message.getClass().getSimpleName() + "\n"
                + (producer != null ? "Sent:            " + numMessages + " messages in " + formatMillis(sendTime) + "\n": "")
                + (consumer != null ? "Received:        " + numMessages + " messages in " + formatMillis(receiveTime) + "\n" : "")
                + "Throughput:      " + Math.round(messagesPerSecond) + " messages/second\n"
                + LOG_SEPERATOR + "\n"
                + "Note that results include time taken for factors\n"
                + "like marshalling/unmarshalling of messages, network\n"
                + "latency, etc., and is not a direct measure of the\n"
                + "broker's performance.\n");
    }

    /**
     * Formats a millisecond value for rounded seconds if sufficiently large
     *
     * @param milliseconds
     * @return the string representation of the given time
     */
    protected String formatMillis(long milliseconds)
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
