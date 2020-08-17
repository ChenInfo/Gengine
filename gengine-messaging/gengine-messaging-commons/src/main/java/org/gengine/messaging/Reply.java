package org.gengine.messaging;

/**
 * Defines a reply message for a specific {@link Request} message
 *
 */
public interface Reply
{

    /**
     * The correlating request ID for the reply.
     * <p>
     * Note that this is unlikely to be the same value as the
     * 'native' correlation ID used by a particular messaging transport.
     *
     * @return the request correlation ID
     */
    public String getRequestId();

}
