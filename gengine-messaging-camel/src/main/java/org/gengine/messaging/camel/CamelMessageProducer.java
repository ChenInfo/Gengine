package org.gengine.messaging.camel;

import org.apache.camel.ProducerTemplate;
import org.gengine.messaging.MessageProducer;
import org.gengine.messaging.MessagingException;

/**
 * An Apache Camel implementation of a message producer
 *
 */
public class CamelMessageProducer implements MessageProducer
{
    protected static final String HEADER_JMS_AMQP_MESSAGE_FORMAT = "JMS_AMQP_MESSAGE_FORMAT";
    protected static final Long HEADER_JMS_AMQP_MESSAGE_FORMAT_VALUE = 0L;

    protected ProducerTemplate producer;
    protected String endpoint;

    /**
     * The Camel producer template
     *
     * @param producer
     */
    public void setProducer(ProducerTemplate producer)
    {
        this.producer = producer;
    }

    /**
     * The Camel endpoint for initial delivery of the messages into the Camel context which
     * can then be routed as needed
     *
     * @param endpoint
     */
    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }


    /**
     * Checks that the given endpoint is valid
     *
     * @param endpoint
     */
    protected void validateEndpoint(String endpoint)
    {
        if (endpoint == null)
        {
            throw new IllegalArgumentException("endpoint must not be null");
        }
    }

    public void send(Object message)
    {
        try
        {
            // Hack for broken JMS to AMQP conversion
            producer.sendBodyAndHeader(endpoint, message,
                    HEADER_JMS_AMQP_MESSAGE_FORMAT, HEADER_JMS_AMQP_MESSAGE_FORMAT_VALUE);
        }
        catch (Exception e)
        {
            throw new MessagingException(e);
        }
    }

    public void send(Object message, String queueName)
    {
        try
        {
            // Hack for broken JMS to AMQP conversion
            producer.sendBodyAndHeader(queueName, message,
                    HEADER_JMS_AMQP_MESSAGE_FORMAT, HEADER_JMS_AMQP_MESSAGE_FORMAT_VALUE);
        }
        catch (Exception e)
        {
            throw new MessagingException(e);
        }
    }

}
