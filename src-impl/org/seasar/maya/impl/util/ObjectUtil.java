/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.BooleanConverter;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ObjectUtil {

    private ObjectUtil() {
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

    public static Class loadClass(String className, Class expectedType) {
        if(expectedType == null) {
            throw new IllegalArgumentException();
        }
        Class clazz = loadClass(className);
        if(expectedType.isAssignableFrom(clazz)) {
            return clazz;
        }
        throw new IllegalTypeException(expectedType, clazz);
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

    public static Class getPropertyType(Class beanClass, String propertyName) {
        PropertyDescriptor[] descs = PropertyUtils.getPropertyDescriptors(beanClass);
        for(int i = 0; i < descs.length; i++) {
            if(descs[i].getName().equals(propertyName)) {
                return descs[i].getPropertyType();
            }
        }
        return null;
    }

    public static Class getPropertyType(Object bean, String propertyName) {
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

    public static Object convert(Class expectedType, Object value) {
        Converter converter = ConvertUtils.lookup(expectedType);
        if(converter != null) {
            return converter.convert(expectedType, value);
        }
        return value;
    }

    public static void setProperty(Object bean, String propertyName, Object value) {
        try {
            Class propertyType = getPropertyType(bean, propertyName);
            if(propertyType == null) {
                throw new NoSuchPropertyException(bean.getClass(), propertyName);
            }
            value = convert(propertyType, value);
            PropertyUtils.setProperty(bean, propertyName, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean booleanValue(Object obj, boolean defaultValue) {
        Object def = defaultValue ? Boolean.TRUE : Boolean.FALSE;
        BooleanConverter converter = new BooleanConverter(def);
        return ((Boolean)converter.convert(null, obj)).booleanValue();
    }

}
