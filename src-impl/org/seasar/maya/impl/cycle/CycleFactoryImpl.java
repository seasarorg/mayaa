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
package org.seasar.maya.impl.cycle;

import java.util.Iterator;

import org.seasar.maya.cycle.CycleFactory;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.RequestScope;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.util.ObjectUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleFactoryImpl 
		extends ParameterAwareImpl implements CycleFactory {

    private static final long serialVersionUID = 6930908159752133949L;

    private Object _context;
    private Class _serviceClass;
    private ThreadLocal _currentCycle = new ThreadLocal();

    public void setServiceClass(Class serviceClass) {
        if(serviceClass == null) {
            throw new IllegalArgumentException();
        }
        _serviceClass = serviceClass;
    }
    
    public Class getServiceClass() {
        if(_serviceClass == null) {
            throw new IllegalArgumentException();
        }
        return _serviceClass;
    }
    
    public void initialize(Object requestContext, Object responseContext) {
        if(requestContext == null || responseContext == null) {
            throw new IllegalArgumentException();
        }
        Class serviceCycleClass = getServiceClass();
        if(serviceCycleClass == null) {
            throw new IllegalStateException();
        }
        ServiceCycle cycle = 
            (ServiceCycle)ObjectUtil.newInstance(serviceCycleClass);
        cycle.setUnderlyingContext(getUnderlyingContext());
        for(Iterator it = iterateParameterNames(); it.hasNext(); ) {
            String key = (String)it.next();
            String value = getParameter(key);
            cycle.setParameter(key, value);
        }
        _currentCycle.set(cycle);
        RequestScope request = cycle.getRequestScope();
        request.setUnderlyingContext(requestContext);
        Response response = cycle.getResponse();
        response.setUnderlyingContext(responseContext);
    }

    public ServiceCycle getServiceCycle() {
        ServiceCycle cycle = (ServiceCycle)_currentCycle.get();
        if(cycle == null) {
            throw new CycleNotInitializedException();
        }
        return cycle;
    }

    // ContextAware implements -------------------------------------
    
	public void setUnderlyingContext(Object context) {
		if(context == null) {
			throw new IllegalArgumentException();
		}
		_context = context;
	}
    
    public Object getUnderlyingContext() {
    	if(_context == null) {
    		throw new IllegalStateException();
    	}
		return _context;
	}

}
