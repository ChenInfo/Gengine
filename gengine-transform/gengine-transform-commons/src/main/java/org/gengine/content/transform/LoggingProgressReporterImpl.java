package org.gengine.content.transform;

import java.util.List;

import org.apache.commons.logging.Log;
import org.gengine.content.ContentWorkResult;

/**
 * Progress reporter which logs via a given logger.
 *
 */
public class LoggingProgressReporterImpl implements ContentTransformerWorkerProgressReporter
{
    private Log logger;

    public LoggingProgressReporterImpl(Log logger)
    {
        this.logger = logger;
    }

    public void onTransformationStarted()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Starting transformation");
        }
    }

    public void onTransformationProgress(float progress)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(progress*100 + "% progress on transformation");
        }
    }

    public void onTransformationComplete(List<ContentWorkResult> results)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Completed transformation");
        }
    }

    @Override
    public void onTransformationError(String errorMessage)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Error performing transformation: " + errorMessage);
        }
    }

}
