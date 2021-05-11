package org.gengine.content.transform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;
import org.gengine.content.ContentWorkResult;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.handler.FileContentReferenceHandler;
import org.gengine.content.mediatype.FileMediaType;
import org.gengine.content.transform.options.TransformationOptions;
import org.gengine.error.GengineRuntimeException;

/**
 * Extension of AbstractContentTransformerWorker for dealing with file
 * content references
 *
 */
public abstract class AbstractFileContentTransformerWorker extends AbstractContentTransformerWorker
{
    private static final Log logger = LogFactory.getLog(AbstractFileContentTransformerWorker.class);

    /**
     * Creates source pairs from the given source content references
     *
     * @param sources
     * @return the source pairs
     * @throws ContentIOException
     * @throws InterruptedException
     * @throws IOException
     */
    protected List<FileContentReferencePair> getSourcePairs(List<ContentReference> sources)
            throws ContentIOException, InterruptedException, IOException
    {
        List<FileContentReferencePair> sourcePairs = null;
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
        return sourcePairs;
    }

    /**
     * Creates target pairs from the given target content references
     *
     * @param targets
     * @return the target pairs
     * @throws ContentIOException
     * @throws InterruptedException
     * @throws IOException
     */
    protected List<FileContentReferencePair> getTargetPairs(List<ContentReference> targets)
            throws ContentIOException, InterruptedException, IOException
    {
        List<FileContentReferencePair> targetPairs = null;
        if (targets != null)
        {
            targetPairs = new ArrayList<FileContentReferencePair>(targets.size());

            for (ContentReference target : targets)
            {
                File targetFile;
                if (targetContentReferenceHandler instanceof FileContentReferenceHandler)
                {
                    targetFile = ((FileContentReferenceHandler) targetContentReferenceHandler).getFile(
                            target, false);
                }
                else
                {
                    targetFile = createTempFile(target);
                }
                targetPairs.add(new FileContentReferencePair(targetFile, target));
            }
        }
        return targetPairs;
    }

    /**
     * Determines if the target content references must be managed via local temp file copies
     * or can be managed directly.
     *
     * @return whether or not temp target files are used
     */
    protected boolean isTempTargetUsed()
    {
        return !(targetContentReferenceHandler instanceof FileContentReferenceHandler);
    }

    public List<ContentWorkResult> transform(
            List<ContentReference> sources,
            List<ContentReference> targets,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        if (!isAvailable())
        {
            throw new IllegalStateException("transform called but worker is marked as unavailable");
        }
        List<FileContentReferencePair> sourcePairs = getSourcePairs(sources);
        List<FileContentReferencePair> targetPairs = getTargetPairs(targets);

        if (logger.isDebugEnabled())
        {
            logger.debug("Files obtained, calling " +
                    this.getClass().getSimpleName() + ".transformInternal...");
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

        List<ContentWorkResult> results = new ArrayList<ContentWorkResult>(resultFiles.size());

        for (int i = 0; i < resultFiles.size(); i++)
        {
            File resultFile = resultFiles.get(i);
            String resultMediaType = FileMediaType.SERVICE.getMediaTypeByName(resultFile);
            ContentReference target = null;
            if (isTempTargetUsed())
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
                target = new ContentReference(
                        resultFile.toURI().toString(), resultMediaType, resultFile.length());
            }
            target.setSize(resultFile.length());
            results.add(new ContentWorkResult(target, null));
        }
        return results;
    }

    @Override
    public List<ContentWorkResult> transform(List<ContentReference> sources, String targetMediaType,
            TransformationOptions options, ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        ContentReference targetContentReference = createTargetContentReference(targetMediaType);
        return transform(sources, Arrays.asList(targetContentReference), options, progressReporter);
    }

    /**
     * Creates a temp file from the given content reference
     *
     * @param contentReference
     * @return the temp file
     */
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
     * Creates a target content references using the targetContentReferenceHandler
     *
     * @param mediaType
     * @return the target content reference
     */
    protected ContentReference createTargetContentReference(String mediaType)
    {
        String filename = this.getClass().getSimpleName() + "-target-" +
                UUID.randomUUID().toString() + "." + FileMediaType.SERVICE.getExtension(mediaType);

        return targetContentReferenceHandler.createContentReference(filename, mediaType);
    }

    /**
     * Tests that the worker is configured and working as expected.
     *
     * @param sourcePath
     * @param targetMediaType
     * @param options
     * @throws Exception
     */
    protected void initializationTest(
            String sourcePath,
            String targetMediaType,
            TransformationOptions options) throws Exception
    {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(sourcePath);
        if (inputStream == null)
        {
            throw new GengineRuntimeException("Test file not found: " + sourcePath);
        }

        String sourceExtension = "." + sourcePath.split("\\.")[sourcePath.split("\\.").length-1];

        // Create temp source file
        File sourceFile = TempFileProvider.createTempFile(
                this.getClass().getSimpleName() + "_init_source_", sourceExtension);
        String sourceMediaType = FileMediaType.SERVICE.getMediaTypeByName(sourceFile);

        IOUtils.copy(inputStream, new FileOutputStream(sourceFile));
        ContentReference sourceContentReference = new ContentReference(
                sourceFile.toURI().toString(),
                sourceMediaType,
                sourceFile.length());
        FileContentReferencePair sourcePair =
                new FileContentReferencePair(sourceFile, sourceContentReference);

        // create the output file
        String targetExtension = "." + FileMediaType.SERVICE.getExtension(targetMediaType);
        File targetFile = TempFileProvider.createTempFile(
                this.getClass().getSimpleName() + "_init_target_", targetExtension);
        ContentReference targetContentReference = new ContentReference(
                targetFile.toURI().toString(),
                targetMediaType);
        FileContentReferencePair targetPair =
                new FileContentReferencePair(targetFile, targetContentReference);

        if (logger.isDebugEnabled())
        {
            logger.debug("Initialization test conversion from " + sourceFile + " to " + targetFile);
        }
        // execute it
        transformInternal(
                Arrays.asList(sourcePair),
                Arrays.asList(targetPair),
                options,
                null);

        // check that the file exists
        if (!targetFile.exists() || targetFile.length() == 0)
        {
            throw new GengineRuntimeException("Conversion failed: \n" + "   from: " + sourceFile
                + "\n" + "   to: " + targetFile);
        }
        // we can be sure that it works
        setIsAvailable(true);
    }


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
