package org.gengine.messaging;

/**
 * Base implementation of a reply.
 *
 */
public abstract class AbstractReply implements Reply
{
    private String requestId;

    public AbstractReply() {
        super();
    }

    public AbstractReply(Request<?> request)
    {
        super();
        this.requestId = request.getRequestId();
    }

    /**
     * Gets the UUID for the original hash request
     *
     * @return the hash request ID
     */
    public String getRequestId()
    {
        return requestId;
    }

    /**
     * Sets the UUID for the original hash request
     *
     * @param requestId
     */
    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

}
