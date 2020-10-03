package org.gengine.content.transform.imagemagick;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.ContentIOException;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.mediatype.FileMediaType;
import org.gengine.content.transform.AbstractRuntimeExecContentTransformerWorker;
import org.gengine.content.transform.ContentTransformerWorkerProgressReporter;
import org.gengine.content.transform.options.CropSourceOptions;
import org.gengine.content.transform.options.ImageResizeOptions;
import org.gengine.content.transform.options.ImageTransformationOptions;
import org.gengine.content.transform.options.PagedSourceOptions;
import org.gengine.content.transform.options.TransformationOptions;
import org.gengine.error.GengineRuntimeException;
import org.gengine.util.exec.RuntimeExec;

/**
 * Executes a statement to implement
 *
 */
public class ImageMagickContentTransformerWorker extends AbstractRuntimeExecContentTransformerWorker
{
    /** options variable name */
    private static final String KEY_OPTIONS = "options";
    /** source variable name */
    private static final String VAR_SOURCE = "source";
    /** target variable name */
    private static final String VAR_TARGET = "target";

    private static final Log logger = LogFactory.getLog(ImageMagickContentTransformerWorker.class);

    private String imgExe = "convert";

    /**
     * Default constructor
     */
    public ImageMagickContentTransformerWorker()
    {
        // Intentionally empty
    }

    /**
     * Set the runtime command executer that must be executed in order to run
     * <b>ImageMagick</b>.  Whether or not this is the full path to the convertCommand
     * or just the convertCommand itself depends the environment setup.
     * <p>
     * The command must contain the variables <code>${source}</code> and
     * <code>${target}</code>, which will be replaced by the names of the file to
     * be transformed and the name of the output file respectively.
     * <pre>
     *    convert ${source} ${target}
     * </pre>
     *
     * @param executer the system command executer
     */
    public void setExecuter(RuntimeExec executer)
    {
        executer.setProcessProperty(
                "MAGICK_TMPDIR", TempFileProvider.getTempDir().getAbsolutePath());
        this.executer = executer;
    }

    @Override
    protected void initializeExecuter()
    {
        if (executer == null)
        {
            if (System.getProperty("img.exe") != null)
            {
                imgExe = System.getProperty("img.exe");
            }
            executer = new RuntimeExec();
            Map<String, String[]> commandsAndArguments = new HashMap<>();
            commandsAndArguments.put(".*", new String[] {
                imgExe,
                "${source}",
                "SPLIT:${options}",
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
                imgExe,
                "-version",
            });
            versionDetailsExecuter.setCommandsAndArguments(checkCommandsAndArguments);
        }
    }

    @Override
    protected void initializationTest()
    {
        try
        {
            initializationTest(
                    "org/gengine/content/transform/imagemagick/cheninfo.gif",
                    FileMediaType.IMAGE_PNG.getMediaType(),
                    new ImageTransformationOptions());
        }
        catch (Exception e)
        {
            throw new GengineRuntimeException("Could not initialize worker: " + e.getMessage(), e);
        }
    }

    /**
     * Some image formats are not supported by ImageMagick, or at least appear not to work.
     *
     * @param mediaType the mimetype to check
     * @return Returns true if ImageMagic can handle the given image format
     */
    protected boolean isSupported(String mediaType)
    {
        // There are a few mimetypes in the system that do not start with "image/" but which
        // nevertheless are supported by this transformer.
        if (mediaType.equals(FileMediaType.APPLICATION_EPS.getMediaType()))
        {
            return true;
        }
        else if (mediaType.equals(FileMediaType.APPLICATION_PHOTOSHOP.getMediaType()))
        {
            return true;
        }

        else if (!mediaType.startsWith(FileMediaType.PREFIX_IMAGE))
        {
            return false;   // not an image
        }
        else if (mediaType.equals(FileMediaType.IMAGE_RGB.getMediaType()))
        {
            return false;   // rgb extension doesn't work
        }
        else if (mediaType.equals(FileMediaType.IMAGE_SVG.getMediaType()))
        {
            return false;   // svg extension doesn't work
        }
        else if (mediaType.equals(FileMediaType.IMG_DWG.getMediaType()))
        {
            return false;   // dwg extension doesn't work
        }
        else
        {
            return true;
        }
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

        // Add limited support (so lots of other transforms are not supported) for PDF to PNG. An ALF-14303 workaround.
        // Will only be used as part of failover transformer.PdfToImage. Note .ai is the same format as .pdf
        if ( (FileMediaType.PDF.getMediaType().equals(sourceMediaType) ||
              FileMediaType.APPLICATION_ILLUSTRATOR.equals(sourceMediaType)) &&
              FileMediaType.IMAGE_PNG.equals(targetMediaType))
        {
            return true;
        }

        // Add extra support for tiff to pdf to allow multiple page preview (ALF-7278)
        if (FileMediaType.IMAGE_TIFF.getMediaType().equals(sourceMediaType) &&
                FileMediaType.PDF.getMediaType().equals(targetMediaType))
        {
            return true;
        }

        if (!isSupported(sourceMediaType) ||
                !isSupported(targetMediaType))
        {
            // only support IMAGE -> IMAGE (excl. RGB)
            return false;
        }
        else
        {
            return true;
        }
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
        FileContentReferencePair targetPair = targetPairs.iterator().next();
        File targetFile = targetPair.getFile();
        singleTransformInternal(
                sourcePairs.iterator().next(),
                targetPair,
                options, progressReporter);

        if (targetFile.exists() && targetFile.length() > 0)
        {
            return Arrays.asList(targetFile);
        }
        else
        {
            return getMultipleTargetSiblings(targetFile);
        }

        // TODO: Other transform types, i.e.:
        //   - Layer multiple sources into one target
    }

