package org.gengine.content.transform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.AbstractComponent;
import org.gengine.content.transform.TransformationReply;
import org.gengine.content.transform.TransformationRequest;
import org.gengine.messaging.MessageProducer;
import org.gengine.messaging.MessagingException;

/**
 * A base implementation of a transform node which receives messages, uses a {@link ContentTransformerWorker}
 * to perform the transformation, then uses a {@link MessageProducer} to send the reply.
 *
 */
public class BaseContentTransformerComponent extends AbstractComponent<ContentTransformerWorker>
{
    private static final Log logger = LogFactory.getLog(BaseContentTransformerComponent.class);

    public void onReceive(Object message)
    {
        TransformationRequest request = (TransformationRequest) message;
        logger.info("Processing transformation request " + request.getRequestId());
        ContentTransformerWorkerProgressReporterImpl progressReporter =
                new ContentTransformerWorkerProgressReporterImpl(request);
        try
        {
            progressReporter.onTransformationStarted();

            worker.transform(
                    request.getSourceContentReference(),
                    request.getTargetContentReference(),
                    request.getOptions(),
                    progressReporter);

            progressReporter.onTransformationComplete();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            // TODO send error reply
        }
    }

    public Class<?> getConsumingMessageBodyClass()
    {
        return TransformationRequest.class;
    }

    /**
     * Implementation of the progress reporter which sends reply messages with
     * progress on the transformation.
     */
    public class ContentTransformerWorkerProgressReporterImpl implements ContentTransformerWorkerProgressReporter
    {
        private TransformationRequest request;

        public ContentTransformerWorkerProgressReporterImpl(TransformationRequest request)
        {
            this.request = request;
        }

        public void onTransformationStarted() throws ContentTransformationException
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Starting transformation of " +
                        "requestId=" + request.getRequestId());
            }
            TransformationReply reply =
                    new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_IN_PROGRESS);
            try
            {
                messageProducer.send(reply, request.getReplyTo());
            }
            catch (MessagingException e)
            {
                throw new ContentTransformationException(e);
            }
        }

        public void onTransformationProgress(float progress) throws ContentTransformationException
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(progress*100 + "% progress on transformation of " +
                        "requestId=" + request.getRequestId());
            }
            TransformationReply reply = new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_IN_PROGRESS);
            reply.setProgress(progress);
            try
            {
                messageProducer.send(reply, request.getReplyTo());
            }
            catch (MessagingException e)
            {
                throw new ContentTransformationException(e);
            }
        }

        public void onTransformationComplete() throws ContentTransformationException
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Completed transformation of " +
                        "requestId=" + request.getRequestId());
            }
            TransformationReply reply = new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_COMPLETE);
            try
            {
                messageProducer.send(reply, request.getReplyTo());
            }
            catch (MessagingException e)
            {
                throw new ContentTransformationException(e);
            }
        }
    }

}
