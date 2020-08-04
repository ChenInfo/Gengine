package org.gengine.content.hash;

import java.util.List;

import org.gengine.content.AbstractContentRequest;
import org.gengine.content.ContentReference;
import org.gengine.messaging.Request;

/**
 * Represents a request for content hash
 *
 */
public class HashRequest extends AbstractContentRequest implements Request<HashReply>
{
    private String hashAlgorithm;

    public HashRequest()
    {
        super();
    }

    public HashRequest(List<ContentReference> sourceContentReferences, String hashAlgorithm)
    {
        super();
        setSourceContentReferences(sourceContentReferences);
        this.hashAlgorithm = hashAlgorithm;
    }

    /**
     * Gets the hash algorithm to be used
     *
     * @return the hash algorithm
     */
    public String getHashAlgorithm()
    {
        return hashAlgorithm;
    }

    /**
     * Sets hash algorithm to be used
     *
     * @param hashAlgorithm
     */
    public void setHashAlgorithm(String hashAlgorithm)
    {
        this.hashAlgorithm = hashAlgorithm;
    }

    @Override
    public Class<HashReply> getReplyClass()
    {
        return HashReply.class;
    }

}
