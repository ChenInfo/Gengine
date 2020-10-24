package org.gengine.content.transform.options;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base implementation of TransformationSourceOptions which holds applicable mimetypes
 * and handles merge of options.
 *
 */
public abstract class AbstractTransformationSourceOptions implements TransformationSourceOptions, Cloneable
{

    /** The list of applicable media types */
    private List<String> applicableMediaTypes;

    /**
     * Gets the list of applicable media types
     *
     * @return the applicable media types
     */
    @JsonIgnore
    public List<String> getApplicableMediaTypes()
    {
        return applicableMediaTypes;
    }

    /**
     * Sets the list of applicable media types
     *
     * @param applicableMimetypes the applicable media types
     */
    public void setApplicableMediaTypes(List<String> applicableMediaTypes)
    {
        this.applicableMediaTypes = applicableMediaTypes;
    }

    /**
     * Gets whether or not these transformation source options apply for the
     * given media type
     *
     * @param mediaType the media type of the source
     * @return if these transformation source options apply
     */
    public boolean isApplicableForMediaType(String mediaType)
    {
        if (mediaType != null && applicableMediaTypes != null) { return applicableMediaTypes.contains(mediaType); }
        return false;
    }

    @Override
    protected AbstractTransformationSourceOptions clone() throws CloneNotSupportedException
    {
        return (AbstractTransformationSourceOptions) super.clone();
    }

    /**
     * Creates a new <code>TransformationSourceOptions</code> object from this
     * one, merging any non-null overriding fields in the given
     * <code>overridingOptions</code>
     *
     * @param overridingOptions
     * @return a merged <code>TransformationSourceOptions</code> object
     */
    public TransformationSourceOptions mergedOptions(TransformationSourceOptions overridingOptions)
    {
        try
        {
            AbstractTransformationSourceOptions mergedOptions = this.clone();
            mergedOptions.setApplicableMediaTypes(this.getApplicableMediaTypes());

            return mergedOptions;
        }
        catch (CloneNotSupportedException e)
        {
            // Not thrown
        }
        return null;
    }

    /**
     * Adds the given paramValue to the given params if it's not null.
     *
     * @param paramName
     * @param paramValue
     * @param params
     */
    protected void putParameterIfNotNull(String paramName, Serializable paramValue, Map<String, Serializable> params)
    {
        if (paramValue != null)
        {
            params.put(paramName, paramValue);
        }
    }

}
