package org.gengine.content.handler;

import org.cheninfo.service.cmr.repository.ContentIOException;
import org.gengine.content.ContentReference;

/**
 * A base ContentReferenceHandler implementation with convenience methods for dealing with URLs.
 */
public abstract class AbstractUrlContentReferenceHandler implements ContentReferenceHandler
{
    protected boolean isAvailable = false;
    protected boolean waitForAvailability;

    /**
     * Performs any initialization needed for the hanlder.
     */
    public abstract void init();

    /**
     * Gets the remote base URL string for the handler.  Content references
     * contain the path so implementations may choose to only
     * use this for validation purposes.
     *
     * @return the remote base URL string
     */
    protected abstract String getRemoteBaseUrl();

    /**
     * Gets a URL from the {@link #getRemoteBaseUrl()} and the given relative path.
     *
     * @param remoteRelativePath
     * @return the new URL string
     */
    protected String createNewUrl(String remoteRelativePath)
    {
        return getRemoteBaseUrl() + remoteRelativePath;
    }

    /**
     * Builds the remote path for the given remote file name. The base
     * implementation simply returns the given filename indicating
     * all files should be saved in the 'root' of the {@link #getRemoteBaseUrl()}.
     *
     * @param remoteFilename
     * @return the remote file path
     */
    protected String getRemotePath(String remoteFilename)
    {
        // root is fine for now
        return remoteFilename;
    }

    /**
     * Gets the relative path from the remote URL string
     *
     * @param remoteContentUrl
     * @return the relative path
     */
    protected String getRelativePath(String remoteContentUrl)
    {
        if (remoteContentUrl == null)
        {
            return null;
        }
        return remoteContentUrl.replaceFirst(getRemoteBaseUrl(), "");
    }

    /**
     * Gets the extension from the remote URL string
     *
     * @param remoteContentUrl
     * @return the extension of the remote URL string
     */
    protected String getExtension(String remoteContentUrl)
    {
        if (remoteContentUrl == null)
        {
            return null;
        }
        String[] urlComponents = remoteContentUrl.split("\\.");
        return urlComponents[urlComponents.length-1];
    }

    public boolean isAvailable()
    {
        return isAvailable;
    }

    @Override
    public ContentReference createContentReference(String fileName, String mediaType) throws ContentIOException
    {
        String remoteBaseUrl = getRemoteBaseUrl();
        if (remoteBaseUrl == null)
        {
            throw new ContentIOException("remoteBaseUrl must not be null");
        }
        StringBuilder uriBuilder = new StringBuilder(remoteBaseUrl);
        uriBuilder.append(remoteBaseUrl.endsWith("/") ? "" : "/");
        uriBuilder.append(fileName);
        return new ContentReference(uriBuilder.toString(), mediaType);
    }

    /**
     * Gets the remote URL string from the given content reference.  The base implementation
     * simply returns the content reference's URI.  Other implementations
     * may need to perform some mapping to another protocol and/or path.
     *
     * @param contentReference
     * @return the remote file URL
     */
    protected String getRemoteFileUrl(ContentReference contentReference)
    {
        if (contentReference == null)
        {
            return null;
        }
        return contentReference.getUri();
    }

}
