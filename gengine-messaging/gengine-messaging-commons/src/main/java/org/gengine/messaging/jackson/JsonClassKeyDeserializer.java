package org.gengine.messaging.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class JsonClassKeyDeserializer extends KeyDeserializer
{

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException,
            JsonProcessingException
    {
        if (!key.startsWith("class "))
        {
            throw new IllegalArgumentException("Invalid key format");
        }
        String classname = key.replaceFirst("class ", "");
        try
        {
            return this.getClass().getClassLoader().loadClass(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

}
