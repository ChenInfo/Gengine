package org.gengine.content.dropwizard.health;

import org.gengine.content.transform.BaseContentTransformerComponent;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;

import com.codahale.metrics.health.HealthCheck;

/**
 * Transformer health check
 */
public class TransformerHealthCheck extends HealthCheck
{
    private BaseContentTransformerComponent component;
    private AmqpDirectEndpoint endpoint;

    public TransformerHealthCheck(BaseContentTransformerComponent component, AmqpDirectEndpoint endpoint)
    {
        this.component = component;
        this.endpoint = endpoint;
    }

    @Override
    protected Result check() throws Exception
    {
        if (endpoint == null || !endpoint.isInitialized())
        {
            return Result.unhealthy("AMQP endpoint could not be initialized, "
                    + "please check the logs for additional information");
        }
        return Result.healthy();
    }

}