    /**
     * Transform the image content from the source file to the target file.
     * <p>
     * Note that ImageMagick may create multiple outputs for paged file types
     * if no page limit or paged source options are present in the transformation
     * options to constrain that behavior.
     *
     * @param sourcePair
     * @param targetPair
     * @param options
     * @param progressReporter
     * @throws Exception
     */
    protected void singleTransformInternal(
            FileContentReferencePair sourcePair,
            FileContentReferencePair targetPair,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception
    {
        File sourceFile = sourcePair.getFile();
        File targetFile = targetPair.getFile();

        String sourceMimetype = sourcePair.getContentReference().getMediaType();
        String targetMimetype = targetPair.getContentReference().getMediaType();

        Map<String, String> properties = new HashMap<String, String>(5);
        // set properties
        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions)options;
            CropSourceOptions cropOptions = imageOptions.getSourceOptions(CropSourceOptions.class);
            ImageResizeOptions resizeOptions = imageOptions.getResizeOptions();
            String commandOptions = imageOptions.getCommandOptions();
            if (commandOptions == null)
            {
                commandOptions = "";
            }
            if (imageOptions.isAutoOrient())
            {
                commandOptions = commandOptions + " -auto-orient";
            }
            if (cropOptions != null)
            {
                commandOptions = commandOptions + " " + getImageCropCommandOptions(cropOptions);
            }
            if (resizeOptions != null)
            {
                commandOptions = commandOptions + " " + getImageResizeCommandOptions(resizeOptions);
            }
            properties.put(KEY_OPTIONS, commandOptions);
        }
        properties.put(VAR_SOURCE, sourceFile.getAbsolutePath() +
                getSourcePageRange(options, sourceMimetype, targetMimetype));
        properties.put(VAR_TARGET, targetFile.getAbsolutePath());

