/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.BooleanConverter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ObjectUtil {

    private ObjectUtil() {
        // no instantiation.
    }

    protected static String getClassSignature(String className) {
        if(StringUtil.isEmpty(className)) {
            throw new IllegalArgumentException();
        }
        if(className.endsWith("[]") == false) {
            return className;
        }
        StringBuffer buffer = new StringBuffer();
        while(className.endsWith("[]")) {
            buffer.append("[");
            className = className.substring(0, className.length() - 2);
        }
        if("short".equals(className)) {
            buffer.append("S");
        } else if("int".equals(className)) {
            buffer.append("I");
        } else if("long".equals(className)) {
            buffer.append("J");
        } else if("float".equals(className)) {
            buffer.append("F");
        } else if("double".equals(className)) {
            buffer.append("D");
        } else if("byte".equals(className)) {
            buffer.append("B");
        } else if("char".equals(className)) {
            buffer.append("C");
        } else if("boolean".equals(className)) {
            buffer.append("Z");
        } else if("void".equals(className)) {
            throw new IllegalArgumentException();
        } else {
            buffer.append("L").append(className).append(";");
        }
        return buffer.toString();
    }

    protected static Class loadPrimitiveClass(String className) {
        if(StringUtil.isEmpty(className)) {
            throw new IllegalArgumentException();
        }
        if("short".equals(className)) {
            return Short.TYPE;
        } else if("int".equals(className)) {
            return Integer.TYPE;
        } else if("long".equals(className)) {
            return Long.TYPE;
        } else if("float".equals(className)) {
            return Float.TYPE;
        } else if("double".equals(className)) {
            return Double.TYPE;
        } else if("byte".equals(className)) {
            return Byte.TYPE;
        } else if("char".equals(className)) {
            return Character.TYPE;
        } else if("boolean".equals(className)) {
            return Boolean.TYPE;
        } else if("void".equals(className)) {
            return Void.TYPE;
        }
        return null;
    }

    public static Class loadClass(String className) {
        if(StringUtil.isEmpty(className)) {
            throw new IllegalArgumentException();
        }
        Class primitive = loadPrimitiveClass(className);
        if(primitive != null) {
            return primitive;
        }
        className = getClassSignature(className);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class loadClass(String className, Class expectedClass) {
        if(expectedClass == null) {
            throw new IllegalArgumentException();
        }
        Class clazz = loadClass(className);
        if(expectedClass.isAssignableFrom(clazz)) {
            return clazz;
        }
        throw new IllegalClassTypeException(expectedClass, clazz);
    }

    public static Object newInstance(Class clazz) {
        if(clazz == null) {
            throw new IllegalArgumentException();
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor getConstructor(
            Class clazz, Class[] argTypes) {
        if(clazz == null || argTypes == null) {
            throw new IllegalArgumentException();
        }
        try {
            return clazz.getConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Object newInstance(
            Constructor constructor, Object[] argValues) {
        if(constructor == null || argValues == null ||
                constructor.getParameterTypes().length != argValues.length) {
            throw new IllegalArgumentException();
        }
        try {
            return constructor.newInstance(argValues);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasProperty(
            Class beanClass, String propertyName) {
        PropertyDescriptor[] descriptors =
            PropertyUtils.getPropertyDescriptors(beanClass);
        for(int i = 0; i < descriptors.length; i++) {
            if(descriptors[i].getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    public static Class getPropertyClass(
            Class beanClass, String propertyName) {
        PropertyDescriptor[] descriptors =
            PropertyUtils.getPropertyDescriptors(beanClass);
        for(int i = 0; i < descriptors.length; i++) {
            if(descriptors[i].getName().equals(propertyName)) {
                return descriptors[i].getPropertyType();
            }
        }
        return null;
    }

    public static Class getPropertyClass(
            Object bean, String propertyName) {
        try {
            return PropertyUtils.getPropertyType(bean, propertyName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object convert(Class expectedClass, Object value) {
        Converter converter = ConvertUtils.lookup(expectedClass);
        if(converter != null) {
            return converter.convert(expectedClass, value);
        }
        return value;
    }

    public static void setProperty(
            Object bean, String propertyName, Object value) {
        try {
            Class propertyClass = getPropertyClass(bean, propertyName);
            if(propertyClass == null) {
                throw new NoSuchPropertyException(
                        bean.getClass(), propertyName);
            }
            value = convert(propertyClass, value);
            PropertyUtils.setProperty(bean, propertyName, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getProperty(Object bean, String propertyName) {
        try {
            return PropertyUtils.getProperty(bean, propertyName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invoke(Object bean,
            String methodName, Object[] args, Class[] argClasses) {
        try {
            return MethodUtils.invokeMethod(
                    bean, methodName, args, argClasses);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean booleanValue(
            Object obj, boolean defaultValue) {
        Object def = defaultValue ? Boolean.TRUE : Boolean.FALSE;
        BooleanConverter converter = new BooleanConverter(def);
        return ((Boolean)converter.convert(null, obj)).booleanValue();
    }

    public static Number numberValue(Object obj, Number defaultValue) {
        if (obj instanceof Number) {
            return (Number) obj;
        } else if (obj instanceof String) {
            String str = (String) obj;
            try {
                if (str.indexOf('.') != -1) {
                    return new BigDecimal(str);
                }
                return new BigInteger(str);
            } catch (NumberFormatException ignore) {
                // 
            }
        }

        if (defaultValue != null) {
            return defaultValue;
        }

        throw new IllegalArgumentException(
                "argument type mismatch: " + obj.getClass().getName());
    }

}
