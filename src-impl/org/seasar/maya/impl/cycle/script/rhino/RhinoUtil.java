/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.cycle.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaAdapter;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.AttributeScope;
import org.seasar.maya.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RhinoUtil {

    private RhinoUtil() {
        // no instantiation.
    }

    public static Context enter(WrapFactory wrap) {
        Context cx = Context.enter();
        if(wrap != null) {
            cx.setWrapFactory(wrap);
        }
        return cx;
    }
    
    public static Scriptable getScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope attrs = cycle.getPageScope();
        if(attrs instanceof Scriptable) {
            return (Scriptable)attrs;
        }
        throw new IllegalStateException();
    }

    public static Object convertResult(
            Context cx, Class expectedClass, Object jsRet) {
        Object ret = null;
        if(expectedClass.equals(Boolean.TYPE)) {
            // workaround to ECMA1.3 
            ret = JavaAdapter.convertResult(jsRet, Object.class);
        } else if(expectedClass == Void.class
                || (jsRet instanceof org.mozilla.javascript.Undefined)) {
            ret = null;
        } else {
            ret = JavaAdapter.convertResult(jsRet, expectedClass);
        }

        return ret;
    }

    public static  void removeWrappedException(WrappedException e) {
        Throwable t = e.getWrappedException();
        if(t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        throw new RuntimeException(t);
    }
    
}
