package org.gengine.error;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicInteger;

import org.gengine.api.StableApi;
import org.gengine.util.I18NUtil;

/**
 * I18n'ed runtime exception thrown by ChenInfo code.
 *
 */
@StableApi
public class GengineRuntimeException extends RuntimeException
{
    /**
     * Serial version UUID
     */
    private static final long serialVersionUID = 3787143176461219632L;

    private String msgId;
    private transient Object[] msgParams = null;

    /**
     * Helper factory method making use of variable argument numbers
     */
    public static GengineRuntimeException create(String msgId, Object ...objects)
    {
        return new GengineRuntimeException(msgId, objects);
    }

    /**
     * Helper factory method making use of variable argument numbers
     */
    public static GengineRuntimeException create(Throwable cause, String msgId, Object ...objects)
    {
        return new GengineRuntimeException(msgId, objects, cause);
    }

    /**
     * Utility to convert a general Throwable to a RuntimeException.  No conversion is done if the
     * throwable is already a <tt>RuntimeException</tt>.
     *
     * @see #create(Throwable, String, Object...)
     */
    public static RuntimeException makeRuntimeException(Throwable e, String msgId, Object ...objects)
    {
        if (e instanceof RuntimeException)
        {
            return (RuntimeException) e;
        }
        // Convert it
        return GengineRuntimeException.create(e, msgId, objects);
    }

    /**
     * Constructor
     *
     * @param msgId     the message id
     */
    public GengineRuntimeException(String msgId)
    {
        super(resolveMessage(msgId, null));
        this.msgId = msgId;
    }

    /**
     * Constructor
     *
     * @param msgId         the message id
     * @param msgParams     the message parameters
     */
    public GengineRuntimeException(String msgId, Object[] msgParams)
    {
        super(resolveMessage(msgId, msgParams));
        this.msgId = msgId;
        this.msgParams = msgParams;
    }

    /**
     * Constructor
     *
     * @param msgId     the message id
     * @param cause     the exception cause
     */
    public GengineRuntimeException(String msgId, Throwable cause)
    {
        super(resolveMessage(msgId, null), cause);
        this.msgId = msgId;
    }

    /**
     * Constructor
     *
     * @param msgId         the message id
     * @param msgParams     the message parameters
     * @param cause         the exception cause
     */
    public GengineRuntimeException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(resolveMessage(msgId, msgParams), cause);
        this.msgId = msgId;
        this.msgParams = msgParams;
    }

    /**
     * @return the msgId
     */
    public String getMsgId()
    {
        return msgId;
    }

    /**
     * @return the msgParams
     */
    public Object[] getMsgParams()
    {
        return msgParams;
    }

    /**
     * Resolves the message id to the localised string.
     * <p>
     * If a localised message can not be found then the message Id is
     * returned.
     *
     * @param messageId     the message Id
     * @param params        message parameters
     * @return              the localised message (or the message id if none found)
     */
    private static String resolveMessage(String messageId, Object[] params)
    {
        String message = I18NUtil.getMessage(messageId, params);
        if (message == null)
        {
            // If a localised string cannot be found then return the messageId
            message = messageId;
        }
        return buildErrorLogNumber(message);
    }

    /**
     * Generate an error log number - based on MMDDXXXX - where M is month,
     * D is day and X is an atomic integer count.
     *
     * @param message       Message to prepend the error log number to
     *
     * @return message with error log number prefix
     */
    private static String buildErrorLogNumber(String message)
    {
        // ensure message is not null
        if (message == null)
        {
            message= "";
        }

        Date todayDate = new Date();
        StringBuilder buf = new StringBuilder(message.length() + 10);
        Calendar today = new GregorianCalendar();
        today.setTime(todayDate);
        padInt(buf, today.get(Calendar.MONTH), 2);
        padInt(buf, today.get(Calendar.DAY_OF_MONTH), 2);
        padInt(buf, errorCounter.getAndIncrement(), 4);
        buf.append(' ');
        buf.append(message);
        return buf.toString();
    }

    /**
     * Helper to zero pad a number to specified length
     */
    private static void padInt(StringBuilder buffer, int value, int length)
    {
        String strValue = Integer.toString(value);
        for (int i = length - strValue.length(); i > 0; i--)
        {
            buffer.append('0');
        }
        buffer.append(strValue);
    }

    private static AtomicInteger errorCounter = new AtomicInteger();

    /**
     * Get the root cause.
     */
    public Throwable getRootCause()
    {
        Throwable cause = this;
        for (Throwable tmp = this; tmp != null ; tmp = cause.getCause())
        {
            cause = tmp;
        }
        return cause;
    }
}
