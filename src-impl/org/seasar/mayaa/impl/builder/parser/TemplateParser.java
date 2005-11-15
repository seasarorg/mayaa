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
package org.seasar.maya.impl.builder.parser;

import org.apache.xerces.parsers.AbstractSAXParser;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.HTMLScanner;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateParser extends AbstractSAXParser {
    
    public TemplateParser(HTMLScanner scanner) {
        super(new TemplateParserConfiguration(scanner));
    }

    private static class TemplateParserConfiguration extends HTMLConfiguration {

    	public TemplateParserConfiguration(HTMLScanner scanner) {
            AdditionalHandlerFilter starter = new AdditionalHandlerFilter();
            addComponent(starter);
            setProperty(TemplateScanner._NAMES_ELEMS, "match");
            setProperty(TemplateScanner._NAMES_ATTRS, "no-change");
            setProperty(TemplateScanner.FILTERS, new XMLDocumentFilter[] { starter });
            fDocumentScanner = scanner;
            fDocumentScanner.reset(this);
        }
    
    }
    
}
