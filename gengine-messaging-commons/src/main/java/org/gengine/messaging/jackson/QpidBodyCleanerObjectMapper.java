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
 * added by the Qpid library.
 */
public class QpidBodyCleanerObjectMapper extends ObjectMapper
{
    private static final long serialVersionUID = 2568701685293341501L;

    private static final String DEFAULT_ENCODING = "utf8";

    private boolean cleanMessageBody;

    public void setCleanMessageBody(boolean cleanMessageBody)
    {
        this.cleanMessageBody = cleanMessageBody;
    }

    public <T> T readValue(InputStream inputStream, Class<T> valueType) throws JsonParseException, JsonMappingException, IOException
    {
        if (!cleanMessageBody)
        {
            return super.readValue(inputStream, valueType);
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, DEFAULT_ENCODING);
        String content = writer.toString();
        content = content.substring(content.indexOf("{"), content.length());
        return readValue(content, valueType);
    }
}
