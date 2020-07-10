package org.gengine.content.handler;

import java.io.File;
import java.io.InputStream;

import org.cheninfo.service.cmr.repository.ContentIOException;
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
     * Gets a file object for the given content reference
     *
     * @param contentReference
     * @return a file object representation of the content reference
     * @throws ContentIOException
     */
    public File getFile(ContentReference contentReference) throws ContentIOException;

    /**
     * Gets a file object for the given content reference and optionally validates
     * the file through size or hash comparisons
     *
     * @param contentReference
     * @param waitForTransfer whether or not to check file size and wait for transfer completion
     * @return a file object representation of the content reference
     * @throws ContentIOException
     * @throws InterruptedException
     */
    public File getFile(ContentReference contentReference, boolean waitForTransfer) throws ContentIOException, InterruptedException;

    /**
     * Writes the given source file into the given target content reference
     *
     * @param sourceFile
     * @param targetContentReference
     * @return the size copied
     * @throws ContentIOException
     */
    public long putFile(File sourceFile, ContentReference targetContentReference) throws ContentIOException;

    /**
     * Gets an input stream for the given content reference
     *
     * @param contentReference
     * @return the content reference input stream
     * @throws ContentIOException
     */
    public InputStream getInputStream(ContentReference contentReference) throws ContentIOException;

    /**
     * Writes the given source input stream into the given target content reference
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
