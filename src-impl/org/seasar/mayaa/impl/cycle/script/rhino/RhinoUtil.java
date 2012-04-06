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
package org.seasar.mayaa.impl.cycle.script.rhino;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaAdapter;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * Rhinoを利用する際に使うさまざまなメソッドを集めたユーティリティクラス。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Koji Suga (Gluegent Inc.)
 */
public class RhinoUtil {

    private static final Log LOG = LogFactory.getLog(RhinoUtil.class);

    /** 引数無しメソッド取得に使う引数 */
    private static final Class[] VOID_ARGS_CLASS = new Class[0];

    private RhinoUtil() {
        // no instantiation.
    }

    /**
     * カレントスレッドに関連付いたContextを取得します。
     * 同時にScriptEnvironmentからWrapFactoryを取得してContextにセットします。
     * Rhinoスクリプト実行前に、Context.enter()の代わりに利用します。
     * これを実行したあとは、try {} finally {} を使って Context.exit() を
     * 必ず実行する必要があります。
     *
     * @return カレントスレッドに関連付いたContext
     */
    public static Context enter() {
        Context cx = Context.enter();
        WrapFactory factory = ScriptEnvironmentImpl.getWrapFactory();
        if (factory != null) {
            cx.setWrapFactory(factory);
        }
        return cx;
    }

