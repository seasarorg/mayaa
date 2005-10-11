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

import org.seasar.maya.FactoryFactory;
import org.seasar.maya.cycle.factory.CycleFactory;
import org.seasar.maya.impl.cycle.factory.CycleFactoryImpl;
import org.seasar.maya.impl.cycle.web.ApplicationScopeImpl;
import org.seasar.maya.impl.cycle.web.ServiceCycleImpl;
import org.seasar.maya.impl.provider.factory.ProviderFactoryImpl;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.source.factory.SourceFactoryImpl;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FactoryFactoryImpl extends FactoryFactory {

    private static final long serialVersionUID = -1393736148065197812L;

    protected CycleFactory createCycleFactory(Object context) {
        // TODO Žb’è
        CycleFactoryImpl factory = new CycleFactoryImpl();
        factory.setCycleClass(ServiceCycleImpl.class);
        ApplicationScopeImpl application = new ApplicationScopeImpl();
        application.setUnderlyingContext(context);
        factory.setApplicationScope(application);
        return factory;
    }

    protected ProviderFactory createProviderFactory(Object context) {
        // TODO Žb’è
        return new ProviderFactoryImpl(context);
    }

    protected SourceFactory createSourceFactory(Object context) {
        // TODO Žb’è
        SourceFactoryImpl factory = new SourceFactoryImpl();
        factory.setSourceClass(PageSourceDescriptor.class);
        factory.setParameter("folder", "/WEB-INF/page");
        return factory;
    }
    
}
