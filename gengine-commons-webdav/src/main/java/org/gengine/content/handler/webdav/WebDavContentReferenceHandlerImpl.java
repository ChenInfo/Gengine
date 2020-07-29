package org.gengine.content.handler.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.cheninfo.service.cmr.repository.ContentIOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.ContentReference;
import org.gengine.content.handler.AbstractUrlContentReferenceHandler;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;

/**
 * WebDAV content handler implementation
 *
 */
public class WebDavContentReferenceHandlerImpl extends AbstractUrlContentReferenceHandler
{
    private static final Log logger = LogFactory.getLog(WebDavContentReferenceHandlerImpl.class);

    /** store protocol that is used as prefix in contentUrls */
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    private static final long DEFAULT_TRANSFER_CHECK_PERIOD_MS = 1000;

    private Sardine sardine;

    private String remoteBaseUrl;
    private String username;
    private String password;
    private Long transferCheckPeriodMs = DEFAULT_TRANSFER_CHECK_PERIOD_MS;

    public void setSardine(Sardine sardine)
    {
        this.sardine = sardine;
    }

    protected String getRemoteBaseUrl()
    {
        return remoteBaseUrl;
    }

    public void setRemoteBaseUrl(String remoteBaseUrl)
    {
        this.remoteBaseUrl = remoteBaseUrl;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setTransferCheckPeriodMs(Long transferCheckPeriodMs)
    {
        this.transferCheckPeriodMs = transferCheckPeriodMs;
    }

    public void init()
    {
        // Instantiate Sardine object
        if (sardine == null)
        {
            sardine = SardineFactory.begin(username, password);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("WebDAV content transport initialization complete: " +
                    "{ remoteBaseUrl: '"+remoteBaseUrl+"' }");
        }
        this.waitForAvailability = transferCheckPeriodMs != null;
        this.isAvailable = true;
    }


    @Override
    public boolean isContentReferenceSupported(ContentReference contentReference)
    {
        if (contentReference == null)
        {
            return false;
        }
        String uri = contentReference.getUri();
        if (uri == null)
        {
            return false;
        }
        if (uri.startsWith(remoteBaseUrl))
        {
            return true;
        }
        if ((uri.startsWith(HTTPS_PROTOCOL) || uri.startsWith(HTTP_PROTOCOL)))
        {
            return true;
        }
        return false;
    }

    @Override
    public InputStream getInputStream(ContentReference contentReference, boolean waitForAvailability) throws ContentIOException, InterruptedException
    {
        if (!isContentReferenceSupported(contentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }
        try
        {
            String remoteFileUrl = getRemoteFileUrl(contentReference);
            while (true)
            {
                try
                {
                    return sardine.get(remoteFileUrl);
                }
                catch (SardineException e)
                {
                    if (waitForAvailability && e.getMessage().contains("404"))
                    {
                        logger.trace(remoteFileUrl + " not yet available, waiting " + transferCheckPeriodMs + "ms");
                        Thread.sleep(transferCheckPeriodMs);
                    }
                    else
                    {
                        logger.error(e.getMessage(), e);
                        throw new ContentIOException("Failed to read content: " + e.getMessage(), e);
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to read content: " + e.getMessage(), e);
        }
    }

    @Override
    public long putInputStream(InputStream sourceInputStream, ContentReference targetContentReference)
            throws ContentIOException
    {
        if (!isContentReferenceSupported(targetContentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }

        try
        {
            String remoteFileUrl = getRemoteFileUrl(targetContentReference);

            logger.debug("Putting input stream to " + remoteFileUrl);
            sardine.put(remoteFileUrl, sourceInputStream);

            List<DavResource> resources = sardine.list(remoteFileUrl);
            if (resources == null || resources.size() > 1)
            {
                throw new ContentIOException("Unable to determine result of transfer");
            }
            DavResource davResource = resources.iterator().next();
            return davResource.getContentLength();
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
            throw new ContentIOException("Failed to write content: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(ContentReference contentReference) throws ContentIOException
    {
        if (!isContentReferenceSupported(contentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }

        String remoteFileUrl = getRemoteFileUrl(contentReference);

        try
        {
            sardine.delete(remoteFileUrl);
        }
        catch (IOException e)
        {
            throw new ContentIOException("Failed to delete content: " + e.getMessage(), e);
        }

    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + "[");
        builder.append("sardine: " + (sardine == null ? "null" : sardine.toString()));
        builder.append(", ");
        builder.append("remoteBaseUrl: " + remoteBaseUrl);
        builder.append(", ");
        builder.append("isAvailable: " + isAvailable());
        builder.append("]");
        return builder.toString();
    }

}
