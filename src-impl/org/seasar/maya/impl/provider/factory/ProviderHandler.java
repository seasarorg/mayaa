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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.xml.XMLHandler;
import org.seasar.maya.provider.ServiceProvider;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProviderHandler extends XMLHandler
        implements CONST_IMPL {

    private static Log LOG = LogFactory.getLog(ProviderHandler.class); 
    
    private ServiceTagHandler _rootHandler;
    
    public ProviderHandler(
            Object context, ServiceProvider unmarshall) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        _rootHandler = new ServiceTagHandler(unmarshall);
        setRootHandler(_rootHandler);
        setLog(LOG);
        getEntityMap().put(PUBLIC_PROVIDER10, "maya-provider_1_0.dtd");
    }
    
    public ServiceProvider getServiceProvider() {
        return _rootHandler.getServiceProvider();
    }
    
}
