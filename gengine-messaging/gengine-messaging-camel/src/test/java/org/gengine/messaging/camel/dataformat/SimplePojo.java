package org.gengine.messaging.camel.dataformat;

import java.util.HashMap;
import java.util.Map;

public class SimplePojo {
    public enum EnumValue {
        VALUE_1, VALUE_2
    }

    private String field1;
    private Integer field2;
    private EnumValue field3;
    private Map<Class<?>, String> field4;
    private Map<String, String> field5;

    // Constructor
    public SimplePojo(String field1, Integer field2, EnumValue field3) {
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
    }

    // Getters and setters for field1, field2, and field3
    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public Integer getField2() {
        return field2;
    }

    public void setField2(Integer field2) {
        this.field2 = field2;
    }

    public EnumValue getField3() {
        return field3;
    }

    public void setField3(EnumValue field3) {
        this.field3 = field3;
    }

    // Getters and setters for field4
    public Map<Class<?>, String> getField4() {
        return field4;
    }

    public void setField4(Map<Class<?>, String> field4) {
        this.field4 = field4;
    }

    // Getters and setters for field5
    public Map<String, String> getField5() {
        return field5;
    }

    public void setField5(Map<String, String> field5) {
        this.field5 = field5;
    }
}
