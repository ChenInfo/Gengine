package org.gengine.content;

import java.util.Map;

/**
 * The result of some operation on content which includes a content reference
 * and details about the operation.
 * <p>
 * The content reference could indicate the source content reference on which the
 * work was requested or could be a new target content reference.
 *
 */
public class ContentWorkResult
{
    private ContentReference contentReference;
    private Map<String, Object> details;

    public ContentWorkResult()
    {
    }

    public ContentWorkResult(ContentReference contentReference, Map<String, Object> details)
    {
        this.contentReference = contentReference;
        this.details = details;
    }

    /**
     * Gets the content reference associated with the result.
     *
     * @return the content reference
     */
    public ContentReference getContentReference()
    {
        return contentReference;
    }

    /**
     * Sets the content reference associated with the result.
     *
     * @param contentReference
     */
    public void setContentReference(ContentReference contentReference)
    {
        this.contentReference = contentReference;
    }

    /**
     * Gets the additional details of the result of the content work.
     *
     * @return additional details
     */
    public Map<String, Object> getDetails()
    {
        return details;
    }

    /**
     * Sets the additional details of the result of the content work.
     *
     * @param details
     */
    public void setDetails(Map<String, Object> details)
    {
        this.details = details;
    }

    /**
     * Convenience method for getting a specific result detail.
     *
     * @param detailKey
     * @return the result detail
     */
    public Object getDetail(String detailKey)
    {
        if (details == null)
        {
            return null;
        }
        return details.get(detailKey);
    }

}
