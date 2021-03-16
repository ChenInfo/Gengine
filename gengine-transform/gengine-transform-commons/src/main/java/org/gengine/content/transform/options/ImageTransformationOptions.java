package org.gengine.content.transform.options;

import org.gengine.util.BeanUtils;
import org.gengine.util.CloneField;
import org.gengine.util.ToStringProperty;


/**
 * Image transformation options
 *
 */
public class ImageTransformationOptions extends TransformationOptionsImpl
{
    private static final long serialVersionUID = -609731059750625205L;

    public static final String OPT_COMMAND_OPTIONS = "commandOptions";
    public static final String OPT_IMAGE_RESIZE_OPTIONS = "imageResizeOptions";
    public static final String OPT_IMAGE_AUTO_ORIENTATION = "imageAutoOrient";

    /** Command string options, provided for backward compatibility */
    private String commandOptions = "";

    /** Image resize options */
    private ImageResizeOptions resizeOptions;

    private boolean autoOrient = true;

    public ImageTransformationOptions()
    {
        super();
    }

    public ImageTransformationOptions(ImageTransformationOptions origOptions)
    {
        super(origOptions);
        setCommandOptions(origOptions.getCommandOptions());
        setResizeOptions(origOptions.getResizeOptions());
        setAutoOrient(origOptions.isAutoOrient());
    }

    @Override
    public void merge(TransformationOptions override)
    {
        super.merge(override);
        ImageTransformationOptions options = (ImageTransformationOptions) override;
        if (options.getCommandOptions() != null)
        {
            setCommandOptions(options.getCommandOptions());
        }
        if (options.getResizeOptions() != null)
        {
            setResizeOptions(options.getResizeOptions());
        }
        setAutoOrient(options.isAutoOrient());
    }

    /**
     * Set the command string options
     *
     * @param commandOptions    the command string options
     */
    public void setCommandOptions(String commandOptions)
    {
        this.commandOptions = commandOptions;
    }

    /**
     * Get the command string options
     *
     * @return  String  the command string options
     */
    @CloneField
    public String getCommandOptions()
    {
        return commandOptions;
    }

    /**
     * Set the image resize options
     *
     * @param resizeOptions image resize options
     */
    public void setResizeOptions(ImageResizeOptions resizeOptions)
    {
        this.resizeOptions = resizeOptions;
    }

    /**
     * Get the image resize options
     *
     * @return  ImageResizeOptions  image resize options
     */
    @CloneField
    public ImageResizeOptions getResizeOptions()
    {
        return resizeOptions;
    }

    /**
     * @return Will the image be automatically oriented(rotated) based on the EXIF "Orientation" data.
     * Defaults to TRUE
     */
    @ToStringProperty
    @CloneField
    public boolean isAutoOrient()
    {
        return this.autoOrient;
    }

    /**
     * @param autoOrient automatically orient (rotate) based on the EXIF "Orientation" data
     */
    public void setAutoOrient(boolean autoOrient)
    {
        this.autoOrient = autoOrient;
    }

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder();
        output.append(BeanUtils.TO_STR_OBJ_START);
        output.append("\"").append("resizeOptions").append("\"").append(BeanUtils.TO_STR_KEY_VAL).
            append(BeanUtils.TO_STR_OBJ_START).append(BeanUtils.toString(getResizeOptions())).append(BeanUtils.TO_STR_OBJ_END);
        output.append(BeanUtils.TO_STR_DEL);
        output.append(BeanUtils.toString(this));
        output.append(BeanUtils.TO_STR_DEL);
        output.append(toStringSourceOptions());
        output.append(BeanUtils.TO_STR_OBJ_END);
        return output.toString();
    }
}
