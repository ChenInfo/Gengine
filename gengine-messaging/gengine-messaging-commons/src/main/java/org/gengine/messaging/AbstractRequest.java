package org.gengine.messaging;

import java.util.UUID;

/**
 * Represents a generic messaging request
 *
 */
public abstract class AbstractRequest
{
    private final String requestId;
    private String replyQueueName;

    public AbstractRequest()
    {
        super();
        this.requestId = UUID.randomUUID().toString();
    }

    /**
     * Gets the generated UUID for the transformation request
     *
     * @return the request ID
     */
    public String getRequestId()
    {
        return requestId;
    }

    /**
     * Gets the optional overriding reply to queue replies should be sent to
     *
     * @return the optional override reply to queue
     */
    public String getReplyTo()
    {
        return replyQueueName;
    }

    /**
     * Sets the optional overriding reply to queue replies should be sent to
     *
     * @param replyQueueName
     */
    public void setReplyTo(String replyQueueName)
    {
        this.replyQueueName = replyQueueName;
    }

}
