package org.gengine.health;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Determines the {@link ComponentUnavailableAction} to execute when a
 * {@link ComponentUnavailableException} is thrown
 *
 */
public class ComponentUnavailableExceptionHandler
{
    private static final Log logger = LogFactory.getLog(ComponentUnavailableExceptionHandler.class);

    private Map<String, ComponentUnavailableAction> policies;

    /**
     * A map of ComponentUnavailableException canonical class names to the
     * ComponentUnavailableAction that should be executed.
     *
     * @param policies
     */
    public void setPolicies(Map<String, ComponentUnavailableAction> policies)
    {
        this.policies = policies;
    }

    /**
     * Determines the ComponentUnavailableAction to execute based on the given exception
     * and executes it.
     *
     * @param e
     */
    public void handle(Throwable e)
    {
        if (e == null)
        {
            return;
        }
        String actionKey = e.getClass().getCanonicalName();
        if (policies == null || !policies.containsKey(actionKey))
        {
            logger.error("Received " + actionKey +
                    " but no corresponding action registered: " + e.getMessage(), e);
        }
        else
        {
            policies.get(actionKey).execute(e);
        }
    }

}
