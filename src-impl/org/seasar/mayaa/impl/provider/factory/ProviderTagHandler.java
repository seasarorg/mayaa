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
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.provider.ServiceProvider;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ProviderTagHandler
        extends AbstractParameterAwareTagHandler {

    private ServiceProvider _beforeProvider;
    private ServiceProvider _currentProvider;

    public ProviderTagHandler(ServiceProvider beforeProvider) {
        super("provider");
        _beforeProvider = beforeProvider;
        putHandler(new EngineTagHandler(this, beforeProvider));
        putHandler(new ScriptEnvirionmentTagHandler(this, beforeProvider));
        putHandler(new SpecificationBuilderTagHandler(this, beforeProvider));
        putHandler(new LibraryManagerTagHandler(this, beforeProvider));
        putHandler(new TemplateBuilderTagHandler(this, beforeProvider));
        putHandler(new PathAdjusterTagHandler(this, beforeProvider));
        putHandler(new TemplateAttributeReaderTagHandler(this, beforeProvider));
        putHandler(new ParentSpecificationResolverTagHandler(this, beforeProvider));
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        Class providerClass = XMLUtil.getClassValue(
                attributes, "class", null);
        _currentProvider = (ServiceProvider) MarshallUtil.marshall(
                providerClass, ServiceProvider.class, _beforeProvider,
                systemID, lineNumber);
    }

    public ServiceProvider getServiceProvider() {
        if (_currentProvider == null) {
            throw new IllegalStateException();
        }
        return _currentProvider;
    }

    public ParameterAware getParameterAware() {
        return getServiceProvider();
    }

}
