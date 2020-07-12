package org.gengine.content.transform.ffmpeg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cheninfo.repo.content.transform.magick.ImageResizeOptions;
import org.cheninfo.service.cmr.repository.TemporalSourceOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.transform.AbstractContentTransformerWorker;
import org.gengine.content.transform.ContentTransformerWorkerProgressReporter;
import org.gengine.content.transform.options.ImageTransformationOptions;
import org.gengine.content.transform.options.TransformationOptions;
import org.gengine.util.exec.RuntimeExec;

/**
 * An FFmpeg command line implementation of a content hash node worker
 *
 */
public class FfmpegContentTransformerWorker extends AbstractContentTransformerWorker
{
    private static final Log logger = LogFactory.getLog(FfmpegContentTransformerWorker.class);

    protected static final String CMD_OPT_ASSIGNMENT = " ";
    protected static final String CMD_OPT_DELIMITER = " ";
    protected static final String CMD_OPT_NUM_VIDEO_FRAMES = "-vframes";
    protected static final String CMD_OPT_DISABLE_AUDIO = "-an";
    protected static final String CMD_OPT_DISABLE_VIDEO = "-vn";
    protected static final String CMD_OPT_DISABLE_SUBTITLES = "-sn";
    protected static final String CMD_OPT_VIDEO_CODEC = "-vcodec";
    protected static final String CMD_OPT_FORMAT = "-f";
    protected static final String CMD_OPT_DURATION = "-t";
    protected static final String CMD_OPT_OFFSET = "-ss";
    protected static final String CMD_OPT_SIZE = "-s";
    protected static final String CMD_OPT_PAIR_1_FRAME = CMD_OPT_NUM_VIDEO_FRAMES + CMD_OPT_ASSIGNMENT + "1";

    public static final String VAR_OPTIONS = "options";

    /** offset variable name */
    public static final String VAR_OFFSET = "offset";

    /** duration variable name */
    public static final String VAR_DURATION = "duration";

    /** source variable name */
    public static final String VAR_SOURCE = "source";

    /** target variable name */
    public static final String VAR_TARGET = "target";

    protected static final String DEFAULT_OFFSET = "00:00:00";

    private static final String PREFIX_IMAGE = "image/";
    private static final String PREFIX_AUDIO = "audio/";

    /** the system command executer */
    private RuntimeExec executer;
    private String ffmpegExe = "ffmpeg";

    @Override
    public void initialize()
    {
        super.initialize();
        if (executer == null)
        {
            if (System.getProperty("ffmpeg.exe") != null)
            {
                ffmpegExe = System.getProperty("ffmpeg.exe");
            }
            executer = new RuntimeExec();
            Map<String, String[]> commandsAndArguments = new HashMap<>();
            commandsAndArguments.put(".*", new String[] {
                ffmpegExe,
                "-y",
                "-i",
                "${source}",
                "SPLIT:${options}",
                "${target}"
            });
            executer.setCommandsAndArguments(commandsAndArguments);
        }
    }

    protected void transformInternal(
            File sourceFile, String sourceMimetype,
            File targetFile, String targetMimetype,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        Map<String, String> properties = new HashMap<String, String>(5);
        // set properties
        String commandOptions = "";

        String sourceTemporalOptions = getSourceTemporalCommandOptions(sourceMimetype, targetMimetype, options);
        if (sourceTemporalOptions != null && !sourceTemporalOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + sourceTemporalOptions;
        }

        String formatOptions = getFormatCommandOptions(sourceMimetype, targetMimetype);
        if (formatOptions != null && !formatOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + formatOptions;
        }

        String exclusionOptions = getComponentExclusionCommandOptions(sourceMimetype, targetMimetype);
        if (exclusionOptions != null && !exclusionOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + exclusionOptions;
        }

        String resizeOptions = getTargetResizeCommandOptions(options);
        if (resizeOptions != null && !resizeOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + resizeOptions;
        }

        properties.put(VAR_OPTIONS, commandOptions.trim());
        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath());
        properties.put(VAR_TARGET, targetFile.getAbsolutePath());

