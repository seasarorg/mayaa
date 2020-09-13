/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl;

import org.seasar.mayaa.cycle.CycleFactory;
import org.seasar.mayaa.cycle.CycleLocalVariables;
import org.seasar.mayaa.cycle.ServiceCycle;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TestCycleFactory extends ParameterAwareImpl
        implements CycleFactory {

    private static final long serialVersionUID = -7850907202847628535L;
    private CycleFactory _before;
    private Object _context;
    private CycleLocalVariables _localVariables = new CycleLocalVariablesImpl();

    public String[] STANDARD_SCOPES = new String[] {
        ServiceCycle.SCOPE_PAGE,
        ServiceCycle.SCOPE_REQUEST,
        ServiceCycle.SCOPE_SESSION,
        ServiceCycle.SCOPE_APPLICATION
    };

    public TestCycleFactory(CycleFactory before) {
        _before = before;
    }

    public String[] getStandardScopeNames() {
        return STANDARD_SCOPES;
    }

    public void setUnderlyingContext(Object context) {
        _context = context;
    }

    public Object getUnderlyingContext() {
        return _context;
    }

    public void setServiceClass(Class<?> serviceClass) {
        // no op
    }

    public Class<?> getServiceClass() {
        return _before.getServiceClass();
    }

    public ServiceCycle getServiceCycle() {
        return _before.getServiceCycle();
    }

    public void initialize(
            Object requestContext, Object responseContext) {
        _before.initialize(requestContext, responseContext);
    }

    public void cycleFinalize() {
        _before.cycleFinalize();
        _before = null;
        _context = null;
    }

    public CycleLocalVariables getLocalVariables() {
        return _localVariables;
    }

}