    /**
     * 現在位置のスコープを取得します。
     *
     * @return 現在位置のスコープ
     */
    public static Scriptable getScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrs = cycle.getPageScope();
        if (attrs instanceof PageAttributeScope) {
            attrs = (AttributeScope) attrs.getAttribute(
                    PageAttributeScope.KEY_CURRENT);
        }
        if (attrs instanceof Scriptable) {
            return (Scriptable) attrs;
        }
        throw new IllegalStateException("script scope does not get");
    }

    /**
     * Rhinoスクリプトの実行結果をJavaのオブジェクトに変換して返します。
     * 特殊な変換をするのは3種類。
     * <ul><li>booleanの場合、JavaScriptの仕様で「undefinedでない=true」
     * という判定になるのを防ぐ</li>
     * <li>戻り値の型がvoidの場合、実行結果がundefinedの場合にnullを返す</li>
     * <li>戻り値の型が数値の場合、元の型を維持するようにする (RhinoはDoubleにしてしまう)</li>
     * </ul>
     *
     * @param cx 現在スレッドのコンテキスト
     * @param expectedClass 戻り値の型
     * @param jsRet Rhinoスクリプト実行結果
     * @return jsRetをJavaで利用するために変換したオブジェクト
     */
    public static Object convertResult(
            Context cx, Class expectedClass, Object jsRet) {
        Object ret;
        if (expectedClass.equals(Boolean.TYPE)) {
            // workaround to ECMA1.3
            ret = JavaAdapter.convertResult(jsRet, Object.class);
        } else if (expectedClass == Void.class
                || expectedClass == void.class
                || jsRet == Undefined.instance) {
            ret = null;
        } else {
            if (isNumber(expectedClass, jsRet)) {
                ret = jsRet;
            } else {
                ret = JavaAdapter.convertResult(jsRet, expectedClass);
            }
        }

        return ret;
    }

    /**
     * Rhinoスクリプト実行中に発生した例外を、wrapされていない元の例外にして
     * 再度throwします。もしwrapされていた例外がRuntimeExceptionでない場合、
     * RuntimeExceptionでwrapしてthrowします。
     *
     * @param e Rhinoスクリプト中で発生した例外
     */
    public static void removeWrappedException(WrappedException e) {
        Throwable t = e.getWrappedException();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        throw new RuntimeException(t);
    }

    /**
     * Rhinoスクリプトの実行結果が数値の場合、無変換で返すかどうかを判定します。
     * 戻り値の型がNumber, Objectの場合、および実行結果の型と同じ場合に無変換で
     * 返すと判定します。
     *
     * @param expectedClass 戻り値の型
     * @param jsRet Rhinoスクリプトの実行結果
     * @return 無変換で返せる場合はtrue
     */
    public static boolean isNumber(Class expectedClass, Object jsRet) {
        if (jsRet != null && expectedClass != null) {
            Class originalClass = jsRet.getClass();
            if (Number.class.isAssignableFrom(originalClass)) {
                if (Object.class.equals(expectedClass) ||
                        Number.class.equals(expectedClass) ||
                        originalClass.equals(expectedClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * propertyNameから単純にプロパティを取得します。
     * プロパティと見なすのは順にpublicなgetterメソッド、publicなフィールドです。
     * 命名規則はJavaBeanの命名規則に準拠します。
     * ただし、インデックス付きアクセスはサポートしません。
     *
     * @param bean プロパティを取得する対象のオブジェクト
     * @param propertyName プロパティ名
     * @return プロパティの値
     * @throws RuntimeException Reflectionでのアクセス時に発生する
     *      IllegalAccessException, InvocationTargetException, NoSuchMethodException
     *      をcauseとするRuntimeException
     */
    public static Object getSimpleProperty(Object bean, String propertyName) {
        Throwable noSuchMethod;
        try {
            return getWithGetterMethod(bean, propertyName);
        } catch (NoSuchMethodException e) {
            // try field
            noSuchMethod = e;
        }
        Object result = getFromPublicField(bean, propertyName);
        if (result != null) {
            return result;
        }

        throw new RuntimeException(noSuchMethod);
    }

    /**
     * プロパティ名の先頭を大文字に変換します。
     *
     * @param propertyName プロパティ名
     * @return 先頭を大文字にした名前
     */
    protected static String capitalizePropertyName(String propertyName) {
        if (propertyName.length() == 0) {
            return propertyName;
        }

        char chars[] = propertyName.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * getterメソッドを利用して値を取得します。
     * JavaBeanの命名規則に準拠します。
     *
     * @param bean 対象のオブジェクト
     * @param propertyName プロパティ名
     * @return プロパティの値。メソッドにアクセスできない場合はnull
     * @throws NoSuchMethodException getterメソッドが見つからない場合
     */
    protected static Object getWithGetterMethod(Object bean, String propertyName)
            throws NoSuchMethodException {
        Class beanClass = bean.getClass();
        String baseName = capitalizePropertyName(propertyName);
        Method getter = null;
        try {
            getter = beanClass.getMethod("get" + baseName, VOID_ARGS_CLASS);
        } catch (NoSuchMethodException ignore) {
            // try boolean
        }
        if (getter == null) {
            try {
                // TODO Methodキャッシュ
                Method booleanGetter =
                    beanClass.getMethod("is" + baseName, VOID_ARGS_CLASS);
                if (booleanGetter != null) {
                    Class returnType = booleanGetter.getReturnType();
                    if (returnType.equals(Boolean.class)
                            || returnType.equals(Boolean.TYPE)) {
                        getter = booleanGetter;
                    }
                }
            } catch (NoSuchMethodException ignore) {
                // throw new exception for "getXxx"
            }
        }
        if (getter == null || Modifier.isPublic(getter.getModifiers()) == false) {
            throw new NoSuchMethodException(
                    beanClass.toString() + ".get" + baseName + "()");
        }

        if (getter.isAccessible() == false) {
            getter.setAccessible(true);
        }
        try {
            return getter.invoke(bean, null);
        } catch (IllegalAccessException e) {
            LOG.debug(StringUtil.getMessage(RhinoUtil.class, 1,
                    propertyName, beanClass.getName()));
        } catch (InvocationTargetException e) {
            LOG.debug(StringUtil.getMessage(RhinoUtil.class, 1,
                    propertyName, beanClass.getName()));
        }
        return null;
    }

    /**
     * publicなフィールドからプロパティを取得します。
     *
     * @param bean 対象オブジェクト
     * @param propertyName プロパティ名
     * @return フィールドの値。フィールドがなければnull
     */
    protected static Object getFromPublicField(Object bean, String propertyName) {
        try {
            // TODO Fieldキャッシュ
            Class beanClass = bean.getClass();
            Field field = beanClass.getField(propertyName);
            if (Modifier.isPublic(field.getModifiers())) {
                if (field.isAccessible() == false) {
                    field.setAccessible(true);
                }
                return field.get(bean);
            }
        } catch (SecurityException ignore) {
            // no-op
        } catch (NoSuchFieldException ignore) {
            // no-op
        } catch (IllegalArgumentException ignore) {
            // no-op
        } catch (IllegalAccessException eignore) {
            // no-op
        }

        return null;
    }

    /**
     * Rhinoのバージョンを取得する。
     * @return Rhinoのバージョン
     */
    public static String getRhinoVersion() {
    	return Context.getCurrentContext().getImplementationVersion();
    }

    /*
    public static class NativeEmpty extends NativeJavaObject {

        private static final long serialVersionUID = 7282176381199691056L;

        public static final NativeEmpty instance = new NativeEmpty();

        private NativeEmpty() {
            // singleton
        }

        public String getClassName() {
            return "undefined";
        }

        public String toString() {
            return "";
        }
    }
    */

}
