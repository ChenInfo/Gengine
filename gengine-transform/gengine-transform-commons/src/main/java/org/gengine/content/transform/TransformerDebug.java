package org.gengine.content.transform;

import org.gengine.content.transform.options.TransformationOptionPair;

/**
 * Generates logging for transformers.
 * <p>
 * Currently contains the minimum contract required by {@link TransformationOptionPair}.
 *
 */
public interface TransformerDebug
{
    public boolean isEnabled();

    public <T extends Throwable> T setCause(T t);

    public void debug(String message);

    public void debug(String message, Throwable t);
}
