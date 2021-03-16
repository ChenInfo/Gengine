package org.gengine.util;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.gengine.error.GengineRuntimeException;

/**
 * Utility class for common operations on Java beans
 *
 */
public class BeanUtils
{
    public static final String TO_STR_KEY_VAL = ": ";
    public static final String TO_STR_OBJ_START = "{ ";
    public static final String TO_STR_OBJ_END = " }";
    public static final String TO_STR_SET_START = "[ ";
    public static final String TO_STR_SET_END = " ]";
    public static final String TO_STR_DEL = ", ";

    /**
     * Copies fields marked with the {@link CloneField} annotation from the given
     * source to the given target, check that the value is not null if merge is true.
     *
     * @param source
     * @param target
     * @param merge
     */
    public static void copyFields(Object source, Object target, boolean merge)
    {
        if (source == null || target == null || source.getClass() != target.getClass())
        {
            throw new IllegalArgumentException(
                    "source and target arguments must be of the same class");
        }
        try
        {
            for (Method method: source.getClass().getMethods())
            {
                if (method.isAnnotationPresent(CloneField.class))
                {
                    Object value = method.invoke(source);
                    if ((merge && value != null) || !merge)
                    {
                        String setterName = method.getName().replaceFirst("is|get", "set");
                        Method setterMethod = source.getClass().getMethod(
                                setterName, method.getReturnType());
                        setterMethod.invoke(target, value);
                    }
                }
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
        {
            throw new GengineRuntimeException("Could not copy field", e);
        }
    }

    /**
     * Builds the JSON toString of the given object through methods
     * annotated with {@link ToStringProperty}.
     * <p>
     * We can't rely on a framework here because we need full serialization
     * for messaging.
     *
     * @param object
     * @return the toString representation of the object
     */
    public static String toString(Object object)
    {
        if (object == null)
        {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        Method[] methods = object.getClass().getMethods();
        ArrayList<Method> annotatedMethods = new ArrayList<Method>();
        for (int i = 0; i < methods.length; i++)
        {
            Method method = methods[i];
            if (method.isAnnotationPresent(ToStringProperty.class))
            {
                annotatedMethods.add(method);
            }
        }
        for (Iterator<Method> iterator = annotatedMethods.iterator(); iterator.hasNext();)
        {
            Method method = (Method) iterator.next();

            Object value = "*error*";
            try
            {
                value = method.invoke(object);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
            }
            String methodName = Introspector.decapitalize(method.getName().startsWith("get") ?
                    method.getName().replaceFirst("get",  "") : method.getName());

            builder.append("\"").append(methodName).append("\"").append(TO_STR_KEY_VAL);
            if (value instanceof String)
            {
                builder.append("\"").append(value).append("\"");
            }
            else
            {
                builder.append(value);
            }
            if (iterator.hasNext())
            {
                builder.append(TO_STR_DEL);
            }
        }
        return builder.toString();
    }
}
