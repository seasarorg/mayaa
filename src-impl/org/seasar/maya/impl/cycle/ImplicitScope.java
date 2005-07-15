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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.cycle.implicit.HeaderResolver;
import org.seasar.maya.impl.cycle.implicit.ImplicitObjectResolver;
import org.seasar.maya.impl.cycle.implicit.ParamResolver;
import org.seasar.maya.impl.cycle.implicit.ParamValuesResolver;
import org.seasar.maya.impl.cycle.implicit.ServiceCycleResolver;
import org.seasar.maya.impl.el.context.HeaderValuesResolver;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ImplicitScope implements AttributeScope {

	private static Map _resolverMap;
    static {
    	_resolverMap = new HashMap();
    	_resolverMap.put(ServiceCycle.SERVICE_CYCLE , new ServiceCycleResolver());
    	_resolverMap.put(ServiceCycle.PARAM , new ParamResolver());
    	_resolverMap.put(ServiceCycle.PARAM_VALUES , new ParamValuesResolver());
    	_resolverMap.put(ServiceCycle.HEADER , new HeaderResolver());
    	_resolverMap.put(ServiceCycle.HEADER_VALUES , new HeaderValuesResolver());
    }
    
    private ServiceCycle _cycle;
    private Map _instanceMap = new HashMap();
	
    public ImplicitScope(ServiceCycle cycle) {
    	if(cycle == null) {
    		throw new IllegalArgumentException();
    	}
    	_cycle = cycle;
    }
    
	public String getScopeName() {
		return "implicit";
	}

	public Iterator iterateAttributeNames() {
		return _resolverMap.keySet().iterator();
	}

	public Object getAttribute(String name) {
		if(StringUtil.isEmpty(name)) {
			return null;
		}
        Object object = _instanceMap.get(name);
        if(object == null) {
            ImplicitObjectResolver resolver = (ImplicitObjectResolver)_resolverMap.get(name);
            if(resolver != null) {
                object = resolver.resolve(_cycle);
                _instanceMap.put(name, object);
            }
        }
        return object;
	}

	public void setAttribute(String name, Object attribute) {
		throw new ScopeNotWritableException();
	}
	
}
