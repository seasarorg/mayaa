/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ObjectUtil {

    private static final Log LOG = LogFactory.getLog(ObjectUtil.class);

    private ObjectUtil() {
        // no instantiation.
    }

    protected static String getClassSignature(String className) {
        if (StringUtil.isEmpty(className)) {
            throw new IllegalArgumentException();
        }
        if (className.endsWith("[]") == false) {
            return className;
        }
        StringBuffer buffer = new StringBuffer();
        while (className.endsWith("[]")) {
            buffer.append("[");
            className = className.substring(0, className.length() - 2);
        }
        if ("short".equals(className)) {
            buffer.append("S");
        } else if ("int".equals(className)) {
            buffer.append("I");
        } else if ("long".equals(className)) {
            buffer.append("J");
        } else if ("float".equals(className)) {
            buffer.append("F");
        } else if ("double".equals(className)) {
            buffer.append("D");
        } else if ("byte".equals(className)) {
            buffer.append("B");
        } else if ("char".equals(className)) {
            buffer.append("C");
        } else if ("boolean".equals(className)) {
            buffer.append("Z");
        } else if ("void".equals(className)) {
            throw new IllegalArgumentException();
        } else {
            buffer.append("L").append(className).append(";");
        }
        return buffer.toString();
    }

    protected static Class loadPrimitiveClass(String className) {
        if (StringUtil.isEmpty(className)) {
            throw new IllegalArgumentException();
        }
        if ("short".equals(className)) {
            return Short.TYPE;
        } else if ("int".equals(className)) {
            return Integer.TYPE;
        } else if ("long".equals(className)) {
            return Long.TYPE;
        } else if ("float".equals(className)) {
            return Float.TYPE;
        } else if ("double".equals(className)) {
            return Double.TYPE;
        } else if ("byte".equals(className)) {
            return Byte.TYPE;
        } else if ("char".equals(className)) {
            return Character.TYPE;
        } else if ("boolean".equals(className)) {
            return Boolean.TYPE;
        } else if ("void".equals(className)) {
            return Void.TYPE;
        }
        return null;
    }

    public static Class loadClass(String className) {
        if (StringUtil.isEmpty(className)) {
            throw new IllegalArgumentException();
        }
        Class primitive = loadPrimitiveClass(className);
        if (primitive != null) {
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
        if (expectedClass == null) {
            throw new IllegalArgumentException();
        }
        Class clazz = loadClass(className);
        if (expectedClass.isAssignableFrom(clazz)) {
            return clazz;
        }
        throw new IllegalClassTypeException(expectedClass, clazz);
    }

    public static Object newInstance(Class clazz) {
        if (clazz == null) {
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

    public static Constructor getConstructor(Class clazz, Class[] argTypes) {
        if (clazz == null || argTypes == null) {
            throw new IllegalArgumentException();
        }
        try {
            return clazz.getConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Object newInstance(Constructor constructor, Object[] argValues) {
        if (constructor == null || argValues == null
                || constructor.getParameterTypes().length != argValues.length) {
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

    /**
     * 指定したクラスのプロパティ名の配列を取得します。
     *
     * @param beanClass プロパティ名の配列を取得するクラス
     * @return プロパティ名の配列
     */
    public static String[] getPropertyNames(Class beanClass) {
        PropertyDescriptor[] descriptors =
            PropertyUtils.getPropertyDescriptors(beanClass);
        String[] result = new String[descriptors.length];
        for (int i = 0; i < descriptors.length; i++) {
            result[i] = descriptors[i].getName();
        }
        return result;
    }

    /**
     * 指定したクラスがプロパティを持つかどうかを返します。
     *
     * @param beanClass 対象のクラス
     * @param propertyName 判定するプロパティ名
     * @return プロパティを持つならtrue
     */
    public static boolean hasProperty(Class beanClass, String propertyName) {
        PropertyDescriptor[] descriptors =
            PropertyUtils.getPropertyDescriptors(beanClass);
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * プロパティの型を取得します。
     *
     * @param beanClass クラス情報
     * @param propertyName プロパティ名
     * @return beanClassあるプロパティの型
     */
    public static Class getPropertyClass(Class beanClass, String propertyName) {
        PropertyDescriptor[] descriptors =
            PropertyUtils.getPropertyDescriptors(beanClass);
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals(propertyName)) {
                return descriptors[i].getPropertyType();
            }
        }
        return null;
    }

    /**
     * プロパティの型を取得します。
     *
     * @param bean クラス情報を取得するためのオブジェクト
     * @param propertyName プロパティ名
     * @return beanのクラスにあるプロパティの型
     */
    public static Class getPropertyClass(Object bean, String propertyName) {
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

    /**
     * オブジェクトを指定したクラスのインスタンスに変換して返します。
     * 変換の必要が無い場合はそのまま返します。
     *
     * @param expectedClass 戻り型として期待するクラス
     * @param value 変換する値
     * @return 変換後の値 (expectedClassのインスタンス)
     */
    public static Object convert(Class expectedClass, Object value) {
        if (Object.class.equals(expectedClass) ||
                (value != null && expectedClass.isAssignableFrom(value.getClass()))) {
            return value;
        }
        if (String.class.equals(expectedClass)) {
            return (value != null) ? value.toString() : null;
        }
        if (Boolean.class.equals(expectedClass) || Boolean.TYPE.equals(expectedClass)) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.valueOf(ObjectUtil.booleanValue(value, false));
        }
        Converter converter = ConvertUtils.lookup(expectedClass);
        if (converter != null) {
        	if (value != null) {
        		return converter.convert(expectedClass, value);
        	}
        	if (expectedClass.isPrimitive() || Number.class.isAssignableFrom(expectedClass)) {
        		if (BigInteger.class.isAssignableFrom(expectedClass)) {
        			return BigInteger.ZERO;
        		} else if (BigDecimal.class.isAssignableFrom(expectedClass)) {
        			return BigDecimal.valueOf(0);
        		}
        		return converter.convert(expectedClass, value);
        	}
        }
        return value;
    }

    /**
     * プロパティの値をセットします。
     *
     * @param bean 対象となるオブジェクト
     * @param propertyName 値をセットするプロパティ名
     * @param value セットする値
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        try {
            Class propertyClass = getPropertyClass(bean, propertyName);
            if (propertyClass == null) {
                throw new NoSuchPropertyException(bean.getClass(), propertyName);
            }
            value = convert(propertyClass, value);
            PropertyUtils.setProperty(bean, propertyName, value);
        } catch (IllegalAccessException e) {
            LOG.warn(StringUtil.getMessage(ObjectUtil.class, 3,
                    propertyName, getClassName(bean), getClassName(value)), e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            LOG.warn(StringUtil.getMessage(ObjectUtil.class, 3,
                    propertyName, getClassName(bean), getClassName(value)), e);
            throw new RuntimeException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            LOG.warn(StringUtil.getMessage(ObjectUtil.class, 3,
                    propertyName, getClassName(bean), getClassName(value)), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * プロパティの値を取得します。
     *
     * @param bean 対象となるオブジェクト
     * @param propertyName 値を取得するプロパティ名
     * @return プロパティの値
     */
    public static Object getProperty(Object bean, String propertyName) {
        try {
            return PropertyUtils.getProperty(bean, propertyName);
        } catch (IllegalAccessException e) {
            LOG.warn(StringUtil.getMessage(ObjectUtil.class, 2,
                    propertyName, getClassName(bean)), e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            LOG.warn(StringUtil.getMessage(ObjectUtil.class, 2,
                    propertyName, getClassName(bean)), e);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            LOG.warn(StringUtil.getMessage(ObjectUtil.class, 2,
                    propertyName, getClassName(bean)), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * beanのmethodNameメソッドを実行します。
     *
     * @param bean メソッドを実行するオブジェクト
     * @param methodName メソッド名
     * @param args メソッドに渡す引数
     * @param argClasses 引数の型
     * @return メソッドの実行結果
     */
    public static Object invoke(
            Object bean, String methodName, Object[] args, Class[] argClasses) {
        try {
            return MethodUtils.invokeMethod(bean, methodName, args, argClasses);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getClassName(Object obj) {
        return (obj != null) ? obj.getClass().getName() : "null";
    }

    /**
     * objをbooleanに変換して返します。
     * trueになる条件は、objがBoolean.TRUEであるか、またはtoString()したものが
     * "true","yes","y","on","1"のいずれかであること(大文字小文字区別なし)です。
     * falseになる条件は、objがBoolean.FALSEであるか、またはtoString()したものが
     * "false","no","n","off","0"のいずれかであること(大文字小文字区別なし)です。
     * 上記に該当しない場合、defaultValueを返します。
     *
     * @param obj 変換元のオブジェクト
     * @param defaultValue 変換できない場合の値
     * @return objをbooleanに変換した値、またはdefaultValue
     */
    public static boolean booleanValue(Object obj, boolean defaultValue) {
        if (obj != null) {
            if (obj instanceof Boolean) {
                return ((Boolean) obj).booleanValue();
            }

            String stringValue = obj.toString().toLowerCase();
            if (isTrueString(stringValue)) {
                return true;
            } else if (isFalseString(stringValue)) {
                return false;
            }
        }
        return defaultValue;
    }

    private static boolean isTrueString(String lowerCase) {
        return lowerCase.equals("true") || lowerCase.equals("yes")
                || lowerCase.equals("y") || lowerCase.equals("on")
                || lowerCase.equals("1");
    }

    private static boolean isFalseString(String lowerCase) {
        return lowerCase.equals("false") || lowerCase.equals("no")
                || lowerCase.equals("n") || lowerCase.equals("off")
                || lowerCase.equals("0");
    }

    /**
     * objをBooleanとして解釈できるかを返します。
     * objがBooleanのインスタンスであるか、あるいはBooleanとして解釈できる
     * 文字列になる場合にtrueを返します。(大文字小文字の区別をしません)
     * Booleanとして解釈できる文字列は"true", "yes", "y", "on", "1",
     * "false", "no", "n", "off", "0"です。
     *
     * @param obj Booleanとして解釈できるか判定するオブジェクト
     * @return Booleanとして解釈できるならtrue
     */
    public static boolean canBooleanConvert(Object obj) {
        if (obj != null) {
            if (obj instanceof Boolean) {
                return true;
            }
            String stringValue = obj.toString().toLowerCase();
            return (isTrueString(stringValue) || isFalseString(stringValue));
        }
        return false;
    }

    /**
     * objをNumberとして取得します。
     * objがNumberの場合はそのまま、Stringの場合はBigDecimalまたはBigIntegerに
     * 変換して返します。
     * objがNumberでも数値として解釈できるStringでもない場合はdefaultValueを
     * 返します。
     *
     * @param obj Numberとして取得するオブジェクト
     * @param defaultValue objがNumberにできない場合に返す値
     * @return objをNumberにしたもの、またはdefaultValue
     * @throws IllegalArgumentException objをNumberにできず、かつdefaultValueが
     * nullの場合
     */
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
                StringUtil.getMessage(ObjectUtil.class, 1, String.valueOf(obj)));
    }

    /**
     * パッケージ名を含まないクラス名を取得します。
     *
     * @param clazz 名称を取得するクラス
     * @return clazzのパッケージを含まない名前
     */
    public static String getSimpleClassName(Class clazz) {
        String className = clazz.getName();
        int pos = className.lastIndexOf('.');
        if (pos != -1) {
            className = className.substring(pos + 1);
        }
        return className;
    }

    /**
     * 配列の浅いコピーを作成して返します。
     *
     * @param src 元となる配列
     * @param componentType 配列の要素の型
     * @return srcの浅いコピー
     * @throws NullPointerException
     */
    public static Object[] arraycopy(Object[] src, Class componentType) {
        Object copy = Array.newInstance(componentType, src.length);
        System.arraycopy(src, 0, copy, 0, src.length);
        return (Object[]) copy;
    }

    /**
     * キャッシュを解放します。
     */
    public static void clearCaches() {
        PropertyUtils.clearDescriptors();
    }

}
