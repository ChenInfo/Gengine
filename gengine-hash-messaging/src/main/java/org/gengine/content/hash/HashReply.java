package org.gengine.content.hash;

import java.util.Map;

import org.gengine.content.ContentReference;
import org.gengine.messaging.Reply;

/**
 * Represents a reply from a content hasher on the status of a hash request.
 *
 */
public class HashReply implements Reply
{

    private String requestId;
    private Map<ContentReference, String> hexValues;

    public HashReply() {
        super();
    }

    public HashReply(HashRequest request)
    {
        super();
        this.requestId = request.getRequestId();
    }

    /**
     * Gets the UUID for the original hash request
     *
     * @return the hash request ID
     */
    public String getRequestId()
    {
        return requestId;
    }

    /**
     * Sets the UUID for the original hash request
     *
     * @param requestId
     */
    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    /**
     * Gets the map of content hash values
     *
     * @return the map of hash values
     */
    public Map<ContentReference, String> getHexEncodedValues()
    {
        return hexValues;
    }

    /**
     * Sets the map of content hash values
     *
     * @param the map of hashValues
     */
    public void setHexEncodedValues(Map<ContentReference, String> hexValues)
    {
        this.hexValues = hexValues;
    }

}
