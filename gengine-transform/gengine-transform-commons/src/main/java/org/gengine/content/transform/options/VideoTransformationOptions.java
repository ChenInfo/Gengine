package org.gengine.content.transform.options;

/**
 * Options relating to video transformations
 *
 */
public class VideoTransformationOptions extends AudioTransformationOptions
{

    public static final String VIDEO_CODEC_H264 = "h264";
    public static final String VIDEO_CODEC_MPEG4 = "mpeg4";
    public static final String VIDEO_CODEC_THEORA = "theora";
    public static final String VIDEO_CODEC_VP6 = "vp6";
    public static final String VIDEO_CODEC_VP7 = "vp7";
    public static final String VIDEO_CODEC_VP8 = "vp8";
    public static final String VIDEO_CODEC_WMV = "wmv";

    /** Image resize options */
    private ImageResizeOptions resizeOptions;

    private String targetVideoCodec;
    private String targetVideoCodecProfile;
    private Long targetVideoBitrate;
    private Float targetVideoFrameRate;

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
     * Gets the video codec to use for the target of the transformation
     *
     * @return the target video codec
     */
    public String getTargetVideoCodec()
    {
        return targetVideoCodec;
    }

    /**
     * Sets the video codec to use for the target of the transformation
     *
     * @param targetVideoCodec
     */
    public void setTargetVideoCodec(String targetVideoCodec)
    {
        this.targetVideoCodec = targetVideoCodec;
    }


    /**
     * Gets the video codec profile to use for the target of the transformation
     *
     * @return the target video codec profile
     */
    public String getTargetVideoCodecProfile()
    {
        return targetVideoCodecProfile;
    }

    /**
     * Sets the video codec profile to use for the target of the transformation
     *
     * @param targetVideoCodecProfile
     */
    public void setTargetVideoCodecProfile(String targetVideoCodecProfile)
    {
        this.targetVideoCodecProfile = targetVideoCodecProfile;
    }

    /**
     * Gets the video bitrate to use for the target of the transformation
     *
     * @return the target video bitrate
     */
    public Long getTargetVideoBitrate()
    {
        return targetVideoBitrate;
    }

    /**
     * Sets the video bitrate to use for the target of the transformation
     *
     * @param targetVideoBitrate
     */
    public void setTargetVideoBitrate(Long targetVideoBitrate)
    {
        this.targetVideoBitrate = targetVideoBitrate;
    }

    /**
     * Gets the video frame rate to use for the target of the transformation
     *
     * @return the target video frame rate
     */
    public Float getTargetVideoFrameRate()
    {
        return targetVideoFrameRate;
    }

    /**
     * Sets the video frame rate to use for the target of the transformation
     *
     * @param targetVideoFrameRate
     */
    public void setTargetVideoFrameRate(Float targetVideoFrameRate)
    {
        this.targetVideoFrameRate = targetVideoFrameRate;
    }

}
