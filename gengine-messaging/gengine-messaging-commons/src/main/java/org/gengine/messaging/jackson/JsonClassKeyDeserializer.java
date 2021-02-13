package org.gengine.messaging.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class JsonClassKeyDeserializer extends KeyDeserializer
{

    public static Object deserializeKeyToClass(String key) throws IOException,
            JsonProcessingException
    {
        if (!key.startsWith("class "))
        {
            throw new IllegalArgumentException("Invalid key format");
        }
        String classname = key.replaceFirst("class ", "");
        try
        {
            return JsonClassKeyDeserializer.class.getClassLoader().loadClass(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException,
            JsonProcessingException
    {
        return deserializeKeyToClass(key);
    }

}
