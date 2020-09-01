package org.gengine.content.handler;

import java.io.InputStream;

import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;

/**
 * Defines a handler for reading and writing {@link ContentReference} objects.
 *
 */
public interface ContentReferenceHandler
{

    /**
     * Determines whether the given content reference is supported by the handler
     *
     * @param contentReference
     * @return whether or not the content reference is supported
     */
    public boolean isContentReferenceSupported(ContentReference contentReference);

    /**
     * Creates a content reference of the given file name and media type.
     *
     * @param fileName
     * @param mediaType
     * @return the created content reference
     * @throws ContentIOException
     */
    public ContentReference createContentReference(String fileName, String mediaType) throws ContentIOException;

    /**
     * Gets an input stream for the given content reference
     *
     * @param contentReference
     * @param waitForAvailability whether or not to check and wait for availability
     * @return the content reference input stream
     * @throws ContentIOException
     * @throws InterruptedException
     */
    public InputStream getInputStream(ContentReference contentReference, boolean waitForAvailability) throws ContentIOException, InterruptedException;

    /**
     * Writes the given source input stream into the given target content reference.
     * <p>
     * Implementations should close both streams.
     *
     * @param sourceInputStream
     * @param targetContentReference
     * @return the size copied
     * @throws ContentIOException
     */
    public long putInputStream(InputStream sourceInputStream, ContentReference targetContentReference) throws ContentIOException;

    /**
     * Deletes the given content reference
     * @param contentReference
     * @throws ContentIOException
     */
    public void delete(ContentReference contentReference) throws ContentIOException;

    /**
     * Determines whether or not the handler is currently available, i.e. any required credentials
     * are defined and valid.
     *
     * @return whether or not the handler is available
     */
    public boolean isAvailable();

}
