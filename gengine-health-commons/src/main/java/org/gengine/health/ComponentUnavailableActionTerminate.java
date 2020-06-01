package org.gengine.health;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ComponentUnavailableAction which terminates the JVM with the specified exit code, not to be used lightly.
 *
 */
public class ComponentUnavailableActionTerminate implements ComponentUnavailableAction
{
    private static final Log logger = LogFactory.getLog(ComponentUnavailableActionTerminate.class);

    private static final Integer DEFAULT_EXIT_STATUS_CODE = new Integer(1);

    @Override
    public void execute(Throwable e)
    {
        logger.fatal("Terminating due to " + e.getClass().getSimpleName() + ": " + e.getMessage());
        Integer statusCode = null;
        if (e instanceof ComponentUnavailableException)
        {
            statusCode = ((ComponentUnavailableException) e).getExitStatusCode();
        }
        if (statusCode == null)
        {
            statusCode = DEFAULT_EXIT_STATUS_CODE;
        }
        System.exit(statusCode);
    }

}
