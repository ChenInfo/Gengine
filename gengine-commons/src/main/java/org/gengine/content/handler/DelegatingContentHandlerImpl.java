package org.gengine.content.handler;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;

/**
 * ContentReferenceHandler implementation that maintains a list of delegates that are chosen
 * based on the content references passed to method invocations.
 *
 */
public class DelegatingContentHandlerImpl implements FileContentReferenceHandler
{

    private List<ContentReferenceHandler> delegates;

    /**
     * Sets the list of delegate {@link ContentReferenceHandler} implementations that may
     * perform the actual work based on the {@link ContentReference} sent in to method calls.
     *
     * @param delegates
     */
    public void setDelegates(List<ContentReferenceHandler> delegates)
    {
        this.delegates = delegates;
    }

    @Override
    public boolean isContentReferenceSupported(ContentReference contentReference)
    {
        for (ContentReferenceHandler delegate : delegates)
        {
            if (delegate.isContentReferenceSupported(contentReference))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Chooses the {@link ContentHandler} implementation from the list of delegates based
     * on the given contentReference.
     *
     * @param contentReference
     * @return the delegate ContentHandler
     */
    private ContentReferenceHandler getDelegate(ContentReference contentReference)
    {
        for (ContentReferenceHandler delegate : delegates)
        {
            if (delegate.isContentReferenceSupported(contentReference))
            {
                return delegate;
            }
        }
        throw new UnsupportedOperationException(
                "No delegate ContentHandler found for reference: " + contentReference.toString());
    }

    @Override
    public boolean isContentReferenceExists(ContentReference contentReference)
    {
        ContentReferenceHandler delegate = getDelegate(contentReference);
        return delegate.isContentReferenceExists(contentReference);
    }

    @Override
    public ContentReference createContentReference(String fileName, String mediaType) throws ContentIOException
    {
        throw new UnsupportedOperationException(
                "createContentReference not supported for " + this.getClass().getSimpleName());
    }

    @Override
    public InputStream getInputStream(ContentReference contentReference, boolean waitForAvailability)
            throws ContentIOException, InterruptedException
    {
        ContentReferenceHandler delegate = getDelegate(contentReference);
        return delegate.getInputStream(contentReference, waitForAvailability);
    }

    @Override
    public long putInputStream(InputStream sourceInputStream, ContentReference targetContentReference)
            throws ContentIOException
    {
        ContentReferenceHandler delegate = getDelegate(targetContentReference);
        return delegate.putInputStream(sourceInputStream, targetContentReference);
    }

    @Override
    public long putFile(File sourceFile, ContentReference targetContentReference) throws ContentIOException
    {
        ContentReferenceHandler delegate = getDelegate(targetContentReference);
        return delegate.putFile(sourceFile, targetContentReference);
    }

    @Override
    public void delete(ContentReference contentReference) throws ContentIOException
    {
        ContentReferenceHandler delegate = getDelegate(contentReference);
        delegate.delete(contentReference);
    }

    @Override
    public boolean isAvailable()
    {
        for (ContentReferenceHandler delegate : delegates)
        {
            if (!delegate.isAvailable())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public File getFile(ContentReference contentReference, boolean waitForTransfer)
            throws ContentIOException, InterruptedException
    {
        ContentReferenceHandler delegate = getDelegate(contentReference);
        if (!(FileContentReferenceHandler.class.isAssignableFrom(delegate.getClass())))
        {
            throw new UnsupportedOperationException(
                    delegate.getClass().getSimpleName() + " does not implement " +
                            FileContentReferenceHandler.class.getSimpleName());
        }
        return ((FileContentReferenceHandler) delegate).getFile(contentReference, waitForTransfer);
    }
}
