package org.gengine.content.transform.options;

import java.io.Serializable;
import java.util.Map;

import org.gengine.content.mediatype.FileMediaType;
import org.gengine.content.transform.options.AbstractTransformationSourceOptions;
import org.gengine.error.GengineRuntimeException;
import org.gengine.util.CloneField;
import org.gengine.util.ToStringProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Time-based content conversion options to specify an offset and duration.
 * Useful for audio and video.
 * <p>
 * If only the offset is specified transformers should attempt
 * a transform from that offset to the end if possible.
 * <p>
 * If only a duration is specified transformers should attempt
 * a transform from the start until that duration is reached if possible.
 *
 */
public class TemporalSourceOptions extends AbstractTransformationSourceOptions
{
    private static final long serialVersionUID = 7255206141096239901L;

    /** Validation regex for hh:mm:ss[.xxx], ignoring leap seconds and allowing up to 99 hours */
    private static final String VALID_TIME_STRING_REGEX = "\\d{2}:[0-5][0-9]:[0-5][0-9](\\.\\d{1,3})?";

    /** The offset time code from which to start the transformation */
    private String offset;

    /** The duration of the target video after the transformation */
    private String duration;

    /** The interval at which elements from the source should be pulled */
    private Integer elementIntervalSeconds;

    /** The maximum number of elements which should be returned */
    private Integer maxElements;

    public TemporalSourceOptions()
    {
        super();
    }

    public TemporalSourceOptions(TemporalSourceOptions options)
    {
        super(options);
    }

    @Override
    public boolean isApplicableForMediaType(String sourceMimetype)
    {
        return ((sourceMimetype != null &&
                sourceMimetype.startsWith(FileMediaType.PREFIX_VIDEO) ||
                sourceMimetype.startsWith(FileMediaType.PREFIX_AUDIO)) ||
                super.isApplicableForMediaType(sourceMimetype));
    }

    /**
     * Gets the offset time code from which to start the transformation
     * with a format of hh:mm:ss[.xxx]
     *
     * @return the offset
     */
    @ToStringProperty
    @CloneField
    public String getOffset()
    {
        return offset;
    }

    /**
     * Sets the offset time code from which to start the transformation
     * with a format of hh:mm:ss[.xxx]
     *
     * @param offset
     */
    public void setOffset(String offset)
    {
        TemporalSourceOptions.validateTimeString(offset);
        this.offset = offset;
    }

    /**
     * Gets the duration of the source to read
     * with a format of hh:mm:ss[.xxx]
     *
     * @return
     */
    @ToStringProperty
    @CloneField
    public String getDuration()
    {
        return duration;
    }

    /**
     * Sets the duration of the source to read
     * with a format of hh:mm:ss[.xxx]
     *
     * @param duration
     */
    public void setDuration(String duration)
    {
        TemporalSourceOptions.validateTimeString(duration);
        this.duration = duration;
    }

    /**
     * Gets the interval in seconds to pull elements from the source,
     * i.e. 3 indicates one element (frame) every 3 seconds.
     *
     * @return the element interval
     */
    @ToStringProperty
    @CloneField
    public Integer getElementIntervalSeconds()
    {
        return elementIntervalSeconds;
    }

    /**
     * Sets the interval in seconds to pull elements from the source,
     * i.e. 3 indicates one element (frame) every 3 seconds.
     *
     * @param elementIntervalSeconds
     */
    public void setElementIntervalSeconds(Integer elementIntervalSeconds)
    {
        this.elementIntervalSeconds = elementIntervalSeconds;
    }

    /**
     * Gets the maximum number of elements that should be
     * returned from the transformation.
     *
     * @return the maximum number of elements
     */
    @ToStringProperty
    @CloneField
    public Integer getMaxElements()
    {
        return maxElements;
    }

    /**
     * Sets the maximum number of elements that should be
     * returned from the transformation.
     *
     * @param maxElements
     */
    public void setMaxElements(Integer maxElements)
    {
        this.maxElements = maxElements;
    }

    /**
     * Validates that the given value is of the form hh:mm:ss[.xxx]
     *
     * @param value
     */
    public static void validateTimeString(String value)
    {
        if (value != null && !value.matches(VALID_TIME_STRING_REGEX))
        {
            throw new GengineRuntimeException("'" + value + "' is not a valid time specification of the form hh:mm:ss[.xxx]");
        }
    }

    @Override
    @JsonIgnore
    public TransformationSourceOptionsSerializer getSerializer()
    {
        return TemporalSourceOptions.createSerializerInstance();
    }

    /**
     * Creates an instance of the options serializer
     *
     * @return the options serializer
     */
    public static TransformationSourceOptionsSerializer createSerializerInstance()
    {
        return (new TemporalSourceOptions()).new TemporalSourceOptionsSerializer();
    }

    /**
     * Serializer for temporal source options
     */
    public class TemporalSourceOptionsSerializer implements TransformationSourceOptionsSerializer
    {
        public static final String PARAM_SOURCE_TIME_OFFSET = "source_time_offset";
        public static final String PARAM_SOURCE_TIME_DURATION = "source_time_duration";
        public static final String PARAM_SOURCE_TIME_ELEMENT_INTERVAL = "source_time_element_interval";
        public static final String PARAM_SOURCE_TIME_MAX_ELEMENTS = "source_time_max_elements";

        @Override
        public TransformationSourceOptions deserialize(SerializedTransformationOptionsAccessor serializedOptions)
        {
            String offset = serializedOptions.getCheckedParam(PARAM_SOURCE_TIME_OFFSET, String.class);
            String duration = serializedOptions.getCheckedParam(PARAM_SOURCE_TIME_DURATION, String.class);
            Integer elementInterval = serializedOptions.getCheckedParam(PARAM_SOURCE_TIME_ELEMENT_INTERVAL, Integer.class);
            Integer maxElements = serializedOptions.getCheckedParam(PARAM_SOURCE_TIME_MAX_ELEMENTS, Integer.class);

            if (offset == null && duration == null && elementInterval == null && maxElements == null)
            {
                return null;
            }

            TemporalSourceOptions sourceOptions = new TemporalSourceOptions();
            sourceOptions.setOffset(offset);
            sourceOptions.setDuration(duration);
            sourceOptions.setElementIntervalSeconds(elementInterval);
            sourceOptions.setMaxElements(maxElements);
            return sourceOptions;
        }

        @Override
        public void serialize(TransformationSourceOptions sourceOptions,
                Map<String, Serializable> parameters)
        {
            if (parameters == null || sourceOptions == null)
                return;
            TemporalSourceOptions temporalSourceOptions = (TemporalSourceOptions) sourceOptions;
            parameters.put(PARAM_SOURCE_TIME_OFFSET, temporalSourceOptions.getOffset());
            parameters.put(PARAM_SOURCE_TIME_DURATION, temporalSourceOptions.getDuration());
            parameters.put(PARAM_SOURCE_TIME_ELEMENT_INTERVAL, temporalSourceOptions.getElementIntervalSeconds());
            parameters.put(PARAM_SOURCE_TIME_MAX_ELEMENTS, temporalSourceOptions.getMaxElements());
        }
    }

}
