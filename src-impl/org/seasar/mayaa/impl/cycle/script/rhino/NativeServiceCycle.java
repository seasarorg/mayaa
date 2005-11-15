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
package org.seasar.maya.impl.cycle.script.rhino;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.AttributeScope;
import org.seasar.maya.impl.cycle.CycleUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NativeServiceCycle extends NativeJavaObject {

	private static final long serialVersionUID = 326309209323166932L;

	private ServiceCycle _cycle;
    
    public NativeServiceCycle(Scriptable scope, ServiceCycle cycle) {
        super(scope, cycle, Map.class);
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        _cycle = cycle;
    }
    
    public boolean has(String name, Scriptable start) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if(cycle.hasAttributeScope(name) || 
                CycleUtil.findStandardAttributeScope(name) != null) {
            return true;
        }
        return super.has(name, start);
    }    

    public Object get(String name, Scriptable start) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if(cycle.hasAttributeScope(name)) {
            return cycle.getAttributeScope(name);
        }
        AttributeScope scope = CycleUtil.findStandardAttributeScope(name);
        if(scope != null) {
            return scope.getAttribute(name);
        }
        return super.get(name, start);
    }

    protected void addAttributesTo(AttributeScope scope, Set set) {
        for(Iterator it = scope.iterateAttributeNames(); it.hasNext(); ) {
            Object attrName = it.next();
            if(set.contains(attrName) == false) {
                set.add(attrName);
            }
        }
    }
    
	public Object[] getIds() {
        Set set = new HashSet();
        for(Iterator it = _cycle.iterateAttributeScope(); it.hasNext(); ) {
            AttributeScope attrs = (AttributeScope)it.next();
            String scopeName = attrs.getScopeName();
            if(set.contains(scopeName) == false) {
                set.add(scopeName);
            }
        }
        addAttributesTo(_cycle.getPageScope(), set);
        addAttributesTo(_cycle.getRequestScope(), set);
        addAttributesTo(_cycle.getSessionScope(), set);
        addAttributesTo(_cycle.getApplicationScope(), set);
        Object[] ids = super.getIds();
        for(int i = 0; i < ids.length; i++) {
            Object name = ids[i];
            if(set.contains(name) == false) {
                set.add(name);
            }
        }
        return set.toArray(new Object[set.size()]);
    }
    
    public String getClassName() {
        return "serviceCycle";
    }

}
