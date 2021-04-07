package org.gengine.content.transform.ffmpeg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

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
import org.gengine.content.transform.options.AudioTransformationOptions;
import org.gengine.content.transform.options.ImageResizeOptions;
import org.gengine.content.transform.options.TemporalSourceOptions;
import org.gengine.content.transform.options.TransformationOptionsImpl;
import org.gengine.content.transform.options.VideoTransformationOptions;
import org.junit.Before;
import org.junit.Test;

/**
 * @see {@link FfmpegContentTransformerWorker}
 *
 */
public class FfmpegContentTransformerWorkerIT extends AbstractContentTransformerWorkerTest
{
    private static final Log logger = LogFactory.getLog(FfmpegContentTransformerWorkerIT.class);

    protected static final String TEST_RESOURCE_CLASSPATH = "/quick/quick.mpg";
    protected static final String TEST_RESOURCE_MEDIATYPE = "video/mpeg";
    protected static final String TEST_RESOURCE_1080_CLASSPATH = "/quick/quick-1080.mov";
    protected static final String TEST_RESOURCE_1080_MEDIATYPE = "video/quicktime";
    protected static final String TEST_RESOURCE_STORYBOARD_CLASSPATH = "/quick/countdown-leader.mp4";
    protected static final String TEST_RESOURCE_STORYBOARD_MEDIATYPE = "video/mp4";

    private ContentTransformerWorker transformerWorker;
    private boolean isFfmpegVersion1;
    private StringListProgressReporter testProgressReporter;
    private File sourceFile;
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
        isFfmpegVersion1 = ((FfmpegContentTransformerWorker) transformerWorker).isVersion1orGreater();

        testProgressReporter = new StringListProgressReporter();

