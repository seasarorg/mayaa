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
package org.seasar.mayaa.impl.provider.factory;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.builder.SpecificationBuilder;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.builder.SpecificationBuilderImpl;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.provider.ServiceProvider;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationBuilderTagHandler extends AbstractParameterAwareTagHandler {

    private static Class<SpecificationBuilder> INTERFACE_CLASS = SpecificationBuilder.class;
    private static Class<? extends SpecificationBuilder> DEFAULT_IMPL_CLASS = SpecificationBuilderImpl.class;

    private ProviderTagHandler _parent;
    private SpecificationBuilder _beforeBuilder;
    private SpecificationBuilder _currentBuilder;

    public SpecificationBuilderTagHandler(
            ProviderTagHandler parent, ServiceProvider beforeProvider) {
        super("specificationBuilder");
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        if (beforeProvider != null) {
            _beforeBuilder = beforeProvider.getSpecificationBuilder();
        }
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        Class<?> builderClass = XMLUtil.getClassValue(attributes, "class", DEFAULT_IMPL_CLASS);
        _currentBuilder = MarshallUtil.marshall(
                builderClass, INTERFACE_CLASS, _beforeBuilder,
                systemID, lineNumber);
        _parent.getServiceProvider().setSpecificationBuilder(_currentBuilder);
    }

    protected void end(String body) {
        _currentBuilder = null;
    }

    public ParameterAware getParameterAware() {
        if (_currentBuilder == null) {
            throw new IllegalStateException();
        }
        return _currentBuilder;
    }

}
