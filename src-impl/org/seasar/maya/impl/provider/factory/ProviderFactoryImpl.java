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

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.source.BootstrapSourceDescriptor;
import org.seasar.maya.impl.util.IOUtil;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProviderFactoryImpl extends ProviderFactory
        implements CONST_IMPL {

    private static final long serialVersionUID = 3581634661222113559L;
    protected static final String KEY_SERVICE =
        ServiceProvider.class.getName();
    private boolean _inithialized;

    private ServiceProvider createServiceProvider(
    		ServletContext servletContext) {
        BootstrapSourceDescriptor source = new BootstrapSourceDescriptor();
        source.setServletContext(servletContext);
        source.setSystemID("/maya.provider");
        if(source.exists()) {
            ProviderHandler handler = new ProviderHandler(servletContext);
            InputStream stream = source.getInputStream();
            try {
                XMLUtil.parse(handler, stream, PUBLIC_PROVIDER10,
                        source.getSystemID(), true, true, false);
                _inithialized = true;
                return handler.getResult();
            } finally {
                IOUtil.close(stream);
            }
        }
        throw new RuntimeException(
                new FileNotFoundException(source.getSystemID()));
    }

    protected boolean isProviderInithialized() {
        return _inithialized;
    }

    public ServiceProvider getServiceProvider(Object context) {
        if(context == null || context instanceof ServletContext == false) {
            throw new IllegalArgumentException();
        }
        ServletContext servletContext = (ServletContext)context;
        ServiceProvider provider =
            (ServiceProvider)servletContext.getAttribute(KEY_SERVICE);
        if(provider == null) {
            provider = createServiceProvider(servletContext);
            servletContext.setAttribute(KEY_SERVICE, provider);
        }
        return provider;
    }

}
