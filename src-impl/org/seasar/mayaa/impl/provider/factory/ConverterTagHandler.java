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
import org.seasar.mayaa.builder.library.converter.PropertyConverter;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ConverterTagHandler
        extends AbstractParameterAwareTagHandler {

    private LibraryManagerTagHandler _parent;
    private PropertyConverter _converter;

    public ConverterTagHandler(LibraryManagerTagHandler parent) {
        super("converter");
        if (parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        _converter = (PropertyConverter) XMLUtil.getObjectValue(
                attributes, "class", PropertyConverter.class);
        if (_converter == null) {
            throw new IllegalStateException();
        }
        _converter.setSystemID(systemID);
        _converter.setLineNumber(lineNumber);
        String name = XMLUtil.getStringValue(attributes, "name", "");
        _parent.getLibraryManager().addPropertyConverter(name, _converter);
    }

    protected void end(String body) {
        _converter = null;
    }

    public ParameterAware getParameterAware() {
        if (_converter == null) {
            throw new IllegalStateException();
        }
        return _converter;
    }

}
