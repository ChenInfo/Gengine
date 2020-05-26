package org.gengine.health.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.gengine.health.ComponentUnavailableException;
import org.gengine.health.ComponentUnavailableExceptionHandler;

/**
 * Camel Processor and standard bean which provide methods to pass the exception
 * to the specified handler.
 */
public class ExceptionProcessor implements Processor
{
    private ComponentUnavailableExceptionHandler handler;

    public void setHandler(ComponentUnavailableExceptionHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public void process(Exchange exchange) throws Exception
    {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        // Handler can only deal with ComponentUnavailableExceptions
        if (cause instanceof ComponentUnavailableException)
        {
            handler.handle((ComponentUnavailableException) cause);
        }
    }

    public void onReceive(Object body)
    {
        // Handler can only deal with ComponentUnavailableExceptions
        if (body instanceof ComponentUnavailableException)
        {
            handler.handle((ComponentUnavailableException) body);
        }
    }

}
