package org.gengine.content.transform;

public class ContentTransformationException extends Exception
{
    private static final long serialVersionUID = -5334480453136472986L;

    public ContentTransformationException()
    {
    }

    public ContentTransformationException(String message)
    {
        super(message);
    }

    public ContentTransformationException(Throwable cause)
    {
        super(cause);
    }

    public ContentTransformationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ContentTransformationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
