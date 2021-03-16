package org.gengine.content.transform.options;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.gengine.util.Mergable;

/**
 * Interface defining values of options that are passed to content transformers.  These options
 * are used during the transformation process to provide context or parameter values.
 *
 */
public interface TransformationOptions extends Serializable, Mergable<TransformationOptions>
{

    /**
     * Gets the map of {@link TransformationSourceOptions} class to an object of that class holding
     * the source options values.
     *
     * @return the source options map
     */
    public Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> getSourceOptionsMap();

    /**
     * Sets the map of {@link TransformationSourceOptions} class to an object of that class holding
     * the source options values.
     *
     * @param sourceOptionsMap
     */
    public void setSourceOptionsMap(
            Map<Class<? extends TransformationSourceOptions>, TransformationSourceOptions> sourceOptionsMap);

    /**
     * Sets the list of source options further describing how the source should
     * be transformed based on its mimetype.
     *
     * @param sourceOptionsList the source options list
     */
    public void setSourceOptionsList(Collection<TransformationSourceOptions> sourceOptionsList);

    /**
     * Gets the appropriate source options for the given mimetype if available.
     *
     * @param sourceMimetype
     * @return the source options for the mimetype
     */
    public <T extends TransformationSourceOptions> T getSourceOptions(Class<T> clazz);

    /**
     * Adds the given sourceOptions to the sourceOptionsMap.
     * <p>
     * Note that if source options of the same class already exists a new
     * merged source options object is added.
     *
     * @param sourceOptions
     */
    public void addSourceOptions(TransformationSourceOptions sourceOptions);

    /**
     * Gets the timeout (ms) on the InputStream after which an IOExecption is thrown
     * to terminate very slow transformations or a subprocess is terminated (killed).
     * @return timeoutMs in milliseconds. If less than or equal to zero (the default)
     *         there is no timeout.
     */
    public long getTimeoutMs();

    /**
     * Sets a timeout (ms) on the InputStream after which an IOExecption is thrown
     * to terminate very slow transformations or to terminate (kill) a subprocess.
     * @param timeoutMs in milliseconds. If less than or equal to zero (the default)
     *                  there is no timeout.
     *                  If greater than zero the {@code readLimitTimeMs} must not be set.
     */
    public void setTimeoutMs(long timeoutMs);

    /**
     * Get the page limit before returning EOF.
     * @return If less than or equal to zero (the default) no limit should be applied.
     */
    public int getPageLimit();

    /**
     * Set the number of pages read from the source before returning EOF.
     *
     * @param pageLimit the number of pages to be read from the source. If less
     *        than or equal to zero (the default) no limit is applied.
     */
    public void setPageLimit(int pageLimit);

    /**
     * If the source content includes embedded resources,
     *  should the transformer attempt to transform these
     *  as well?
     * Not many transformers do support embedded resources,
     *  so this option will only affect those that can.
     *
     * @param includeEmbedded the include embedded flag.
     */
    public void setIncludeEmbedded(Boolean includeEmbedded);

    /**
     * If the source content includes embedded resources,
     *  should the transformer attempt to transform these
     *  as well?
     * Not many transformers do support embedded resources,
     *  so this option will only affect those that can.
     *
     * @return true, false, or null for the default for the transformer
     */
    public Boolean getIncludeEmbedded();

    /**
     * Gets the additional transformation options not explicitly modeled in an
     * implementation.
     *
     * @return the additional options map
     */
    public Map<String, Serializable> getAdditionalOptions();

    /**
     * Sets the additional transformation options not explicitly modeled in an
     * implementation.
     *
     * @param additionalOptions the additional options map
     */
    public void setAdditionalOptions(Map<String, Serializable> additionalOptions);

}
