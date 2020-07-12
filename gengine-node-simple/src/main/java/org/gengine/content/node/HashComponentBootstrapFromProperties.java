package org.gengine.content.node;

import java.util.Properties;

import org.gengine.content.AbstractComponent;
import org.gengine.content.hash.AbstractContentHashWorker;
import org.gengine.content.hash.BaseContentHashComponent;

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
        worker.setSourceContentReferenceHandler(
                createFileContentReferenceHandler(PROP_WORKER_DIR_SOURCE));
        worker.initialize();
    }

}
