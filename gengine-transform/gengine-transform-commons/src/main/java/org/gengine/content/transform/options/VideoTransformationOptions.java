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

    private String targetVideoCodec;
    private Integer targetVideoBitrate;

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
     * Gets the video bitrate to use for the target of the transformation
     *
     * @return the target video bitrate
     */
    public Integer getTargetVideoBitrate()
    {
        return targetVideoBitrate;
    }

    /**
     * Sets the video bitrate to use for the target of the transformation
     *
     * @param targetVideoBitrate
     */
    public void setTargetVideoBitrate(Integer targetVideoBitrate)
    {
        this.targetVideoBitrate = targetVideoBitrate;
    }

}
