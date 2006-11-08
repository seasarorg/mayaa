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
package org.seasar.mayaa.impl.cycle.script.rhino;

import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaAdapter;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.impl.IllegalParameterValueException;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.cycle.script.AbstractScriptEnvironment;
import org.seasar.mayaa.impl.cycle.script.LiteralScript;
import org.seasar.mayaa.impl.cycle.script.ScriptBlock;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptEnvironmentImpl extends AbstractScriptEnvironment {
    private static final long serialVersionUID = -4067264733660357274L;

    // singleton
    private static transient WrapFactory _wrap;

    protected CompiledScript compile(
            ScriptBlock scriptBlock, PositionAware position, int offsetLine) {
        if (scriptBlock == null) {
            throw new IllegalArgumentException();
        }
        String text = scriptBlock.getBlockString();
        if (scriptBlock.isLiteral()) {
            return new LiteralScript(text);
        }
        return new TextCompiledScriptImpl(text, position, offsetLine);
    }

    // ScriptEnvironment implements ----------------------------------

    protected String getSourceMimeType(SourceDescriptor source) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        String systemID = source.getSystemID();
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        ApplicationScope application = cycle.getApplicationScope();
        return application.getMimeType(systemID);
    }

    public CompiledScript compile(
            SourceDescriptor source, String encoding) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        return new SourceCompiledScriptImpl(source, encoding);
    }

    private static final String PARENT_SCRIPTABLE_KEY =
            ScriptEnvironmentImpl.class.getName() + "#parentScriptable";
    static {
        CycleUtil.registVariableFactory(PARENT_SCRIPTABLE_KEY,
                new DefaultCycleLocalInstantiator() {

                    protected Scriptable getStandardObjects() {
                        Scriptable _standardObjects;
                        Context cx = Context.enter();
                        try {
                            _standardObjects = cx.initStandardObjects(null, true);
                        } finally {
                            Context.exit();
                        }
                        return _standardObjects;
                    }

                    public Object create(Object[] params) {
                        ServiceCycle cycle = CycleUtil.getServiceCycle();
                        Scriptable parent;
                        Context cx = RhinoUtil.enter();
                        try {

                            Scriptable standard = getStandardObjects();
                            parent = cx.getWrapFactory().wrapAsJavaObject(
                                    cx, standard, cycle, ServiceCycle.class);
                        } finally {
                            Context.exit();
                        }
                        return parent;
                    }
                });
    }

    public void initScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setPageScope(null);
        CycleUtil.clearGlobalVariable(PARENT_SCRIPTABLE_KEY);
    }

    public void startScope(Map variables) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope scope = cycle.getPageScope();
        Scriptable parent;
        if (scope == null) {
            parent = (Scriptable) CycleUtil.getGlobalVariable(PARENT_SCRIPTABLE_KEY, null);
        } else if (scope instanceof PageAttributeScope) {
            PageAttributeScope pageTop = (PageAttributeScope) scope;
            parent = (PageAttributeScope)pageTop.getAttribute(
                    PageAttributeScope.KEY_CURRENT);
        } else {
            throw new IllegalStateException();
        }
        PageAttributeScope pageScope = new PageAttributeScope();
        pageScope.setParentScope(parent);
        if (variables != null) {
            RhinoUtil.enter();
            try {
                for (Iterator it = variables.keySet().iterator(); it.hasNext();) {
                    String name = it.next().toString();
                    Object value = variables.get(name);
                    Object variable = Context.javaToJS(value, pageScope);
                    ScriptableObject.putProperty(pageScope, name, variable);
                }
            } finally {
                Context.exit();
            }
        }
        // only first page scope
        if (cycle.getPageScope() == null) {
            cycle.setPageScope(pageScope);
        }
        cycle.getPageScope().setAttribute(
                PageAttributeScope.KEY_CURRENT, pageScope);
    }

    public void endScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        AttributeScope scope = cycle.getPageScope();
        if (scope instanceof PageAttributeScope) {
            PageAttributeScope pageScope = (PageAttributeScope) scope;
            pageScope = (PageAttributeScope) pageScope.getAttribute(
                        PageAttributeScope.KEY_CURRENT);
            Scriptable current = pageScope.getParentScope();
            if (current instanceof PageAttributeScope) {
                PageAttributeScope parentScope =
                    (PageAttributeScope) current;
                cycle.getPageScope().setAttribute(
                        PageAttributeScope.KEY_CURRENT, parentScope);
                return;
            } else if (current != null) {
                cycle.setPageScope(null);
                return;
            }
        }
        throw new IllegalStateException();
    }

    public Object convertFromScriptObject(Object scriptObject) {
        Object result = JavaAdapter.convertResult(scriptObject, Object.class);

        if (result instanceof NativeArray) {
            NativeArray jsArray = (NativeArray) result;
            int length = (int) jsArray.getLength();
            Object[] array = new Object[length];
            for (int i = 0; i < length; i++) {
                array[i] = jsArray.get(i, null);
            }
            result = array;
        }

        return result;
    }

    static WrapFactory getWrapFactory() {
        return _wrap;
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("wrapFactory".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            Class clazz = ObjectUtil.loadClass(value, WrapFactory.class);
            _wrap = (WrapFactory) ObjectUtil.newInstance(clazz);
        } else if ("blockSign".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            setBlockSign(value);
        }
        super.setParameter(name, value);
    }

}
