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
package org.seasar.maya.impl.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.UnifiedFactory;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.xml.XMLHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class UnifiedFactoryHandler extends XMLHandler
        implements CONST_IMPL {

    private static Log LOG = LogFactory.getLog(UnifiedFactoryHandler.class); 
    
    private FactoryTagHandler _rootHandler;
    
    public UnifiedFactoryHandler() {
        _rootHandler = new FactoryTagHandler();
        setRootHandler(_rootHandler);
        setLog(LOG);
        getEntityMap().put(PUBLIC_FACTORY10, "maya-factory_1_0.dtd");
    }
    
    public UnifiedFactory getUnifiedFactory() {
        return _rootHandler.getFactory();
    }
    
}
