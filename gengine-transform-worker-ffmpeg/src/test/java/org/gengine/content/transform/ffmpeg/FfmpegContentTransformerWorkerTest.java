package org.gengine.content.transform.ffmpeg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Arrays;

import static junit.framework.Assert.*;

import org.cheninfo.service.cmr.repository.TemporalSourceOptions;
import org.gengine.content.ContentReference;
import org.gengine.content.file.FileProvider;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.handler.FileContentReferenceHandlerImpl;
import org.gengine.content.transform.ContentTransformerWorker;
import org.gengine.content.transform.ContentTransformerWorkerProgressReporter;
import org.gengine.content.transform.ffmpeg.FfmpegContentTransformerWorker;
import org.gengine.content.transform.options.TransformationOptions;
import org.gengine.content.transform.options.TransformationOptionsImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @see org.cheninfo.repo.content.transform.ffmpeg.FfmpegContentTransformerWorker
 *
 */
public class FfmpegContentTransformerWorkerTest
{
    private static final Log logger = LogFactory.getLog(FfmpegContentTransformerWorkerTest.class);

    private ContentTransformerWorker transformerWorker;

    @Before
    public void setUp() throws Exception {
        FileProvider fileProvider = new TempFileProvider();
        ContentReferenceHandler contentReferenceHandler = new FileContentReferenceHandlerImpl();
        ((FileContentReferenceHandlerImpl) contentReferenceHandler).setFileProvider(fileProvider);
        transformerWorker = new FfmpegContentTransformerWorker();
        ((FfmpegContentTransformerWorker) transformerWorker).setSourceContentReferenceHandler(
                contentReferenceHandler);
        ((FfmpegContentTransformerWorker) transformerWorker).setTargetContentReferenceHandler(
                contentReferenceHandler);
        ((FfmpegContentTransformerWorker) transformerWorker).initialize();
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

            File sourceFile = new File(this.getClass().getResource("/quick/quick.mpg").toURI());
            long origSize = sourceFile.length();

            ContentReference source = new ContentReference(
                    this.getClass().getResource("/quick/quick.mpg").toURI().toString(), "video/mpeg");

            // make a writer for the target file
            File targetFile = TempFileProvider.createTempFile(
                    getClass().getSimpleName() + "_quick_" + sourceExtension + "_",
                    "." + targetExtension);

            ContentReference target = new ContentReference(
                    targetFile.toURI().toString(), "video/mp4");

            transformerWorker.transform(
                    Arrays.asList(source), Arrays.asList(target),
                    options, new LoggingProgressReporterImpl());

            long targetSize = targetFile.length();

            assertTrue("Target file size is zero", targetSize > 0);
            assertTrue("Trimmed target file size should be less than 1/2 original size of " + origSize +
                    " but was " + targetSize, targetSize < origSize/2);

    }

    public class LoggingProgressReporterImpl implements ContentTransformerWorkerProgressReporter
    {

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

        public void onTransformationComplete()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Completed transformation");
            }
        }
    }
}
