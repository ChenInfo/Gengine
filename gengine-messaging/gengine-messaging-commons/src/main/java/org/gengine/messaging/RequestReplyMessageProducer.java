package org.gengine.messaging;

import java.util.concurrent.Future;

/**
 * Defines an asynchronous request-reply message producer
 *
 * @param <RQ>
 * @param <RP>
 */
public interface RequestReplyMessageProducer<RQ extends Request<RP>, RP extends Reply> extends MessageProducer
{

    /**
     * Sends the given request message to the configured queue and waits for its reply,
     * returning a {@link Future} object that will contain that reply once available.
     *
     * @param request
     * @return an executing future object which will contain the reply once available
     * @throws MessagingException
     */
    public Future<RP> asyncRequest(RQ request) throws MessagingException;

}
