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
import org.seasar.mayaa.builder.library.TemplateAttributeReader;
import org.seasar.mayaa.impl.MarshallUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.provider.ServiceProvider;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TemplateAttributeReaderTagHandler
        extends AbstractParameterAwareTagHandler {

    private ProviderTagHandler _parent;
    private TemplateAttributeReader _beforeReader;
    private TemplateAttributeReader _currentReader;

    public TemplateAttributeReaderTagHandler(
            ProviderTagHandler parent, ServiceProvider beforeProvider) {
        super("templateAttributeReader");
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        if (beforeProvider != null) {
            _beforeReader = beforeProvider.getTemplateAttributeReader();
        }
        putHandler(new IgnoreAttributeTagHandler(this));
        putHandler(new AliasAttributeTagHandler(this));
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        Class builderClass = XMLUtil.getClassValue(
                attributes, "class", null);
        _currentReader = (TemplateAttributeReader) MarshallUtil.marshall(
                builderClass, TemplateAttributeReader.class, _beforeReader,
                systemID, lineNumber);
        _parent.getServiceProvider().setTemplateAttributeReader(_currentReader);
    }

    protected void end(String body) {
        if (_currentReader == null) {
            throw new IllegalStateException();
        }
        _currentReader = null;
    }

    public TemplateAttributeReader getTemplateAttributeReader() {
        if (_currentReader == null) {
            throw new IllegalStateException();
        }
        return _currentReader;
    }

    public ParameterAware getParameterAware() {
        return getTemplateAttributeReader();
    }

}
