package org.gengine.content.hash;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.AbstractComponent;
import org.gengine.content.ContentReference;
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

    protected void onReceiveImpl(Object message)
    {
        HashRequest request = (HashRequest) message;
        if (logger.isDebugEnabled())
        {
            logger.info("Processing hash requestId=" + request.getRequestId());
        }
        try
        {

            Map<ContentReference, String> values = worker.generateHashes(
                    request.getSourceContentReferences(),
                    request.getHashAlgorithm());

            HashReply reply = new HashReply(request);
            reply.setHexEncodedValues(values);

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
