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
package org.seasar.maya.impl.cycle.factory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.factory.CycleFactory;
import org.seasar.maya.cycle.scope.ApplicationScope;
import org.seasar.maya.cycle.scope.RequestScope;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleFactoryImpl implements CycleFactory {

    private static final long serialVersionUID = 6930908159752133949L;

    private ApplicationScope _application;
    private Class _cycleClass;
    private Map _cycleParams;
    private ThreadLocal _currentCycle = new ThreadLocal();

    public void setApplicationScope(ApplicationScope application) {
        if(application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
    }
    
    protected ApplicationScope getApplicationScope() {
        if(_application == null) {
            throw new IllegalStateException();
        }
        return _application;
    }
    
    public void setCycleClass(Class serviceCycleClass) {
        if(serviceCycleClass == null) {
            throw new IllegalArgumentException();
        }
        _cycleClass = serviceCycleClass;
    }
    
    protected Class getCycleClass() {
        if(_cycleClass == null) {
            throw new IllegalArgumentException();
        }
        return _cycleClass;
    }
    
    public void initialize(Object requestContext, Object responseContext) {
        if(requestContext == null || responseContext == null) {
            throw new IllegalArgumentException();
        }
        Class serviceCycleClass = getCycleClass();
        if(serviceCycleClass == null) {
            throw new IllegalStateException();
        }
        ServiceCycle cycle = 
            (ServiceCycle)ObjectUtil.newInstance(serviceCycleClass);
        cycle.setApplicationScope(getApplicationScope());
        if(_cycleParams != null) {
            for(Iterator it = _cycleParams.keySet().iterator();
                    it.hasNext(); ) {
                String key = (String)it.next();
                String value = (String)_cycleParams.get(key);
                cycle.setParameter(key, value);
            }
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
            // TODO èâä˙âªÇçsÇ¡ÇƒÇ¢Ç»Ç¢ó·äO
            throw new IllegalStateException();
        }
        return cycle;
    }

    public void setParameter(String name, String value) {
        if(StringUtil.isEmpty(name) || value == null) {
            throw new IllegalArgumentException();
        }
        if(_cycleParams == null) {
            _cycleParams = new HashMap();
        }
        _cycleParams.put(name, value);
    }

}
