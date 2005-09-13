/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.cycle;

import java.util.Iterator;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleUtil {

    private CycleUtil() {
    }

    public static Request getRequest() {
    	ServiceCycle cycle = CycleUtil.getServiceCycle();
    	return cycle.getRequest();
    }

    public static Response getResponse() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        return cycle.getResponse();
    }

    public static ServiceCycle getServiceCycle() {
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        return provider.getServiceCycle();
    }

    public static AttributeScope findAttributeScope(String name) {
        if(StringUtil.isEmpty(name)) {
            return null;
        }
        for(Iterator it = getServiceCycle().iterateAttributeScope(); it.hasNext(); ) {
            AttributeScope scope = (AttributeScope)it.next();
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

    public static void setAttribute(String name, Object value, String scopeName) {
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
