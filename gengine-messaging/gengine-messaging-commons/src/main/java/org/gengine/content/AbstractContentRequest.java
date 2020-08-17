package org.gengine.content;

import java.util.List;

import org.gengine.content.ContentReference;
import org.gengine.messaging.AbstractRequest;

/**
 * Represents a request for some operation on content sources.
 *
 */
public abstract class AbstractContentRequest extends AbstractRequest
{
    private List<ContentReference> sourceContentReferences;

    public AbstractContentRequest()
    {
        super();
    }

    /**
     * Gets the source content reference objects
     *
     * @return source content references
     */
    public List<ContentReference> getSourceContentReferences()
    {
        return sourceContentReferences;
    }

    /**
     * Sets the source content reference objects
     *
     * @param sourceContentReference
     */
    public void setSourceContentReferences(List<ContentReference> sourceContentReferences)
    {
        this.sourceContentReferences = sourceContentReferences;
    }

}
