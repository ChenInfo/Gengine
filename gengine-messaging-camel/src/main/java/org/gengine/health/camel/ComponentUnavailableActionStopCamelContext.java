package org.gengine.health.camel;

import org.apache.camel.CamelContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.health.ComponentUnavailableAction;
import org.gengine.health.ComponentUnavailableException;

/**
 * ComponentUnavailableAction which stops a given Camel context.
 *
 */
public class ComponentUnavailableActionStopCamelContext implements ComponentUnavailableAction
{
    private static final Log logger = LogFactory.getLog(ComponentUnavailableActionStopCamelContext.class);

    private CamelContext camelContext;
    private long shutdownGracePeriodSeconds = 4;
    private boolean spawnNewThread = false;

    /**
     * Sets the time to allow Camel to gracefully wrap up shutdown before forcing.
     *
     * @param shutdownGracePeriodSeconds
     */
    public void setShutdownGracePeriodSeconds(long shutdownGracePeriodSeconds)
    {
        this.shutdownGracePeriodSeconds = shutdownGracePeriodSeconds;
    }

    /**
     * Sets the Camel context to be stopped
     * @param camelContext
     */
    public void setCamelContext(CamelContext camelContext)
    {
        this.camelContext = camelContext;
    }

    /**
     * Sets whether or not to execute the shutdown in a new thread.
     * <p>
     * If we initiate the stop in another thread Camel / ActiveMQ's default behavior
     * will rollback the transaction and put it in a DLQ.
     * <p>
     * If we initiate the stop in this thread Camel / ActiveMQ's default behavior
     * will return the message to it's place in the queue.
     *
     * @param spawnNewThread
     */
    public void setSpawnNewThread(boolean spawnNewThread)
    {
        this.spawnNewThread = spawnNewThread;
    }

    @Override
    public void execute(ComponentUnavailableException e)
    {
        logger.fatal("Shutting down " + camelContext.getName() + " due to " +
                e.getClass().getSimpleName() + ": " + e.getMessage());

        StopContextRunnable stopContextRunnable = new StopContextRunnable();

        if (spawnNewThread)
        {
            new Thread(stopContextRunnable).start();
        }
        else
        {
            stopContextRunnable.run();
        }
    }

    /**
     * Runnable which stops the Camel context.
     */
    public class StopContextRunnable implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                // First give things a bit to rollback if running in a new thread
                if (spawnNewThread)
                {
                    Thread.sleep(200);
                }
                // Then change the timeout programatically
                camelContext.getShutdownStrategy().setTimeout(shutdownGracePeriodSeconds);
                // Finally, stop the context
                camelContext.stop();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
