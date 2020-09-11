package org.gengine.content;

import java.util.List;

import org.gengine.messaging.AbstractReply;
import org.gengine.messaging.Request;

/**
 * Base implementation of a content reply
 *
 */
public abstract class AbstractContentReply extends AbstractReply implements ContentReply
{
    private List<ContentWorkResult> results;

    public AbstractContentReply()
    {
        super();
    }

    public AbstractContentReply(Request<?> request)
    {
        super(request);
    }

    public List<ContentWorkResult> getResults()
    {
        return results;
    }

    public void setResults(List<ContentWorkResult> results)
    {
        this.results = results;
    }

}
