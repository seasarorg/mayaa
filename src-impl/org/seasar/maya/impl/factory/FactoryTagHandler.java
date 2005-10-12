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

import org.seasar.maya.ParameterAware;
import org.seasar.maya.UnifiedFactory;
import org.seasar.maya.impl.provider.factory.AbstractParameterAwareTagHandler;
import org.seasar.maya.impl.util.XMLUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FactoryTagHandler
        extends AbstractParameterAwareTagHandler {

    private UnifiedFactory _factory;
    
    public FactoryTagHandler() {
        super("factory");
    }

    protected void start(
    		Attributes attributes, String systemID, int lineNumber) {
        _factory = (UnifiedFactory)XMLUtil.getObjectValue(
                attributes, "class", null, UnifiedFactory.class);
        Class serviceClass = XMLUtil.getClassValue(
                attributes, "serviceClass", null);
        _factory.setServiceClass(serviceClass);
    }

    public UnifiedFactory getFactory() {
        if(_factory == null) {
            throw new IllegalStateException();
        }
        return _factory;
    }
    
    public ParameterAware getParameterAware() {
        return getFactory();
    }

}
