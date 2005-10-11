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
package org.seasar.maya.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.ParameterAware;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class ParameterAwareImpl implements ParameterAware {

	private Map _parameters;
	
	public void setParameter(String name, String value) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if(value == null) {
        	throw new IllegalParameterValueException(getClass(), name);
        }
        if(_parameters == null) {
            _parameters = new HashMap();
        }
        _parameters.put(name, value);
    }

	public String getParameter(String name) {
		if(StringUtil.isEmpty(name)) {
			throw new IllegalArgumentException();
		}
		return (String)_parameters.get(name);
	}

	public Iterator iterateParameterNames() {
		if(_parameters == null) {
			return NullIterator.getInstance();
		}
		return _parameters.keySet().iterator();
	}

}
