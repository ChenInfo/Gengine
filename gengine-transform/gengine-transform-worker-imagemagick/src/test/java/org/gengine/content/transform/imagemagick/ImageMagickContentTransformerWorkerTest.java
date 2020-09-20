package org.gengine.content.transform.imagemagick;

import static junit.framework.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cheninfo.service.cmr.repository.PagedSourceOptions;
import org.cheninfo.service.cmr.repository.TransformationSourceOptions;
import org.gengine.content.ContentReference;
import org.gengine.content.ContentWorkResult;
import org.gengine.content.file.FileProvider;
import org.gengine.content.file.FileProviderImpl;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.handler.FileContentReferenceHandlerImpl;
import org.gengine.content.mediatype.FileMediaType;
import org.gengine.content.transform.AbstractContentTransformerWorkerTest;
import org.gengine.content.transform.ContentTransformerWorker;
import org.gengine.content.transform.ContentTransformerWorkerProgressReporter;
import org.gengine.content.transform.options.ImageTransformationOptions;
import org.gengine.content.transform.options.TransformationOptions;
import org.gengine.content.transform.options.TransformationOptionsImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @see org.cheninfo.repo.content.transform.ffmpeg.FfmpegContentTransformerWorker
 *
 */
public class ImageMagickContentTransformerWorkerTest extends AbstractContentTransformerWorkerTest
{
    private ContentTransformerWorker transformerWorker;
    private ContentReferenceHandler contentReferenceHandler;
    private ContentTransformerWorkerProgressReporter progressReporter;

    @Before
    public void setUp() throws Exception {
        FileProvider fileProvider = new FileProviderImpl(TempFileProvider.getTempDir().getPath());
        contentReferenceHandler = new FileContentReferenceHandlerImpl();
        ((FileContentReferenceHandlerImpl) contentReferenceHandler).setFileProvider(fileProvider);
        progressReporter = new LoggingProgressReporterImpl();

        transformerWorker = new ImageMagickContentTransformerWorker();
        ((ImageMagickContentTransformerWorker) transformerWorker).setSourceContentReferenceHandler(
                contentReferenceHandler);
        ((ImageMagickContentTransformerWorker) transformerWorker).setTargetContentReferenceHandler(
                contentReferenceHandler);
        ((ImageMagickContentTransformerWorker) transformerWorker).initialize();
    }

    @Test
    public void testVersion() throws Exception
    {
        assertTrue(transformerWorker.getVersionString().contains("Gengine ImageMagick Content Transformer Worker"));
        assertTrue(transformerWorker.getVersionDetailsString().contains("Version: ImageMagick"));
    }

    @Test
    public void testIsTransformable() throws Exception
    {
        if (!transformerWorker.isAvailable())
        {
            fail("worker not available");
        }
        boolean isTransformable = transformerWorker.isTransformable(
                Arrays.asList(FileMediaType.IMAGE_GIF.getMediaType()),
                FileMediaType.TEXT_PLAIN.getMediaType(),
                new TransformationOptionsImpl());
        assertFalse("Mimetype should not be supported", isTransformable);
        isTransformable = transformerWorker.isTransformable(
                Arrays.asList(FileMediaType.IMAGE_GIF.getMediaType()),
                FileMediaType.IMAGE_JPEG.getMediaType(),
                new TransformationOptionsImpl());
        assertTrue("Mimetype should be supported", isTransformable);
    }

    protected List<ContentWorkResult> transform(
            String sourceMimetype,
            String targetMimetype,
            TransformationOptions options,
            String filename,
            boolean testCreatesTargetReference) throws Exception
    {
        String sourceExtension = filename.substring(filename.lastIndexOf('.')+1);
        String targetExtension = mediaTypeService.getExtension(targetMimetype);

        // is there a test file for this conversion?
        ContentReference sourceReference =
                AbstractContentTransformerWorkerTest.getNamedQuickTestFileReference(
                        filename, sourceMimetype);
        if (sourceReference == null)
        {
            return null;  // no test file available for that extension
        }

        String callingMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();

        List<ContentWorkResult> results = null;

        if (testCreatesTargetReference)
        {
            // make a writer for the target file
            String targetFileName = getClass().getSimpleName() + "_" + callingMethodName + "_" +
            sourceExtension + "." + targetExtension;
            ContentReference createdTargetReference =
                    contentReferenceHandler.createContentReference(targetFileName, targetMimetype);
            List<ContentReference> createdTargetReferences = Arrays.asList(createdTargetReference);

            // do the transformation
            results = transformerWorker.transform(
                    Arrays.asList(sourceReference),
                    createdTargetReferences,
                    options, progressReporter);
        }
        else
        {
            // do the transformation
            results = transformerWorker.transform(
                    Arrays.asList(sourceReference),
                    targetMimetype,
                    options, progressReporter);
        }

        if (results != null)
        {
            for (ContentWorkResult result : results)
            {
                assertTrue(result.getContentReference().getUri() +
                        " size too small ", result.getContentReference().getSize() > 10);
            }
        }
        return results;
    }

