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

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.UnifiedFactory;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.provider.factory.AbstractParameterAwareTagHandler;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FactoryTagHandler
        extends AbstractParameterAwareTagHandler {

    private Class _interfaceClass;
    private UnifiedFactory _beforeFactory;
    private UnifiedFactory _currentFactory;

    public FactoryTagHandler(
            Class interfaceClass, UnifiedFactory beforeFactory) {
        super("factory");
        if (interfaceClass == null) {
            throw new IllegalArgumentException();
        }
        _interfaceClass = interfaceClass;
        _beforeFactory = beforeFactory;
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        Class factoryClass = XMLUtil.getClassValue(
                attributes, "class", null);
        _currentFactory = (UnifiedFactory) MarshallUtil.marshall(
                factoryClass, _interfaceClass, _beforeFactory,
                systemID, lineNumber);
        Class serviceClass = XMLUtil.getClassValue(
                attributes, "serviceClass", null);
        if (serviceClass != null) {
            _currentFactory.setServiceClass(serviceClass);
        } else {
            if (_currentFactory.getServiceClass() == null) {
                throw new IllegalStateException();
            }
        }
    }

    public UnifiedFactory getFactory() {
        if (_currentFactory == null) {
            throw new IllegalStateException();
        }
        return _currentFactory;
    }

    public ParameterAware getParameterAware() {
        return getFactory();
    }

}
