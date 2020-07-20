package org.gengine.messaging.benchmark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.gengine.messaging.MessageConsumer;

/**
 * Consumer of {@link BenchmarkMessage}s which maintains a count of messages received.
 */
public class BenchmarkConsumer implements MessageConsumer
{
    private static final Log logger = LogFactory.getLog(BenchmarkConsumer.class);

    protected static final int LOG_AFTER_NUM_MESSAGES = 1000;

    private int messageCount = 0;

    @Override
    public void onReceive(Object message)
    {
        if (!((BenchmarkMessage) message).getValue().equals(BenchmarkMessage.DEFAULT_VALUE))
        {
            throw new IllegalArgumentException("Could not verify message");
        }
        if (messageCount > 0 && messageCount % LOG_AFTER_NUM_MESSAGES == 0)
        {
            logger.debug("Received " + messageCount + " messages...");
        }
        messageCount++;
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
