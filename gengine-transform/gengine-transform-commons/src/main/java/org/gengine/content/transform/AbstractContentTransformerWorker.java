package org.gengine.content.transform;

import org.gengine.content.AbstractContentWorker;
import org.gengine.content.ContentReference;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.mediatype.FileMediaType;

/**
 * Abstract transform node worker which uses a content reference handler to convert the
 * content reference into a usable File object for the actual implementation.
 *
 */
public abstract class AbstractContentTransformerWorker
        extends AbstractContentWorker implements ContentTransformerWorker
{
    protected ContentReferenceHandler targetContentReferenceHandler;

    public void setTargetContentReferenceHandler(ContentReferenceHandler targetContentReferenceHandler)
    {
        this.targetContentReferenceHandler = targetContentReferenceHandler;
    }

    public void initialize()
    {
    }

    protected String getExtension(ContentReference contentReference)
    {
        return FileMediaType.SERVICE.getExtension(contentReference.getMediaType());
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + "[");
        builder.append("sourceContentReferenceHandler: " + sourceContentReferenceHandler.toString());
        builder.append(", ");
        builder.append("targetContentReferenceHandler: " + targetContentReferenceHandler.toString());
        builder.append("]");
        return builder.toString();
    }

}
