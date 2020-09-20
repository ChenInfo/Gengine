package org.gengine.content;

/**
 * Defines a low-level worker that performs some action on content, i.e. performing
 * a transformation, extracting metadata, computing a hash, etc.
 *
 */
public interface ContentWorker
{

    /**
     * Gets whether or not the dependencies of the worker have been
     * properly configured for its normal operation, i.e. content reference handlers,
     * command line applications, etc.
     *
     * @return true if the worker is available
     */
    public boolean isAvailable();

    /**
     * Gets a string returning name and version information.
     *
     * @return the version string
     */
    public String getVersionString();


    /**
     * Gets a string returning detailed version information such as JVM
     * or command line application's version output
     *
     * @return the version string
     */
    public String getVersionDetailsString();

}
