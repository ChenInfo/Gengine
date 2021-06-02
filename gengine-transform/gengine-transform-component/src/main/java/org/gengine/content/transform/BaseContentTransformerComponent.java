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
public class BaseContentTransformerComponent
    extends AbstractComponent<ContentTransformerWorker>
{
    private static final Log logger = LogFactory.getLog(BaseContentTransformerComponent.class);

    protected TransformationRequest lastRequest;

    /**
     * Gets the last transformation request received.
     * <p>
     * This is simply the last request, stored in-memory, with no indication
     * as to it's status, it may have been completed days ago.
     *
     * @return the last request received
     */
    public TransformationRequest getLastRequest()
    {
        return lastRequest;
    }

    @Override
    protected void onReceiveImpl(Object message)
    {
        TransformationRequest request = (TransformationRequest) message;
        lastRequest = request;
        logger.info("Processing transformation requestId=" + request.getRequestId());
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
            progressReporter.onTransformationError(e.getMessage());
            logger.error(e.getMessage(), e);
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
        private boolean isReportInProgressPermitted = false;

        public ContentTransformerWorkerProgressReporterImpl(TransformationRequest request)
        {
            this.request = request;
        }

        public void onTransformationStarted()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Starting transformation " +
                        "requestId=" + request.getRequestId());
            }
            TransformationReply reply =
                    new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_IN_PROGRESS);

            messageProducer.send(reply, request.getReplyTo());

            // We can now allow in-progress messages
            isReportInProgressPermitted = true;
        }

        public void onTransformationProgress(float progress)
        {
            if (logger.isDebugEnabled())
            {
                String logMessage = progress*100 + "% progress on transformation " +
                        "requestId=" + request.getRequestId();
                if (!isReportInProgressPermitted)
                {
                    logMessage = logMessage + " (in-progress not permitted in this state)";
                }
                logger.debug(logMessage);
            }

            // Don't actually send a reply if in-progress reporting is not allowed in this state
            if (isReportInProgressPermitted)
            {
                TransformationReply reply = new TransformationReply(request);
                reply.setStatus(TransformationReply.STATUS_IN_PROGRESS);
                reply.setProgress(progress);

                messageProducer.send(reply, request.getReplyTo());
            }
        }

        public void onTransformationComplete(List<ContentWorkResult> results)
        {
            // We don't want in-progress messages coming in after completion
            isReportInProgressPermitted = false;

            if (logger.isInfoEnabled())
            {
                logger.info("Completed transformation " +
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
            // We don't want in-progress messages coming in after error
            isReportInProgressPermitted = false;

            TransformationReply reply = new TransformationReply(request);
            reply.setStatus(TransformationReply.STATUS_ERROR);
            reply.setStatusDetail(errorMessage);

            messageProducer.send(reply, request.getReplyTo());
        }
    }

}
