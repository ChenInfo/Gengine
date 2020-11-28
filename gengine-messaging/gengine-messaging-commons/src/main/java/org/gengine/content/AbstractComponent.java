package org.gengine.content;

import java.util.concurrent.ExecutorService;

import org.gengine.messaging.MessageProducer;

/**
 * Base implementation of a component with content worker and messageProducer fields.
 *
 * @param <W>
 */
public abstract class AbstractComponent<W extends ContentWorker> implements Component
{
    protected W worker;
    protected MessageProducer messageProducer;
    protected ExecutorService executorService;

    /**
     * Sets the transformer worker which does the actual work of the transformation
     *
     * @param transformerWorker
     */
    public void setWorker(W worker)
    {
        this.worker = worker;
    }

    /**
     * Sets the message producer used to send replies
     *
     * @param messageProducer
     */
    public void setMessageProducer(MessageProducer messageProducer)
    {
        this.messageProducer = messageProducer;
    }

    /**
     * Sets the executor service components may optionally need for running
     * separate threads.
     *
     * @param executorService
     */
    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    public void init()
    {
    }

    public void onReceive(Object message)
    {
        onReceiveImpl(message);
    }

    protected abstract void onReceiveImpl(Object message);

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + "[");
        builder.append("worker: " +
                ((worker == null) ? worker : worker.toString()));
        builder.append(", ");
        builder.append("messageProducer: " +
                ((messageProducer == null) ? messageProducer : messageProducer.toString()));
        builder.append("]");
        return builder.toString();
    }

}
