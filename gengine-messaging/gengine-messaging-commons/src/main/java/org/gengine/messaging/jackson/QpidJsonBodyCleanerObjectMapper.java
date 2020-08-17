package org.gengine.messaging.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Extension of ObjectMapper which cleans erroneous characters apparently
 * added by the Qpid library before the start of a JSON object.
 */
public class QpidJsonBodyCleanerObjectMapper extends ObjectMapper
{
    private static final long serialVersionUID = 2568701685293341501L;

    private static final String DEFAULT_ENCODING = "utf8";

    public <T> T readValue(InputStream inputStream, Class<T> valueType) throws JsonParseException, JsonMappingException, IOException
    {
        try
        {
            // Try to unmarshal normally
            if (inputStream.markSupported())
            {
                inputStream.mark(1024 * 512);
            }
            return super.readValue(inputStream, valueType);
        }
        catch (JsonParseException e)
        {
            if (!inputStream.markSupported())
            {
                // We can't reset this stream, bail out
                throw e;
            }
            // Reset the stream
            inputStream.reset();
        }
        // Clean the message body and try again
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, DEFAULT_ENCODING);
        String content = writer.toString();
        content = content.substring(content.indexOf("{"), content.length());
        return readValue(content, valueType);
    }
}
