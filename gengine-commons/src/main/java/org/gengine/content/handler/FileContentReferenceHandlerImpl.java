package org.gengine.content.handler;

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
import java.util.Date;

import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;
import org.gengine.content.file.FileProvider;
import org.gengine.content.file.FileProviderImpl;

/**
 * Java {@link File} content reference handler implementation.
 * <p>
 * Content reference creation is delegated to {@link FileProvider}.
 *
 */
public class FileContentReferenceHandlerImpl implements FileContentReferenceHandler
{
    private static final Log logger = LogFactory.getLog(FileContentReferenceHandlerImpl.class);

    public static final String URI_SCHEME_FILE = "file:/";

    private static final long DEFAULT_TRANSFER_CHECK_PERIOD_MS = 2000;
    private static final long DEFAULT_TRANSFER_CHECK_TIMEOUT_MS = 10000;

    private FileProvider fileProvider;
    private long transferCheckPeriodMs = DEFAULT_TRANSFER_CHECK_PERIOD_MS;
    private long transferCheckTimeoutMs = DEFAULT_TRANSFER_CHECK_TIMEOUT_MS;

    public void setFileProvider(FileProvider fileProvider)
    {
        this.fileProvider = fileProvider;
    }

    /**
     * Sets the interval to check for transfer
     *
     * @param transferCheckPeriodMs
     */
    public void setTransferCheckPeriodMs(long transferCheckPeriodMs)
    {
        this.transferCheckPeriodMs = transferCheckPeriodMs;
    }

    /**
     * Sets the transfer check timeout
     *
     * @param transferCheckTimeoutMs
     */
    public void setTransferCheckTimeoutMs(long transferCheckTimeoutMs)
    {
        this.transferCheckTimeoutMs = transferCheckTimeoutMs;
    }

    public void setFileProviderDirectoryPath(String directoryPath)
    {
        if (fileProvider != null)
        {
            throw new IllegalStateException("FileProvider has already been set");
        }
        fileProvider = new FileProviderImpl();
        ((FileProviderImpl) fileProvider).setDirectoryPath(directoryPath);
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

    @Override
    public boolean isContentReferenceExists(ContentReference contentReference)
    {
        if (contentReference == null)
        {
            return false;
        }
        try
        {
            File file = getFile(contentReference, false);
            if (file == null)
            {
                return false;
            }
            return file.exists();
        }
        catch (Exception e)
        {
            // Don't really care why, just that it doesn't exist
            return false;
        }
    }

    @Override
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
        long startTime = (new Date()).getTime();
        long endTime = (new Date()).getTime();
        long expectedSize = contentReference.getSize();
        long actualSize = file.length();
        while (actualSize < expectedSize)
        {
            if (((endTime - startTime) > transferCheckTimeoutMs))
            {
                throw new ContentIOException("Could not get file for content reference: " +
                        contentReference.getUri());
            }
            logger.trace("Checked file, expectedSize=" + expectedSize + " actualSize=" + actualSize +
                    ", waiting " + transferCheckPeriodMs + "ms");
            Thread.sleep(transferCheckPeriodMs);
            actualSize = file.length();
            endTime = (new Date()).getTime();
        }
        logger.debug("File expectedSize=" + expectedSize + " actualSize=" + actualSize + ", ending check");
        return file;
    }

    @Override
    public InputStream getInputStream(ContentReference contentReference, boolean waitForAvailability) throws ContentIOException
    {
        File file = null;
        try
        {
            file = getFile(contentReference, waitForAvailability);
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
    public long putInputStream(InputStream sourceInputStream, ContentReference targetContentReference)
            throws ContentIOException
    {
        FileOutputStream fileOutputStream = null;
        try
        {
            File targetFile = getFile(targetContentReference, false);
            fileOutputStream = new FileOutputStream(targetFile);
            long sizeCopied = IOUtils.copyLarge(sourceInputStream, fileOutputStream);
            fileOutputStream.close();
            return sizeCopied;
        }
        catch (IOException e)
        {
            throw new ContentIOException("Error copying input stream", e);
        }
        catch (InterruptedException e)
        {
            return 0;
        }
        finally
        {
            if (fileOutputStream != null)
            {
                try
                {
                    fileOutputStream.close();
                    sourceInputStream.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    @Override
    public void delete(ContentReference contentReference) throws ContentIOException
    {
        try
        {
            File file = getFile(contentReference, false);
            boolean deleted = file.delete();
            if (!deleted)
            {
                throw new ContentIOException("File could not be deleted");
            }
        }
        catch (InterruptedException e)
        {
            // Should not encounter this with waitForAvailability = false
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
