package org.gengine.content.dropwizard.bootstrap;

import io.dropwizard.setup.Environment;

import org.gengine.content.dropwizard.configuration.NodeConfiguration;
import org.gengine.content.dropwizard.health.TransformerHealthCheck;
import org.gengine.content.transform.AbstractContentTransformerWorker;
import org.gengine.content.transform.BaseContentTransformerComponent;
import org.gengine.content.transform.ContentTransformerWorker;
import org.gengine.error.ChenInfoRuntimeException;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;

import com.codahale.metrics.health.HealthCheck;

/**
 * Bootstraps a transformer component
 *
 */
public class TransformerComponentBootstrapFromConfirguration
        extends AbstractComponentBootstrapFromConfiguration<BaseContentTransformerComponent, ContentTransformerWorker>
{
    public TransformerComponentBootstrapFromConfirguration(
            NodeConfiguration nodeConfig, Environment environment, ContentTransformerWorker worker)
    {
        super(nodeConfig, environment, worker);
    }

    protected static final String PROP_WORKER_DIR_TARGET = "gengine.worker.dir.target";

    @Override
    protected BaseContentTransformerComponent createComponent()
    {
        return new BaseContentTransformerComponent();
    }

    protected void initWorker()
    {
        if (!(worker instanceof AbstractContentTransformerWorker))
        {
            throw new ChenInfoRuntimeException(
                    "Only " + AbstractContentTransformerWorker.class.getSimpleName() + " supported");
        }
        ((AbstractContentTransformerWorker) worker).setSourceContentReferenceHandler(
                createFileContentReferenceHandler(nodeConfig.getSourceDirectory()));
        ((AbstractContentTransformerWorker) worker).setTargetContentReferenceHandler(
                createFileContentReferenceHandler(nodeConfig.getTargetDirectory()));
        ((AbstractContentTransformerWorker) worker).initialize();
    }

    @Override
    public HealthCheck createHealthCheck(BaseContentTransformerComponent component, AmqpDirectEndpoint endpoint)
    {
        return new TransformerHealthCheck(component, endpoint);
    }

}
