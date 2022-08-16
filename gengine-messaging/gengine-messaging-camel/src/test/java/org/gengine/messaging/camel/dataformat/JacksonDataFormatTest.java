package org.gengine.messaging.camel.dataformat;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.gengine.messaging.camel.dataformat.SimplePojo.EnumValue;
import org.gengine.messaging.jackson.ObjectMapperFactory;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class JacksonDataFormatTest
{
    private static final String EXPECTED_FIELD1_VALUE = "value1";
    private static final Integer EXPECTED_FIELD2_VALUE = new Integer(2);
    private static final EnumValue EXPECTED_FIELD3_VALUE = EnumValue.VALUE_1;
    private static final String EXPECTED_CLASS_MAP_VALUE1 = "classMapValue1";
    private static final String EXPECTED_STRING_MAP_KEY1 = "stringMapKey1";
    private static final String EXPECTED_STRING_MAP_VALUE1 = "stringMapValue1";

    private JacksonDataFormat dataFormat;
    private String expectedJson;

    @Before
    public void init()
    {
        ObjectMapper mapper = ObjectMapperFactory.createInstance();
        dataFormat = new JacksonDataFormat(mapper, Object.class);

        String expectedClassMapKey1 =
                isClassMapKeySerializedWithPrefix() ? "class java.lang.Long" : "java.lang.Long";

        expectedJson = "{" +
                "\"@class\":\"" + SimplePojo.class.getCanonicalName() + "\"," +
                "\"field1\":\"" + EXPECTED_FIELD1_VALUE + "\"," +
                "\"field2\":" + EXPECTED_FIELD2_VALUE + "," +
                "\"field3\":\"" + EXPECTED_FIELD3_VALUE + "\"," +
                "\"field4\":{\"@class\":\"java.util.HashMap\"," +
                    "\"" + expectedClassMapKey1 + "\":\"" + EXPECTED_CLASS_MAP_VALUE1 + "\"}," +
                "\"field5\":{\"@class\":\"java.util.HashMap\"," +
                "\"" + EXPECTED_STRING_MAP_KEY1 + "\":\"" + EXPECTED_STRING_MAP_VALUE1 + "\"}" +
                "}";
    }

    /**
     * Determines whether or not the Jackson library present will serialize a class Map
     * key with the 'class ' prefix.
     *
     * @return true if a 'class ' prefix will be present in keys
     */
    protected boolean isClassMapKeySerializedWithPrefix()
    {
        StdSerializer<Object> keySerializer =
                new StdKeySerializers.Default(3, Long.class);
        Package keySerializerPackage = keySerializer.getClass().getPackage();
        String stringVersion = keySerializerPackage.getSpecificationVersion();
        DefaultArtifactVersion currentVersion = new DefaultArtifactVersion(stringVersion);
        DefaultArtifactVersion behaviorChangedVersion = new DefaultArtifactVersion("2.5.1");
        return currentVersion.compareTo(behaviorChangedVersion) < 0;
    }

    @Test
    public void testMarshalPojoToJson() throws Exception
    {
        SimplePojo simplePojo = new SimplePojo(
                EXPECTED_FIELD1_VALUE,
                EXPECTED_FIELD2_VALUE,
                EXPECTED_FIELD3_VALUE);

        HashMap<Class<?>, String> classMap = new HashMap<Class<?>, String>();
        classMap.put(Long.class, EXPECTED_CLASS_MAP_VALUE1);
        simplePojo.setField4(classMap);

        HashMap<String, String> stringMap = new HashMap<String, String>();
        stringMap.put(EXPECTED_STRING_MAP_KEY1, EXPECTED_STRING_MAP_VALUE1);
        simplePojo.setField5(stringMap);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        dataFormat.marshal(exchange, simplePojo, os);

        String result = new String(os.toByteArray(), "UTF-8");

        assertEquals(expectedJson, result);
    }

    @Test
    public void testUnmarshalJsonToPojo() throws Exception
    {
        InputStream is = new ByteArrayInputStream(expectedJson.getBytes("UTF-8"));
        SimplePojo simplePojo = (SimplePojo) dataFormat.unmarshal(null, is);
        assertEquals(EXPECTED_FIELD1_VALUE, simplePojo.getField1());
        assertEquals(EXPECTED_FIELD2_VALUE, simplePojo.getField2());
        assertEquals(EXPECTED_FIELD3_VALUE, simplePojo.getField3());
        assertEquals(EXPECTED_CLASS_MAP_VALUE1, simplePojo.getField4().get(Long.class));
        assertEquals(EXPECTED_STRING_MAP_VALUE1, simplePojo.getField5().get(EXPECTED_STRING_MAP_KEY1));
    }

    @Test
    public void testUnmarshalQpidBodyJsonToPojo() throws Exception
    {
        String messageBody = "sfjh09434" + expectedJson;
        InputStream is = new ByteArrayInputStream(messageBody.getBytes("UTF-8"));
        SimplePojo simplePojo = (SimplePojo) dataFormat.unmarshal(null, is);
        assertEquals(EXPECTED_FIELD1_VALUE, simplePojo.getField1());
        assertEquals(EXPECTED_FIELD2_VALUE, simplePojo.getField2());
        assertEquals(EXPECTED_FIELD3_VALUE, simplePojo.getField3());
    }

}
