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
package org.seasar.mayaa.impl.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.UnifiedFactory;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.xml.XMLHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class UnifiedFactoryHandler<T extends UnifiedFactory> extends XMLHandler
        implements CONST_IMPL {

    private static final Log LOG =
        LogFactory.getLog(UnifiedFactoryHandler.class);

    private FactoryTagHandler<T> _rootHandler;

    public UnifiedFactoryHandler(Class<T> interfaceClass, T beforeFactory) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException();
        }
        _rootHandler = new FactoryTagHandler<>(interfaceClass, beforeFactory);
        setRootHandler(_rootHandler);
        setLog(LOG);
        getEntityMap().put(PUBLIC_FACTORY10, "mayaa-factory_1_0.dtd");
    }

    public T getUnifiedFactory() {
        return _rootHandler.getFactory();
    }

}
