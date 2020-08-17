package org.gengine.content;

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
