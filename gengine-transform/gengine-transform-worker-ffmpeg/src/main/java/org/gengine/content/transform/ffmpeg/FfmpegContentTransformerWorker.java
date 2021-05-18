package org.gengine.content.transform.ffmpeg;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.gengine.content.ContentReference;
import org.gengine.content.mediatype.FileMediaType;
import org.gengine.content.transform.AbstractRuntimeExecContentTransformerWorker;
import org.gengine.content.transform.ContentTransformerWorkerProgressReporter;
import org.gengine.content.transform.options.AudioTransformationOptions;
import org.gengine.content.transform.options.ImageResizeOptions;
import org.gengine.content.transform.options.ImageTransformationOptions;
import org.gengine.content.transform.options.TemporalSourceOptions;
import org.gengine.content.transform.options.TransformationOptions;
import org.gengine.content.transform.options.VideoTransformationOptions;
import org.gengine.error.GengineRuntimeException;
import org.gengine.util.exec.RuntimeExec;
import org.gengine.util.exec.RuntimeExec.ExecutionResult;

/**
 * An FFmpeg command line implementation of a content hash node worker
 *
 */
public class FfmpegContentTransformerWorker extends AbstractRuntimeExecContentTransformerWorker
{
    private static final Log logger = LogFactory.getLog(FfmpegContentTransformerWorker.class);

    protected static final String CMD_OPT_ASSIGNMENT = " ";
    protected static final String CMD_OPT_PARAM_ASSIGNMENT = "=";
    protected static final String CMD_OPT_DELIMITER = " ";
    protected static final String CMD_OPT_NUM_VIDEO_FRAMES = "-vframes";
    protected static final String CMD_OPT_DISABLE_AUDIO = "-an";
    protected static final String CMD_OPT_DISABLE_VIDEO = "-vn";
    protected static final String CMD_OPT_DISABLE_SUBTITLES = "-sn";
    protected static final String CMD_OPT_VIDEO_CODEC_v0 = "-vcodec";
    protected static final String CMD_OPT_VIDEO_CODEC_v1 = "-c:v";
    protected static final String CMD_OPT_VIDEO_BITRATE_v0 = "-vb";
    protected static final String CMD_OPT_VIDEO_BITRATE_v1 = "-b:v";
    protected static final String CMD_OPT_VIDEO_PRESET = "-vpre";
    protected static final String CMD_OPT_AUDIO_CODEC_v0 = "-acodec";
    protected static final String CMD_OPT_AUDIO_CODEC_v1 = "-c:a";
    protected static final String CMD_OPT_AUDIO_BITRATE_v0 = "-ab";
    protected static final String CMD_OPT_AUDIO_BITRATE_v1 = "-b:a";
    protected static final String CMD_OPT_AUDIO_SAMPLING_RATE = "-ar";
    protected static final String CMD_OPT_AUDIO_CHANNELS = "-ac";
    protected static final String CMD_OPT_FORMAT = "-f";
    protected static final String CMD_OPT_DURATION = "-t";
    protected static final String CMD_OPT_OFFSET = "-ss";
    protected static final String CMD_OPT_SIZE = "-s";
    protected static final String CMD_OPT_SCALE = "-vf scale";
    protected static final String CMD_OPT_FRAME_RATE = "-r";
    protected static final String CMD_OPT_FRAME_RATE_FILTER = "-vf fps=fps";
    protected static final String CMD_OPT_MOV_FLAGS = "-movflags";
    protected static final String CMD_OPT_MOV_FLAGS_FASTSTART = "+faststart";
    protected static final String CMD_OPT_ENABLE_EXPERIMENTAL = "-strict experimental";
    protected static final String CMD_OPT_PAIR_1_FRAME = CMD_OPT_NUM_VIDEO_FRAMES + CMD_OPT_DELIMITER + "1";
    protected static final String CMD_OPT_MULTI_TARGET_INDEX_FORMATTER = "%03d";
    protected static final String CMD_OPT_MULTI_TARGET_INDEX_REGEX = "\\\\d{3}";

    protected static final String DEFAULT_VIDEO_PRESET = "libx264-default";
    protected static final String DEFAULT_VIDEO_PRESET_PREFIX = "";
    protected static final String DEFAULT_VIDEO_PRESET_SUFFIX = ".ffpreset";

    public static final String VAR_SOURCE_OPTIONS = "sourceOptions";
    public static final String VAR_TARGET_OPTIONS = "targetOptions";

    /** offset variable name */
    public static final String VAR_OFFSET = "offset";

