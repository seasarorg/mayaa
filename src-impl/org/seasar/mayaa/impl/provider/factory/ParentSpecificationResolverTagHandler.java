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
import org.seasar.mayaa.engine.specification.ParentSpecificationResolver;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.provider.ServiceProvider;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class ParentSpecificationResolverTagHandler
        extends AbstractParameterAwareTagHandler {

    private ProviderTagHandler _parent;
    private ParentSpecificationResolver _beforeResolver;
    private ParentSpecificationResolver _currentResolver;

    public ParentSpecificationResolverTagHandler(
            ProviderTagHandler parent, ServiceProvider beforeProvider) {
        super("parentSpecificationResolver");
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        if (beforeProvider != null) {
            _beforeResolver = beforeProvider.getParentSpecificationResolver();
        }
        _parent = parent;
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        Class adjusterClass = XMLUtil.getClassValue(attributes, "class", null);
        _currentResolver = (ParentSpecificationResolver) MarshallUtil.marshall(
                adjusterClass, ParentSpecificationResolver.class, _beforeResolver,
                systemID, lineNumber);
        _parent.getServiceProvider().setParentSpecificationResolver(_currentResolver);
    }

    protected void end(String body) {
        if (_currentResolver == null) {
            throw new IllegalStateException();
        }
        _currentResolver = null;
    }

    public ParentSpecificationResolver getParentSpecificationResolver() {
        if (_currentResolver == null) {
            throw new IllegalStateException();
        }
        return _currentResolver;
    }

    public ParameterAware getParameterAware() {
        return getParentSpecificationResolver();
    }

}
