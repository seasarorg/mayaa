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
import org.seasar.maya.UnifiedFactory;
import org.seasar.maya.cycle.CycleFactory;
import org.seasar.maya.impl.cycle.CycleFactoryImpl;
import org.seasar.maya.impl.cycle.web.ServiceCycleImpl;
import org.seasar.maya.impl.provider.ProviderFactoryImpl;
import org.seasar.maya.impl.provider.ServiceProviderImpl;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.source.SourceFactoryImpl;
import org.seasar.maya.provider.ProviderFactory;
import org.seasar.maya.source.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FactoryFactoryImpl extends FactoryFactory {

    private static final long serialVersionUID = -1393736148065197812L;

    protected UnifiedFactory createFactory(
    		Class interfaceClass, Object context) {
    	if(interfaceClass == null || context == null) {
    		throw new IllegalArgumentException();
    	}
        // TODO Žb’è
        if(CycleFactory.class.isAssignableFrom(interfaceClass)) {
	    	CycleFactoryImpl factory = new CycleFactoryImpl();
	        factory.setServiceClass(ServiceCycleImpl.class);
	        factory.setUnderlyingContext(context);
	        return factory;
        }
        if(ProviderFactory.class.isAssignableFrom(interfaceClass)) {
	    	ProviderFactoryImpl factory = new ProviderFactoryImpl();
	        factory.setServiceClass(ServiceProviderImpl.class);
	    	factory.setUnderlyingContext(context);
	    	return factory;
	    }
        if(SourceFactory.class.isAssignableFrom(interfaceClass)) {
	        SourceFactoryImpl factory = new SourceFactoryImpl();
	        factory.setServiceClass(PageSourceDescriptor.class);
	        factory.setParameter("folder", "/WEB-INF/page");
	        return factory;
	    }
        throw new IllegalArgumentException();
    }
    
}
