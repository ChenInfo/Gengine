package org.gengine.content.file;

import java.io.File;

/**
 * Defines methods to create files.  Implementations might include leverage Java's temporary
 * file components, explicit user-defined directories, etc.
 *
 */
public interface FileProvider
{

    /**
     * Create a file with the given prefix and suffix.
     *
     * @param prefix
     * @param suffix
     * @return the newly created File object
     */
    public File createFile(String prefix, String suffix);

    /**
     * Determines whether or not the file provider is available.
     * <p>
     * Some implementations might check permissions via this method.
     *
     * @return whether or not the file provider is available as configured
     */
    public boolean isAvailable();

}
