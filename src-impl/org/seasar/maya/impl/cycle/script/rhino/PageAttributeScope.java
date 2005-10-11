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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.NativeJavaMethod;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.seasar.maya.cycle.scope.AttributeScope;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.impl.provider.ProviderUtil;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageAttributeScope extends ScriptableObject
        implements AttributeScope, Wrapper {

    private static final long serialVersionUID = 7746385735022710670L;
    private static Map _methodMap;
    
    private static void setMethod(String name, Class[] args) {
        try {
            Method method = PageAttributeScope.class.getMethod(name, args);
            _methodMap.put(name, new NativeJavaMethod(method, name));
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } 
    }

    static {
        _methodMap = new HashMap();
        Class[] nullArg = new Class[0];
        setMethod("getScopeName", nullArg);
        setMethod("iterateAttributeNames", nullArg);
        setMethod("isAttributeWritable", nullArg);
        Class[] stringArg = new Class[] { String.class };
        setMethod("hasAttribute", stringArg);
        setMethod("getAttribute", stringArg);
        setMethod("removeAttribute", stringArg);
        Class[] stringObjectArg = new Class[] { String.class, Object.class };
        setMethod("setAttribute", stringObjectArg);
        Class[] stringClassArg = new Class[] { String.class, Class.class };
        setMethod("newAttribute", stringClassArg);
    }
    
    public String getClassName() {
        return "pageScope";
    }
    
    public boolean has(String name, Scriptable start) {
        if(_methodMap.containsKey(name)) {
            return true;
        }
        return super.has(name, start);
    }    

    public Object get(String name, Scriptable start) {
        if(_methodMap.containsKey(name)) {
            return _methodMap.get(name);
        }
        return super.get(name, start);
    }

    public Object[] getIds() {
        Set set = new HashSet(_methodMap.keySet());
        Object[] ids = super.getIds();
        for(int i = 0; i < ids.length; i++) {
            Object name = ids[i];
            if(set.contains(name) == false) {
                set.add(name);
            }
        }
        return set.toArray(new Object[set.size()]);
    }
    
    // Wrapper implements -------------------------------------------

    public Object unwrap() {
        return this;
    }

    // AttributeScope implements -------------------------------------
    
    public String getScopeName() {
        return "page";
    }

    public Iterator iterateAttributeNames() {
        List list = new ArrayList();
        for(Scriptable scope = this;
                scope instanceof PageAttributeScope; 
                scope = scope.getParentScope()) {
            Object[] ids = scope.getIds();
            for(int i = 0; i < ids.length; i++) {
                if(ids[i] instanceof String && list.contains(ids[i]) == false) {
                    list.add(ids[i]);
                }
            }
        }
        return list.iterator();
    }

    protected Scriptable findScope(String name) {
        for(Scriptable scope = this;
                scope instanceof PageAttributeScope; 
                scope = scope.getParentScope()) {
            if(scope.has(name, this)) {
                return scope;
            }
        }
        return null;
    }

    public boolean hasAttribute(String name) {
        Scriptable scope = findScope(name);
        if(scope != null) {
            return true;
        }
        return false;
    }

    public Object getAttribute(String name) {
        Scriptable scope = findScope(name);
        if(scope != null) {
            ScriptEnvironment env = ProviderUtil.getScriptEnvironment(); 
            return env.convertFromScriptObject(scope.get(name, this));
        }
        return null;
    }

    public boolean isAttributeWritable() {
        return true;
    }

    public void setAttribute(String name, Object attribute) {
        put(name, this, attribute);
    }

    public void removeAttribute(String name) {
        for(Scriptable scope = this;
                scope instanceof PageAttributeScope; 
                scope = scope.getParentScope()) {
            scope.delete(name);
        }
    }

    public Object newAttribute(String name, Class attributeClass) {
        if(hasAttribute(name)) {
            return getAttribute(name); 
        }
        Object model = ObjectUtil.newInstance(attributeClass);
        setAttribute(name, model);
        return model;
    }

    // Parameterizable implements ------------------------------------
    
    public void setParameter(String name, String value) {
    }

	public String getParameter(String name) {
		return null;
	}

	public Iterator iterateParameterNames() {
		return NullIterator.getInstance();
	}
    
}
