package org.cheninfo.service.cmr.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of TransformationSourceOptions which holds applicable mimetypes
 * and handles merge of options.
 *
 */
public abstract class AbstractTransformationSourceOptions implements TransformationSourceOptions, Cloneable
{

    /** The list of applicable mimetypes */
    private List<String> applicableMimetypes;

    /**
     * Gets the list of applicable mimetypes
     *
     * @return the applicable mimetypes
     */
    public List<String> getApplicableMimetypes()
    {
        return applicableMimetypes;
    }

    /**
     * Sets the list of applicable mimetypes
     *
     * @param applicableMimetypes the applicable mimetypes
     */
    public void setApplicableMimetypes(List<String> applicableMimetypes)
    {
        this.applicableMimetypes = applicableMimetypes;
    }

    /**
     * Gets whether or not these transformation source options apply for the
     * given mimetype
     *
     * @param mimetype the mimetype of the source
     * @return if these transformation source options apply
     */
    public boolean isApplicableForMimetype(String mimetype)
    {
        if (mimetype != null && applicableMimetypes != null) { return applicableMimetypes.contains(mimetype); }
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
            mergedOptions.setApplicableMimetypes(this.getApplicableMimetypes());

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
