package org.gengine.content.node;

import java.util.Properties;

import org.gengine.content.AbstractComponent;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.hash.AbstractContentHashWorker;
import org.gengine.content.hash.BaseContentHashComponent;

/**
 * Bootraps a hash component
 *
 * @param <W>
 */
public class HashComponentBootstrapFromProperties<W extends AbstractContentHashWorker> extends
        AbstractComponentBootstrapFromProperties<W>
{
    public HashComponentBootstrapFromProperties(Properties properties, W worker)
    {
        super(properties, worker);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected AbstractComponent createComponent()
    {
        return new BaseContentHashComponent();
    }

    protected void initWorker()
    {
        ContentReferenceHandler sourceHandler = createContentReferenceHandler(
                PROP_WORKER_CONTENT_REF_HANDLER_SOURCE_PREFIX);
        worker.setSourceContentReferenceHandler(sourceHandler);
        worker.initialize();
    }

}
