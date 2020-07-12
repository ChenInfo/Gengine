package org.gengine.content.transform;

import java.io.File;

import org.gengine.content.AbstractContentWorker;
import org.gengine.content.ContentReference;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.transform.options.TransformationOptions;

/**
 * Abstract transform node worker which uses a content reference handler to convert the
 * content reference into a usable File object for the actual implementation.
 *
 */
public abstract class AbstractContentTransformerWorker
        extends AbstractContentWorker implements ContentTransformerWorker
{
    protected ContentReferenceHandler targetContentReferenceHandler;

    public void setTargetContentReferenceHandler(ContentReferenceHandler targetContentReferenceHandler)
    {
        this.targetContentReferenceHandler = targetContentReferenceHandler;
    }

    public void initialize()
    {
    }

    public void transform(
            ContentReference source,
            ContentReference target,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        File sourceFile = sourceContentReferenceHandler.getFile(source, true);
        File targetFile = targetContentReferenceHandler.getFile(target);
        transformInternal(
                sourceFile, source.getMediaType(),
                targetFile, target.getMediaType(),
                options,
                progressReporter);
        target.setSize(targetFile.length());
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
