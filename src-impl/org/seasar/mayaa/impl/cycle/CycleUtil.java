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
package org.seasar.mayaa.impl.cycle;

import org.seasar.mayaa.FactoryFactory;
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

    private static CycleUtil _singleton = new CycleUtil();
    private volatile CycleFactory _factory;

    private static StandardScope _standardScope = new StandardScope();

    private CycleUtil() {
        // singleton.
    }

    public static boolean isInitialized() {
        return _singleton._factory != null;
    }

    public static CycleFactory getFactory() {
        if (_singleton._factory == null) {
            synchronized (_singleton) {
                if (_singleton._factory == null) {
                    _singleton._factory =
                        (CycleFactory) FactoryFactory.getFactory(CycleFactory.class);
                }
            }
        }
        return _singleton._factory;
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
        return _standardScope;
    }

    public static void addStandardScope(String newScopeName) {
        _standardScope.addScope(newScopeName);
    }

    /**
     * ルートのPageスコープを取得します。
     *
     * @return ルートのPageスコープ
     */
    public static AttributeScope getPageScope() {
        return getServiceCycle().getPageScope();
    }

    /**
     * 現在のPageスコープを取得します。
     *
     * @return 現在のPageスコープ
     */
    public static AttributeScope getCurrentPageScope() {
        return (AttributeScope) getPageScope().getAttribute(
                PageAttributeScope.KEY_CURRENT);
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
        for (int i = 0; i < _standardScope.size(); i++) {
            AttributeScope scope = cycle.getAttributeScope(_standardScope.get(i));
            if (scope != null) {
                if (scope instanceof PageAttributeScope) {
                    scope = getCurrentPageScope();
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

    /** ノードツリー最適化中か否かを保持するキー */
    private static final ThreadLocal/*<Boolean>*/ IS_DRAFT_WRITING =
        new ThreadLocal/*<Boolean>*/();

    /**
     * ノードツリー最適化中か。
     *
     * @return ノードツリー最適化中ならtrue
     */
    public static boolean isDraftWriting() {
        return IS_DRAFT_WRITING.get() != null;
    }

    /**
     * ノードツリー最適化を開始する。
     */
    public static void beginDraftWriting() {
        IS_DRAFT_WRITING.set(Boolean.TRUE);
    }

    /**
     * ノードツリー最適化を終了する。
     */
    public static void endDraftWriting() {
        IS_DRAFT_WRITING.set(null);
    }

}
