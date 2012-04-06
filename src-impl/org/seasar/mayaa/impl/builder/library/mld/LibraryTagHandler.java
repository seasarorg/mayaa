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
package org.seasar.mayaa.impl.builder.library.mld;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.factory.AbstractParameterAwareTagHandler;
import org.seasar.mayaa.impl.util.StringUtil;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryTagHandler
        extends AbstractParameterAwareTagHandler {

    private LibraryDefinitionImpl _libraryDefinition;

    public LibraryTagHandler() {
        super("library");
        putHandler(new ConverterTagHandler(this, this));
        putHandler(new ProcessorTagHandler(this));
        putHandler(new PropertySetTagHandler("propertySet", this, this));
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        String uri = attributes.getValue("uri");
        _libraryDefinition = new LibraryDefinitionImpl();
        _libraryDefinition.setNamespaceURI(SpecificationUtil.createURI(uri));
        _libraryDefinition.setSystemID(StringUtil.removeFileProtocol(systemID));
        _libraryDefinition.setLineNumber(lineNumber);
    }

    public LibraryDefinitionImpl getLibraryDefinition() {
        if (_libraryDefinition == null) {
            throw new IllegalStateException();
        }
        return _libraryDefinition;
    }

    public ParameterAware getParameterAware() {
        return getLibraryDefinition();
    }

}
