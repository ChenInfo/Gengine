package org.gengine.content;

import org.gengine.content.handler.ContentReferenceHandler;

/**
 * A reference to content by its URI and media type (mimetype).
 *
 * @see {@link ContentReferenceHandler}
 *
 */
public class ContentReference
{

    private String uri;
    private String mediaType;
    private Long size;

    public ContentReference()
    {
    }

    public ContentReference(String uri, String mediaType)
    {
        super();
        this.uri = uri;
        this.mediaType = mediaType;
    }

    public ContentReference(String uri, String mediaType, Long size)
    {
        super();
        this.uri = uri;
        this.mediaType = mediaType;
        this.size = size;
    }

    /**
     * Gets the URI for the content reference
     *
     * @return the content URI
     */
    public String getUri()
    {
        return uri;
    }

    /**
     * Sets the URI for the content reference
     *
     * @param uri
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }

    /**
     * Gets the media type (mimetype) of the content reference
     *
     * @return media type
     */
    public String getMediaType()
    {
        return mediaType;
    }

    /**
     * Sets the media type (mimetype) of the content reference
     *
     * @param mediaType
     */
    public void setMediaType(String mediaType)
    {
        this.mediaType = mediaType;
    }

    /**
     * Gets the size of the content binary if available
     *
     * @return
     */
    public Long getSize()
    {
        return size;
    }

    /**
     * Sets the size of the content binary
     *
     * @param size
     */
    public void setSize(Long size)
    {
        this.size = size;
    }

}
