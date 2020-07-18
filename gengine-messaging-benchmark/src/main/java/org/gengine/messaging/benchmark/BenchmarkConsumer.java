package org.gengine.messaging.benchmark;

import org.gengine.messaging.MessageConsumer;

/**
 * Consumer of {@link BenchmarkMessage}s which maintains a count of messages received.
 */
public class BenchmarkConsumer implements MessageConsumer
{
    private int messageCount = 0;

    @Override
    public void onReceive(Object message)
    {
        if (!((BenchmarkMessage) message).getValue().equals(BenchmarkMessage.DEFAULT_VALUE))
        {
            throw new IllegalArgumentException("Could not verify message");
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
