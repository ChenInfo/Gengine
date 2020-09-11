package org.gengine.content;

import java.util.List;

import org.gengine.messaging.Reply;

public interface ContentReply extends Reply
{

    /**
     * Gets the results of the content operation.
     *
     * @return the results
     */
    public List<ContentWorkResult> getResults();
}
