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

import java.io.IOException;

import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.cyberneko.html.HTMLScanner;
import org.seasar.maya.impl.CONST_IMPL;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateScanner extends HTMLScanner implements CONST_IMPL {

    public static final String DEFAULT_ENCODING = HTMLScanner.DEFAULT_ENCODING;
    public static final String NOTIFY_CHAR_REFS = HTMLScanner.NOTIFY_CHAR_REFS;
    public static final String NOTIFY_XML_BUILTIN_REFS = HTMLScanner.NOTIFY_XML_BUILTIN_REFS;
    public static final String NOTIFY_HTML_BUILTIN_REFS = HTMLScanner.NOTIFY_HTML_BUILTIN_REFS;
    public static final String NAMES_ELEMS = HTMLScanner.NAMES_ELEMS;
    public static final String NAMES_ATTRS = HTMLScanner.NAMES_ATTRS;
    public static final String LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
    public static final String FILTERS = "http://cyberneko.org/html/properties/filters";
    
    private static final XMLAttributes ATTR_EMPTY = new XMLAttributesImpl();
    private static final QName NAME_CODELET = new QName("", "%", "%", "");
    
    public TemplateScanner() {
        HTMLScanner.Scanner scanner = createScanner();
    	if(scanner != null) {
    	    fContentScanner = scanner;
    	}
    }
    
    protected HTMLScanner.Scanner createScanner() {
        return new CodeletScanner();
    }
    
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
            if (!Character.isLetterOrDigit((char)c) && c != '#') {
                if (fReportErrors) {
                    fErrorReporter.reportWarning("HTML1004", null);
                }
                unread(1);
                outputCharacters(str, content);
                return -1;
            }
            str.append((char)c);
        }
        outputCharacters(str, content);
        return -1;
    }

    public class CodeletScanner extends HTMLScanner.ContentScanner {
    	
    	protected String scanStartElement(boolean[] empty) throws IOException {
            int c = read();
        	if(c == '%') {
                setScannerState(STATE_CONTENT);
                fDocumentHandler.startElement(NAME_CODELET, ATTR_EMPTY, locationAugs());
                return null; 
            }
        	unread(1);
			return super.scanStartElement(empty);
		}

        protected void scanCharacters() throws IOException {
            int newlines = skipNewlines();
            if (newlines == 0 && fCurrentEntity.offset == fCurrentEntity.length) {
                return;
            }
            char c;
            int offset = fCurrentEntity.offset - newlines;
            for (int i = offset; i < fCurrentEntity.offset; i++) {
                fCurrentEntity.buffer[i] = '\n';
            }
            boolean codelet = false;
            while (fCurrentEntity.offset < fCurrentEntity.length) {
                c = fCurrentEntity.buffer[fCurrentEntity.offset];
                if (c == '<' || c == '&' || c == '\n' || c == '\r') {
                    break;
                }
                fCurrentEntity.offset++;
                fCurrentEntity.columnNumber++;
                if(c == '%') {
                    c = fCurrentEntity.buffer[fCurrentEntity.offset];
                    if(c == '>') {
                        codelet = true;
                        fCurrentEntity.offset--;
                        fCurrentEntity.columnNumber--;
                        break;
                    }
                }
            }
            if (fCurrentEntity.offset > offset && 
                fDocumentHandler != null && fElementCount >= fElementDepth) {
                fString.setValues(fCurrentEntity.buffer, offset, fCurrentEntity.offset - offset);
                fEndLineNumber = fCurrentEntity.lineNumber;
                fEndColumnNumber = fCurrentEntity.columnNumber;
                fDocumentHandler.characters(fString, locationAugs());
            }
            if(codelet) {
                fCurrentEntity.offset += 2;
                fCurrentEntity.columnNumber += 2;
                fDocumentHandler.endElement(NAME_CODELET, locationAugs());
            }
        }
    }
    
}