    protected List<ContentWorkResult> transform(
            String sourceMimetype,
            String targetMimetype,
            TransformationOptions options,
            boolean testsCreatesTargetReferences) throws Exception
    {
        List<ContentWorkResult> results = new ArrayList<ContentWorkResult>();
        String[] quickFiles = getQuickFilenames(sourceMimetype);
        for (String quickFile : quickFiles)
        {
            List<ContentWorkResult> quickResults = transform(
                    sourceMimetype, targetMimetype, options, quickFile, testsCreatesTargetReferences);
            if (quickResults != null)
            {
                results.addAll(quickResults);
            }
        }
        return results;
    }

    @Test
    public void testEmptyPageSourceOptions() throws Exception
    {
        // Test empty source options
        ImageTransformationOptions options = new ImageTransformationOptions();
        List<ContentWorkResult> results = this.transform(
                FileMediaType.PDF.getMediaType(),
                FileMediaType.IMAGE_PNG.getMediaType(),
                options,
                true);
        assertTrue(results.get(0).getContentReference().getSize() > 10);
    }

    @Test
    public void testWorkerMultiTargetCreation() throws Exception
    {
        // Test empty source options
        ImageTransformationOptions options = new ImageTransformationOptions();
        List<ContentWorkResult> results = this.transform(
                FileMediaType.PDF.getMediaType(),
                FileMediaType.IMAGE_PNG.getMediaType(),
                options,
                "quick.pdf",
                false);
        assertEquals(2, results.size());
        assertTrue(results.get(0).getContentReference().getSize() > 10);
    }

    @Test
    public void testFirstPageSourceOptions() throws Exception
    {
        // Test first page
        ImageTransformationOptions options = new ImageTransformationOptions();
        List<TransformationSourceOptions> sourceOptionsList = new ArrayList<TransformationSourceOptions>();
        sourceOptionsList.add(PagedSourceOptions.getPage1Instance());
        options.setSourceOptionsList(sourceOptionsList);
        List<ContentWorkResult> results = this.transform(
                FileMediaType.PDF.getMediaType(),
                FileMediaType.IMAGE_PNG.getMediaType(),
                options,
                true);
        assertTrue(results.get(0).getContentReference().getSize() > 10);
    }

    @Test
    public void testSecondPageSourceOptions() throws Exception
    {
        // Test second page
        ImageTransformationOptions options = new ImageTransformationOptions();
        List<TransformationSourceOptions> sourceOptionsList = new ArrayList<TransformationSourceOptions>();
        PagedSourceOptions sourceOptions = new PagedSourceOptions();
        sourceOptions.setStartPageNumber(2);
        sourceOptions.setEndPageNumber(2);
        sourceOptionsList.add(sourceOptions);
        options.setSourceOptionsList(sourceOptionsList);
        List<ContentWorkResult> results = this.transform(
                FileMediaType.PDF.getMediaType(),
                FileMediaType.IMAGE_PNG.getMediaType(),
                options,
                true);
        assertTrue(results.get(0).getContentReference().getSize() > 10);
    }

    @Test
    public void testRangePageSourceOptions() throws Exception
    {
        // Test page range invalid for target type
        ImageTransformationOptions options = new ImageTransformationOptions();
        List<TransformationSourceOptions> sourceOptionsList = new ArrayList<TransformationSourceOptions>();
        PagedSourceOptions sourceOptions = new PagedSourceOptions();
        sourceOptions.setStartPageNumber(1);
        sourceOptions.setEndPageNumber(2);
        sourceOptionsList.add(sourceOptions);
        options.setSourceOptionsList(sourceOptionsList);
        try {
            this.transform(
                    FileMediaType.PDF.getMediaType(),
                    FileMediaType.APPLICATION_PHOTOSHOP.getMediaType(),
                    options,
                    true);
            fail("An exception regarding an invalid page range should have been thrown");
        }
        catch (Exception e)
        {
            // failure expected
        }
    }

    @Test
    public void testOutOfRangePageSourceOptions() throws Exception
    {
        // Test page out of range
        ImageTransformationOptions options = new ImageTransformationOptions();
        List<TransformationSourceOptions> sourceOptionsList = new ArrayList<TransformationSourceOptions>();
        PagedSourceOptions sourceOptions = new PagedSourceOptions();
        sourceOptions.setStartPageNumber(3);
        sourceOptions.setEndPageNumber(3);
        sourceOptionsList.add(sourceOptions);
        options.setSourceOptionsList(sourceOptionsList);
        try {
            this.transform(
                    FileMediaType.PDF.getMediaType(),
                    FileMediaType.IMAGE_PNG.getMediaType(),
                    options,
                    true);
            fail("An exception regarding an invalid page range should have been thrown");
        }
        catch (Exception e)
        {
            // failure expected
        }
    }
}
