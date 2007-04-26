/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.cycle.script.rhino;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.NativeJavaTopPackage;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class VirtualNativeObject extends NativeObject {

    private static final long serialVersionUID = 4781599810286253299L;
    
    private static final String GLOBAL_OBJECT = "__global__";
    private static final Log LOG = LogFactory.getLog(VirtualNativeObject.class);

    private boolean _initializing = true;

    private NativeObject _copyGlobal = new NativeObject();

    public void initEnd() {
        _initializing = false;
    }

    public boolean has(String name, Scriptable start) {
        return GLOBAL_OBJECT.equals(name) || _copyGlobal.has(name, start);
    }

    public Object get(String name, Scriptable start) {
        if (GLOBAL_OBJECT.equals(name)) return _copyGlobal;
        return _copyGlobal.get(name, start);
    }
    
    public void put(int index, Scriptable start, Object value) {
    	_copyGlobal.put(index, start, value);
    }

    public void put(String name, Scriptable start, Object value) {
        // __global__ is readonly
        if (GLOBAL_OBJECT.equals(name)) return;

        if (_initializing
                || value instanceof IdFunctionObject
                || value instanceof NativeJavaTopPackage
                || value instanceof NativeJavaPackage) {
            _copyGlobal.put(name, _copyGlobal, value);
            return;
        }
        if (LOG.isTraceEnabled()) {
        	LOG.trace("put name: " + name + ", value: " + value + ", start: " + start.getClassName());
        }
        AttributeScope pageScope = CycleUtil.getPageScope();
        if (pageScope != null) {
            pageScope.setAttribute(name, value);
        } else {
            _copyGlobal.put(name, _copyGlobal, value);
        }
    }

	public void defineFunctionProperties(String[] names, Class clazz, int attributes) {
		_copyGlobal.defineFunctionProperties(names, clazz, attributes);
	}

	public void defineProperty(String propertyName, Class clazz, int attributes) {
		_copyGlobal.defineProperty(propertyName, clazz, attributes);
	}

	public void defineProperty(String propertyName, Object value, int attributes) {
		_copyGlobal.defineProperty(propertyName, value, attributes);
	}

	public void defineProperty(String propertyName, Object delegateTo, Method getter, Method setter, int attributes) {
		_copyGlobal.defineProperty(propertyName, delegateTo, getter, setter, attributes);
	}

	public void delete(int index) {
		_copyGlobal.delete(index);
	}

	public void delete(String name) {
		_copyGlobal.delete(name);
	}

	public boolean equals(Object obj) {
		return _copyGlobal.equals(obj);
	}

	public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return _copyGlobal.execIdCall(f, cx, scope, thisObj, args);
	}

	public Object get(int index, Scriptable start) {
		return _copyGlobal.get(index, start);
	}

	public Object[] getAllIds() {
		return _copyGlobal.getAllIds();
	}

	public int getAttributes(int index) {
		return _copyGlobal.getAttributes(index);
	}

	public int getAttributes(String name) {
		return _copyGlobal.getAttributes(name);
	}

	public String getClassName() {
		return _copyGlobal.getClassName();
	}

	public Object getDefaultValue(Class typeHint) {
		return _copyGlobal.getDefaultValue(typeHint);
	}

	public Object[] getIds() {
		return _copyGlobal.getIds();
	}

	public Scriptable getParentScope() {
		return _copyGlobal.getParentScope();
	}

	public Scriptable getPrototype() {
		return _copyGlobal.getPrototype();
	}

	public boolean has(int index, Scriptable start) {
		return _copyGlobal.has(index, start);
	}

	public int hashCode() {
		return _copyGlobal.hashCode();
	}

	public boolean hasInstance(Scriptable instance) {
		return _copyGlobal.hasInstance(instance);
	}

	public void sealObject() {
		_copyGlobal.sealObject();
	}

	public void setAttributes(int index, int attributes) {
		_copyGlobal.setAttributes(index, attributes);
	}

	public void setAttributes(int index, Scriptable start, int attributes) {
		throw new UnsupportedOperationException("deprecated");
	}

	public void setAttributes(String name, int attributes) {
		_copyGlobal.setAttributes(name, attributes);
	}

	public void setParentScope(Scriptable m) {
		_copyGlobal.setParentScope(m);
	}

	public void setPrototype(Scriptable m) {
		_copyGlobal.setPrototype(m);
	}

	public String toString() {
		return _copyGlobal.toString();
	}

}
