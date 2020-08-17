package org.gengine.messaging;

/**
 * Defines a request message where a {@link Reply} is expected
 *
 * @param <RP> the Reply type
 */
public interface Request<RP extends Reply>
{
    /**
     * Gets the correlating request ID.
     * <p>
     * Note that this is unlikely to be the same value as the
     * 'native' correlation ID used by a particular messaging transport.
     *
     * @return the request correlation ID
     */
    public String getRequestId();

    /**
     * Gets the class of expected {@link Reply} messages
     *
     * @return the reply message class
     */
    public Class<RP> getReplyClass();

    /**
     * Gets the optional overriding reply to queue name
     *
     * @return the reply to queue name
     */
    public String getReplyTo();

    /**
     * Sets the optional overriding reply to queue name
     *
     * @param replyTo
     */
    public void setReplyTo(String replyTo);

}
