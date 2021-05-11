package org.gengine.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.gengine.content.handler.ContentReferenceHandler;

/**
 * Base implementation of a content worker with a <code>sourceContentReferenceHandler</code>
 * field.
 *
 */
public abstract class AbstractContentWorker implements ContentWorker
{
    private static final Log logger = LogFactory.getLog(AbstractContentWorker.class);

    protected static final String FRAMEWORK_PROPERTY_NAME = "name";
    protected static final String FRAMEWORK_PROPERTY_VERSION = "version";

    protected ContentReferenceHandler sourceContentReferenceHandler;
    private boolean isAvailable;
    private Properties properties;
    protected String versionString;
    protected String versionDetailsString;

    /**
     * Sets the content reference handler to be used for retrieving
     * the source content to be worked on.
     *
     * @param sourceContentReferenceHandler
     */
    public void setSourceContentReferenceHandler(ContentReferenceHandler sourceContentReferenceHandler)
    {
        this.sourceContentReferenceHandler = sourceContentReferenceHandler;
    }

    /**
     * Performs any initialization needed after content reference handlers are set
     */
    public void initialize()
    {
        try
        {
            loadProperties();
            initializeVersionString();
            initializeVersionDetailsString();
            this.isAvailable = true;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            this.isAvailable = false;
        }
    }

    @Override
    public boolean isAvailable()
    {
        return isAvailable;
    }

    protected void setIsAvailable(boolean isAvailable)
    {
        this.isAvailable = isAvailable;
    }

    protected Properties getProperties()
    {
        return properties;
    }

    protected void loadProperties()
    {
        String propertiesFilePath = "/" +
                this.getClass().getCanonicalName().replaceAll("\\.", "/") + ".properties";
        InputStream inputStream = this.getClass().getResourceAsStream(propertiesFilePath);
        if (inputStream == null)
        {
            logger.debug(propertiesFilePath + " not found");
            return;
        }
        try
        {
            properties = new Properties();
            properties.load(inputStream);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
            properties = null;
        }
        finally
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected void initializeVersionString()
    {
        if (getProperties() == null)
        {
            versionString = this.getClass().getSimpleName();
        }
        else
        {
            versionString = getProperties().getProperty(FRAMEWORK_PROPERTY_NAME) + " " +
                getProperties().getProperty(FRAMEWORK_PROPERTY_VERSION);
        }
    }

    @Override
    public String getVersionString()
    {
        return versionString;
    }

    protected void initializeVersionDetailsString()
    {
        versionDetailsString = "JVM: " + System.getProperty("java.version");
    }

    @Override
    public String getVersionDetailsString()
    {
        return versionDetailsString;
    }

}
