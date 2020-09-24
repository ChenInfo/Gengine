package org.gengine.content.transform.ffmpeg;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

import org.cheninfo.service.cmr.repository.TemporalSourceOptions;
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
import org.gengine.content.transform.ffmpeg.FfmpegContentTransformerWorker;
import org.gengine.content.transform.options.TransformationOptions;
import org.gengine.content.transform.options.TransformationOptionsImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @see org.cheninfo.repo.content.transform.ffmpeg.FfmpegContentTransformerWorker
 *
 */
public class FfmpegContentTransformerWorkerTest extends AbstractContentTransformerWorkerTest
{

    private ContentTransformerWorker transformerWorker;
    private ContentTransformerWorkerProgressReporter progressReporter;
    private ContentReference source;

    @Before
    public void setUp() throws Exception {
        FileProvider fileProvider = new FileProviderImpl(TempFileProvider.getTempDir().getPath());
        ContentReferenceHandler contentReferenceHandler = new FileContentReferenceHandlerImpl();
        ((FileContentReferenceHandlerImpl) contentReferenceHandler).setFileProvider(fileProvider);
        transformerWorker = new FfmpegContentTransformerWorker();
        ((FfmpegContentTransformerWorker) transformerWorker).setSourceContentReferenceHandler(
                contentReferenceHandler);
        ((FfmpegContentTransformerWorker) transformerWorker).setTargetContentReferenceHandler(
                contentReferenceHandler);
        ((FfmpegContentTransformerWorker) transformerWorker).initialize();

        progressReporter = new LoggingProgressReporterImpl();

        File sourceFile = new File(this.getClass().getResource("/quick/quick.mpg").toURI());
        source = new ContentReference(
                sourceFile.toURI().toString(), "video/mpeg", sourceFile.length());
    }

    @Test
    public void testVersion() throws Exception
    {
        assertTrue(transformerWorker.getVersionString().contains("Gengine FFmpeg Content Transformer Worker"));
        assertTrue(transformerWorker.getVersionDetailsString().contains("ffmpeg version") ||
                transformerWorker.getVersionDetailsString().contains("FFmpeg version"));
    }

    @Test
    public void testIsTransformable() throws Exception
    {
        boolean isTransformable = transformerWorker.isTransformable(
                Arrays.asList(FileMediaType.VIDEO_MP4.getMediaType()),
                FileMediaType.IMAGE_PNG.getMediaType(),
                new TransformationOptionsImpl());
        assertTrue("Should be supported with standard options", isTransformable);
        isTransformable = transformerWorker.isTransformable(
                Arrays.asList(FileMediaType.VIDEO_MPG.getMediaType()),
                FileMediaType.VIDEO_MPG.getMediaType(),
                new TransformationOptionsImpl());
        assertTrue("Should be supported with standard options", isTransformable);

        isTransformable = transformerWorker.isTransformable(
                Arrays.asList("audio/vnd.adobe.soundbooth"),
                FileMediaType.MP3.getMediaType(),
                new TransformationOptionsImpl());
        assertFalse("Should *not* be supported with standard options", isTransformable);
        isTransformable = transformerWorker.isTransformable(
                Arrays.asList(FileMediaType.MP3.getMediaType()),
                FileMediaType.IMAGE_PNG.getMediaType(),
                new TransformationOptionsImpl());
        assertFalse("Should *not* be supported with standard options", isTransformable);
    }

    @Test
    public void testTrimTransformation() throws Exception
    {
        TemporalSourceOptions temporalSourceOptions = new TemporalSourceOptions();
        temporalSourceOptions.setOffset("00:00:00.5");
        temporalSourceOptions.setDuration("00:00:00.2");
        TransformationOptions options = new TransformationOptionsImpl();
        options.addSourceOptions(temporalSourceOptions);

        String sourceExtension = "mpg";
        String targetExtension = "mp4";

        // make a writer for the target file
        File targetFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_quick_" + sourceExtension + "_",
                "." + targetExtension);

        ContentReference target = new ContentReference(
                targetFile.toURI().toString(), "video/mp4");

        transformerWorker.transform(
                Arrays.asList(source),
                Arrays.asList(target),
                options,
                progressReporter);

        long targetSize = targetFile.length();

        assertTrue("Target file size is zero", targetSize > 0);
        assertTrue("Trimmed target file size should be less than 1/2 original size of " + source.getSize() +
                " but was " + targetSize, targetSize < source.getSize()/2);
    }

    @Test
    @Ignore
    public void testStoryboardThumbnails() throws Exception
    {
        TransformationOptions options = new TransformationOptionsImpl();
        List<ContentWorkResult> results = transformerWorker.transform(
                Arrays.asList(source),
                FileMediaType.IMAGE_JPEG.getMediaType(),
                options,
                progressReporter);
        assertEquals(4, results.size());
        for (ContentWorkResult result : results)
        {
            assertTrue("Target file size is zero", result.getContentReference().getSize() > 0);
        }
    }
}