        sourceFile = new File(this.getClass().getResource(TEST_RESOURCE_CLASSPATH).toURI());
        source = new ContentReference(
                sourceFile.toURI().toString(), TEST_RESOURCE_MEDIATYPE, sourceFile.length());
    }

    @Test
    public void testVersion() throws Exception
    {
        String versionMatch = "Gengine FFmpeg \\([\\w\\.\\-]+\\) Content Transformer Worker.*";
        assertTrue(
                "\n\n\tExpected to match: " + versionMatch +
                "\n\tActual: " + transformerWorker.getVersionString(),
                transformerWorker.getVersionString().matches(versionMatch));
        assertTrue(transformerWorker.getVersionDetailsString().contains("ffmpeg version") ||
                transformerWorker.getVersionDetailsString().contains("FFmpeg version 0"));
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
        VideoTransformationOptions options = new VideoTransformationOptions();
        options.addSourceOptions(temporalSourceOptions);
        options.setTargetAudioCodec(AudioTransformationOptions.AUDIO_CODEC_PASSTHROUGH);
        options.setTargetVideoCodec(VideoTransformationOptions.VIDEO_CODEC_PASSTHROUGH);

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
                testProgressReporter);

        long targetSize = targetFile.length();

        assertTrue("Target file size is zero", targetSize > 0);
        assertTrue("Trimmed target file size should be less than 1/2 original size of " + source.getSize() +
                " but was " + targetSize, targetSize < source.getSize()/2);
    }

    protected List<String> getStreamDetails(String details)
    {
        List<String> streams = new ArrayList<String>();
        String[] lines = details.split(System.getProperty("line.separator"));
        for (String line : lines)
        {
            if (line.matches(".*Stream .*"))
            {
                streams.add(line.trim());
            }
        }
        return streams;
    }

    protected void assertSomeStreamMatches(String message, List<String> streams, String regex)
    {
        boolean matched = false;
        for (String stream : streams)
        {
            if (stream.matches(regex))
            {
                matched = true;
            }
            message = message + " ---- " + stream;
        }
        assertTrue(message, matched);
    }

    @Test
    public void testGetDetails() throws Exception
    {
        String details = ((FfmpegContentTransformerWorker) transformerWorker).getDetails(sourceFile);
        assertTrue(details.contains("mpeg1video"));
    }

    protected List<String> testTransformation(
            FileMediaType targetMediaType,
            VideoTransformationOptions options) throws Exception
    {
        return testTransformation(source, targetMediaType, options, testProgressReporter);
    }

    protected List<String> testTransformation(
            ContentReference sourceReference,
            FileMediaType targetMediaType,
            VideoTransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        String sourceDetails = ((FfmpegContentTransformerWorker) transformerWorker).getDetails(sourceFile);
        List<String> sourceStreams = getStreamDetails(sourceDetails);
        for (String stream : sourceStreams)
        {
            logger.debug("Source " + stream);
        }

        String sourceExtension = FileMediaType.VIDEO_MPG.getExtension();
        String targetExtension = targetMediaType.getExtension();

        // make a writer for the target file
        File targetFile = TempFileProvider.createTempFile(
                getClass().getSimpleName() + "_quick_" + sourceExtension + "_",
                "." + targetExtension);

        ContentReference target = new ContentReference(
                targetFile.toURI().toString(), targetMediaType.getMediaType());

        transformerWorker.transform(
                Arrays.asList(sourceReference),
                Arrays.asList(target),
                options,
                progressReporter);

        long targetSize = targetFile.length();
        assertTrue("Target file size is zero", targetSize > 0);

        String targetDetails = ((FfmpegContentTransformerWorker) transformerWorker).getDetails(targetFile);
        List<String> targetStreams = getStreamDetails(targetDetails);
        if (targetStreams != null)
        {
            for (String stream : targetStreams)
            {
                logger.debug("Target " + stream);
            }
        }
        return targetStreams;
    }

    @Test
    public void testM4VTransformation() throws Exception
    {
        VideoTransformationOptions options = new VideoTransformationOptions();

        List<String> targetStreams = testTransformation(FileMediaType.VIDEO_M4V, options);

        assertSomeStreamMatches("Target resolution incorrect, expected 480x270", targetStreams, ".*Video: .*480x270.*");
    }

    @Test
    public void testResizeMaintainAspectRatioTransformation() throws Exception
    {
        ImageResizeOptions resizeOptions = new ImageResizeOptions();
        resizeOptions.setHeight(180);
        resizeOptions.setWidth(396);
        resizeOptions.setMaintainAspectRatio(true);

        VideoTransformationOptions options = new VideoTransformationOptions();
        options.setResizeOptions(resizeOptions);

        List<String> targetStreams = testTransformation(FileMediaType.VIDEO_MP4, options);

        assertSomeStreamMatches("Target resolution incorrect, expected 320x180", targetStreams, ".*Video: .*320x180.*");
    }

    @Test
    public void testResizeMaintainAspectRatioNoWidthTransformation() throws Exception
    {
        ImageResizeOptions resizeOptions = new ImageResizeOptions();
        resizeOptions.setHeight(180);
        resizeOptions.setMaintainAspectRatio(true);

        VideoTransformationOptions options = new VideoTransformationOptions();
        options.setResizeOptions(resizeOptions);

        List<String> targetStreams = testTransformation(FileMediaType.VIDEO_MP4, options);

        assertSomeStreamMatches("Target resolution incorrect, expected 320x180", targetStreams, ".*Video: .*320x180.*");
    }

    @Test
    public void testResizeStretchTransformation() throws Exception
    {
        ImageResizeOptions resizeOptions = new ImageResizeOptions();
        resizeOptions.setHeight(180);
        resizeOptions.setWidth(396);
        resizeOptions.setMaintainAspectRatio(false);

        VideoTransformationOptions options = new VideoTransformationOptions();
        options.setResizeOptions(resizeOptions);

        List<String> targetStreams = testTransformation(FileMediaType.VIDEO_MP4, options);

        assertSomeStreamMatches("Target resolution incorrect, expected 396x180", targetStreams, ".*Video: .*396x180.*");
    }

    @Test
    public void testProxyTransformation() throws Exception
    {
        // Override with a higher res file so we can detect progress
        sourceFile = new File(this.getClass().getResource(TEST_RESOURCE_1080_CLASSPATH).toURI());
        source = new ContentReference(
                sourceFile.toURI().toString(), TEST_RESOURCE_1080_MEDIATYPE, sourceFile.length());

        ImageResizeOptions resizeOptions = new ImageResizeOptions();
        resizeOptions.setHeight(180);
        resizeOptions.setWidth(396);

        VideoTransformationOptions options = new VideoTransformationOptions();
        options.setResizeOptions(resizeOptions);
        options.setTargetVideoCodec(VideoTransformationOptions.VIDEO_CODEC_H264);
        options.setTargetVideoBitrate(80000L);
        options.setTargetVideoFrameRate(10.0f);
        options.setTargetAudioCodec(AudioTransformationOptions.AUDIO_CODEC_AAC);
        options.setTargetAudioSamplingRate(11025);
        options.setTargetAudioBitrate(20000L);
        options.setTargetAudioChannels(2);

        List<String> targetStreams = testTransformation(FileMediaType.VIDEO_MP4, options);

        assertSomeStreamMatches("Target video codec incorrect, expected h264", targetStreams, ".*Video: .*h264.*");
        assertSomeStreamMatches("Target resolution incorrect, expected 320x180", targetStreams, ".*Video: .*320x180.*");
        assertSomeStreamMatches("Target frame rate incorrect, expected 10 fps", targetStreams, ".*Video: .*10 fps.*");
        assertSomeStreamMatches("Target audio codec incorrect, expected aac", targetStreams, ".*Audio: .*aac.*");
        assertSomeStreamMatches("Target audio sampling rate incorrect, expected 11025 Hz", targetStreams, ".*Audio: .*11025 Hz.*");
        assertSomeStreamMatches("Target audio channels incorrect, expected stereo", targetStreams, ".*Audio: .*stereo.*");
        // bitrates may vary, particularly depending on presets used in ffmpeg < 1, but should not be over 100 kb/s
        if (isFfmpegVersion1)
        {
            assertSomeStreamMatches("Target video bitrate incorrect", targetStreams, ".*Video: .*, \\d\\d kb\\/s.*");
            assertSomeStreamMatches("Target audio bitrate incorrect", targetStreams, ".*Audio: .*, \\d\\d kb\\/s.*");
        }

        // TODO We're gonna need a bigger... test file, this doesn't work with faster FFmpeg processing
        // List<String> progressEvents = testProgressReporter.getProgressEvents();
        // assertTrue("Expected at least one progress event", progressEvents.size() > 0);
    }

    @Test
    public void testInavlidTransformation() throws Exception
    {
        VideoTransformationOptions options = new VideoTransformationOptions();
        options.setTargetVideoCodec(VideoTransformationOptions.VIDEO_CODEC_H264);
        options.setTargetAudioCodec(AudioTransformationOptions.AUDIO_CODEC_AAC);

        try
        {
            testTransformation(FileMediaType.VIDEO_WEBM, options);
            fail("Only VP8 video and Vorbis audio are supported for WebM");
        }
        catch (Exception e)
        {
            assertTrue("Expected message about codec support "
                    + "(.*Only VP8.*are supported.*) "
                    + "but received: " +  e.getMessage(),
                    e.getMessage().contains("supported for WebM"));
        }
    }

    @Test
    public void testStoryboardThumbnails() throws Exception
    {
        File storyboardTestFile = new File(this.getClass().getResource(TEST_RESOURCE_STORYBOARD_CLASSPATH).toURI());
        ContentReference storyboardTestSource = new ContentReference(
                storyboardTestFile.toURI().toString(), TEST_RESOURCE_STORYBOARD_MEDIATYPE, storyboardTestFile.length());
        TemporalSourceOptions temporalSourceOptions = new TemporalSourceOptions();
        temporalSourceOptions.setElementIntervalSeconds(1);
        VideoTransformationOptions options = new VideoTransformationOptions();
        options.addSourceOptions(temporalSourceOptions);
        List<ContentWorkResult> results = transformerWorker.transform(
                Arrays.asList(storyboardTestSource),
                FileMediaType.IMAGE_JPEG.getMediaType(),
                options,
                testProgressReporter);
        assertEquals(
                TEST_RESOURCE_STORYBOARD_CLASSPATH +
                " is just over 11 secs, should have 12 thumbnails",
                12, results.size());
        for (ContentWorkResult result : results)
        {
            assertTrue("Target file size is zero", result.getContentReference().getSize() > 0);
        }
    }

    protected class StringListProgressReporter implements ContentTransformerWorkerProgressReporter
    {
        protected static final String PREFIX_STARTED = "STARTED";
        protected static final String PREFIX_PROGRESS = "PROGRESS: ";
        protected static final String PREFIX_COMPLETE = "COMPLETE: ";
        protected static final String PREFIX_ERROR = "ERROR: ";

        private List<String> progressEvents = new ArrayList<String>();

        @Override
        public void onTransformationStarted()
        {
            progressEvents.add(PREFIX_STARTED);
        }

        @Override
        public void onTransformationProgress(float progress)
        {
            String progressEvent = PREFIX_PROGRESS + progress;
            progressEvents.add(PREFIX_PROGRESS + progress);
            logger.debug("**** progressEvent=" + progressEvent);
        }

        @Override
        public void onTransformationComplete(List<ContentWorkResult> results)
        {
            progressEvents.add(PREFIX_COMPLETE);
        }

        @Override
        public void onTransformationError(String errorMessage)
        {
            progressEvents.add(PREFIX_ERROR + errorMessage);
        }

        public List<String> getProgressEvents()
        {
            return progressEvents;
        }
    }
}
