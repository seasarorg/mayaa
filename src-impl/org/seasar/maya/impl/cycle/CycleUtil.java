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

import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.AttributeScope;
import org.seasar.maya.cycle.scope.RequestScope;
import org.seasar.maya.impl.cycle.scope.ScopeNotWritableException;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

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
        // no instanciation.
    }

    public static RequestScope getRequestScope() {
    	ServiceCycle cycle = CycleUtil.getServiceCycle();
    	return cycle.getRequestScope();
    }

    public static Response getResponse() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        return cycle.getResponse();
    }

    public static ServiceCycle getServiceCycle() {
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        return provider.getServiceCycle();
    }

    public static ServiceCycle getServiceCycleSafely() {
        if(ProviderFactory.isInithialized()) {
            return getServiceCycle();
        }
        return null;
    }
    
    public static AttributeScope findStandardAttributeScope(String name) {
        if(StringUtil.isEmpty(name)) {
            return null;
        }
        for(int i = 0; i < STANDARD_SCOPES.length; i++) {
            ServiceCycle cycle = getServiceCycle();
            AttributeScope scope = 
                cycle.getAttributeScope(STANDARD_SCOPES[i]);
            if(scope.hasAttribute(name)) {
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
        if(scope.isAttributeWritable()) {
            scope.setAttribute(name, value);
        } else {
            throw new ScopeNotWritableException(scopeName);
        }
    }

    public static void removeAttribute(String name, String scopeName) {
        ServiceCycle cycle = getServiceCycle();
    	AttributeScope scope = cycle.getAttributeScope(scopeName);
        if(scope.isAttributeWritable()) {
            scope.removeAttribute(name);
        } else {
            throw new ScopeNotWritableException(scopeName);
        }
    }

    
}
