package org.gengine.messaging;

public class MessagingException extends RuntimeException
{
    private static final long serialVersionUID = 8192266871339806688L;

    public MessagingException()
    {
        super();
    }

    public MessagingException(String message)
    {
        super(message);
    }

    public MessagingException(Throwable cause)
    {
        super(cause);
    }

    public MessagingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MessagingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
