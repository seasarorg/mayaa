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

import java.util.Iterator;

import org.seasar.maya.FactoryFactory;
import org.seasar.maya.cycle.CycleFactory;
import org.seasar.maya.impl.cycle.CycleFactoryImpl;
import org.seasar.maya.impl.cycle.web.ServiceCycleImpl;
import org.seasar.maya.impl.provider.ProviderFactoryImpl;
import org.seasar.maya.impl.provider.ServiceProviderImpl;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.source.SourceFactoryImpl;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.provider.ProviderFactory;
import org.seasar.maya.source.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FactoryFactoryImpl extends FactoryFactory {

    private static final long serialVersionUID = -1393736148065197812L;

    protected CycleFactory createCycleFactory(Object context) {
        // TODO Žb’è
        CycleFactoryImpl factory = new CycleFactoryImpl();
        factory.setServiceClass(ServiceCycleImpl.class);
        factory.setUnderlyingContext(context);
        return factory;
    }

    protected ProviderFactory createProviderFactory(Object context) {
        // TODO Žb’è
    	ProviderFactoryImpl factory = new ProviderFactoryImpl();
        factory.setServiceClass(ServiceProviderImpl.class);
    	factory.setUnderlyingContext(context);
    	return factory;
    }

    protected SourceFactory createSourceFactory(Object context) {
        // TODO Žb’è
        SourceFactoryImpl factory = new SourceFactoryImpl();
        factory.setServiceClass(PageSourceDescriptor.class);
        factory.setParameter("folder", "/WEB-INF/page");
        return factory;
    }

    // Parameterizable implements ------------------------------------

	public void setParameter(String name, String value) {
        // do nothing.
	}

	public String getParameter(String name) {
		return null;
	}

	public Iterator iterateParameterNames() {
		return NullIterator.getInstance();
	}
    
}
