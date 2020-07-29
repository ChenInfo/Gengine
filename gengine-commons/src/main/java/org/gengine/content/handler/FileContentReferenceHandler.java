package org.gengine.content.handler;

import java.io.File;

import org.cheninfo.service.cmr.repository.ContentIOException;
import org.gengine.content.ContentReference;

/**
 * Adds file handling to the ContentReferenceHandler interface.
 */
public interface FileContentReferenceHandler extends ContentReferenceHandler
{

    /**
     * Gets a File object for the given content reference, optionally waiting for the
     * file to be available and match the expected file size.
     * <p>
     * This is useful for implementations that already use a file-based implementation
     * and can prevent workers from unnecessarily copying I/O streams.
     *
     * @param contentReference
     * @param waitForTransfer
     * @return the File for the content reference
     * @throws ContentIOException
     * @throws InterruptedException
     */
    public File getFile(ContentReference contentReference, boolean waitForTransfer) throws ContentIOException, InterruptedException;
}
