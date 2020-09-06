package org.gengine.messaging.benchmark;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A simple message with a decent sized payload for benchmarking.
 *
 */
public class BenchmarkMessage
{
    private static int NUM_SECTIONS = 100;
    public static final String DEFAULT_VALUE = getDefaultValue();

    private String id;
    private Long timestamp;
    private String value;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public static BenchmarkMessage createInstance()
    {
        BenchmarkMessage message = new BenchmarkMessage();
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp((new Date()).getTime());
        message.setValue(DEFAULT_VALUE);
        return message;
    }

    private static String getDefaultValue()
    {
        String section = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, "
                + "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
                + "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris "
                + "nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in "
                + "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
                + "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia "
                + "deserunt mollit anim id est laborum.\n\n";
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < NUM_SECTIONS - 1; i++)
        {
            value.append(section);
        }
        return value.toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
            append(id).
            append(timestamp).
            append(value).
            toHashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (that == null)
        {
            return false;
        }
        if (that == this)
        {
            return true;
        }
        if (!(that instanceof BenchmarkMessage))
        {
            return false;
        }

        BenchmarkMessage thatMessage = (BenchmarkMessage) that;
        return new EqualsBuilder().
            append(id, thatMessage.id).
            append(timestamp, thatMessage.timestamp).
            append(value, thatMessage.value).
            isEquals();
    }
}
