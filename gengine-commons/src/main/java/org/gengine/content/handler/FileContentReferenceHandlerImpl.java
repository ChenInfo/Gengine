package org.gengine.content.handler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.cheninfo.service.cmr.repository.ContentIOException;
import org.gengine.content.ContentReference;
import org.gengine.content.file.FileProvider;

/**
 * Java {@link File} content reference handler implementation.
 * <p>
 * Content reference creation is delegated to {@link TempFileProvider}.
 *
 */
public class FileContentReferenceHandlerImpl implements ContentReferenceHandler
{
    private static final Log logger = LogFactory.getLog(FileContentReferenceHandlerImpl.class);

    public static final String URI_SCHEME_FILE = "file:/";

    private static final long TRANSFER_CHECK_PERIOD_MS = 2000;

    private FileProvider fileProvider;

    public void setFileProvider(FileProvider fileProvider)
    {
        this.fileProvider = fileProvider;
    }

    @Override
    public boolean isContentReferenceSupported(ContentReference contentReference)
    {
        if (contentReference == null)
        {
            return false;
        }
        return contentReference.getUri().startsWith(URI_SCHEME_FILE);
    }

    public ContentReference createContentReference(String fileName, String mediaType) throws ContentIOException
    {
        String suffix = fileName.substring(StringUtils.lastIndexOf(fileName, "."), fileName.length());
        String prefix = fileName.substring(0, StringUtils.lastIndexOf(fileName, "."));

        File tempFile = fileProvider.createFile(prefix, suffix);

        if (logger.isDebugEnabled())
        {
            logger.debug("Created file content reference for " +
            		"mediaType=" + mediaType + ": " + tempFile.getAbsolutePath());
        }

        return new ContentReference(tempFile.toURI().toString(), mediaType);
    }

    @Override
    public File getFile(ContentReference contentReference) throws ContentIOException
    {
        try
        {
            return getFile(contentReference, false);
        }
        catch (InterruptedException e)
        {
            // Should not encounter interrupted when validate=false
            throw new ContentIOException("Interrupt caught: " + e.getMessage());
        }
    }

    @Override
    public File getFile(ContentReference contentReference, boolean waitForTransfer) throws ContentIOException, InterruptedException
    {
        if (!isContentReferenceSupported(contentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("Getting file for content reference: " +
                    contentReference.getUri());
        }
        File file = null;
        try
        {
            file = new File(new URI(contentReference.getUri()));
        }
        catch (URISyntaxException e)
        {
            throw new ContentIOException("Syntax error getting file reference", e);
        }
        if (!waitForTransfer)
        {
            return file;
        }
        if (contentReference.getSize() == null)
        {
            logger.debug("Expected file size unknown, skipping size check");
            return file;
        }
        long expectedSize = contentReference.getSize();
        long actualSize = file.length();
        while (actualSize < expectedSize)
        {
            logger.trace("Checked file, expectedSize=" + expectedSize + " actualSize=" + actualSize +
                    ", waiting " + TRANSFER_CHECK_PERIOD_MS + "ms");
            Thread.sleep(TRANSFER_CHECK_PERIOD_MS);
            actualSize = file.length();
        }
        logger.debug("File expectedSize=" + expectedSize + " actualSize=" + actualSize + ", ending check");
        return file;
    }

    @Override
    public InputStream getInputStream(ContentReference contentReference) throws ContentIOException
    {
        File file = null;
        try
        {
            file = getFile(contentReference, true);
        }
        catch (InterruptedException e1)
        {
            // We were asked to stop
        }
        if (file == null)
        {
            throw new ContentIOException("File not found");
        }
        try
        {
            return new FileInputStream(file);
        }
        catch (FileNotFoundException e)
        {
            throw new ContentIOException("File not found", e);
        }
    }

    @Override
    public long putFile(File sourceFile, ContentReference targetContentReference) throws ContentIOException
    {
        File targetFile = getFile(targetContentReference);
        try
        {
            FileUtils.copyFile(sourceFile, targetFile);
            return targetFile.length();
        }
        catch (IOException e)
        {
            throw new ContentIOException("Error copying file", e);
        }

    }

    @Override
    public long putInputStream(InputStream sourceInputStream, ContentReference targetContentReference)
            throws ContentIOException
    {
        File targetFile = getFile(targetContentReference);
        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            long sizeCopied = IOUtils.copyLarge(sourceInputStream, fileOutputStream);
            fileOutputStream.close();
            return sizeCopied;
        }
        catch (IOException e)
        {
            throw new ContentIOException("Error copying input stream", e);
        }
    }

    @Override
    public void delete(ContentReference contentReference) throws ContentIOException
    {
        File file = getFile(contentReference);
        boolean deleted = file.delete();
        if (!deleted)
        {
            throw new ContentIOException("File could not be deleted");
        }
    }

    @Override
    public boolean isAvailable()
    {
        return fileProvider != null && fileProvider.isAvailable();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + "[");
        builder.append("fileProvider: " + fileProvider.toString());
        builder.append(", ");
        builder.append("isAvailable: " + isAvailable());
        builder.append("]");
        return builder.toString();
    }

}