        // execute the statement
        RuntimeExec.ExecutionResult result = executer.execute(properties);
        if (result.getExitValue() != 0 && result.getStdErr() != null && result.getStdErr().length() > 0)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("full error: \n" + result.getStdErr());
            }
            throw new Exception("Failed to perform ffmpeg transformation: \n" + result);
        }
        // success
        if (logger.isDebugEnabled())
        {
            logger.debug("ffmpeg executed successfully: \n" + result);
        }
    }

    /**
     * Gets the ffmpeg command string for the transform options
     * provided
     * <p>
     * Note: The current implementation assumes a 4:3 aspect ratio in the source
     * and that the <code>imageResizeOptions</code> given signify max width and
     * heights.
     * <p>
     * TODO: Future implementations should examine the source for the aspect ratio to
     * correctly create the thumbnail.
     *
     * @param imageResizeOptions image resize options
     * @return String the ffmpeg command options
     */
    protected String getTargetResizeCommandOptions(TransformationOptions options)
    {
        if (options == null || !(options instanceof ImageTransformationOptions))
        {
            return null;
        }
        ImageResizeOptions imageResizeOptions = ((ImageTransformationOptions) options).getResizeOptions();
        if (imageResizeOptions == null)
        {
            return null;
        }

        float aspectRatio = 1.3333f;

        StringBuilder builder = new StringBuilder(32);
        int width = 0;
        int height = 0;

        if (imageResizeOptions.getWidth() > 0 && imageResizeOptions.getHeight() > 0)
        {
            if (imageResizeOptions.getWidth() <= imageResizeOptions.getHeight())
            {
                width = imageResizeOptions.getWidth();
                height = Math.round(width * (1 / aspectRatio));
            }
            else if (imageResizeOptions.getWidth() > imageResizeOptions.getHeight())
            {
                height = imageResizeOptions.getHeight();
                width = Math.round(height * aspectRatio);
            }
        }

        if (width > 0 && height > 0)
        {
            if ((height % 2) != 0)
            {
                height = height - 1;
            }
            if ((width % 2) != 0)
            {
                width = width + 1;
            }
            builder.append(CMD_OPT_SIZE);
            builder.append(CMD_OPT_ASSIGNMENT);
            builder.append(width);
            builder.append("x");
            builder.append(height);
        }

        return builder.toString();
    }

    protected String getComponentExclusionCommandOptions(String sourceMimetype, String targetMimetype)
    {
        String commandOptions = "";
        if (disableVideo(sourceMimetype, targetMimetype))
        {
            commandOptions = commandOptions + CMD_OPT_DISABLE_VIDEO + CMD_OPT_DELIMITER;
        }
        if (disableAudio(sourceMimetype, targetMimetype))
        {
            commandOptions = commandOptions + CMD_OPT_DISABLE_AUDIO + CMD_OPT_DELIMITER;
        }
        if (disableSubtitles(sourceMimetype, targetMimetype))
        {
            commandOptions = commandOptions + CMD_OPT_DISABLE_SUBTITLES + CMD_OPT_DELIMITER;
        }
        return commandOptions.trim();
    }

    protected String getFormatCommandOptions(String sourceMimetype, String targetMimetype)
    {
        if (targetMimetype.startsWith(PREFIX_IMAGE))
        {
            return CMD_OPT_FORMAT + CMD_OPT_ASSIGNMENT + "image2";
        }
        if (targetMimetype.equals("audio/ogg"))
        {
            return CMD_OPT_FORMAT + CMD_OPT_ASSIGNMENT + "ogg";
        }
        return null;
    }

    /**
     * Gets the ffmpeg command string for the time-based video conversion transform options
     * provided
     *
     * @param options time-based options
     * @return String the ffmpeg command options
     */
    protected String getSourceTemporalCommandOptions(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        TemporalSourceOptions temporalSourceOptions = null;
        if (options != null)
        {
            temporalSourceOptions = options.getSourceOptions(TemporalSourceOptions.class);
        }
        String commandOptions = "";
        if (isSingleSourceFrameRangeRequired(sourceMimetype, targetMimetype))
        {
            commandOptions = commandOptions + CMD_OPT_PAIR_1_FRAME + CMD_OPT_DELIMITER;
        }
        else
        {
            if (temporalSourceOptions != null && temporalSourceOptions.getDuration() != null)
            {
                commandOptions = commandOptions +
                        CMD_OPT_DURATION + CMD_OPT_ASSIGNMENT + temporalSourceOptions.getDuration() +
                        CMD_OPT_DELIMITER;
            }
        }
        if (temporalSourceOptions != null && temporalSourceOptions.getOffset() != null)
        {
            commandOptions = commandOptions +
                    CMD_OPT_OFFSET + CMD_OPT_ASSIGNMENT + temporalSourceOptions.getOffset() +
                    CMD_OPT_DELIMITER;
        }
        return commandOptions.trim();
    }

    /**
     * Determines whether or not a single frame is required for the given source and target mimetypes.
     *
     * @param sourceMimetype
     * @param targetMimetype
     * @return whether or not a page range must be specified for the transformer to read the target files
     */
    protected boolean isSingleSourceFrameRangeRequired(String sourceMimetype, String targetMimetype)
    {
        // Need a single frame if we're transforming from video to an image
        return (targetMimetype.startsWith(PREFIX_IMAGE));
    }

    /**
     * Determines whether or not video should be disabled for the given source and target mimetypes.
     *
     * @param sourceMimetype
     * @param targetMimetype
     * @return whether or not to disable video in the output
     */
    protected boolean disableVideo(String sourceMimetype, String targetMimetype)
    {
        return (targetMimetype.startsWith(PREFIX_AUDIO));
    }

    /**
     * Determines whether or not audio should be disabled for the given source and target mimetypes.
     *
     * @param sourceMimetype
     * @param targetMimetype
     * @return whether or not to disable audio in the output
     */
    protected boolean disableAudio(String sourceMimetype, String targetMimetype)
    {
        return (targetMimetype.startsWith(PREFIX_IMAGE));
    }

    /**
     * Determines whether or not subtitles should be disabled for the given source and target mimetypes.
     *
     * @param sourceMimetype
     * @param targetMimetype
     * @return whether or not to disable subtitles in the output
     */
    protected boolean disableSubtitles(String sourceMimetype, String targetMimetype)
    {
        return (targetMimetype.startsWith(PREFIX_AUDIO));
    }

}
