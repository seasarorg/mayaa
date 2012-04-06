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
import org.seasar.mayaa.builder.injection.InjectionResolver;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ResolverTagHandler
        extends AbstractParameterAwareTagHandler {

    private TemplateBuilderTagHandler _parent;
    private InjectionResolver _resolver;

    public ResolverTagHandler(TemplateBuilderTagHandler parent) {
        super("resolver");
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        _resolver = (InjectionResolver) XMLUtil.getObjectValue(
                attributes, "class", InjectionResolver.class);
        if (_resolver == null) {
            throw new IllegalStateException();
        }
        _resolver.setSystemID(systemID);
        _resolver.setLineNumber(lineNumber);
        _parent.getTemplateBuilder().addInjectionResolver(_resolver);
    }

    protected void end(String body) {
        _resolver = null;
    }

    public ParameterAware getParameterAware() {
        if (_resolver == null) {
            throw new IllegalStateException();
        }
        return _resolver;
    }

}
