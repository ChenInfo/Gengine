package org.gengine.content.dropwizard.bootstrap;

import io.dropwizard.setup.Environment;

import org.gengine.content.dropwizard.configuration.NodeConfiguration;
import org.gengine.content.dropwizard.health.HashHealthCheck;
import org.gengine.content.hash.AbstractContentHashWorker;
import org.gengine.content.hash.BaseContentHashComponent;
import org.gengine.content.hash.ContentHashWorker;
import org.gengine.error.GengineRuntimeException;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;

import com.codahale.metrics.health.HealthCheck;

/**
 * Bootraps a hash component
 *
 */
public class HashComponentBootstrapFromConfirguration
        extends AbstractComponentBootstrapFromConfiguration<BaseContentHashComponent, ContentHashWorker>
{
    public HashComponentBootstrapFromConfirguration(
            NodeConfiguration nodeConfig, Environment environment, ContentHashWorker worker)
    {
        super(nodeConfig, environment, worker);
    }

    @Override
    protected BaseContentHashComponent createComponent()
    {
        return new BaseContentHashComponent();
    }

    protected void initWorker()
    {
        if (!(worker instanceof AbstractContentHashWorker))
        {
            throw new GengineRuntimeException(
                    "Only " + AbstractContentHashWorker.class.getSimpleName() + " supported");
        }
        ((AbstractContentHashWorker) worker).setSourceContentReferenceHandler(
                createContentReferenceHandler(nodeConfig.getContentReferenceHandlersConfig().getSource()));
        ((AbstractContentHashWorker) worker).initialize();
    }

    @Override
    public HealthCheck createHealthCheck(BaseContentHashComponent component, AmqpDirectEndpoint endpoint)
    {
        return new HashHealthCheck(component, endpoint);
    }

}
