package org.gengine.content.dropwizard.bootstrap;

import io.dropwizard.setup.Environment;

import org.gengine.content.dropwizard.configuration.NodeConfiguration;
import org.gengine.content.dropwizard.health.TransformerHealthCheck;
import org.gengine.content.transform.AbstractContentTransformerWorker;
import org.gengine.content.transform.BaseContentTransformerComponent;
import org.gengine.content.transform.ContentTransformerWorker;
import org.gengine.error.GengineRuntimeException;
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

    @Override
    protected BaseContentTransformerComponent createComponent()
    {
        return new BaseContentTransformerComponent();
    }

    protected void initWorker()
    {
        if (!(worker instanceof AbstractContentTransformerWorker))
        {
            throw new GengineRuntimeException(
                    "Only " + AbstractContentTransformerWorker.class.getSimpleName() + " supported");
        }
        ((AbstractContentTransformerWorker) worker).setSourceContentReferenceHandler(
                createContentReferenceHandler(nodeConfig.getContentReferenceHandlersConfig().getSource()));
        ((AbstractContentTransformerWorker) worker).setTargetContentReferenceHandler(
                createContentReferenceHandler(nodeConfig.getContentReferenceHandlersConfig().getTarget()));
        ((AbstractContentTransformerWorker) worker).initialize();
    }

    @Override
    public HealthCheck createHealthCheck(BaseContentTransformerComponent component, AmqpDirectEndpoint endpoint)
    {
        return new TransformerHealthCheck(component, endpoint);
    }

}
