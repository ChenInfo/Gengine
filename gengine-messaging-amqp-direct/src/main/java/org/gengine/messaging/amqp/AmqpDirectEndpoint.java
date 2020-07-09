package org.gengine.messaging.amqp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.qpid.amqp_1_0.jms.Connection;
import org.apache.qpid.amqp_1_0.jms.ConnectionFactory;
import org.apache.qpid.amqp_1_0.jms.Session;
import org.apache.qpid.amqp_1_0.jms.TextMessage;
import org.apache.qpid.amqp_1_0.jms.impl.ConnectionFactoryImpl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Queue;

import org.gengine.messaging.MessageConsumer;
import org.gengine.messaging.MessageProducer;
import org.gengine.messaging.MessagingException;
import org.gengine.messaging.Request;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A {@link MessageProducer} and message listener which interacts with AMQP
 * queues directly using Apache Qpid.
 *
 */
public class AmqpDirectEndpoint implements MessageProducer
{
    private static final Log logger = LogFactory.getLog(AmqpDirectEndpoint.class);

    private static final int DEFAULT_PORT = 5672;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "password";

    private String host;
    private int port = DEFAULT_PORT;
    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;
    private String receiveQueueName;
    private String sendQueueName;

    private Connection connection;
    private Session session;
    private org.apache.qpid.amqp_1_0.jms.MessageProducer defaultMessageProducer;

    private MessageConsumer messageConsumer;
    private AmqpListener listener;
    private ObjectMapper objectMapper;

    protected class AmqpListener implements Runnable
    {
        protected boolean isInitialized = false;

        public void run()
        {
            try
            {
                Queue receiveQueue = getSession().createQueue(receiveQueueName);
                org.apache.qpid.amqp_1_0.jms.MessageConsumer receiver =
                        getSession().createConsumer(receiveQueue);

                isInitialized = true;

                logger.info("Waiting for an AMQP message on " + host + ":" + receiveQueueName);
                receiver.setMessageListener(new MessageListener()
                {
                    public void onMessage(final javax.jms.Message message)
                    {
                        try
                        {
                            logger.debug("Processing AMQP message");
                            String stringMessage = null;

                            if (message instanceof TextMessage)
                            {
                                stringMessage = ((TextMessage) message).getText();
                            }
                            if (stringMessage != null)
                            {
                                Object pojoMessage = objectMapper.readValue(stringMessage,
                                        messageConsumer.getConsumingMessageBodyClass());
                                if (pojoMessage == null)
                                {
                                    logger.error("Request could not be unmarshalled");
                                }
                                else
                                {
                                    if (pojoMessage instanceof Request<?>)
                                    {
                                        // Check for a reply to queue message header
                                       if (StringUtils.isEmpty(((Request<?>) pojoMessage).getReplyTo()))
                                       {
                                           if (message.getJMSReplyTo() != null)

                                           {
                                               String replyQueueName = message.getJMSReplyTo().toString();
                                               if (!StringUtils.isEmpty(replyQueueName))
                                               {
                                                   ((Request<?>) pojoMessage).setReplyTo(replyQueueName);
                                               }
                                           }
                                       }
                                    }

                                    messageConsumer.onReceive(pojoMessage);
                                }
                            }
                            else
                            {
                                logger.error("No valid message body found in " + message.toString());
                            }
                        }
                        catch (JMSException | IOException e)
                        {
                            logger.error(e.getMessage(), e);
                        }
                    }
                });

                getConnection().start();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setReceiveQueueName(String receiveQueueName)
    {
        this.receiveQueueName = receiveQueueName;
    }

    public void setSendQueueName(String sendQueueName)
    {
        this.sendQueueName = sendQueueName;
    }

    public void setMessageConsumer(MessageConsumer messageConsumer)
    {
        this.messageConsumer = messageConsumer;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    private Connection getConnection() throws JMSException
    {
        if (connection == null)
        {
            ConnectionFactory connectionFactory =
                    new ConnectionFactoryImpl(host, port, username, password);
            connection = connectionFactory.createConnection();

        }
        return connection;
    }

    private Session getSession() throws JMSException
    {
        if (session == null)
        {
            session = getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
        }
        return session;
    }

    private org.apache.qpid.amqp_1_0.jms.MessageProducer getDefaultMessageProducer() throws JMSException
    {
        if (defaultMessageProducer == null)
        {
            Queue sendQueue = getSession().createQueue(sendQueueName);
            defaultMessageProducer = getSession().createProducer(sendQueue);
        }
        return defaultMessageProducer;
    }

    private org.apache.qpid.amqp_1_0.jms.MessageProducer getMessageProducer(String queueName) throws JMSException
    {
        if (sendQueueName.equals(queueName))
        {
            return getDefaultMessageProducer();
        }
        Queue queue = getSession().createQueue(queueName);
        return getSession().createProducer(queue);
    }

    public void send(Object message) {
        send(message, sendQueueName);
    }

    public void send(Object message, String queueName) {
        try
        {
            Writer strWriter = new StringWriter();
            objectMapper.writeValue(strWriter, message);
            String stringMessage = strWriter.toString();

            if (StringUtils.isEmpty(queueName))
            {
                queueName = sendQueueName;
            }

            TextMessage textMessage = getSession().createTextMessage(stringMessage);

            if (logger.isDebugEnabled())
            {
                logger.debug("Sending message to " + host + ":" + queueName + ": " + stringMessage);
            }
            getMessageProducer(queueName).send(textMessage);
        }
        catch (Exception e)
        {
            throw new MessagingException(e);
        }
    }

    public void startListener() {
        if (listener == null)
        {
            listener = new AmqpListener();
        }
        listener.run();
    }

    public AmqpListener getListener()
    {
        if (listener == null)
        {
            listener = new AmqpListener();
        }
        return listener;
    }

    public boolean isInitialized()
    {
        return listener != null && listener.isInitialized;
    }

}
