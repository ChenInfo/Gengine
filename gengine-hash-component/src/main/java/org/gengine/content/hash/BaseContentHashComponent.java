package org.gengine.content.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.AbstractComponent;
import org.gengine.content.hash.HashReply;
import org.gengine.content.hash.HashRequest;
import org.gengine.messaging.MessageProducer;

/**
 * A base implementation of a hash node which receives messages, uses a {@link ContentHashWorker}
 * to perform the hash computation, then uses a {@link MessageProducer} to send the reply.
 *
 */
public class BaseContentHashComponent extends AbstractComponent<ContentHashWorker>
{
    private static final Log logger = LogFactory.getLog(BaseContentHashComponent.class);

    public void onReceive(Object message)
    {
        HashRequest request = (HashRequest) message;
        if (logger.isDebugEnabled())
        {
            logger.info("Processing hash requestId=" + request.getRequestId());
        }
        try
        {

            String value = worker.generateHash(
                    request.getSourceContentReference(),
                    request.getHashAlgorithm());

            HashReply reply = new HashReply(request);
            reply.setHexValue(value);

            if (logger.isDebugEnabled())
            {
                logger.debug("Sending reply");
            }
            messageProducer.send(reply, request.getReplyTo());
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            // TODO send error reply
        }
    }

    public Class<?> getConsumingMessageBodyClass()
    {
        return HashRequest.class;
    }

}
