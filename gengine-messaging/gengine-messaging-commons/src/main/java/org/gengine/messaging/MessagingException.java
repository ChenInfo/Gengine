package org.gengine.messaging;

import org.gengine.error.GengineRuntimeException;

/**
 * Exception thrown when a message is unable to be processed.
 *
 */
public class MessagingException extends GengineRuntimeException
{
    private static final long serialVersionUID = 8192266871339806688L;

    public MessagingException(String message)
    {
        super(message);
    }

    public MessagingException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
