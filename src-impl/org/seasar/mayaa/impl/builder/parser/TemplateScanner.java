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
package org.seasar.mayaa.impl.builder.parser;

import java.io.IOException;

import org.apache.xerces.util.XMLStringBuffer;
import org.cyberneko.html.HTMLScanner;
import org.seasar.mayaa.impl.CONST_IMPL;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateScanner extends HTMLScanner implements CONST_IMPL {

    public static final String HTML_NAMES_ELEMS = HTMLScanner.NAMES_ELEMS;
    public static final String HTML_NAMES_ATTRS = HTMLScanner.NAMES_ATTRS;
    public static final String HTML_DEFAULT_ENCODING = HTMLScanner.DEFAULT_ENCODING;
    public static final String LEXICAL_HANDLER
            = "http://xml.org/sax/properties/lexical-handler";
    public static final String FILTERS
            = "http://cyberneko.org/html/properties/filters";

    protected void unread(int n) {
        fCurrentEntity.offset -= n;
        fCurrentEntity.columnNumber -= n;
    }

    protected void outputCharacters(XMLStringBuffer str, boolean content) {
        if (content && fDocumentHandler != null && fElementCount >= fElementDepth) {
            fEndLineNumber = fCurrentEntity.lineNumber;
            fEndColumnNumber = fCurrentEntity.columnNumber;
            fDocumentHandler.characters(str, locationAugs());
        }
    }

    protected int scanEntityRef(XMLStringBuffer str, boolean content)
            throws IOException {
        str.clear();
        str.append('&');
        while (true) {
            int c = read();
            if (c == ';') {
                str.append(';');
                break;
            }
            if (c == -1) {
                if (fReportErrors) {
                    fErrorReporter.reportWarning("HTML1004", null);
                }
                outputCharacters(str, content);
                return -1;
            }
            if (!Character.isLetterOrDigit((char) c) && c != '#') {
                if (fReportErrors) {
                    fErrorReporter.reportWarning("HTML1004", null);
                }
                unread(1);
                outputCharacters(str, content);
                return -1;
            }
            str.append((char) c);
        }
        outputCharacters(str, content);
        return -1;
    }

}
