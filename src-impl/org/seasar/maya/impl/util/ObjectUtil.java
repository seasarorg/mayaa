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

import java.lang.reflect.InvocationTargetException;

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
    
    public static Class loadClass(String className) {
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
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
    
    public static Class getPropertyType(Object obj, String name) {
        try {
            return PropertyUtils.getPropertyType(obj, name);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void setProperty(Object obj, String propertyName, Object value) {
        try {
            Class type = getPropertyType(obj, propertyName);
            Converter converter = ConvertUtils.lookup(type);
            if(converter != null) {
                value = converter.convert(type, value);
            }
            PropertyUtils.setProperty(obj, propertyName, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invoke(Object obj, String methodName, Object[] args) {
        try {
            return MethodUtils.invokeMethod(obj, methodName, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static boolean booleanValue(Object obj, boolean defaultValue) {
        Object def = defaultValue ? Boolean.TRUE : Boolean.FALSE;
        BooleanConverter converter = new BooleanConverter(def);
        return ((Boolean)converter.convert(null, obj)).booleanValue();
    }
    
}
