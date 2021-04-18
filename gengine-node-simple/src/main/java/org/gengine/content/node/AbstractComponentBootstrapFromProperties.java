package org.gengine.content.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;
import java.util.concurrent.Executors;

import org.gengine.content.AbstractAsyncComponent;
import org.gengine.content.AbstractComponent;
import org.gengine.content.ContentWorker;
import org.gengine.content.file.FileProviderImpl;
import org.gengine.content.handler.ContentReferenceHandler;
import org.gengine.content.handler.FileContentReferenceHandlerImpl;
import org.gengine.content.handler.webdav.WebDavContentReferenceHandlerImpl;
import org.gengine.error.GengineRuntimeException;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;
import org.gengine.messaging.amqp.AmqpNodeBootstrapUtils;

/**
 * Base bootstrap which creates a component, configures it with a worker, and creates
 * an endpoint for sending and receiving messages
 *
 * @param <W>
 */
public abstract class AbstractComponentBootstrapFromProperties<W extends ContentWorker>
{
    private static final Log logger = LogFactory.getLog(AbstractComponentBootstrapFromProperties.class);

    public static final String PROP_WORKER_CONTENT_REF_HANDLER_SOURCE_PREFIX =
            "gengine.worker.contentrefhandler.source";
    public static final String PROP_WORKER_CONTENT_REF_HANDLER_CLASS_SUFFIX = ".class";
    public static final String PROP_WORKER_CONTENT_REF_HANDLER_FILE_DIR_SUFFIX = ".file.dir";
    public static final String PROP_WORKER_CONTENT_REF_HANDLER_WEBDAV_URL_SUFFIX = ".webdav.url";
    public static final String PROP_WORKER_CONTENT_REF_HANDLER_WEBDAV_USERNAME_SUFFIX = ".webdav.username";
    public static final String PROP_WORKER_CONTENT_REF_HANDLER_WEBDAV_PASSWORD_SUFFIX = ".webdav.password";

    protected Properties properties;
    protected W worker;

    public AbstractComponentBootstrapFromProperties(Properties properties, W worker)
    {
        this.properties = properties;
        this.worker = worker;
    }

    /**
     * Constructs a {@link FileContentReferenceHandlerImpl} using the path
     * specified in the properties file under the given propertiesKey.
     *
     * @param handler the content reference handler
     * @param dirPropertiesKey the properties key for getting the directory value
     * @return the ContentReferenceHandler
     */
    protected void setFileContentReferenceHandlerProvider(
            FileContentReferenceHandlerImpl handler, String dirPropertiesKey)
    {
        String directoryPath = properties.getProperty(dirPropertiesKey);
        FileProviderImpl fileProvider = new FileProviderImpl();
        fileProvider.setDirectoryPath(directoryPath);
                new FileContentReferenceHandlerImpl();
        handler.setFileProvider(fileProvider);
    }

    protected void setWebDavContentReferenceHandlerCredentials(
            WebDavContentReferenceHandlerImpl handler,
            String urlPropertiesKey,
            String usernamePropertiesKey,
            String passwordPropertiesKey)
    {
        String url = properties.getProperty(urlPropertiesKey);
        String username = properties.getProperty(usernamePropertiesKey);
        String password = properties.getProperty(passwordPropertiesKey);
        handler.setRemoteBaseUrl(url);
        handler.setUsername(username);
        handler.setPassword(password);
    }

    protected ContentReferenceHandler createContentReferenceHandler(String propertiesKeyPrefix)
    {
        ContentReferenceHandler handler =
                (ContentReferenceHandler) SimpleAmqpNodeBootstrap.createObjectFromClassInProperties(
                        properties, propertiesKeyPrefix + PROP_WORKER_CONTENT_REF_HANDLER_CLASS_SUFFIX);
        if (handler instanceof FileContentReferenceHandlerImpl)
        {
            setFileContentReferenceHandlerProvider(
                    (FileContentReferenceHandlerImpl) handler,
                    propertiesKeyPrefix + PROP_WORKER_CONTENT_REF_HANDLER_FILE_DIR_SUFFIX);
        }
        if (handler instanceof WebDavContentReferenceHandlerImpl)
        {
            setWebDavContentReferenceHandlerCredentials(
                    (WebDavContentReferenceHandlerImpl) handler,
                    propertiesKeyPrefix + PROP_WORKER_CONTENT_REF_HANDLER_WEBDAV_URL_SUFFIX,
                    propertiesKeyPrefix + PROP_WORKER_CONTENT_REF_HANDLER_WEBDAV_USERNAME_SUFFIX,
                    propertiesKeyPrefix + PROP_WORKER_CONTENT_REF_HANDLER_WEBDAV_PASSWORD_SUFFIX);
            ((WebDavContentReferenceHandlerImpl) handler).init();
        }

        return handler;
    }

    /**
     * Constructs a {@link FileContentReferenceHandlerImpl} using the path
     * specified in the properties file under the given propertiesKey.
     *
     * @param propertiesKey
     * @return the ContentReferenceHandler
     */
    protected ContentReferenceHandler createWebDavContentReferenceHandler(String propertiesKey)
    {
        String directoryPath = properties.getProperty(propertiesKey);
        FileProviderImpl fileProvider = new FileProviderImpl();
        fileProvider.setDirectoryPath(directoryPath);
        FileContentReferenceHandlerImpl fileContentReferenceHandler =
                new FileContentReferenceHandlerImpl();
        fileContentReferenceHandler.setFileProvider(fileProvider);
        return fileContentReferenceHandler;
    }

    protected abstract AbstractComponent<W> createComponent();

    protected abstract void initWorker();

    protected void run()
    {
        initWorker();

        AbstractComponent<W> component = createComponent();
        component.setWorker(worker);
        // TODO allow more config
        if (component instanceof AbstractAsyncComponent<?,?,?>)
        {
            ((AbstractAsyncComponent<?,?,?>) component).setExecutorService(
                    Executors.newCachedThreadPool());
        }

        AmqpDirectEndpoint endpoint =
                AmqpNodeBootstrapUtils.createEndpoint(component, properties);
        if (endpoint == null)
        {
            throw new GengineRuntimeException("Could not create AMQP endpoint");
        }

        component.setMessageProducer(endpoint);
        component.init();

        logger.debug("Initialized component " + component.toString());

        endpoint.startListener();
    }


}
