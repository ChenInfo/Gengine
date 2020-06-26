package org.gengine.content.transform;

import java.io.File;

import org.gengine.content.ContentReference;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.transform.options.TransformationOptions;

/**
 * Abstract transform node worker which uses a content reference handler to convert the
 * content reference into a usable File object for the actual implementation.
 *
 */
public abstract class AbstractContentTransformerWorker implements ContentTransformerWorker
{
    protected ContentReferenceHandler sourceContentReferenceHandler;
    protected ContentReferenceHandler targetContentReferenceHandler;

    public void setSourceContentReferenceHandler(ContentReferenceHandler sourceContentReferenceHandler)
    {
        this.sourceContentReferenceHandler = sourceContentReferenceHandler;
    }

    public void setTargetContentReferenceHandler(ContentReferenceHandler targetContentReferenceHandler)
    {
        this.targetContentReferenceHandler = targetContentReferenceHandler;
    }

    public void init()
    {
    }

    public void transform(
            ContentReference source,
            ContentReference target,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        transformInternal(
                sourceContentReferenceHandler.getFile(source), source.getMediaType(),
                targetContentReferenceHandler.getFile(target), target.getMediaType(),
                options,
                progressReporter);
    }

    /**
     * Transforms the given source file to the given target file and media type using
     * the given transformation options and reports progress via the given progress reporter.
     *
     * @param sourceFile
     * @param sourceMimetype
     * @param targetFile
     * @param targetMimetype
     * @param options
     * @param progressReporter
     * @throws Exception
     */
    protected abstract void transformInternal(
            File sourceFile, String sourceMimetype,
            File targetFile, String targetMimetype,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + "[");
        builder.append("sourceContentReferenceHandler: " + sourceContentReferenceHandler.toString());
        builder.append(", ");
        builder.append("targetContentReferenceHandler: " + targetContentReferenceHandler.toString());
        builder.append("]");
        return builder.toString();
    }

}
