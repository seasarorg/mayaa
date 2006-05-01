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
import org.seasar.mayaa.cycle.CycleFactory;
import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.impl.cycle.scope.ScopeNotWritableException;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleUtil {

    private static CycleUtil _singleton = new CycleUtil();
    private CycleFactory _factory;

    private static StandardScope _standardScope = new StandardScope();

    private CycleUtil() {
        // singleton.
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

    public static RequestScope getRequestScope() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        return cycle.getRequestScope();
    }

    public static Response getResponse() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        return cycle.getResponse();
    }

    public static AttributeScope findStandardAttributeScope(String name) {
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        ServiceCycle cycle = getServiceCycle();
        for (int i = 0; i < _standardScope.size(); i++) {
            AttributeScope scope = cycle.getAttributeScope(_standardScope.get(i));
            if (scope != null && scope.hasAttribute(name)) {
                return scope;
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
