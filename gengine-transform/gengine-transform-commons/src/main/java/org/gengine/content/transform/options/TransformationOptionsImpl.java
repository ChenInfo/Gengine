package org.gengine.content.transform.options;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gengine.error.GengineRuntimeException;
import org.gengine.util.BeanUtils;
import org.gengine.util.Mergable;
import org.gengine.util.CloneField;

/**
 * Concrete implementation of transformation options
 *
 */
public class TransformationOptionsImpl implements TransformationOptions, Mergable<TransformationOptions>
{
    private static final long serialVersionUID = -4466824587314591239L;

    /** Source options based on its mimetype */
    private Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> sourceOptionsMap;

    /** The include embedded resources yes/no */
    private Boolean includeEmbedded;

    private Map<String, Serializable> additionalOptions;

    /** Time, KBytes and page limits */
    private TransformationOptionLimits limits = new TransformationOptionLimits();

    public TransformationOptionsImpl()
    {
        super();
    }

    public TransformationOptionsImpl(TransformationOptionsImpl origOptions)
    {
        this();
        BeanUtils.copyFields(origOptions, this, false);
        setSourceOptionsMap(deepCopySourceOptionsMap(origOptions.getSourceOptionsMap()));
        if (origOptions.getAdditionalOptions() != null)
        {
            setAdditionalOptions(new HashMap<String, Serializable>(origOptions.getAdditionalOptions()));
        }
    }

    @Override
    public void merge(TransformationOptions override)
    {
        BeanUtils.copyFields(override, this, true);
        if (override.getSourceOptionsMap() != null)
        {
            for (Class<? extends TransformationSourceOptions> overrideSourceOptionsClass : override.getSourceOptionsMap().keySet())
            {
                addSourceOptions(override.getSourceOptions(overrideSourceOptionsClass));
            }
        }
        if (override.getAdditionalOptions() != null)
        {
            if (this.additionalOptions == null)
            {
                this.additionalOptions =
                        new HashMap<String, Serializable>(override.getAdditionalOptions().size());
            }
            for (String additionalOptionKey : override.getAdditionalOptions().keySet())
            {
                this.additionalOptions.put(
                        additionalOptionKey,
                        override.getAdditionalOptions().get(additionalOptionKey));
            }
        }
    }

    protected Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> deepCopySourceOptionsMap(
            Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> origMap)
    {
        if (origMap == null)
        {
            return null;
        }
        try
        {
            HashMap<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> copyMap =
                    new HashMap<Class<? extends TransformationSourceOptions>, TransformationSourceOptions>(origMap.size());
            for (Class<? extends TransformationSourceOptions> origSourceOptionsClass : origMap.keySet())
            {
                TransformationSourceOptions origSourceOptions = origMap.get(origSourceOptionsClass);
                TransformationSourceOptions copySourceOptions;
                copySourceOptions = origSourceOptionsClass.getConstructor(origSourceOptionsClass).newInstance(origSourceOptions);
                copyMap.put(origSourceOptionsClass, copySourceOptions);
            }
            return copyMap;
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e)
        {
            throw new GengineRuntimeException("Could not copy " + this.getClass().getCanonicalName(), e);
        }
    }

    @Override
    public Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> getSourceOptionsMap()
    {
        return sourceOptionsMap;
    }

    @Override
    public void setSourceOptionsMap(
            Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> sourceOptionsMap)
    {
        this.sourceOptionsMap = sourceOptionsMap;
    }

    @Override
    public void setSourceOptionsList(Collection<TransformationSourceOptions> sourceOptionsList)
    {
        if (sourceOptionsList != null)
        {
            for (TransformationSourceOptions sourceOptions : sourceOptionsList)
            {
                addSourceOptions(sourceOptions);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends TransformationSourceOptions> T getSourceOptions(Class<T> clazz)
    {
        if (sourceOptionsMap == null)
            return null;
        return (T) sourceOptionsMap.get(clazz);
    }

    @Override
    public void addSourceOptions(TransformationSourceOptions sourceOptions)
    {
        if (sourceOptionsMap == null)
        {
            sourceOptionsMap = new HashMap<Class<? extends TransformationSourceOptions>, TransformationSourceOptions>(1);
        }
        TransformationSourceOptions existingOptions = sourceOptionsMap.get(sourceOptions.getClass());
        if (existingOptions != null)
        {
            existingOptions.merge(sourceOptions);
        }
        else
        {
            sourceOptionsMap.put(sourceOptions.getClass(), sourceOptions);
        }
    }

    @Override
    @CloneField
    public long getTimeoutMs()
    {
        return limits.getTimeoutMs();
    }

    @Override
    public void setTimeoutMs(long timeoutMs)
    {
        limits.setTimeoutMs(timeoutMs);
    }

    @Override
    @CloneField
    public int getPageLimit()
    {
        return limits.getPageLimit();
    }

    @Override
    public void setPageLimit(int pageLimit)
    {
        limits.setPageLimit(pageLimit);
    }

    @Override
    public void setIncludeEmbedded(Boolean includeEmbedded)
    {
       this.includeEmbedded = includeEmbedded;
    }

    @Override
    @CloneField
    public Boolean getIncludeEmbedded()
    {
        return includeEmbedded;
    }

    @Override
    public Map<String, Serializable> getAdditionalOptions()
    {
        return additionalOptions;
    }

    @Override
    public void setAdditionalOptions(Map<String, Serializable> additionalOptions)
    {
        this.additionalOptions = additionalOptions;
    }

    /**
     * Builds the source options to string.
     * <p>
     * We can't rely on a framework here because we need full serialization
     * for messaging.
     *
     * @return the source options toString
     */
    protected String toStringSourceOptions()
    {
        StringBuilder output = new StringBuilder();
        output.append("\"sourceOptions\"").append(BeanUtils.TO_STR_KEY_VAL).append(BeanUtils.TO_STR_OBJ_START);
        if (sourceOptionsMap != null)
        {
            for (Iterator<TransformationSourceOptions> iterator = sourceOptionsMap.values().iterator(); iterator.hasNext();)
            {
                TransformationSourceOptions sourceOptions = (TransformationSourceOptions) iterator.next();
                output.append("\"").
                    append(Introspector.decapitalize(sourceOptions.getClass().getSimpleName())).
                    append("\"").append(BeanUtils.TO_STR_KEY_VAL);
                output.append(BeanUtils.TO_STR_OBJ_START).append(sourceOptions.toString()).append(BeanUtils.TO_STR_OBJ_END);
                if (iterator.hasNext())
                {
                    output.append(BeanUtils.TO_STR_DEL);
                }
            }
        }
        output.append(BeanUtils.TO_STR_OBJ_END);
        return output.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder output = new StringBuilder();
        output.append(BeanUtils.TO_STR_OBJ_START);
        output.append(toStringSourceOptions());
        output.append(BeanUtils.TO_STR_OBJ_END);
        return output.toString();
    }


}
