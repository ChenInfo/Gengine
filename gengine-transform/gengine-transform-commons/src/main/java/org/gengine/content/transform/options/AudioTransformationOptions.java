package org.gengine.content.transform.options;

/**
 * Options relating to audio transformations
 *
 */
public class AudioTransformationOptions extends TransformationOptionsImpl
{
    private static final long serialVersionUID = -8948846326897849904L;

    public static final String AUDIO_CODEC_AAC = "aac";
    public static final String AUDIO_CODEC_MP3 = "mp3";
    public static final String AUDIO_CODEC_VORBIS = "vorbis";
    public static final String AUDIO_CODEC_WMA = "wma";
    public static final String AUDIO_CODEC_PASSTHROUGH = "PASSTHROUGH";

    private String targetAudioCodec;
    private Long targetAudioBitrate;
    private Integer targetAudioSamplingRate;
    private Integer targetAudioChannels;
    private boolean targetFastStartEnabled = true;

    /**
     * Gets the audio codec to use for the target of the transformation
     *
     * @return the target audio codec
     */
    @ToStringProperty
    public String getTargetAudioCodec()
    {
        return targetAudioCodec;
    }

    /**
     * Sets the audio codec to use for the target of the transformation
     *
     * @param targetAudioCodec
     */
    public void setTargetAudioCodec(String targetAudioCodec)
    {
        this.targetAudioCodec = targetAudioCodec;
    }

    /**
     * Gets the audio bitrate to use for the target of the transformation
     *
     * @return the target audio bitrate
     */
    @ToStringProperty
    public Long getTargetAudioBitrate()
    {
        return targetAudioBitrate;
    }

    /**
     * Sets the audio bitrate to use for the target of the transformation
     *
     * @param targetAudioBitrate
     */
    public void setTargetAudioBitrate(Long targetAudioBitrate)
    {
        this.targetAudioBitrate = targetAudioBitrate;
    }

    /**
     * Gets the audio sampling rate to use for the target of the transformation
     *
     * @return the target audio sampling rate
     */
    @ToStringProperty
    public Integer getTargetAudioSamplingRate()
    {
        return targetAudioSamplingRate;
    }

    /**
     * Sets the audio sampling rate to use for the target of the transformation
     *
     * @param targetAudioSamplingRate
     */
    public void setTargetAudioSamplingRate(Integer targetAudioSamplingRate)
    {
        this.targetAudioSamplingRate = targetAudioSamplingRate;
    }

    /**
     * Gets the number of audio channels to use for the target of the transformation
     *
     * @return the number of target audio channels
     */
    @ToStringProperty
    public Integer getTargetAudioChannels()
    {
        return targetAudioChannels;
    }

    /**
     * Sets the number of audio channels to use for the target of the transformation
     *
     * @param targetAudioChannels
     */
    public void setTargetAudioChannels(Integer targetAudioChannels)
    {
        this.targetAudioChannels = targetAudioChannels;
    }

    /**
     * Gets whether or not the moov atom should be moved to the start of the file if supported.
     *
     * @return true if moving the moov atom should be attempted
     */
    @ToStringProperty
    public boolean getTargetFastStartEnabled()
    {
        return targetFastStartEnabled;
    }

    /**
     * Sets whether or not the moov atom should be moved to the start of the file if supported.
     *
     * @param targetFastStartEnabled
     */
    public void setTargetFastStartEnabled(boolean targetFastStartEnabled)
    {
        this.targetFastStartEnabled = targetFastStartEnabled;
    }

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder();
        output.append(TO_STR_OBJ_START);
        output.append(toString(this));
        output.append(TO_STR_DEL);
        output.append(toStringSourceOptions());
        output.append(TO_STR_OBJ_END);
        return output.toString();
    }

}
