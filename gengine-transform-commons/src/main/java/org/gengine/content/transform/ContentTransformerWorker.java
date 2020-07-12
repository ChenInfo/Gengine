package org.gengine.content.transform;

import org.gengine.content.ContentReference;
import org.gengine.content.ContentWorker;
import org.gengine.content.transform.options.TransformationOptions;

/**
 * Defines the methods responsible for doing the work of transformation of a content reference
 *
 */
public interface ContentTransformerWorker extends ContentWorker
{
    public void transform(
            ContentReference source,
            ContentReference target,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception;
}
