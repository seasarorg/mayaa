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
package org.seasar.maya.impl.source.factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceFactoryImpl implements SourceFactory {

    private static final long serialVersionUID = 3334813227060846723L;

    private Class _sourceClass;
    private Map _sourceParams;
    
    public void setSourceClass(Class sourceClass) {
        if(sourceClass == null) {
            throw new IllegalArgumentException();
        }
        _sourceClass = sourceClass;
    }
    
    protected Class getSourceClass() {
        if(_sourceClass == null) {
            throw new IllegalStateException();
        }
        return _sourceClass;
    }
    
    protected Map getSourceParams() {
        if(_sourceParams == null) {
            return Collections.EMPTY_MAP;
        }
        return _sourceParams;
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
        Map sourceParams = getSourceParams();
        for(Iterator it = sourceParams.keySet().iterator(); it.hasNext(); ) {
            String key = (String)it.next();
            String value = (String)sourceParams.get(key);
            source.setParameter(key, value);
        }
        return source;
    }

    // Parameterizable implements ------------------------------------
    
    public void setParameter(String name, String value) {
        if(StringUtil.isEmpty(name) || value == null) {
            throw new IllegalArgumentException();
        }
        if(_sourceParams == null) {
            _sourceParams = new HashMap();
        }
        _sourceParams.put(name, value);
    }

}
