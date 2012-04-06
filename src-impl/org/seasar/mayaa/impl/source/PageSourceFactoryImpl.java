/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.IllegalParameterValueException;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.source.PageSourceFactory;
import org.seasar.mayaa.source.SourceHolder;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageSourceFactoryImpl extends ParameterAwareImpl
        implements PageSourceFactory {

    private static final long serialVersionUID = 3334813227060846723L;
    private static final Log LOG = LogFactory.getLog(PageSourceFactoryImpl.class);

    private Object _context;
    private Class _serviceClass;
    private List _parameterNames;
    private List _parameterValues;

    public void setServiceClass(Class serviceClass) {
        if (serviceClass == null) {
            throw new IllegalArgumentException();
        }
        _serviceClass = serviceClass;
    }

    public Class getServiceClass() {
        if (_serviceClass == null) {
            throw new IllegalArgumentException();
        }
        return _serviceClass;
    }

    public SourceDescriptor getPageSource(String systemID) {
        if (StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        Class sourceClass = getServiceClass();
        if (sourceClass == null) {
            throw new IllegalStateException("serviceClass is null");
        }
        SourceDescriptor source =
            (SourceDescriptor) ObjectUtil.newInstance(sourceClass);
        if (_parameterNames != null) {
            for (int i = 0; i < _parameterNames.size(); i++) {
                String key = (String) _parameterNames.get(i);
                String value = (String) _parameterValues.get(i);
                source.setParameter(key, value);
            }
        }
        source.setSystemID(systemID);
        return source;
    }

    // ContextAware implements -------------------------------------

    public void setUnderlyingContext(Object context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        _context = context;
    }

    public Object getUnderlyingContext() {
        if (_context == null) {
            throw new IllegalStateException("context is null");
        }
        return _context;
    }

    // ParameterAware implements -------------------------------------

    public void setParameter(String name, String value) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if (value == null) {
            throw new IllegalParameterValueException(getClass(), name);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(name + ": "+ value);
        }
        if (_parameterNames == null) {
            _parameterNames = new ArrayList();
        }
        if (_parameterValues == null) {
            _parameterValues = new ArrayList();
        }
        _parameterNames.add(name);
        _parameterValues.add(value);

        Class sourceHolderClass = null;
        if ("folder".equals(name)) {
        	if (IS_SECURE_WEB && ("".equals(value) || "/".equals(value))) {
        		sourceHolderClass = WebContextRootResourceHolder.class;
        	} else {
        		sourceHolderClass = WebContextFolderSourceHolder.class;
        	}
        } else if ("absolutePath".equals(name)) {
            sourceHolderClass = AbsolutePathSourceHolder.class;
        } else {
            super.setParameter(name, value);
            return;
        }
        SourceHolder holder =
            (SourceHolder) ObjectUtil.newInstance(sourceHolderClass);
        try {
            holder.setRoot(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalParameterValueException(
                    sourceHolderClass, name + "=" + value);
        }
        SourceHolderFactory.appendSourceHolder(holder);
    }

    // first value only
    public String getParameter(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if (_parameterNames == null) {
            return null;
        }
        for (int i = 0; i < _parameterNames.size(); i++) {
            if (name.equals(_parameterNames.get(i))) {
                return (String) _parameterValues.get(i);
            }
        }
        return null;
    }

    public Iterator iterateParameterNames() {
        if (_parameterNames == null) {
            return NullIterator.getInstance();
        }
        return _parameterNames.iterator();
    }

}
