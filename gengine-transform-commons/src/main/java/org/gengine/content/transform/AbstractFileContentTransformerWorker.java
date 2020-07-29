package org.gengine.content.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.gengine.content.ContentReference;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.handler.FileContentReferenceHandler;
import org.gengine.content.transform.options.TransformationOptions;

public abstract class AbstractFileContentTransformerWorker extends AbstractContentTransformerWorker
{

    public void transform(
            ContentReference source,
            ContentReference target,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        boolean isTempTargetUsed = true;
        File sourceFile = null;
        File targetFile = null;

        if (sourceContentReferenceHandler instanceof FileContentReferenceHandler)
        {
            sourceFile = ((FileContentReferenceHandler) sourceContentReferenceHandler).getFile(source, true);
        }
        else
        {
            InputStream sourceInputStream = sourceContentReferenceHandler.getInputStream(source, true);
            sourceFile = createTempFile(source);
            FileUtils.copyInputStreamToFile(sourceInputStream, sourceFile);
        }
        if (targetContentReferenceHandler instanceof FileContentReferenceHandler)
        {
            targetFile = ((FileContentReferenceHandler) targetContentReferenceHandler).getFile(target, false);
            isTempTargetUsed = false;
        }
        else
        {
            targetFile = createTempFile(source);
        }

        transformInternal(
                sourceFile, source,
                targetFile, target,
                options,
                progressReporter);

        target.setSize(targetFile.length());

        if (isTempTargetUsed)
        {
            FileInputStream targetInputStream = new FileInputStream(targetFile);
            targetContentReferenceHandler.putInputStream(targetInputStream, target);
        }
    }

    protected File createTempFile(ContentReference contentReference)
    {
        return TempFileProvider.createTempFile(
                this.getClass().getSimpleName() + "-" + UUID.randomUUID().toString(),
                "." + getExtension(contentReference));
    }

    /**
     * Transforms the given source file to the given target file and media type using
     * the given transformation options and reports progress via the given progress reporter.
     *
     * @param sourceInputStream
     * @param sourceRef
     * @param targetFile
     * @param targetRef
     * @param options
     * @param progressReporter
     * @throws Exception
     */
    protected abstract void transformInternal(
            File sourceFile, ContentReference sourceRef,
            File targetFile, ContentReference targetRef,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception;

}
