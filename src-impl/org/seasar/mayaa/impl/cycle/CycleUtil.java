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
package org.seasar.mayaa.impl.cycle;

import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.MayaaContext;
import org.seasar.mayaa.cycle.CycleFactory;
import org.seasar.mayaa.cycle.CycleLocalInstantiator;
import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.impl.cycle.scope.ScopeNotWritableException;
import org.seasar.mayaa.impl.cycle.script.rhino.PageAttributeScope;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleUtil {

    private CycleUtil() {
        // singleton.
    }

    public static boolean isInitialized() {
        MayaaContext mayaaContext = MayaaContext.getCurrentContext();
        if (mayaaContext == null || mayaaContext.getFactoryFactory() == null) {
            return false;
        }
        return FactoryFactory.getFactory(CycleFactory.class) != null;
    }

    public static CycleFactory getFactory() {
        if (isInitialized() == false) {
            throw new IllegalStateException();
        }
        return (CycleFactory) FactoryFactory.getFactory(CycleFactory.class);
    }

    public static void initialize(
            Object requestContext, Object responseContext) {
        getFactory().initialize(requestContext, responseContext);
    }

    public static void cycleFinalize() {
        getFactory().cycleFinalize();
    }

    public static void registVariableFactory(String key, CycleLocalInstantiator instantiator) {
        CycleThreadLocalFactory.registFactory(key, instantiator);
    }

    public static Object getGlobalVariable(String key, Object[] params) {
        return getFactory().getLocalVariables().getGlobalVariable(key, params);
    }

    public static void setGlobalVariable(String key, Object value) {
        getFactory().getLocalVariables().setGlobalVariable(key, value);
    }

    public static void clearGlobalVariable(String key) {
        getFactory().getLocalVariables().clearGlobalVariable(key);
    }

    public static Object getLocalVariable(String key, Object owner, Object[] params) {
        return getFactory().getLocalVariables().getVariable(key, owner, params);
    }

    public static void setLocalVariable(String key, Object owner, Object value) {
        getFactory().getLocalVariables().setVariable(key, owner, value);
    }

    public static void clearLocalVariable(String key, Object owner) {
        getFactory().getLocalVariables().clearVariable(key, owner);
    }

    public static ServiceCycle getServiceCycle() {
        if (getFactory() == null) {
            throw new CycleNotInitializedException();
        }
        return getFactory().getServiceCycle();
    }

    public static StandardScope getStandardScope() {
        MayaaContext mayaaContext = MayaaContext.getCurrentContext();
        if (mayaaContext == null) {
            throw new IllegalStateException();
        }
        return (StandardScope) mayaaContext.getGrowAttribute(StandardScope.class.getName(), new MayaaContext.Instantiator() {
            public Object newInstance() {
                return new StandardScope();
            }
        });
    }

    public static void addStandardScope(String newScopeName) {
        getStandardScope().addScope(newScopeName);
    }

    public static RequestScope getRequestScope() {
        return getServiceCycle().getRequestScope();
    }

    public static Response getResponse() {
        return getServiceCycle().getResponse();
    }

    public static AttributeScope findStandardAttributeScope(String name) {
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        ServiceCycle cycle = getServiceCycle();
        StandardScope standardScope = getStandardScope();
        int size = standardScope.size();
        for (int i = 0; i < size; i++) {
            AttributeScope scope = cycle.getAttributeScope(standardScope.get(i));
            if (scope != null) {
                if (scope instanceof PageAttributeScope) {
                    scope = (AttributeScope)
                            scope.getAttribute(PageAttributeScope.KEY_CURRENT);
                    while (scope != null) {
                        if (scope.hasAttribute(name)) {
                            return scope;
                        }
                        Object parent =
                            ((PageAttributeScope)scope).getParentScope();
                        if (parent instanceof AttributeScope) {
                            scope = (AttributeScope)parent;
                        } else {
                            scope = null;
                        }
                    }
                } else if (scope.hasAttribute(name)) {
                    return scope;
                }
            }
        }
        return null;
    }

    public static Object getAttribute(String name, String scopeName) {
        ServiceCycle cycle = getServiceCycle();
        AttributeScope scope = cycle.getAttributeScope(scopeName);
        return scope.getAttribute(name);
    }

    public static void setAttribute(
            String name, Object value, String scopeName) {
        ServiceCycle cycle = getServiceCycle();
        AttributeScope scope = cycle.getAttributeScope(scopeName);
        if (scope.isAttributeWritable()) {
            scope.setAttribute(name, value);
        } else {
            throw new ScopeNotWritableException(scopeName);
        }
    }

    public static void removeAttribute(String name, String scopeName) {
        ServiceCycle cycle = getServiceCycle();
        AttributeScope scope = cycle.getAttributeScope(scopeName);
        if (scope.isAttributeWritable()) {
            scope.removeAttribute(name);
        } else {
            throw new ScopeNotWritableException(scopeName);
        }
    }

    private static final String DRAFT_WRITING =
            "org.seasar.mayaa.cycle.DRAFT_WRITING";

    public static boolean isDraftWriting() {
        return Boolean.TRUE.equals(
                getAttribute(DRAFT_WRITING, ServiceCycle.SCOPE_REQUEST));
    }

    public static void beginDraftWriting() {
        setAttribute(DRAFT_WRITING, Boolean.TRUE, ServiceCycle.SCOPE_REQUEST);
    }

    public static void endDraftWriting() {
        setAttribute(DRAFT_WRITING, Boolean.FALSE, ServiceCycle.SCOPE_REQUEST);
    }

}
