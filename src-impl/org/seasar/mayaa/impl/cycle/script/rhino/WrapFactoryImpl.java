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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.DynaBean;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WrapFactoryImpl extends WrapFactory implements Serializable {

    private static final long serialVersionUID = -8130663823197873722L;

    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
            Object javaObject, Class staticClass) {
        if (javaObject instanceof Map) {
            return new NativeMap(scope, (Map) javaObject);
        } else if (javaObject instanceof List) {
            return new NativeList(scope, (List) javaObject);
        } else if (javaObject instanceof AttributeScope
                && javaObject instanceof Scriptable == false) {
            AttributeScope attrs = (AttributeScope) javaObject;
            return new NativeAttributeScope(scope, attrs);
        } else if (javaObject instanceof ServiceCycle) {
            ServiceCycle cycle = (ServiceCycle) javaObject;
            return new NativeServiceCycle(scope, cycle);
        } else if (javaObject instanceof DynaBean) {
            DynaBean dynaBean = (DynaBean) javaObject;
            return new NativeDynaBean(scope, dynaBean);
        }
        return super.wrapAsJavaObject(cx, scope, javaObject, staticClass);
    }

}