    /** duration variable name */
    public static final String VAR_DURATION = "duration";

    protected static final String DEFAULT_OFFSET = "00:00:00";

    private String ffmpegExe = "ffmpeg";
    private String ffmpegPresetsDir; // Often "/usr/share/ffmpeg"
    private String versionFullDetailsString;

    @Override
    protected void initializeExecuter()
    {
        if (executer == null)
        {
            if (System.getProperty("ffmpeg.exe") != null)
            {
                ffmpegExe = System.getProperty("ffmpeg.exe");
            }
            if (System.getProperty("ffmpeg.presets.dir") != null)
            {
                ffmpegPresetsDir = System.getProperty("ffmpeg.presets.dir");
            }
            executer = new RuntimeExec();
            Map<String, String[]> commandsAndArguments = new HashMap<>();
            commandsAndArguments.put(".*", new String[] {
                ffmpegExe,
                "-y",
                "SPLIT:${sourceOptions}",
                "-i",
                "${source}",
                "SPLIT:${targetOptions}",
                "${target}"
            });
            executer.setCommandsAndArguments(commandsAndArguments);
        }
    }

    @Override
    protected void initializeVersionDetailsExecuter()
    {
        if (versionDetailsExecuter == null)
        {
            versionDetailsExecuter = new RuntimeExec();
            Map<String, String[]> checkCommandsAndArguments = new HashMap<>();
            checkCommandsAndArguments.put(".*", new String[] {
                ffmpegExe,
                "-version",
            });
            versionDetailsExecuter.setCommandsAndArguments(checkCommandsAndArguments);
        }
    }

    @Override
    protected void initializeFileDetailsExecuter()
    {
        if (fileDetailsExecuter == null)
        {
            fileDetailsExecuter = new RuntimeExec();
            Map<String, String[]> commandsAndArguments = new HashMap<>();
            commandsAndArguments.put(".*", new String[] {
                ffmpegExe,
                "-i",
                "${source}"
            });
            fileDetailsExecuter.setCommandsAndArguments(commandsAndArguments);
        }
    }

