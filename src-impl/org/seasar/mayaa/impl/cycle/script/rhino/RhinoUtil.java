/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaAdapter;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RhinoUtil {

    private RhinoUtil() {
        // no instantiation.
    }

    public static Context enter() {
        Context cx = Context.enter();
        WrapFactory factory = ScriptEnvironmentImpl.getWrapFactory();
        if (factory != null) {
            cx.setWrapFactory(factory);
        }
        return cx;
    }

    public static Scriptable getScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrs = cycle.getPageScope();
        if (attrs instanceof PageAttributeScope) {
            attrs = (AttributeScope)attrs.getAttribute(
                    PageAttributeScope.KEY_CURRENT);
        }
        if (attrs instanceof Scriptable) {
            return (Scriptable) attrs;
        }
        throw new IllegalStateException("script scope does not get");
    }

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

    public static void removeWrappedException(WrappedException e) {
        Throwable t = e.getWrappedException();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        throw new RuntimeException(t);
    }

    public static boolean isNumber(Class expectedClass, Object jsRet) {
        if (jsRet != null && expectedClass != null) {
            Class originalClass = jsRet.getClass();
            if (originalClass.equals(Number.class) ||
                    originalClass.equals(Byte.class) ||
                    originalClass.equals(Short.class) ||
                    originalClass.equals(Integer.class) ||
                    originalClass.equals(Long.class) ||
                    originalClass.equals(Float.class) ||
                    originalClass.equals(Double.class) ||
                    originalClass.equals(BigInteger.class) ||
                    originalClass.equals(BigDecimal.class)) {
                if (expectedClass == Object.class ||
                        expectedClass == Number.class ||
                        originalClass == expectedClass) {
                    return true;
                }
            }
        }
        return false;
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
