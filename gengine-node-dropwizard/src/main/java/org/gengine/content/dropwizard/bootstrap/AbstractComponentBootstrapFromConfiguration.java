package org.gengine.content.dropwizard.bootstrap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.dropwizard.setup.Environment;

import org.gengine.content.AbstractComponent;
import org.gengine.content.ContentWorker;
import org.gengine.content.dropwizard.configuration.BrokerConfiguration;
import org.gengine.content.dropwizard.configuration.ComponentConfiguration;
import org.gengine.content.dropwizard.configuration.NodeConfiguration;
import org.gengine.content.file.FileProviderImpl;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.handler.FileContentReferenceHandlerImpl;
import org.gengine.error.ChenInfoRuntimeException;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;
import org.gengine.messaging.amqp.AmqpNodeBootstrapUtils;

import com.codahale.metrics.health.HealthCheck;

/**
 * Base bootstrap which creates a component, configures it with a worker, and creates
 * an endpoint for sending and receiving messages
 *
 * @param <C> the component type
 * @param <W> the worker type
 */
public abstract class
        AbstractComponentBootstrapFromConfiguration<C extends AbstractComponent<W>, W extends ContentWorker>
{
    private static final Log logger = LogFactory.getLog(AbstractComponentBootstrapFromConfiguration.class);

    public static final String PROP_WORKER_DIR_SOURCE = "gengine.worker.dir.source";

    protected NodeConfiguration nodeConfig;
    protected Environment environment;
    protected W worker;

    public AbstractComponentBootstrapFromConfiguration(
            NodeConfiguration nodeConfig, Environment environment, W worker)
    {
        this.nodeConfig = nodeConfig;
        this.environment = environment;
        this.worker = worker;
    }

    /**
     * Creates a new file content reference handler from the given directory path.
     *
     * @param directoryPath
     * @return the new file content reference handler
     */
    protected ContentReferenceHandler createFileContentReferenceHandler(
            String directoryPath)
    {
        FileProviderImpl fileProvider = new FileProviderImpl();
        fileProvider.setDirectoryPath(directoryPath);
        FileContentReferenceHandlerImpl fileContentReferenceHandler =
                new FileContentReferenceHandlerImpl();
        fileContentReferenceHandler.setFileProvider(fileProvider);
        return fileContentReferenceHandler;
    }

    /**
     * Constructs a {@link FileContentReferenceHandlerImpl} using a {@link TempFileProvider}.
     *
     * @return the ContentReferenceHandler
     */
    protected ContentReferenceHandler createTempFileContentReferenceHandler()
    {
        TempFileProvider fileProvider = new TempFileProvider();
        FileContentReferenceHandlerImpl fileContentReferenceHandler =
                new FileContentReferenceHandlerImpl();
        fileContentReferenceHandler.setFileProvider(fileProvider);
        return fileContentReferenceHandler;
    }

    protected abstract C createComponent();

    protected abstract void initWorker();


    /**
     * Initializes a new component with the given transformer worker with elements from the configuration,
     * creates an {@link AmqpDirectEndpoint}, and initializes it.
     *
     * @param configuration
     * @param environment
     * @param worker
     * @param componentConfig
     * @return the initialized AMQP endpoint
     */
    public AmqpDirectEndpoint initComponentAndEndpoint(
            NodeConfiguration nodeConfig, Environment environment,
            ComponentConfiguration componentConfig)
    {
        initWorker();

        C component = createComponent();
        component.setWorker(worker);

        BrokerConfiguration brokerConfig = nodeConfig.getMessagingConfig().getBroker();

        AmqpDirectEndpoint endpoint =
                AmqpNodeBootstrapUtils.createEndpoint(component,
                        brokerConfig.getUrl(),
                        brokerConfig.getUsername(),
                        brokerConfig.getPassword(),
                        componentConfig.getRequestQueue(),
                        componentConfig.getReplyQueue());
        if (endpoint == null)
        {
            throw new ChenInfoRuntimeException("Could not create AMQP endpoint");
        }
        component.setMessageProducer(endpoint);

        logger.debug("Initialized component " + component.toString());

        environment.healthChecks().register(
                componentConfig.getName(), getHealthCheck(component, endpoint));

        return endpoint;
    }

    public abstract HealthCheck getHealthCheck(
            C component, AmqpDirectEndpoint endpoint);

}
