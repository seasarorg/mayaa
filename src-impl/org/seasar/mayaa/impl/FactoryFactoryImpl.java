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
package org.seasar.mayaa.impl;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.UnifiedFactory;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.cycle.web.ApplicationScopeImpl;
import org.seasar.mayaa.impl.factory.UnifiedFactoryHandler;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FactoryFactoryImpl extends FactoryFactory
        implements CONST_IMPL {

    private static final long serialVersionUID = -1393736148065197812L;
    private static final Log LOG = LogFactory.getLog(FactoryFactoryImpl.class);

    protected boolean checkInterface(Class clazz) {
        if (clazz != null && clazz.isInterface()
                && UnifiedFactory.class.isAssignableFrom(clazz)) {
            return true;
        }
        return false;
    }

    protected UnifiedFactory marshallFactory(
            Class interfaceClass, Object context,
            SourceDescriptor source, UnifiedFactory beforeFactory) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        String systemID = source.getSystemID();
        UnifiedFactory factory;
        if (source.exists()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("marshall factory: " + source.getSystemID());
            }
            UnifiedFactoryHandler handler =
                new UnifiedFactoryHandler(interfaceClass, beforeFactory);
            InputStream stream = source.getInputStream();
            try {
                XMLUtil.parse(handler, stream, PUBLIC_FACTORY10,
                        systemID, true, true, false);
            } catch (Throwable t) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Factory parse error on " + systemID, t);
                }
                factory = beforeFactory;
            } finally {
                IOUtil.close(stream);
            }
            factory = handler.getUnifiedFactory();
        } else {
            factory = beforeFactory;
        }
        if (factory != null) {
            factory.setUnderlyingContext(context);
        }
        return factory;
    }

    protected UnifiedFactory getFactory(
            Class interfaceClass, Object context) {
        if (checkInterface(interfaceClass) == false || context == null) {
            throw new IllegalArgumentException();
        }
        String systemID = interfaceClass.getName();
        SourceDescriptor source = MarshallUtil.getDefaultSource(
                systemID, UnifiedFactoryHandler.class);
        UnifiedFactory factory = marshallFactory(
                interfaceClass, context, source, null);
        Iterator it = MarshallUtil.iterateMetaInfSources(systemID);
        while (it.hasNext()) {
            source = (SourceDescriptor) it.next();
            factory = marshallFactory(interfaceClass, context, source, factory);
        }
        source = getBootstrapSource(
                ApplicationSourceDescriptor.WEB_INF, systemID);
        factory = marshallFactory(interfaceClass, context, source, factory);
        return factory;
    }

    protected ApplicationScope getApplicationScope(Object context) {
        ApplicationScope application = new ApplicationScopeImpl();
        application.setUnderlyingContext(context);
        return application;
    }

    protected SourceDescriptor getBootstrapSource(
            String root, String systemID, Object context) {
        ApplicationSourceDescriptor appSource =
            new ApplicationSourceDescriptor();
        if (StringUtil.hasValue(root)) {
            appSource.setRoot(root);
        }
        appSource.setSystemID(systemID);
        appSource.setApplicationScope(getApplicationScope(context));
        return appSource;
    }

}
