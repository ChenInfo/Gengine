package org.gengine.content;

import org.gengine.messaging.MessageConsumer;

/**
 * Defines a component which is a consumer of request for content action messages,
 * delegates that work to a worker, and sends a reply with the results.
 *
 */
public interface Component extends MessageConsumer
{
    /**
     * Gets the name of the component, useful for health checks.
     *
     * @return the component name
     */
    public String getName();

    /**
     * Determines whether or not the worker is available.
     *
     * @return true if the worker is available
     * @see {@link ContentWorker#isAvailable()}
     */
    public boolean isWorkerAvailable();

    /**
     * Gets a string returning name and version information
     *
     * @return the version string
     * @see {@link ContentWorker#getVersionString()}
     */
    public String getWorkerVersionString();


    /**
     * Gets a string returning detailed version information such as JVM
     * or command line application's version output
     *
     * @return the version string
     * @see {@link ContentWorker#getVersionDetailsString()}
     */
    public String getWorkerVersionDetailsString();

}
