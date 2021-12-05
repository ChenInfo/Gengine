package org.gengine.messaging.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.messaging.MessageConsumer;

/**
 * Consumer of {@link BenchmarkMessage}s which maintains a count of messages received.
 *
 */
public class BenchmarkConsumer implements MessageConsumer
{
    private static final Log logger = LogFactory.getLog(BenchmarkConsumer.class);

    protected int logAfterNumMessages = 1000;
    protected int messageCount = 0;

    public void setLogAfterNumMessages(int logAfterNumMessages)
    {
        this.logAfterNumMessages = logAfterNumMessages;
    }

    @Override
    public void onReceive(Object message)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Receiving message, current messageCount=" + messageCount + "...");
        }
        validateMessage(message);

        messageCount++;

        if (messageCount > 0 && messageCount % logAfterNumMessages == 0)
        {
            logger.debug("Received " + messageCount + " messages...");
        }
        else
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Received " + messageCount + " messages...");
            }
        }
    }

    protected void validateMessage(Object message)
    {
        if (message == null || ((BenchmarkMessage) message).getValue() == null ||
                !((BenchmarkMessage) message).getValue().equals(BenchmarkMessage.getDefaultValue()))
        {
            throw new IllegalArgumentException("Could not verify message");
        }
    }

    @Override
    public Class<?> getConsumingMessageBodyClass()
    {
        return BenchmarkMessage.class;
    }

    public int getMessageCount()
    {
        return messageCount;
    }
}
