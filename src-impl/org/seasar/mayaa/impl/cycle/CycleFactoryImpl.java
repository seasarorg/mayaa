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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.CycleFactory;
import org.seasar.mayaa.cycle.CycleLocalVariables;
import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.impl.CycleLocalVariablesImpl;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.cycle.scope.ScopeNotFoundException;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleFactoryImpl
        extends ParameterAwareImpl implements CycleFactory {

    private static final long serialVersionUID = 6930908159752133949L;
    private static final Log LOG = LogFactory.getLog(CycleFactoryImpl.class);

    private static ThreadLocal _currentCycle = new ThreadLocal();

    protected StandardScope _standardScope = new StandardScope();
    private Object _context;
    private Class _serviceClass;
    private CycleLocalVariables _localVariables = new CycleLocalVariablesImpl();

    protected synchronized void initCurrentStrage() {
        if (_currentCycle.get() == null) {
            _currentCycle.set(new Object[1]);
        }
    }

    protected ServiceCycle getCurrentServiceCycle() {
        Object[] storage = (Object[]) _currentCycle.get();
        if (storage == null) {
            initCurrentStrage();
            storage = (Object[]) _currentCycle.get();
        }
        return (ServiceCycle) storage[0];
    }

    protected void setCurrentServiceCycle(ServiceCycle serviceCycle) {
        Object[] storage = (Object[]) _currentCycle.get();
        if (storage == null) {
            initCurrentStrage();
            storage = (Object[]) _currentCycle.get();
        }
        storage[0] = serviceCycle;
    }

    public void cycleFinalize() {
        CycleThreadLocalFactory.cycleLocalFinalize();
        setCurrentServiceCycle(null);
        _currentCycle.set(null);
    }

    public void initialize(Object requestContext, Object responseContext) {
        if (requestContext == null || responseContext == null) {
            throw new IllegalArgumentException();
        }
        ServiceCycle cycle = defaultServiceCycle();
        setCurrentServiceCycle(cycle);
        RequestScope request = cycle.getRequestScope();
        request.setUnderlyingContext(requestContext);
        Response response = cycle.getResponse();
        response.setUnderlyingContext(responseContext);
        CycleThreadLocalFactory.cycleLocalInitialize();
    }

    public void setServiceClass(Class serviceClass) {
        if (serviceClass == null) {
            throw new IllegalArgumentException();
        }
        _serviceClass = serviceClass;
    }

    public Class getServiceClass() {
        if (_serviceClass == null) {
            throw new IllegalArgumentException();
        }
        return _serviceClass;
    }

    public CycleLocalVariables getLocalVariables() {
        return _localVariables;
    }

    protected ServiceCycle defaultServiceCycle() {
        Class serviceCycleClass = getServiceClass();
        if (serviceCycleClass == null) {
            throw new IllegalStateException();
        }
        ServiceCycle cycle =
            (ServiceCycle) ObjectUtil.newInstance(serviceCycleClass);
        for (Iterator it = iterateParameterNames(); it.hasNext();) {
            String key = (String) it.next();
            String value = getParameter(key);
            cycle.setParameter(key, value);
        }
        return cycle;
    }

    public ServiceCycle getServiceCycle() {
        ServiceCycle cycle = getCurrentServiceCycle();
        if (cycle == null) {
            cycle = defaultServiceCycle();
            setCurrentServiceCycle(cycle);
            LOG.info("serviceCycle created out of request cycle.");
        }
        return cycle;
    }

    public StandardScope getStandardScope() {
        return _standardScope;
    }

    // ContextAware implements -------------------------------------

    public void setUnderlyingContext(Object context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
    }

    public Object getUnderlyingContext() {
        if (_context == null) {
            throw new IllegalStateException();
        }
        return _context;
    }

    private boolean isValidScopeName(String scopeName) {
        if (StringUtil.hasValue(scopeName)) {
            ScriptEnvironment scriptEnvironment =
                ProviderUtil.getScriptEnvironment();
            for (Iterator it = scriptEnvironment.iterateAttributeScope();
                    it.hasNext(); ) {
                AttributeScope scope = (AttributeScope) it.next();
                if (scopeName.equals(scope.getScopeName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setParameter(String name, String value) {
        if ("addedStandardScope".equals(name)) {
            if (isValidScopeName(value) == false) {
                throw new ScopeNotFoundException(value);
            }

            CycleUtil.addStandardScope(value);
        }
        super.setParameter(name, value);
    }

}
