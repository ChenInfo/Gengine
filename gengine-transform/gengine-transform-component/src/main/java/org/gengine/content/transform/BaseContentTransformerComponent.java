package org.gengine.content.transform;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.AbstractComponent;
import org.gengine.content.ContentWorkResult;
import org.gengine.content.transform.TransformationReply;
import org.gengine.content.transform.TransformationRequest;
import org.gengine.messaging.MessageProducer;

/**
 * A base implementation of a transform node which receives messages, uses a {@link ContentTransformerWorker}
 * to perform the transformation, then uses a {@link MessageProducer} to send the reply.
 *
 */
public class BaseContentTransformerComponent extends AbstractComponent<ContentTransformerWorker>
{
    private static final Log logger = LogFactory.getLog(BaseContentTransformerComponent.class);

    protected void onReceiveImpl(Object message)
    {
        TransformationRequest request = (TransformationRequest) message;
        logger.info("Processing transformation request " + request.getRequestId());
        ContentTransformerWorkerProgressReporterImpl progressReporter =
                new ContentTransformerWorkerProgressReporterImpl(request);
        try
        {
            progressReporter.onTransformationStarted();

            List<ContentWorkResult> results = worker.transform(
                    request.getSourceContentReferences(),
                    request.getTargetContentReferences(),
                    request.getOptions(),
                    progressReporter);

            progressReporter.onTransformationComplete(results);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            progressReporter.onTransformationError(e.getMessage());
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

        public void onTransformationStarted()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Starting transformation of " +
                        "requestId=" + request.getRequestId());
            }
            TransformationReply reply =
                    new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_IN_PROGRESS);

            messageProducer.send(reply, request.getReplyTo());
        }

        public void onTransformationProgress(float progress)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(progress*100 + "% progress on transformation of " +
                        "requestId=" + request.getRequestId());
            }
            TransformationReply reply = new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_IN_PROGRESS);
            reply.setProgress(progress);

            messageProducer.send(reply, request.getReplyTo());
        }

        public void onTransformationComplete(List<ContentWorkResult> results)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Completed transformation of " +
                        "requestId=" + request.getRequestId());
            }
            TransformationReply reply = new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_COMPLETE);
            reply.setResults(results);

            messageProducer.send(reply, request.getReplyTo());
        }

        @Override
        public void onTransformationError(String errorMessage)
        {
            TransformationReply reply = new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_ERROR);
            reply.setStatusDetail(errorMessage);

            messageProducer.send(reply, request.getReplyTo());
        }
    }

}
