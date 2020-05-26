package org.gengine.health;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ComponentUnavailableAction which iterates over a list of specified actions.
 *
 */
public class CompositeComponentUnavailableAction implements ComponentUnavailableAction
{
    private static final Log logger = LogFactory.getLog(CompositeComponentUnavailableAction.class);

    private List<ComponentUnavailableAction> actions;
    private long actionTimeoutMs = 10000;

    private ExecutorService executorService;

    public void setActions(List<ComponentUnavailableAction> actions)
    {
        this.actions = actions;
    }

    public void setActionTimeoutMs(long actionTimeoutMs)
    {
        this.actionTimeoutMs = actionTimeoutMs;
    }

    public void init()
    {
        if (executorService == null)
        {
            executorService = Executors.newCachedThreadPool();
        }
    }


    @Override
    public void execute(ComponentUnavailableException e)
    {
        if (actions == null)
        {
            return;
        }
        if (logger.isInfoEnabled())
        {
            String message = e.getClass().getSimpleName() + ", executing: ";
            for (Iterator<ComponentUnavailableAction> iterator = actions.iterator(); iterator.hasNext();)
            {
                ComponentUnavailableAction action = (ComponentUnavailableAction) iterator.next();
                message = message + action.getClass().getSimpleName();
                if (iterator.hasNext())
                {
                    message = message + ", ";
                }
            }
            logger.info(message);
        }

        for (ComponentUnavailableAction action : actions)
        {
            try
            {
                execute(action, e);
            }
            catch (Throwable e1)
            {
                logger.error("Error executing action: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Gets the <code>ExecutorService</code> to be used for timeout-aware
     * actions.
     * <p>
     * If no <code>ExecutorService</code> has been defined a default
     * of <code>Executors.newCachedThreadPool()</code> is used during
     * {@link CompositeComponentUnavailableAction#init()}.
     *
     * @return the defined or default <code>ExecutorService</code>
     */
    protected ExecutorService getExecutorService()
    {
        return executorService;
    }

    protected void execute(final ComponentUnavailableAction action, final ComponentUnavailableException e) throws Throwable
    {
        FutureTask<Void> task = null;
        try
        {
            task = new FutureTask<Void>(new Runnable()
            {
                @Override
                public void run()
                {
                    action.execute(e);
                }
            }, null);

            getExecutorService().execute(task);
            task.get(actionTimeoutMs, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e1)
        {
            task.cancel(true);
            throw e;
        }
        catch (InterruptedException e1)
        {
            // We were asked to stop
            task.cancel(true);
        }
        catch (ExecutionException e1)
        {
            // Unwrap our cause and throw that
            Throwable cause = e.getCause();
            throw cause;
        }
    }

}
