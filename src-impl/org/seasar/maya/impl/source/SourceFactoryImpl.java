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
package org.seasar.maya.impl.source;

import java.util.Iterator;

import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceFactoryImpl extends ParameterAwareImpl 
		implements SourceFactory {

    private static final long serialVersionUID = 3334813227060846723L;

    private Object _context;
    private Class _sourceClass;
    
    public void setSourceClass(Class sourceClass) {
        if(sourceClass == null) {
            throw new IllegalArgumentException();
        }
        _sourceClass = sourceClass;
    }
    
    public Class getSourceClass() {
        if(_sourceClass == null) {
            throw new IllegalStateException();
        }
        return _sourceClass;
    }
    
    public SourceDescriptor getSourceDescriptor(String systemID) {
        if(StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        Class sourceClass = getSourceClass();
        if(sourceClass == null) {
            throw new IllegalStateException();
        }
        SourceDescriptor source = 
            (SourceDescriptor)ObjectUtil.newInstance(sourceClass);
        source.setSystemID(systemID);
        for(Iterator it = iterateParameterNames(); it.hasNext(); ) {
            String key = (String)it.next();
            String value = getParameter(key);
            source.setParameter(key, value);
        }
        return source;
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
