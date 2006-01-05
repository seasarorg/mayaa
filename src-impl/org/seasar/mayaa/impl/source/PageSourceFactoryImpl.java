/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.source;

import java.util.Iterator;

import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.source.PageSourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageSourceFactoryImpl extends ParameterAwareImpl
        implements PageSourceFactory {

    private static final long serialVersionUID = 3334813227060846723L;

    private Object _context;
    private Class _serviceClass;

    public void setServiceClass(Class serviceClass) {
        if(serviceClass == null) {
            throw new IllegalArgumentException();
        }
        _serviceClass = serviceClass;
    }

    public Class getServiceClass() {
        if(_serviceClass == null) {
            throw new IllegalArgumentException();
        }
        return _serviceClass;
    }

    public SourceDescriptor getPageSource(String systemID) {
        if(StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        Class sourceClass = getServiceClass();
        if(sourceClass == null) {
            throw new IllegalStateException();
        }
        SourceDescriptor source =
            (SourceDescriptor)ObjectUtil.newInstance(sourceClass);
        for(Iterator it = iterateParameterNames(); it.hasNext(); ) {
            String key = (String)it.next();
            String value = getParameter(key);
            source.setParameter(key, value);
        }
        source.setSystemID(systemID);
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