        // execute the statement
        long timeoutMs = options.getTimeoutMs();
        RuntimeExec.ExecutionResult result = executer.execute(properties, timeoutMs);
        if (result.getExitValue() != 0 && result.getStdErr() != null && result.getStdErr().length() > 0)
        {
            throw new ContentIOException("Failed to perform ImageMagick transformation: \n" + result);
        }
        // success
        if (logger.isDebugEnabled())
        {
            logger.debug("ImageMagic executed successfully: \n" + executer);
        }
    }

    /**
     * Gets the any siblings in the same directory as the targetFile with the same name
     * prefix used by ImageMagick for multiple outputs.
     * <p>
     * For example, when requesting a conversion of a 2 page PDF to JPEG:
     *
     *     convert source.pdf result.jpg
     *
     * ImageMagick will create:
     *
     *     result-0.jpg
     *     result-1.jpg
     *
     * @param targetFile
     * @return the list of siblings with the same name prefix as the target file
     */
    protected List<File> getMultipleTargetSiblings(File targetFile)
    {
        // Check for multiple target files result
        List<File> targetFiles = new ArrayList<File>();
        String targetFileNameWithoutExtensions = targetFile.getName().split("\\.")[0];
        File[] siblings = targetFile.getParentFile().listFiles();
        for (int i = 0; i < siblings.length; i++)
        {
            File targetSibling = siblings[i];
            String nameWithoutExtension = targetSibling.getName().split("\\.")[0];
            if (nameWithoutExtension.startsWith(targetFileNameWithoutExtensions) &&
                    !nameWithoutExtension.equals(targetFileNameWithoutExtensions))
            {
                targetFiles.add(targetSibling);
            }
        }
        if (targetFiles.size() > 0)
        {
            return targetFiles;
        }
        return null;
    }

    /**
     * Gets the imagemagick command string for the image crop options provided
     *
     * @param imageResizeOptions    image resize options
     * @return String               the imagemagick command options
     */
    private String getImageCropCommandOptions(CropSourceOptions cropOptions)
    {
        StringBuilder builder = new StringBuilder(32);
        String gravity = cropOptions.getGravity();
        if(gravity!=null)
        {
            builder.append("-gravity ");
            builder.append(gravity);
            builder.append(" ");
        }
        builder.append("-crop ");
        int width = cropOptions.getWidth();
        if (width > -1)
        {
            builder.append(width);
        }

        int height = cropOptions.getHeight();
        if (height > -1)
        {
            builder.append("x");
            builder.append(height);
        }

        if (cropOptions.isPercentageCrop())
        {
            builder.append("%");
        }
        appendOffset(builder, cropOptions.getXOffset());
        appendOffset(builder, cropOptions.getYOffset());
        builder.append(" +repage");
        return builder.toString();
    }

    /**
     * @param builder
     * @param xOffset
     */
    private void appendOffset(StringBuilder builder, int xOffset)
    {
        if(xOffset>=0)
        {
            builder.append("+");
        }
        builder.append(xOffset);
    }

    /**
     * Gets the imagemagick command string for the image resize options provided
     *
     * @param imageResizeOptions    image resize options
     * @return String               the imagemagick command options
     */
    private String getImageResizeCommandOptions(ImageResizeOptions imageResizeOptions)
    {
        StringBuilder builder = new StringBuilder(32);

        // These are ImageMagick options. See http://www.imagemagick.org/script/command-line-processing.php#geometry for details.
        if (imageResizeOptions.isResizeToThumbnail() == true)
        {
            builder.append("-thumbnail ");
        }
        else
        {
            builder.append("-resize ");
        }

        if (imageResizeOptions.getWidth() > -1)
        {
            builder.append(imageResizeOptions.getWidth());
        }

        if (imageResizeOptions.getHeight() > -1)
        {
            builder.append("x");
            builder.append(imageResizeOptions.getHeight());
        }

        if (imageResizeOptions.isPercentResize() == true)
        {
            builder.append("%");
        }
        // ALF-7308. Disallow the enlargement of small images e.g. within imgpreview thumbnail.
        if (!imageResizeOptions.getAllowEnlargement())
        {
            builder.append(">");
        }

        if (imageResizeOptions.isMaintainAspectRatio() == false)
        {
            builder.append("!");
        }

        return builder.toString();
    }

    /**
     * Determines whether or not a single page range is required for the given source and target mimetypes.
     *
     * @param sourceMimetype
     * @param targetMimetype
     * @return whether or not a page range must be specified for the transformer to read the target files
     */
    private boolean isSingleSourcePageRangeRequired(String sourceMimetype, String targetMimetype)
    {
        // Need a page source if we're transforming from or to PSD or to EPS
        return (
                targetMimetype.equals(FileMediaType.APPLICATION_PHOTOSHOP.getMediaType()) ||
                targetMimetype.equals(FileMediaType.APPLICATION_EPS.getMediaType())) ||
                sourceMimetype.equals(FileMediaType.APPLICATION_PHOTOSHOP.getMediaType());
    }

    /**
     * Gets the page range from the source to use in the command line.
     *
     * @param options the transformation options
     * @param sourceMimetype the source mimetype
     * @param targetMimetype the target mimetype
     * @return the source page range for the command line
     */
    private String getSourcePageRange(TransformationOptions options, String sourceMimetype, String targetMimetype)
    {
        // Check for PagedContentSourceOptions in the options
        if (options instanceof ImageTransformationOptions)
        {
            ImageTransformationOptions imageOptions = (ImageTransformationOptions) options;
            PagedSourceOptions pagedSourceOptions = imageOptions.getSourceOptions(PagedSourceOptions.class);
            if (pagedSourceOptions != null)
            {
                if (pagedSourceOptions.getStartPageNumber() != null &&
                        pagedSourceOptions.getEndPageNumber() != null)
                {
                    if (pagedSourceOptions.getStartPageNumber().equals(pagedSourceOptions.getEndPageNumber()))
                    {
                        return "[" + (pagedSourceOptions.getStartPageNumber() - 1) + "]";
                    }
                    else
                    {
                        if (isSingleSourcePageRangeRequired(sourceMimetype, targetMimetype))
                        {
                            throw new GengineRuntimeException(
                                    "A single page is required for targets of type " + targetMimetype);
                        }
                        return "[" + (pagedSourceOptions.getStartPageNumber() - 1) +
                                "-" + (pagedSourceOptions.getEndPageNumber() - 1) + "]";
                    }
                }
                else
                {
                    // TODO specified start to end of doc and start of doc to specified end not yet supported
                    // Just grab a single page specified by either start or end
                    if (pagedSourceOptions.getStartPageNumber() != null)
                        return "[" + (pagedSourceOptions.getStartPageNumber() - 1) + "]";
                    if (pagedSourceOptions.getEndPageNumber() != null)
                        return "[" + (pagedSourceOptions.getEndPageNumber() - 1) + "]";
                }
            }
        }
        if (options.getPageLimit() == 1 || isSingleSourcePageRangeRequired(sourceMimetype, targetMimetype))
        {
            return "[0]";
        }
        else
        {
            return "";
        }
    }
}
