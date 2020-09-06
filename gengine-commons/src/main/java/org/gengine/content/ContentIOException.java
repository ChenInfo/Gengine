package org.gengine.content;

import org.gengine.api.StableApi;
import org.gengine.error.GengineRuntimeException;


/**
 * Wraps a general <code>Exceptions</code> that occurred while reading or writing
 * content.
 *
 * @see Throwable#getCause()
 *
 */
@StableApi
public class ContentIOException extends GengineRuntimeException
{
    private static final long serialVersionUID = 3258130249983276087L;

    public ContentIOException(String msg)
    {
        super(msg);
    }

    public ContentIOException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
