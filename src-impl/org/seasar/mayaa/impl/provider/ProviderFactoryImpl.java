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
package org.seasar.mayaa.impl.provider;

import java.io.InputStream;
import java.util.Iterator;

import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.provider.factory.ServiceProviderHandler;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.provider.ProviderFactory;
import org.seasar.mayaa.provider.ServiceProvider;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProviderFactoryImpl extends ParameterAwareImpl
        implements ProviderFactory, CONST_IMPL {

    private static final long serialVersionUID = 3581634661222113559L;

    private Object _context;
    private Class _serviceClass;
    private ServiceProvider _provider;

    public boolean isServiceProviderInitialized() {
        return _provider != null;
    }

    protected ServiceProvider marshallServiceProvider(
            SourceDescriptor source, ServiceProvider beforeProvider) {
        if (source.exists()) {
            ServiceProviderHandler handler =
                new ServiceProviderHandler(beforeProvider);
            InputStream stream = source.getInputStream();
            try {
                XMLUtil.parse(handler, stream, PUBLIC_PROVIDER10,
                        source.getSystemID(), true, true, false);
                return handler.getServiceProvider();
            } finally {
                IOUtil.close(stream);
            }
        }
        return beforeProvider;
    }

    protected ServiceProvider getServiceProvider(Object context) {
        final String systemID = "org.seasar.mayaa.provider.ServiceProvider";
        SourceDescriptor source = MarshallUtil.getDefaultSource(
                systemID, ServiceProviderHandler.class);
        ServiceProvider provider = marshallServiceProvider(source, null);
        Iterator it = MarshallUtil.iterateMetaInfSources(systemID);
        while (it.hasNext()) {
            source = (SourceDescriptor) it.next();
            provider = marshallServiceProvider(source, provider);
        }
        source = FactoryFactory.getBootstrapSource(
                ApplicationSourceDescriptor.WEB_INF, systemID);
        provider = marshallServiceProvider(source, provider);
        return provider;
    }

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

    public ServiceProvider getServiceProvider() {
        if (_provider == null) {
            _provider = getServiceProvider(_context);
        }
        return _provider;
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
            throw new IllegalStateException();
        }
        return _context;
    }

}
