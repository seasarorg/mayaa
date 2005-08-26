/*
 * Copyright (c) 2004-2005 the Seasar Founcation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.util;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleUtil {
    
    private CycleUtil() {
    }

    private static final String[] SCOPES = {
        ServiceCycle.SCOPE_PAGE,
        ServiceCycle.SCOPE_REQUEST,
        ServiceCycle.SCOPE_SESSION,
        ServiceCycle.SCOPE_APPLICATION
    };
    
    public static ServiceCycle getServiceCycle() {
    	ServiceProvider provider = ProviderFactory.getServiceProvider();
    	return provider.getServiceCycle();
    }
    
    public static Application getApplication() {
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        return provider.getApplication();
    }
    
    public static Request getRequest() {
    	ServiceCycle cycle = getServiceCycle();
    	return cycle.getRequest();
    }

    public static Object getAttribute(String name) {
        return getAttribute(name, ServiceCycle.SCOPE_PAGE);
    }
    
    public static Object getAttribute(String name, String scopeString) {
        AttributeScope scope = getServiceCycle().getAttributeScope(scopeString);
        if(scope == null) {
            throw new IllegalArgumentException();
        }
        return scope.getAttribute(name);
    }

    public static void removeAttribute(String name) {
	    removeAttribute(name, ServiceCycle.SCOPE_PAGE);
    }
    
    public static void removeAttribute(String name, String scopeString) {
		AttributeScope scope = getServiceCycle().getAttributeScope(scopeString);
        if(scope == null) {
            throw new IllegalArgumentException();
        }
		scope.removeAttribute(name);
    }

    public static void setAttribute(String name, Object value) {
	    setAttribute(name, value, ServiceCycle.SCOPE_PAGE);
    }
    
    public static void setAttribute(String name, Object value, String scopeString) {
		AttributeScope scope = getServiceCycle().getAttributeScope(scopeString);
        if(scope == null) {
            throw new IllegalArgumentException();
        }
		scope.setAttribute(name, value);
    }
    
    public static Object findAttribute(String name) {
        for (int i = 0; i < SCOPES.length; i++) {
            Object obj = getAttribute(name, SCOPES[i]);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    public static void rewriteAttribute(String name, Object value) {
    	ServiceCycle cycle = getServiceCycle();
        for (int i = 0; i < SCOPES.length; i++) {
            AttributeScope scope = cycle.getAttributeScope(SCOPES[i]);
            Object obj = scope.getAttribute(name);
            if (obj != null) {
                scope.setAttribute(name, value);
                return;
            }
        }
    }
    
}
