package org.gengine.content.hash;

import org.gengine.content.AbstractContentReply;
import org.gengine.messaging.Request;

/**
 * Represents a reply from a content hasher on the status of a hash request.
 *
 */
public class HashReply extends AbstractContentReply
{

    public HashReply()
    {
        super();
    }

    public HashReply(Request<?> request)
    {
        super(request);
    }

}