    @Override
    protected void initializationTest()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("FFmpeg version " + getFfmpegVersionNumber() + " initialization test...");
        }
        if (getFfmpegVersionNumber() == null || "null".equals(getFfmpegVersionNumber()))
        {
            throw new GengineRuntimeException("Could not determine version of FFmpeg");
        }
        try
        {
            initializationTest(
                    "org/gengine/content/transform/ffmpeg/test.mp4",
                    FileMediaType.VIDEO_AVI.getMediaType(),
                    new VideoTransformationOptions());
        }
        catch (Exception e)
        {
            throw new GengineRuntimeException("Could not initialize worker: " + e.getMessage(), e);
        }
    }

    /**
     * Uses the executable defined in <code>versionDetailsExecuter</code> with the
     * given alternative arguments to obtain additional version detail output
     *
     * @param arguments
     * @return the additional version details output
     */
    protected String getVersionDetailOutput(String[] arguments)
    {
        String ffmpegVersionExe = ffmpegExe;
        RuntimeExec additionalDetailsExecuter = new RuntimeExec();
        if (versionDetailsExecuter != null)
        {
            String[] command = versionDetailsExecuter.getCommand();
            if (command != null && command.length > 0)
            {
                ffmpegVersionExe = command[0];
            }
        }
        Map<String, String[]> checkCommandsAndArguments = new HashMap<String, String[]>();
        checkCommandsAndArguments.put(".*", (String[]) ArrayUtils.addAll(
                new String[] { ffmpegVersionExe }, arguments));
        additionalDetailsExecuter.setCommandsAndArguments(checkCommandsAndArguments);
        String output = null;
        try
        {
            ExecutionResult result = additionalDetailsExecuter.execute();
            String out = result.getStdOut().trim();
            if (!out.equals(""))
            {
                output = out;
            }
            else
            {
                output = result.getStdErr().trim();
            }
        }
        catch (Throwable e)
        {
            logger.info(getClass().getSimpleName() + " could not get additional details: "
                    + (e.getMessage() != null ? e.getMessage() : ""));
        }
        return output;
    }

    @Override
    protected void initializeVersionDetailsString()
    {
        super.initializeVersionDetailsString();
        if (logger.isDebugEnabled())
        {
            logger.debug("StdOut versionDetailsString=" + this.versionDetailsString);
        }
        if (this.versionDetailsString == null)
        {
            ExecutionResult result = this.versionDetailsExecuter.execute();
            this.versionDetailsString = result.getStdErr().trim();
            if (logger.isDebugEnabled())
            {
                logger.debug("StdErr versionDetailsString=" + this.versionDetailsString);
            }
        }
        this.versionFullDetailsString = this.versionDetailsString + "";
        // Get additional details on supported formats
        String fullHelp = getVersionDetailOutput(new String[] { "-h", "full" });
        if (fullHelp != null)
        {
            this.versionFullDetailsString = this.versionFullDetailsString + "\n\n" +
                    fullHelp;
        }
        String formats = getVersionDetailOutput(new String[] { "-formats" } );
        if (formats != null)
        {
            this.versionFullDetailsString = this.versionFullDetailsString + "\n\n" +
                    formats;
        }
        reinitializeVersionString();
    }

    protected void reinitializeVersionString()
    {
        if (getProperties() == null)
        {
            versionString = this.getClass().getSimpleName();
        }
        else
        {
            String nameFormat = getProperties().getProperty(FRAMEWORK_PROPERTY_NAME);
            String name = MessageFormat.format(nameFormat, getFfmpegVersionNumber());
            versionString = name + " " + getProperties().getProperty(FRAMEWORK_PROPERTY_VERSION);
        }
    }

    /**
     * Determines if the source mimetype is supported by ffmpeg
     *
     * @param mediaType the mimetype to check
     * @return Returns true if ffmpeg can handle the given mimetype format
     */
    public static boolean isSupportedSource(String mediaType)
    {
        return ((mediaType.startsWith(FileMediaType.PREFIX_VIDEO) && !(
                mediaType.equals("video/x-rad-screenplay") ||
                mediaType.equals("video/x-sgi-movie") ||
                mediaType.equals("video/mpeg2"))) ||
                (mediaType.startsWith(FileMediaType.PREFIX_AUDIO) && !(
                mediaType.equals("audio/vnd.adobe.soundbooth"))) ||
                mediaType.equals("application/mxf"));
    }

    /**
     * Determines if FFmpeg can be made to support the given target mimetype.
     *
     * @param mimetype the mimetype to check
     * @return Returns true if ffmpeg can handle the given mimetype format
     * @see #setUnsupportedMimetypes(String)
     */
    public static boolean isSupportedTarget(String mimetype)
    {
        return ((mimetype.startsWith(FileMediaType.PREFIX_VIDEO) && !(
                mimetype.equals("video/x-rad-screenplay") ||
                mimetype.equals("video/x-sgi-movie") ||
                mimetype.equals("video/mpeg2"))) ||
                (mimetype.startsWith(FileMediaType.PREFIX_IMAGE) && !(
                mimetype.equals(FileMediaType.IMAGE_SVG.getMediaType()) ||
                mimetype.equals(FileMediaType.APPLICATION_PHOTOSHOP.getMediaType()) ||
                mimetype.equals(FileMediaType.IMG_DWG.getMediaType()) ||
                mimetype.equals("image/vnd.adobe.premiere") ||
                mimetype.equals("image/x-portable-anymap") ||
                mimetype.equals("image/x-xpixmap") ||
                mimetype.equals("image/x-dwt") ||
                mimetype.equals("image/cgm") ||
                mimetype.equals("image/ief"))) ||
                (mimetype.startsWith(FileMediaType.PREFIX_AUDIO) && !(
                mimetype.equals("audio/vnd.adobe.soundbooth"))));
    }

    @Override
    public boolean isTransformable(List<String> sourceMediaTypes, String targetMediaType, TransformationOptions options)
    {
        if (!isAvailable())
        {
            return false;
        }

        // TODO: Other transform types, i.e.:
        //   - Layer multiple sources into one target
        if (sourceMediaTypes.size() > 1)
        {
            return false;
        }

        String sourceMediaType = sourceMediaTypes.get(0);

        if (logger.isTraceEnabled() && options != null)
        {
            logger.trace("checking support of " +
                    "sourceMediaType=" + sourceMediaType + " " +
                    "targetMediaType=" + targetMediaType + " " +
                    options.getClass().getCanonicalName() + "=" + options.toString());
        }

        if (sourceMediaType.startsWith(FileMediaType.PREFIX_AUDIO) &&
                targetMediaType.startsWith(FileMediaType.PREFIX_IMAGE))
        {
            // Might be able to support audio to waveform image in the future, but for now...
            return false;
        }
        return (isSupportedSource(sourceMediaType) && isSupportedTarget(targetMediaType));
    }

    protected List<File> transformInternal(
            List<FileContentReferencePair> sourcePairs,
            List<FileContentReferencePair> targetPairs,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        if (sourcePairs.size() > 1 || targetPairs.size() > 1)
        {
            throw new IllegalArgumentException("Only single source and target "
                    + "transformations are currently supported");
        }

        FileContentReferencePair sourcePair = sourcePairs.iterator().next();
        FileContentReferencePair targetPair = targetPairs.iterator().next();

        File sourceFile = sourcePair.getFile();
        File targetFile = targetPair.getFile();

        String sourceMimetype = sourcePair.getContentReference().getMediaType();
        String targetMimetype = targetPair.getContentReference().getMediaType();

        boolean isStoryboardThumbnailRequest =
                isStoryboardThumbnailRequest(sourceMimetype, targetMimetype, options);

        if (isStoryboardThumbnailRequest)
        {
            // The default target created won't work, we need a special one
            targetContentReferenceHandler.delete(targetPair.getContentReference());
            ContentReference multiTargetContentReference =
                    createMultiTargetContentReference(targetMimetype);
            List<FileContentReferencePair> multiTargetPairs =
                    getTargetPairs(Arrays.asList(multiTargetContentReference));
            FileContentReferencePair multiTargetPair = multiTargetPairs.iterator().next();
            targetFile = multiTargetPair.getFile();
        }

        singleTransformInternal(
                sourceFile, sourceMimetype,
                targetFile, targetMimetype,
                options, progressReporter);

        if (isStoryboardThumbnailRequest)
        {
            File targetParent = targetFile.getParentFile();
            final String multiTargetFilenameMatch = targetFile.getName().replaceFirst(
                    CMD_OPT_MULTI_TARGET_INDEX_FORMATTER, CMD_OPT_MULTI_TARGET_INDEX_REGEX);
            if (logger.isDebugEnabled())
            {
                logger.debug("Looking for filenames matching " + multiTargetFilenameMatch + " in " + targetParent.getAbsolutePath());
            }
            File[] multiTargetFiles = targetParent.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.matches(multiTargetFilenameMatch);
                }
            } );
            if (logger.isDebugEnabled())
            {
                logger.debug("Found " + multiTargetFiles.length + " files");
            }
            return Arrays.asList(multiTargetFiles);
        }

        return Arrays.asList(targetFile);
        // TODO: Other transform types, i.e.:
        //   - Stitch multiple sources into one target
        //   - Merge multiple images into a movie?
        //   - Extract multiple audio tracks from a movie
    }

    protected void singleTransformInternal(
            File sourceFile, String sourceMimetype,
            File targetFile, String targetMimetype,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        Map<String, String> properties = new HashMap<String, String>(5);
        // set properties
        String sourceCommandOptions = getSourceCommandOptions(
                sourceFile, targetFile, sourceMimetype, targetMimetype, options);
        String targetCommandOptions = getTargetCommandOptions(
                sourceFile, targetFile, sourceMimetype, targetMimetype, options);

        properties.put(VAR_SOURCE_OPTIONS, sourceCommandOptions.trim());
        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath());
        properties.put(VAR_TARGET_OPTIONS, targetCommandOptions.trim());
        properties.put(VAR_TARGET, targetFile.getAbsolutePath());

        long timeoutMs = options.getTimeoutMs();

        if (logger.isTraceEnabled())
        {
            logger.trace("Executing with timeoutMs=" + timeoutMs +
                    ", properties=" + properties.toString());
        }
        // execute the statement
        RuntimeExec.ExecutionResult result = executer.execute(
                properties,
                null,
                new FfmpegInputStreamReaderThreadFactory(progressReporter, isVersion1orGreater()),
                timeoutMs);
        if (result.getExitValue() != 0 && result.getStdErr() != null && result.getStdErr().length() > 0)
        {
            throw new Exception("Failed to perform ffmpeg transformation: \n" +
                    result.toString() + "\n\n-------- Full Error --------\n" +
                    result.getStdErr() + "\n----------------------------\n");
        }
        // success
        if (logger.isDebugEnabled())
        {
            logger.debug("ffmpeg executed successfully: \n" + result);
        }
    }

    protected String getSourceCommandOptions(
            File sourceFile,
            File targetFile,
            String sourceMimetype,
            String targetMimetype,
            TransformationOptions options) throws Exception
    {
        String commandOptions = "";

        String sourceTemporalOptions = getSourceTemporalCommandOptions(sourceMimetype, targetMimetype, options);
        if (sourceTemporalOptions != null && !sourceTemporalOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + sourceTemporalOptions;
        }
        return commandOptions;
    }

    protected String getTargetCommandOptions(
            File sourceFile,
            File targetFile,
            String sourceMimetype,
            String targetMimetype,
            TransformationOptions options) throws Exception
    {
        String commandOptions = "";

        String targetTemporalOptions = getTargetTemporalCommandOptions(sourceMimetype, targetMimetype, options);
        if (targetTemporalOptions != null && !targetTemporalOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + targetTemporalOptions;
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

        String resizeOptions = getTargetResizeCommandOptions(options, sourceFile);
        if (resizeOptions != null && !resizeOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + resizeOptions;
        }

        String targetVideoOptions = getTargetVideoCommandOptions(
                sourceMimetype, targetMimetype, options);
        if (targetVideoOptions != null && !targetVideoOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + targetVideoOptions;
        }

        String targetAudioOptions = getTargetAudioCommandOptions(targetMimetype, options);
        if (targetAudioOptions != null && !targetAudioOptions.equals(""))
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER + targetAudioOptions;
        }
        return commandOptions;
    }

    protected String getFfmpegVersionNumber()
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("getFfmpegVersionNumber versionDetailsString=" + this.versionDetailsString);
        }
        Pattern verisonNumPattern =
                Pattern.compile("(FFmpeg version |ffmpeg version )((\\w|\\.|\\-)+)(.*)");
        try
        {
            Matcher versionNumMatcher = verisonNumPattern.matcher(this.versionDetailsString);
            if (versionNumMatcher.find())
            {
                return versionNumMatcher.group(2);
            }
        }
        catch (Throwable e)
        {
            logger.info("Could not determine version of FFmpeg: " + e.getMessage());
        }
        return null;
    }

    protected boolean isFilterSupported()
    {
        String ffmpegVersionNumber = getFfmpegVersionNumber();
        if (ffmpegVersionNumber == null)
        {
            return false;
        }
        // TODO Better method than to assume nightly is greater than 1.0
        if (ffmpegVersionNumber.startsWith("N-"))
        {
            return true;
        }
        DefaultArtifactVersion filtersSupportedVersion = new DefaultArtifactVersion("0.7");
        DefaultArtifactVersion thisVersion = new DefaultArtifactVersion(ffmpegVersionNumber);
        return thisVersion.compareTo(filtersSupportedVersion) >= 0;
    }

    public boolean isVersion1orGreater()
    {
        String ffmpegVersionNumber = getFfmpegVersionNumber();
        if (ffmpegVersionNumber == null)
        {
            return false;
        }

        // TODO Better method than to assume nightly is greater than 1.0
        if (ffmpegVersionNumber.startsWith("N-"))
        {
            return true;
        }
        DefaultArtifactVersion version1 = new DefaultArtifactVersion("1.0");
        DefaultArtifactVersion thisVersion = new DefaultArtifactVersion(ffmpegVersionNumber);
        return thisVersion.compareTo(version1) >= 0;
    }

    protected String getResolution(String details)
    {
        if (details == null)
        {
            return null;
        }
        String[] segments = details.split(", ");
        for (String segment : segments)
        {
            if (segment.matches("[0-9]+x[0-9]+( \\[.*\\])?"))
            {
                if (segment.contains(" "))
                {
                    return segment.split(" ")[0];
                }
                return segment;
            }
        }
        return null;
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
     * @param options transformation options
     * @param sourceFile
     * @return String the ffmpeg command options
     */
    protected String getTargetResizeCommandOptions(
            TransformationOptions options, File sourceFile)
    {
        if (options == null)
        {
            return null;
        }
        ImageResizeOptions imageResizeOptions = null;
        if (options instanceof ImageTransformationOptions)
        {
            imageResizeOptions = ((ImageTransformationOptions) options).getResizeOptions();
        }
        if (options instanceof VideoTransformationOptions)
        {
            imageResizeOptions = ((VideoTransformationOptions) options).getResizeOptions();
        }
        if (imageResizeOptions == null)
        {
            return null;
        }

        float aspectRatio = 1.3333f; // default
        try
        {
            String sourceDetails = getDetails(sourceFile);
            String sourceResolution = getResolution(sourceDetails);
            if (sourceResolution != null)
            {
                Integer sourceWidth = new Integer(sourceResolution.split("x")[0]);
                Integer sourceHeight = new Integer(sourceResolution.split("x")[1]);
                aspectRatio = sourceWidth.floatValue() / sourceHeight.floatValue();
            }
        }
        catch (Exception e)
        {
            logger.warn("Could not get file details: " + e.getMessage());
        }

        StringBuilder builder = new StringBuilder(32);
        int width = imageResizeOptions.getWidth();
        int height = imageResizeOptions.getHeight();

        if (imageResizeOptions.isMaintainAspectRatio())
        {
            // Could use ffmpeg's scale features here but this seems easier
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
            else if (!isFilterSupported())
            {
                if (imageResizeOptions.getHeight() < 0)
                {
                    width = imageResizeOptions.getWidth();
                    height = Math.round(width * (1 / aspectRatio));
                }
                else
                {
                    height = imageResizeOptions.getHeight();
                    width = Math.round(height * aspectRatio);
                }
            }
            if (height > 0 && (height % 2) != 0)
            {
                height = height - 1;
            }
            if (width > 0 && (width % 2) != 0)
            {
                width = width + 1;
            }
        }

        if (isFilterSupported())
        {
            builder.append(CMD_OPT_SCALE);
            builder.append(CMD_OPT_PARAM_ASSIGNMENT);
            builder.append(width);
            builder.append(":");
            builder.append(height);
        }
        else
        {
            builder.append(CMD_OPT_SIZE);
            builder.append(CMD_OPT_ASSIGNMENT);
            builder.append(width);
            builder.append("x");
            builder.append(height);
        }

        return builder.toString();
    }

    protected String getFfmpegVideoCodec(String gengineVideoCodec)
    {
        if (versionDetailsString == null)
        {
            return null;
        }
        if (VideoTransformationOptions.VIDEO_CODEC_PASSTHROUGH.equals(gengineVideoCodec))
        {
            return "copy";
        }
        if (VideoTransformationOptions.VIDEO_CODEC_H264.equals(gengineVideoCodec))
        {
            return "libx264";
        }
        if (VideoTransformationOptions.VIDEO_CODEC_MPEG4.equals(gengineVideoCodec))
        {
            return "mpeg4";
        }
        if (VideoTransformationOptions.VIDEO_CODEC_THEORA.equals(gengineVideoCodec))
        {
            return "libtheora";
        }
        if (VideoTransformationOptions.VIDEO_CODEC_VP8.equals(gengineVideoCodec))
        {
            return "libvpx";
        }
        if (VideoTransformationOptions.VIDEO_CODEC_WMV.equals(gengineVideoCodec))
        {
            return null;
        }
        return null;
    }

    protected String getCmdOptVideoBitrate()
    {
        return (isVersion1orGreater() ? CMD_OPT_VIDEO_BITRATE_v1 : CMD_OPT_VIDEO_BITRATE_v0);
    }

    protected String getCmdOptVideoCodec()
    {
        return (isVersion1orGreater() ? CMD_OPT_VIDEO_CODEC_v1 : CMD_OPT_VIDEO_CODEC_v0);
    }

    protected String getCmdOptAudioBitrate()
    {
        return (isVersion1orGreater() ? CMD_OPT_AUDIO_BITRATE_v1 : CMD_OPT_AUDIO_BITRATE_v0);
    }

    protected String getCmdOptAudioCodec()
    {
        return (isVersion1orGreater() ? CMD_OPT_AUDIO_CODEC_v1 : CMD_OPT_AUDIO_CODEC_v0);
    }

    protected String getTargetVideoCommandOptions(String sourceMediaType, String targetMediaType, TransformationOptions options)
    {
        String commandOptions = "";
        if (options == null || !(options instanceof VideoTransformationOptions))
        {
            if (!isVersion1orGreater() && targetMediaType.equals(FileMediaType.VIDEO_M4V.getMediaType()))
            {
                commandOptions = commandOptions.trim() + CMD_OPT_DELIMITER + getVideoPresetOptions();
                return commandOptions.trim();
            }
            return null;
        }
        Float frameRate = ((VideoTransformationOptions) options).getTargetVideoFrameRate();
        Long videoBitrate = ((VideoTransformationOptions) options).getTargetVideoBitrate();
        String videoCodec = ((VideoTransformationOptions) options).getTargetVideoCodec();

        if (frameRate != null)
        {
            commandOptions = commandOptions +
                    CMD_OPT_FRAME_RATE + CMD_OPT_DELIMITER + frameRate;
        }
        if (videoBitrate != null)
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER +
                    getCmdOptVideoBitrate() + CMD_OPT_DELIMITER + (videoBitrate / 1000) + "k";
        }
        if (videoCodec != null)
        {
            commandOptions = commandOptions.trim() + CMD_OPT_DELIMITER +
                    getCmdOptVideoCodec() + CMD_OPT_DELIMITER + getFfmpegVideoCodec(videoCodec);
        }
        if (!isVersion1orGreater() &&
                ((videoCodec != null && videoCodec.equals(VideoTransformationOptions.VIDEO_CODEC_H264)) ||
                targetMediaType.equals(FileMediaType.VIDEO_M4V.getMediaType())))
        {
            commandOptions = commandOptions.trim() + CMD_OPT_DELIMITER + getVideoPresetOptions();
        }
        return commandOptions.trim();
    }

    protected String getVideoPresetOptions()
    {
        String preset = DEFAULT_VIDEO_PRESET;
        if (ffmpegPresetsDir != null)
        {
            preset = ffmpegPresetsDir + System.getProperty("file.separator") +
                    DEFAULT_VIDEO_PRESET_PREFIX + DEFAULT_VIDEO_PRESET + DEFAULT_VIDEO_PRESET_SUFFIX;
        }
        return CMD_OPT_VIDEO_PRESET + CMD_OPT_DELIMITER + preset;
    }

    protected String getFfmpegAudioCodec(String targetMediaType, String gengineAudioCodec)
    {
        if (versionFullDetailsString == null)
        {
            return null;
        }
        if (AudioTransformationOptions.AUDIO_CODEC_PASSTHROUGH.equals(gengineAudioCodec))
        {
            return "copy";
        }
        if (AudioTransformationOptions.AUDIO_CODEC_AAC.equals(gengineAudioCodec) ||
                targetMediaType.equals(FileMediaType.VIDEO_M4V.getMediaType()) ||
                (gengineAudioCodec == null && targetMediaType.equals(FileMediaType.VIDEO_MP4.getMediaType())))
        {
            if (versionFullDetailsString.contains("libfdk-aac"))
            {
                return "libfdk_aac";
            }
            if (versionFullDetailsString.contains("libfaac"))
            {
                return "libfaac";
            }
            if (versionFullDetailsString.contains("libvo-aacenc"))
            {
                return "libvo_aacenc";
            }
            return "aac" + CMD_OPT_DELIMITER + CMD_OPT_ENABLE_EXPERIMENTAL;
        }
        if (AudioTransformationOptions.AUDIO_CODEC_MP3.equals(gengineAudioCodec))
        {
            return "libmp3lame";
        }
        if (AudioTransformationOptions.AUDIO_CODEC_VORBIS.equals(gengineAudioCodec))
        {
            return "libvorbis";
        }
        if (AudioTransformationOptions.AUDIO_CODEC_WMA.equals(gengineAudioCodec))
        {
            return "wmav2";
        }
        return null;
    }

    protected String getTargetAudioCommandOptions(String targetMediaType, TransformationOptions options)
    {
        String commandOptions = "";
        if (options == null)
        {
            return null;
        }
        if (!(options instanceof AudioTransformationOptions))
        {
            return null;
        }
        Long audioBitrate = ((AudioTransformationOptions) options).getTargetAudioBitrate();
        Integer audioSamplingRate = ((AudioTransformationOptions) options).getTargetAudioSamplingRate();
        Integer audioChannels = ((AudioTransformationOptions) options).getTargetAudioChannels();
        String audioCodec = ((AudioTransformationOptions) options).getTargetAudioCodec();
        boolean fastStartEnabled = ((AudioTransformationOptions) options).getTargetFastStartEnabled();

        if (audioBitrate != null)
        {
            commandOptions = commandOptions +
                    getCmdOptAudioBitrate() + CMD_OPT_DELIMITER + (audioBitrate / 1000) + "k";
        }
        if (audioSamplingRate != null)
        {
            commandOptions = commandOptions + CMD_OPT_DELIMITER +
                    CMD_OPT_AUDIO_SAMPLING_RATE + CMD_OPT_DELIMITER + audioSamplingRate;
        }
        if (audioChannels != null)
        {
            commandOptions = commandOptions.trim() + CMD_OPT_DELIMITER +
                    CMD_OPT_AUDIO_CHANNELS + CMD_OPT_DELIMITER + audioChannels;
        }
        if (audioCodec != null ||
                targetMediaType.equals(FileMediaType.VIDEO_M4V.getMediaType()) ||
                targetMediaType.equals(FileMediaType.VIDEO_MP4.getMediaType()))
        {
            commandOptions = commandOptions.trim() + CMD_OPT_DELIMITER +
                    getCmdOptAudioCodec() + CMD_OPT_DELIMITER +
                    getFfmpegAudioCodec(targetMediaType, audioCodec);
        }
        if (fastStartEnabled)
        {
            if (versionFullDetailsString != null && versionFullDetailsString.contains("faststart"))
            {
                commandOptions = commandOptions.trim() + CMD_OPT_DELIMITER +
                        CMD_OPT_MOV_FLAGS + CMD_OPT_DELIMITER + CMD_OPT_MOV_FLAGS_FASTSTART;
            }
        }
        return commandOptions.trim();
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
        if (targetMimetype.startsWith(FileMediaType.PREFIX_IMAGE))
        {
            return CMD_OPT_FORMAT + CMD_OPT_DELIMITER + "image2";
        }
        if (targetMimetype.equals("audio/ogg"))
        {
            return CMD_OPT_FORMAT + CMD_OPT_DELIMITER + "ogg";
        }
        return null;
    }

    /**
     * Gets the ffmpeg command string for the time-based video conversion transform options
     * provided which apply to the source input
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

        if (temporalSourceOptions != null && temporalSourceOptions.getOffset() != null)
        {
            commandOptions = commandOptions +
                    CMD_OPT_OFFSET + CMD_OPT_DELIMITER + temporalSourceOptions.getOffset() +
                    CMD_OPT_DELIMITER;
        }
        return commandOptions.trim();
    }

    /**
     * Gets the ffmpeg command string for the time-based video conversion transform options
     * provided which apply to the target output
     *
     * @param options time-based options
     * @return String the ffmpeg command options
     */
    protected String getTargetTemporalCommandOptions(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        TemporalSourceOptions temporalSourceOptions = null;
        if (options != null)
        {
            temporalSourceOptions = options.getSourceOptions(TemporalSourceOptions.class);
        }
        String commandOptions = "";

        if (isSingleSourceFrameRangeRequired(sourceMimetype, targetMimetype, options))
        {
            commandOptions = commandOptions + CMD_OPT_PAIR_1_FRAME + CMD_OPT_DELIMITER;
        }
        else
        {
            if (temporalSourceOptions != null && temporalSourceOptions.getDuration() != null)
            {
                commandOptions = commandOptions +
                        CMD_OPT_DURATION + CMD_OPT_DELIMITER + temporalSourceOptions.getDuration() +
                        CMD_OPT_DELIMITER;
            }
        }
        if (temporalSourceOptions != null && temporalSourceOptions.getElementIntervalSeconds() != null)
        {
            commandOptions = commandOptions +
                    CMD_OPT_FRAME_RATE_FILTER + CMD_OPT_PARAM_ASSIGNMENT + temporalSourceOptions.getElementIntervalSeconds() +
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
    protected boolean isSingleSourceFrameRangeRequired(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (isStoryboardThumbnailRequest(sourceMimetype, targetMimetype, options))
        {
            return false;
        }
        // Need a single frame if we're transforming from video to an image
        return targetMimetype.startsWith(FileMediaType.PREFIX_IMAGE);
    }

    /**
     * Determines whether or not the transformation request is for storyboard
     * thumbnails.
     *
     * @param sourceMimetype
     * @param targetMimetype
     * @param options
     * @return true if requesting storyboard thumbnails
     */
    protected boolean isStoryboardThumbnailRequest(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!targetMimetype.startsWith(FileMediaType.PREFIX_IMAGE) || options == null ||
                !(options instanceof VideoTransformationOptions))
        {
            return false;
        }
        TemporalSourceOptions temporalSourceOptions = options.getSourceOptions(TemporalSourceOptions.class);

        return (temporalSourceOptions != null && temporalSourceOptions.getElementIntervalSeconds() != null);
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
        return (targetMimetype.startsWith(FileMediaType.PREFIX_AUDIO));
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
        return (targetMimetype.startsWith(FileMediaType.PREFIX_IMAGE));
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
        return (targetMimetype.startsWith(FileMediaType.PREFIX_AUDIO));
    }

    protected ContentReference createMultiTargetContentReference(String mediaType)
    {
        String filename = this.getClass().getSimpleName() + "-target-" +
                UUID.randomUUID().toString() + CMD_OPT_MULTI_TARGET_INDEX_FORMATTER +
                "." + FileMediaType.SERVICE.getExtension(mediaType);
        ContentReference multiTarget =
                targetContentReferenceHandler.createContentReference(filename, mediaType);
        // FFmpeg will create multiple targets, we don't need a real file here
        targetContentReferenceHandler.delete(multiTarget);
        return multiTarget;
    }

}
