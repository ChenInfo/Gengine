package org.gengine.content;

import org.gengine.content.handler.ContentReferenceHandler;

/**
 * Base implementation of a content worker with a <code>sourceContentReferenceHandler</code>
 * field.
 *
 */
public abstract class AbstractContentWorker implements ContentWorker
{
    protected ContentReferenceHandler sourceContentReferenceHandler;

    /**
     * Sets the content reference handler to be used for retrieving
     * the source content to be worked on.
     *
     * @param sourceContentReferenceHandler
     */
    public void setSourceContentReferenceHandler(ContentReferenceHandler sourceContentReferenceHandler)
    {
        this.sourceContentReferenceHandler = sourceContentReferenceHandler;
    }

    /**
     * Performs any initialization needed after content reference handlers are set
     */
    public abstract void initialize();
}
