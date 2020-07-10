package org.gengine.content.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.gengine.content.file.FileProviderImpl;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.handler.FileContentReferenceHandlerImpl;
import org.gengine.error.ChenInfoRuntimeException;
import org.gengine.messaging.MessageConsumer;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;
import org.gengine.messaging.amqp.AmqpNodeBootstrapUtils;

/**
 * Base boostrap class which loads a properties file from the path
 * specified via command line argument, loads a worker class and creates an instance of it,
 * then creates an AMQP endpoint and starts a listener for it.
 *
 * @param <W> the type of worker
 * @param <N> the type of node
 */
public abstract class AbstractSimpleAmqpNodeBootstrap<W, N extends MessageConsumer>
{
    protected static final String PROP_WORKER_CLASS = "gengine.worker.class";
    protected static final String PROP_WORKER_DIR_SOURCE = "gengine.worker.dir.source";

    private String propertiesFilePath;
    private Properties properties;

    public void run(String[] args)
    {
        if (args.length < 1)
        {
            throw new IllegalArgumentException("USAGE: propertiesFilePath");
        }

        this.propertiesFilePath = args[0];

        getProperties();

        W worker = createWorkerFromPropClassname();
        initWorker(worker);

        N node = createNode(worker);

        if (node == null)
        {
            throw new ChenInfoRuntimeException("Could not create node");
        }

        AmqpDirectEndpoint endpoint = createEndpoint(node);

        if (endpoint == null)
        {
            throw new ChenInfoRuntimeException("Could not create endpoint");
        }

        initNode(node, endpoint);

        endpoint.startListener();
    }

    /**
     * Loads the properties from the path specified via command line.
     *
     * @return the properties object
     */
    protected Properties getProperties()
    {
        if (properties == null)
        {
            InputStream inputStream = null;
            try
            {
                properties = new Properties();
                inputStream = new FileInputStream(propertiesFilePath);
                properties.load(inputStream);
            }
            catch (IOException e)
            {
                throw new ChenInfoRuntimeException("Could not load required " + propertiesFilePath);
            }
            finally
            {
                try
                {
                    if (inputStream != null)
                    {
                        inputStream.close();
                    }
                }
                catch (IOException e)
                {

                }
            }
        }
        return properties;
    }

    /**
     * Attempts to load the worker class specified in the properties file and
     * create a new instance of it.
     *
     * @return the newly created worker object
     */
    @SuppressWarnings("unchecked")
    protected W createWorkerFromPropClassname()
    {
        try
        {
            String workerClassName = getProperties().getProperty(PROP_WORKER_CLASS);
            Class<W> workerClass = (Class<W>) AbstractSimpleAmqpNodeBootstrap.class.getClassLoader().loadClass(workerClassName);
            return workerClass.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            throw new ChenInfoRuntimeException("Could not load worker class", e);
        }
    }

    /**
     * Initializes the worker, i.e. setting source and target directories.
     *
     * @param worker
     */
    protected abstract void initWorker(W worker);

    /**
     * Gets the ndoe that request message should be sent to.
     *
     * @param worker
     * @return the node
     */
    protected abstract N createNode(W worker);

    /**
     * Initializes the node, i.e. setting the message producer for replies
     *
     * @param node
     * @param endpoint
     */
    protected abstract void initNode(N node, AmqpDirectEndpoint endpoint);

    /**
     * Creates and caches an AMQP endpoint using the properties file configuration.
     *
     * @param messageConsumer
     * @return the AMQP endpoint
     */
    protected AmqpDirectEndpoint createEndpoint(MessageConsumer messageConsumer)
    {
        return AmqpNodeBootstrapUtils.createEndpoint(
                    messageConsumer, getProperties());
    }

    /**
     * Constructs a {@link FileContentReferenceHandlerImpl} using the path
     * specified in the properties file under the given propertiesKey.
     *
     * @param propertiesKey
     * @return the ContentReferenceHandler
     */
    protected ContentReferenceHandler createFileContentReferenceHandler(String propertiesKey)
    {
        String directoryPath = getProperties().getProperty(propertiesKey);
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

}
