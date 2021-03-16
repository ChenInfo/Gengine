package org.gengine.content.file;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FileProvider implementation which is configured with a directoryPath in which
 * files should be created
 *
 */
public class FileProviderImpl implements FileProvider
{
    private static final Log logger = LogFactory.getLog(FileProviderImpl.class);

    private String directoryPath;
    private boolean createDirectory = false;
    private File directory;

    public FileProviderImpl()
    {

    }

    public FileProviderImpl(String directoryPath)
    {
        this.directoryPath = directoryPath;
    }

    public void setDirectoryPath(String directoryPath)
    {
        this.directoryPath = directoryPath;
    }

    public void setCreateDirectory(boolean create)
    {
        this.createDirectory = create;
    }

    /**
     * Get the dir.
     * Will create the dir on the fly if it does not already exist.
     *
     * @return Returns a directory
     */
    protected File getDirectory()
    {
        if (directory == null)
        {
            directory = new File(directoryPath);
            // ensure that the directory exists
            if (directory.exists())
            {
                // nothing to do
            }
            else
            {
                // not there yet
                if (!directory.mkdirs() && createDirectory)
                {
                    throw new RuntimeException("Failed to create directory: " + directory);
                }
                if (logger.isDebugEnabled())
                {
                    logger.debug("Created directory: " + directory);
                }
            }
        }
        // done
        return directory;
    }

    /**
     * Create a file in the configured dir.
     *
     * @return Returns a temp <code>File</code> that will be located in the
     *         configured directory
     *
     * @see File#createTempFile(java.lang.String, java.lang.String)
     */
    public File createFile(String prefix, String suffix)
    {
        // we have the directory we want to use
        return createFile(prefix, suffix, getDirectory());
    }

    /**
     * @return Returns a <code>File</code> that will be located in the
     *         given directory
     *
     * @see File#createTempFile(java.lang.String, java.lang.String)
     */
    protected File createFile(String prefix, String suffix, File directory)
    {
        try
        {
            String filePath = directory.getAbsolutePath() + File.separator + prefix + suffix;
            if (logger.isDebugEnabled())
            {
                logger.debug("Creating file: " + filePath);
            }
            File file = new File(filePath);
            if (file.createNewFile())
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(filePath + " created");
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(filePath + " already exists");
                }
            }
            return file;
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to created file: \n" +
                    "   prefix: " + prefix + "\n"
                    + "   suffix: " + suffix + "\n" +
                    "   directory: " + directory,
                    e);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + "[");
        builder.append("directory: " + getDirectory().toString());
        builder.append(", ");
        builder.append("createDirectory: " + createDirectory);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean isAvailable()
    {
        File dir = getDirectory();
        return dir != null && dir.exists();
    }

}
