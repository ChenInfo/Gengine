package org.gengine.content.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.gengine.content.ContentReference;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.handler.FileContentReferenceHandler;
import org.gengine.content.mediatype.FileMediaType;
import org.gengine.content.transform.options.TransformationOptions;

public abstract class AbstractFileContentTransformerWorker extends AbstractContentTransformerWorker
{

    public List<ContentReference> transform(
            List<ContentReference> sources,
            List<ContentReference> targets,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        boolean isTempTargetUsed = true;
        List<FileContentReferencePair> sourcePairs = null;
        List<FileContentReferencePair> targetPairs = null;

        if (sources != null)
        {
            sourcePairs = new ArrayList<>(sources.size());
            if (sourceContentReferenceHandler instanceof FileContentReferenceHandler)
            {
                for (ContentReference source : sources)
                {
                    sourcePairs.add(new FileContentReferencePair(
                            ((FileContentReferenceHandler) sourceContentReferenceHandler).getFile(
                                    source, true),
                            source));
                }
            }
            else
            {
                for (ContentReference source : sources)
                {
                    InputStream sourceInputStream = sourceContentReferenceHandler.getInputStream(source, true);
                    File sourceFile = createTempFile(source);
                    FileUtils.copyInputStreamToFile(sourceInputStream, sourceFile);

                    sourcePairs.add(new FileContentReferencePair(sourceFile, source));
                }
            }
        }
        if (targets != null)
        {
            targetPairs = new ArrayList<FileContentReferencePair>(targets.size());
            if (targetContentReferenceHandler instanceof FileContentReferenceHandler)
            {
                isTempTargetUsed = false;
                for (ContentReference target : targets)
                {
                    File targetFile = ((FileContentReferenceHandler) targetContentReferenceHandler).getFile(target, false);
                    targetPairs.add(new FileContentReferencePair(targetFile, target));
                }
            }
            else
            {
                for (ContentReference target : targets)
                {
                    File targetFile = createTempFile(target);
                    targetPairs.add(new FileContentReferencePair(targetFile, target));
                }
            }
        }

        List<File> resultFiles = transformInternal(
                sourcePairs,
                targetPairs,
                options,
                progressReporter);

        if (resultFiles == null)
        {
            return null;
        }
        // We're assuming the final results are the same size and in the same order as the targets if present
        if (targets != null && targets.size() != resultFiles.size())
        {
            throw new IllegalStateException(
                    "The number of actual target files (" + resultFiles.size() + ") " +
                    "did not match the number of expected targets (" + targets.size() + ")");
        }

        List<ContentReference> results = new ArrayList<ContentReference>(resultFiles.size());

        for (int i = 0; i < resultFiles.size(); i++)
        {
            File resultFile = resultFiles.get(i);
            String resultMediaType = FileMediaType.SERVICE.getMediaTypeByName(resultFile);
            ContentReference target = null;
            if (isTempTargetUsed)
            {
                if (targets != null)
                {
                    target = targets.get(i);
                }
                else
                {
                    target = targetContentReferenceHandler.createContentReference(
                            resultFile.getName(), resultMediaType);
                }
                FileInputStream targetInputStream = new FileInputStream(resultFile);
                targetContentReferenceHandler.putInputStream(targetInputStream, target);
            }
            else
            {
                target = targets.get(i);
            }
            target.setSize(resultFile.length());
            results.add(target);
        }
        return results;
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
     * @param sourceFiles
     * @param sourceRefs
     * @param targetFiles
     * @param targetRefs
     * @param options
     * @param progressReporter
     * @throws Exception
     */
    protected abstract List<File> transformInternal(
            List<FileContentReferencePair> sources,
            List<FileContentReferencePair> targets,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception;

    /**
     * Wrapper for a content reference and a {@link File}, useful
     * for passing around an already instantiated File object with
     * a related content reference.
     */
    protected class FileContentReferencePair
    {
        private File file;
        private ContentReference contentReference;

        public FileContentReferencePair(File file, ContentReference contentReference)
        {
            super();
            this.file = file;
            this.contentReference = contentReference;
        }

        public File getFile()
        {
            return file;
        }
        public void setFile(File file)
        {
            this.file = file;
        }
        public ContentReference getContentReference()
        {
            return contentReference;
        }
        public void setContentReference(ContentReference contentReference)
        {
            this.contentReference = contentReference;
        }
    }
}
