package org.gengine.health;

/**
 * Thrown when a dependent component such as a database or other service is unavailable.
 *
 */
public class ComponentUnavailableException extends RuntimeException
{

    private static final long serialVersionUID = 4814912374901612569L;

    public ComponentUnavailableException()
    {
    }

    public ComponentUnavailableException(String message)
    {
        super(message);
    }

    public ComponentUnavailableException(Throwable cause)
    {
        super(cause);
    }

    public ComponentUnavailableException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ComponentUnavailableException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public Integer getExitStatusCode()
    {
        return 1;
    }

}
