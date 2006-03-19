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

    public static final String[] STANDARD_SCOPES = new String[] {
        ServiceCycle.SCOPE_PAGE,
        ServiceCycle.SCOPE_REQUEST,
        ServiceCycle.SCOPE_SESSION,
        ServiceCycle.SCOPE_APPLICATION
    };

    private CycleUtil() {
        // no instantiation.
    }

    public static void initialize(
            Object requestContext, Object responseContext) {
        CycleFactory factory =
            (CycleFactory) FactoryFactory.getFactory(CycleFactory.class);
        factory.initialize(requestContext, responseContext);
    }

    public static ServiceCycle getServiceCycle() {
        CycleFactory factory =
            (CycleFactory) FactoryFactory.getFactory(CycleFactory.class);
        return factory.getServiceCycle();
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
        for (int i = 0; i < STANDARD_SCOPES.length; i++) {
            ServiceCycle cycle = getServiceCycle();
            AttributeScope scope =
                cycle.getAttributeScope(STANDARD_SCOPES[i]);
            if (scope.hasAttribute(name)) {
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
        ServiceCycle cycle = getServiceCycle();
        return Boolean.TRUE.equals(
                cycle.getAttributeScope(ServiceCycle.SCOPE_REQUEST)
                        .getAttribute(DRAFT_WRITING));
    }

    public static void beginDraftWriting() {
        getServiceCycle().getAttributeScope(ServiceCycle.SCOPE_REQUEST)
                .setAttribute(DRAFT_WRITING, Boolean.TRUE);
    }

    public static void endDraftWriting() {
        getServiceCycle().getAttributeScope(ServiceCycle.SCOPE_REQUEST)
                .setAttribute(DRAFT_WRITING, Boolean.FALSE);
    }

}
