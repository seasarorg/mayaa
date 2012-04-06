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
package org.seasar.mayaa.impl.builder.library.tld;

import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.builder.library.TLDLibraryDefinition;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TaglibTagHandler extends TagHandler {

    private TLDLibraryDefinition _library;

    public TaglibTagHandler() {
        super("taglib");
        putHandler(new TagTagHandler(this));
        putHandler(new TagHandler("jsp-version") {
            protected void end(String body) {
                setRequiredVersion(body);
            }
        });
        putHandler(new TagHandler("jspversion") {
            protected void end(String body) {
                setRequiredVersion(body);
            }
        });
        putHandler(new TagHandler("uri") {
            protected void end(String body) {
                setNamespaceURI(SpecificationUtil.createURI(body));
            }
        });
    }

    protected void setRequiredVersion(String requiredVersion) {
        _library.setRequiredVersion(requiredVersion);
    }

    protected void setNamespaceURI(URI namespaceURI) {
        _library.setNamespaceURI(namespaceURI);
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        _library = new TLDLibraryDefinition();
        _library.setSystemID(StringUtil.removeFileProtocol(systemID));
        _library.setLineNumber(lineNumber);
    }

    public TLDLibraryDefinition getLibraryDefinition() {
        return _library;
    }

}
