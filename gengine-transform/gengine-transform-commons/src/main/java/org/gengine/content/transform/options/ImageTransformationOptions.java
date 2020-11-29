package org.gengine.content.transform.options;


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
    public ImageResizeOptions getResizeOptions()
    {
        return resizeOptions;
    }

    /**
     * @return Will the image be automatically oriented(rotated) based on the EXIF "Orientation" data.
     * Defaults to TRUE
     */
    @ToStringProperty
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
        output.append(TO_STR_OBJ_START);
        output.append("\"").append("resizeOptions").append("\"").append(TO_STR_KEY_VAL).
            append(TO_STR_OBJ_START).append(toString(getResizeOptions())).append(TO_STR_OBJ_END);
        output.append(TO_STR_DEL);
        output.append(toString(this));
        output.append(TO_STR_DEL);
        output.append(toStringSourceOptions());
        output.append(TO_STR_OBJ_END);
        return output.toString();
    }
}
