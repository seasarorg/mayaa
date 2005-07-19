/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.builder.parser;

import org.apache.xerces.parsers.AbstractSAXParser;
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
            setProperty(TemplateScanner.NAMES_ELEMS, "match");
            setProperty(TemplateScanner.NAMES_ATTRS, "no-change");
            fDocumentScanner = scanner;
            fDocumentScanner.reset(this);
        }
    
    }
    
}
