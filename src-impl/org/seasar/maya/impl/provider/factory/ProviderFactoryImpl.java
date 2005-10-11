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
package org.seasar.maya.impl.provider.factory;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.source.BootstrapSourceDescriptor;
import org.seasar.maya.impl.util.IOUtil;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProviderFactoryImpl
        implements ProviderFactory, CONST_IMPL {

    private static final long serialVersionUID = 3581634661222113559L;

    private ServletContext _context;
    private ServiceProvider _provider;
    
    public ProviderFactoryImpl(Object context) {
        if(context == null || context instanceof ServletContext == false) {
            throw new IllegalArgumentException();
        }
        _context = (ServletContext)context;
    }
    
    protected ServiceProvider createServiceProvider(
    		ServletContext servletContext, SourceDescriptor source,
            ServiceProvider unmarshall) {
        if(source.exists()) {
            ProviderHandler handler = 
                new ProviderHandler(servletContext, unmarshall);
            InputStream stream = source.getInputStream();
            try {
                XMLUtil.parse(handler, stream, PUBLIC_PROVIDER10,
                        source.getSystemID(), true, true, false);
                return handler.getResult();
            } finally {
                IOUtil.close(stream);
            }
        }
        return unmarshall;
    }

    protected ServiceProvider createServiceProvider(
            ServletContext servletContext) {
        BootstrapSourceDescriptor source = new BootstrapSourceDescriptor();
        source.setServletContext(servletContext);
        source.setSystemID("/maya.provider");
        ServiceProvider provider = 
            createServiceProvider(servletContext, source, null);
        return provider;
    }

    public ServiceProvider getServiceProvider() {
        if(_provider == null) {
            _provider = createServiceProvider(_context);
        }
        return _provider;
    }

}
