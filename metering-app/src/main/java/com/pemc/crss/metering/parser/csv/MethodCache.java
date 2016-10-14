package com.pemc.crss.metering.parser.csv;

import java.lang.reflect.Method;

import org.supercsv.exception.SuperCsvReflectionException;
import org.supercsv.util.ReflectionUtils;
import org.supercsv.util.ThreeDHashMap;
import org.supercsv.util.TwoDHashMap;

/**
 * This class cache's method lookups. Hence first time it introspects the instance's class, while subsequent method
 * lookups are super fast.
 */
public class MethodCache {

    /**
     * A cache of setter methods. The three keys are the class the setter is being invoked on, the parameter type of the
     * setter, and the variable name. The value is the setter method.
     */
    private final ThreeDHashMap<Class<?>, Class<?>, String, Method> setMethodsCache = new ThreeDHashMap<>();

    /**
     * A cache of getter methods. The two keys are the name of the class the getter is being invoked on, and the
     * variable name. The value is the getter method.
     */
    private final TwoDHashMap<String, String, Method> getCache = new TwoDHashMap<>();

    /**
     * Returns the getter method for field on an object.
     *
     * @param object
     *            the object
     * @param fieldName
     *            the field name
     * @return the getter associated with the field on the object
     * @throws NullPointerException
     *             if object or fieldName is null
     * @throws SuperCsvReflectionException
     *             if the getter doesn't exist or is not visible
     */
    public Method getGetMethod(final Object object, final String fieldName) {
        if( object == null ) {
            throw new NullPointerException("object should not be null");
        } else if( fieldName == null ) {
            throw new NullPointerException("fieldName should not be null");
        }

        Method method = getCache.get(object.getClass().getName(), fieldName);
        if( method == null ) {
            method = ReflectionUtils.findGetter(object, fieldName);
            getCache.set(object.getClass().getName(), fieldName, method);
        }
        return method;
    }

    /**
     * Returns the setter method for the field on an object.
     *
     * @param object
     *            the object
     * @param fieldName
     *            the field name
     * @param argumentType
     *            the type to be passed to the setter
     * @param <T>
     *            the object type
     * @return the setter method associated with the field on the object
     * @throws NullPointerException
     *             if object, fieldName or fieldType is null
     * @throws SuperCsvReflectionException
     *             if the setter doesn't exist or is not visible
     */
    public <T> Method getSetMethod(final Object object, final String fieldName, final Class<?> argumentType) {
        if( object == null ) {
            throw new NullPointerException("object should not be null");
        } else if( fieldName == null ) {
            throw new NullPointerException("fieldName should not be null");
        } else if( argumentType == null ) {
            throw new NullPointerException("argumentType should not be null");
        }

        Method method = setMethodsCache.get(object.getClass(), argumentType, fieldName);
        if( method == null ) {
            method = ReflectionUtils.findSetter(object, fieldName, argumentType);
            setMethodsCache.set(object.getClass(), argumentType, fieldName, method);
        }
        return method;
    }

}
