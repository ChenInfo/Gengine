package org.gengine.messaging.amqp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.gengine.messaging.MessageConsumer;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;
import org.gengine.messaging.jackson.ObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A utility class for helping to bootstrap AMQP nodes from command line arguments
 *
 */
public class AmqpNodeBootstrapUtils
{

    public static final String PROP_MESSAGING_BROKER_URL = "gengine.messaging.broker.url";
    public static final String PROP_MESSAGING_BROKER_USERNAME = "gengine.messaging.broker.username";
    public static final String PROP_MESSAGING_BROKER_PASSWORD = "gengine.messaging.broker.password";
    public static final String PROP_MESSAGING_QUEUE_REQUEST = "gengine.messaging.queue.request";
    public static final String PROP_MESSAGING_QUEUE_REPLY = "gengine.messaging.queue.reply";

    /**
     * Creates an AMQP endpoint (sender and receiver) from the given arguments
     *
     * @param messageConsumer the processor received messages are sent to
     * @param args
     * @return
     */
    public static AmqpDirectEndpoint createEndpoint(MessageConsumer messageConsumer,
            String brokerUrl,
            String brokerUsername, String brokerPassword,
            String requestEndpoint, String replyEndpoint)
    {
        validate(brokerUrl, requestEndpoint, replyEndpoint);

        AmqpDirectEndpoint messageProducer = new AmqpDirectEndpoint();
        ObjectMapper objectMapper = ObjectMapperFactory.createInstance();

        URI broker = null;
        try
        {
            broker = new URI(brokerUrl);
        }
        catch (URISyntaxException e)
        {
            // This would have been caught by validation above
        }

        messageProducer.setHost(broker.getHost());
        Integer brokerPort = broker.getPort();
        if (brokerPort != null)
        {
            messageProducer.setPort(brokerPort);
        }

        String brokerScheme = broker.getScheme();
        if ((brokerScheme != null) && (brokerScheme.equals("amqps")||brokerScheme.equals("amqp+ssl")))
        {
            messageProducer.setIsSSL(true);
        }

        if (brokerUsername != null)
        {
            messageProducer.setUsername(brokerUsername);
        }

        if (brokerPassword != null)
        {
            messageProducer.setPassword(brokerPassword);
        }

        messageProducer.setReceiveEndpoint(requestEndpoint);
        messageProducer.setSendEndpoint(replyEndpoint);

        messageProducer.setMessageConsumer(messageConsumer);
        messageProducer.setObjectMapper(objectMapper);

        return messageProducer;
    }

    public static AmqpDirectEndpoint createEndpoint(MessageConsumer messageConsumer, Properties properties)
    {
        String brokerUrl = properties.getProperty(PROP_MESSAGING_BROKER_URL);
        String brokerUsername = properties.getProperty(PROP_MESSAGING_BROKER_USERNAME);
        String brokerPassword = properties.getProperty(PROP_MESSAGING_BROKER_PASSWORD);
        String receiveQueueName = properties.getProperty(PROP_MESSAGING_QUEUE_REQUEST);
        String replyQueueName = properties.getProperty(PROP_MESSAGING_QUEUE_REPLY);
        validate(brokerUrl, receiveQueueName, replyQueueName);
        return createEndpoint(messageConsumer,
                brokerUrl, brokerUsername, brokerPassword,
                receiveQueueName, replyQueueName);
    }

    public static void validate(
            String brokerUrl, String requestQueueName, String replyQueueName)
    {
        if (StringUtils.isEmpty(brokerUrl) ||
                (StringUtils.isEmpty(requestQueueName) &&
                StringUtils.isEmpty(replyQueueName)))
        {
            throw new IllegalArgumentException(
                    "brokerUrl, requestQueueName, and replyQueueName must not be empty");
        }
        try
        {
            new URI(brokerUrl);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

}
