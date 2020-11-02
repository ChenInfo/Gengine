package org.gengine.content.dropwizard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gengine.content.AbstractComponent;
import org.gengine.content.ContentWorker;
import org.gengine.content.dropwizard.bootstrap.AbstractComponentBootstrapFromConfiguration;
import org.gengine.content.dropwizard.bootstrap.HashComponentBootstrapFromConfirguration;
import org.gengine.content.dropwizard.bootstrap.TransformerComponentBootstrapFromConfirguration;
import org.gengine.content.dropwizard.configuration.ComponentConfiguration;
import org.gengine.content.dropwizard.configuration.NodeConfiguration;
import org.gengine.content.dropwizard.resources.NodeResource;
import org.gengine.content.hash.AbstractContentHashWorker;
import org.gengine.content.transform.AbstractContentTransformerWorker;
import org.gengine.error.GengineRuntimeException;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

/**
 * Dropwizard application implementation for a node
 *
 */
public class NodeApplication extends Application<NodeConfiguration>
{
    private static final Log logger = LogFactory.getLog(NodeApplication.class);

    protected ExecutorService executorService;
    protected List<AmqpDirectEndpoint> endpoints;

    public static void main(String[] args) throws Exception {
        NodeApplication nodeApplication = new NodeApplication();
        nodeApplication.run(args);
        // Let Dropwizard come up
        Thread.sleep(2000);
        nodeApplication.startEndpoints();
    }

    @Override
    public void initialize(Bootstrap<NodeConfiguration> bootstrap)
    {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle());

        executorService = Executors.newCachedThreadPool();
    }

    /**
     * Initializes all configured endpoints
     */
    protected void startEndpoints()
    {
        if (endpoints == null)
        {
            return;
        }
        logger.debug("Starting endpoints");
        for (AmqpDirectEndpoint endpoint : endpoints)
        {
            startEndpoint(endpoint);
        }
    }

    /**
     * Initializes the given endpoint, running its listener using the executor service.
     *
     * @param endpoint
     */
    protected void startEndpoint(AmqpDirectEndpoint endpoint)
    {
        if (endpoint.getListener() != null)
        {
            logger.trace("Initializing new AMQP listener");
            executorService.execute(endpoint.getListener());
        }

        try
        {
            // Let the listener start up
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            executorService.shutdown();
            Thread.interrupted();
        }
    }

    /**
     * Attempts to load the worker class specified in the properties file and
     * create a new instance of it.
     *
     * @return the newly created worker object
     */
    protected ContentWorker createWorkerFromConfigClassname(ComponentConfiguration componentConfiguration)
    {
        try
        {
            String workerClassName = componentConfiguration.getWorkerClass();
            Class<?> workerClass = this.getClass().getClassLoader().loadClass(workerClassName);
            return (ContentWorker) workerClass.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            throw new GengineRuntimeException("Could not load worker class: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({ "unchecked" })
    protected static <B extends AbstractComponentBootstrapFromConfiguration<C, W>, C extends AbstractComponent<W>, W extends ContentWorker> B getComponentBootrap(
            W worker, NodeConfiguration nodeConfig, Environment environment)
    {
        if (worker instanceof AbstractContentTransformerWorker)
        {
            return (B) new TransformerComponentBootstrapFromConfirguration(
                    nodeConfig, environment, (AbstractContentTransformerWorker) worker);
        }
        if (worker instanceof AbstractContentHashWorker)
        {
            return (B) new HashComponentBootstrapFromConfirguration(
                    nodeConfig, environment, (AbstractContentHashWorker) worker);
        }
        return null;
    }

    @Override
    public void run(NodeConfiguration nodeConfig, Environment environment) throws Exception
    {
        final NodeResource resource = new NodeResource();
        environment.jersey().register(resource);

        endpoints = new ArrayList<AmqpDirectEndpoint>();
        List<ComponentConfiguration> components = nodeConfig.getComponents();
        for (ComponentConfiguration componentConfig : components)
        {
            ContentWorker worker = createWorkerFromConfigClassname(componentConfig);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            AbstractComponentBootstrapFromConfiguration componentBootrap =
                getComponentBootrap(worker, nodeConfig, environment);

            componentBootrap.init(nodeConfig, environment, componentConfig);

            endpoints.add(componentBootrap.getEndpoint());

            environment.healthChecks().register(
                    componentConfig.getName(), componentBootrap.getHealthCheck());
        }
    }

}
