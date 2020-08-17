package org.gengine.content.hash;

public class ContentHashException extends Exception
{
    private static final long serialVersionUID = -5334480453136472986L;

    public ContentHashException()
    {
    }

    public ContentHashException(String message)
    {
        super(message);
    }

    public ContentHashException(Throwable cause)
    {
        super(cause);
    }

    public ContentHashException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ContentHashException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
