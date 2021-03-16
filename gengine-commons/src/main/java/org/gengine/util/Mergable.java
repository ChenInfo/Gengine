package org.gengine.util;

/**
 * Defines that a class can be merged with the overriding values from another
 * object of the same type.
 *
 * @param <T>
 */
public interface Mergable<T>
{

    /**
     * Merge the non-null field values from the given override object
     *
     * @param override
     */
    public void merge(T override);

}
