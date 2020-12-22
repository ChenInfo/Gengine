package org.gengine.content.transform.ffmpeg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.gengine.content.transform.ContentTransformerWorkerProgressReporter;
import org.gengine.util.exec.RuntimeExec.InputStreamReaderThread;
import org.gengine.util.exec.RuntimeExec.InputStreamReaderThreadFactory;

/**
 * InputStreamReaderThreadFactory extension which, in addition to adding the input
 * stream to a string, parses the buffered portions of the stream for transformation
 * progress reported via a {@link ContentTransformerWorkerProgressReporter}.
 *
 */
public class FfmpegInputStreamReaderThreadFactory extends InputStreamReaderThreadFactory
{
    private static final Log logger = LogFactory.getLog(FfmpegInputStreamReaderThreadFactory.class);

    private static final long PROGRESS_REPORT_FREQUENCY_MS = 2000;
    protected static final Pattern DURATION_PATTERN = Pattern.compile("(?<=Duration: )[^,]*");
    protected static final Pattern TIME_PATTERN_V0 = Pattern.compile("(?<=time=)[\\d.]*");
    protected static final Pattern TIME_PATTERN_V1 = Pattern.compile("(?<=time=)[\\d:.]*");

    /** The progress reporter */
    protected ContentTransformerWorkerProgressReporter progressReporter;
    protected boolean isFfmpegVersion1;

    /**
     * Constructor specifying a progress reporter
     *
     * @param progressReporter
     */
    public FfmpegInputStreamReaderThreadFactory(
            ContentTransformerWorkerProgressReporter progressReporter,
            boolean isFfmpegVersion1)
    {
        this.progressReporter = progressReporter;
        this.isFfmpegVersion1 = isFfmpegVersion1;
    }

    @Override
    public InputStreamReaderThread createInstance(InputStream is, Charset charset)
    {
        return new FfmpegInputStreamReaderThread(is, charset, progressReporter, isFfmpegVersion1);
    }

    /**
     * Reads the input stream to a string buffer via InputStreamReaderThread but also
     * parses for progress
     */
    public static class FfmpegInputStreamReaderThread extends InputStreamReaderThread
    {
        protected ContentTransformerWorkerProgressReporter progressReporter;
        protected boolean isFfmpegVersion1;

        private Double durationTotalSecs;
        private long lastReportTime = 0;

        public FfmpegInputStreamReaderThread(InputStream is, Charset charset,
                ContentTransformerWorkerProgressReporter progressReporter,
                boolean isFfmpegVersion1)
        {
            super(is, charset);
            this.progressReporter = progressReporter;
            this.isFfmpegVersion1 = isFfmpegVersion1;
        }

        @Override
        protected void processBytes(byte[] bytes, int count) throws UnsupportedEncodingException
        {
            String toWrite = new String(bytes, 0, count, charset.name());
            super.addToBuffer(toWrite);
            if (progressReporter != null)
            {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                read(byteArrayInputStream);
            }
        }

        /**
         * Gets the total seconds from the given HH:mm:ss formatted string
         *
         * @param timeHHmmss
         * @return the total number of seconds the string represents
         */
        protected double getTotalSeconds(String timeHHmmss)
        {
            String[] timeSegments = timeHHmmss.split(":");
            return Integer.parseInt(timeSegments[0]) * 3600
                    + Integer.parseInt(timeSegments[1]) * 60
                    + Double.parseDouble(timeSegments[2]);
        }

        /**
         * Checks the given input stream for progress data and
         * if present reports it via the progressReporter if sufficient time has
         * past since the last report.
         *
         * @param inputStream
         */
        protected void read(InputStream inputStream)
        {
            Scanner scanner = new Scanner(inputStream);

            if (durationTotalSecs == null)
            {
                String duration = scanner.findWithinHorizon(DURATION_PATTERN, 0);
                if (duration == null)
                {
                    // We can't do anything without the duration, skip this segment
                    scanner.close();
                    return;
                }
                try
                {
                    durationTotalSecs = getTotalSeconds(duration);
                }
                catch (Exception e)
                {
                    logger.debug("could not get duration for progress reporting from '" +
                            duration + "' with isFfmpegVersions=" + isFfmpegVersion1 + ": " + e.getMessage());
                    scanner.close();
                    return;
                }
            }

            String match;
            Pattern timePattern = isFfmpegVersion1 ? TIME_PATTERN_V1 : TIME_PATTERN_V0;
            while (null != (match = scanner.findWithinHorizon(timePattern, 0))) {
                long now = (new Date()).getTime();
                if ((now - lastReportTime) > PROGRESS_REPORT_FREQUENCY_MS)
                {
                    if (match.equals(""))
                    {
                        // we're probably done
                        break;
                    }
                    try
                    {
                        double progressTotalSecs =
                                isFfmpegVersion1 ? getTotalSeconds(match) : Double.parseDouble(match);
                        float progress = new Double(progressTotalSecs / durationTotalSecs).floatValue();
                        progressReporter.onTransformationProgress(progress);
                    }
                    catch (Exception e)
                    {
                        logger.debug("could not get progress for reporting from'" + match
                                + "' with isFfmpegVersions=" + isFfmpegVersion1 + ": " + e.getMessage());
                    }
                    lastReportTime = (new Date()).getTime();
                }
            }
            scanner.close();
        }
    }

}
