package org.gengine.messaging.camel.dataformat;

public class SimplePojo
{
    public enum EnumValue { VALUE_1, VALUE_2 }

    private String field1;
    private Integer field2;
    private EnumValue field3;

    public SimplePojo()
    {
    }

    public SimplePojo(String field1, Integer field2, EnumValue field3)
    {
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
    }

    public String getField1()
    {
        return field1;
    }
    public void setField1(String field1)
    {
        this.field1 = field1;
    }
    public Integer getField2()
    {
        return field2;
    }
    public void setField2(Integer field2)
    {
        this.field2 = field2;
    }
    public EnumValue getField3()
    {
        return field3;
    }
    public void setField3(EnumValue field3)
    {
        this.field3 = field3;
    }

}
