package org.gengine.content.transform.ffmpeg;

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
    private static final long PROGRESS_REPORT_FREQUENCY_MS = 2000;
    protected static final Pattern DURATION_PATTERN = Pattern.compile("(?<=Duration: )[^,]*");
    protected static final Pattern TIME_PATTERN = Pattern.compile("(?<=time=)[\\d:.]*");

    /** The progress reporter */
    protected ContentTransformerWorkerProgressReporter progressReporter;

    /**
     * Constructor specifying a progress reporter
     *
     * @param progressReporter
     */
    public FfmpegInputStreamReaderThreadFactory(ContentTransformerWorkerProgressReporter progressReporter)
    {
        this.progressReporter = progressReporter;
    }

    @Override
    public InputStreamReaderThread createInstance(InputStream is, Charset charset)
    {
        return new FfmpegInputStreamReaderThread(is, charset, progressReporter);
    }

    /**
     * Reads the input stream to a string buffer via InputStreamReaderThread but also
     * parses for progress
     */
    public static class FfmpegInputStreamReaderThread extends InputStreamReaderThread
    {
        protected ContentTransformerWorkerProgressReporter progressReporter;

        private Double durationTotalSecs;
        private long lastReportTime = 0;

        public FfmpegInputStreamReaderThread(InputStream is, Charset charset,
                ContentTransformerWorkerProgressReporter progressReporter)
        {
            super(is, charset);
            this.progressReporter = progressReporter;
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
                durationTotalSecs = getTotalSeconds(duration);
            }

            String match;
            while (null != (match = scanner.findWithinHorizon(TIME_PATTERN, 0))) {
                long now = (new Date()).getTime();
                if ((now - lastReportTime) > PROGRESS_REPORT_FREQUENCY_MS)
                {
                    double progressTotalSecs = getTotalSeconds(match);
                    float progress = new Double(progressTotalSecs / durationTotalSecs).floatValue();
                    progressReporter.onTransformationProgress(progress);
                    lastReportTime = (new Date()).getTime();
                }
            }
            scanner.close();
        }
    }

}
