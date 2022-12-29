/*
 * Copyright 2004-2022 the Seasar Foundation and the Others.
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
import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityHandler;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.seasar.mayaa.impl.builder.parser.HtmlTokenizer.TagToken;
import org.xml.sax.Locator;

/**
 * 
 * 外部の文書宣言は解決しない
 * フォーマル公開識別子については妥当性の検証を行わない。
 * HTML文字参照以外は解決しない
 */
public class HtmlStandardScanner implements XMLComponent, XMLDocumentScanner, XMLEntityHandler {

    static final String NS_URI_HTML = "http://www.w3.org/1999/xhtml";

    static final String NS_URI_MATHML = "http://www.w3.org/1998/Math/MathML";

    static final String NS_URI_SVG = "http://www.w3.org/2000/svg";

    static final String NS_URI_XLINK = "http://www.w3.org/1999/xlink";

    static final String NS_URI_XML = "http://www.w3.org/XML/1998/namespace";

    static final String NS_URI_XMLNS = "http://www.w3.org/2000/xmlns/";

    static final Attributes EMPTY_ATTRIBUTES = new Attributes();

    static final Pattern REGEX_WHITESPACE_ONLY = Pattern.compile("\\s+");

    static final QName QN_HTML = new QName(null, "html", "html", NS_URI_HTML);
    static final QName QN_HEAD = new QName(null, "head", "head", NS_URI_HTML);
    static final QName QN_BODY = new QName(null, "body", "body", NS_URI_HTML);
    static final QName QN_TEMPLATE = new QName(null, "template", "template", NS_URI_HTML);

    /**  */
    XMLDocumentHandler documentHandler = null;

    HtmlTokenizer tokenizer;

    //ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
    // CharBuffer buffer = CharBuffer.allocate(8 * 1024);
    XMLInputSource inputSource;

    @Override
    public void setInputSource(XMLInputSource inputSource) throws IOException {
        tokenizer.setInputSource(inputSource);
        this.inputSource = inputSource;
    }

    @Override
    public void setDocumentHandler(XMLDocumentHandler handler) {
        this.documentHandler = handler;
    }

    @Override
    public XMLDocumentHandler getDocumentHandler() {
        return documentHandler;
    }

    // FEATURES
    public static final String FEATURE_PREFIX = "http://mayaa.seasar.org/parser/feature/";
    public static final String FEATURE_DELETE_UNEXPECTED_ELEMENT = FEATURE_PREFIX + "delete-unexpected-element";
    public static final String FEATURE_INSERT_IMPLIED_ELEMENT = FEATURE_PREFIX + "insert-implied-element";
    public static final String FEATURE_DOCUMENT_FRAGMENT = FEATURE_PREFIX + "document-fragment";

    boolean featureInsertImpliedElement = false;
    boolean featureDeleteUnexpectedElement = false;
    boolean featureDocumentFragment = true;
    
    @Override
    public String[] getRecognizedFeatures() {
        return new String[] {
            FEATURE_DELETE_UNEXPECTED_ELEMENT,
            FEATURE_INSERT_IMPLIED_ELEMENT,
            FEATURE_DOCUMENT_FRAGMENT,
        };
    }

    @Override
    public Boolean getFeatureDefault(String featureId) {
        switch (featureId) {
            case FEATURE_DELETE_UNEXPECTED_ELEMENT: return Boolean.FALSE;
            case FEATURE_INSERT_IMPLIED_ELEMENT: return Boolean.FALSE;
            case FEATURE_DOCUMENT_FRAGMENT: return Boolean.TRUE;
            default: return Boolean.FALSE;
        }
    }

    @Override
    public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
        switch (featureId) {
            case FEATURE_DELETE_UNEXPECTED_ELEMENT:
                featureDeleteUnexpectedElement = state;
                break;
            case FEATURE_INSERT_IMPLIED_ELEMENT:
                featureInsertImpliedElement = state;
                break;
            case FEATURE_DOCUMENT_FRAGMENT:
                featureDocumentFragment = state;
                break;
        }
    }
    // FEATURES:END

    // PROPERTEIS
    static final String ERROR_REPORTER = Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    @Override
    public String[] getRecognizedProperties() {
        return new String[] {
        };
    }

    @Override
    public Object getPropertyDefault(String propertyId) {
        return null;
    }

    @Override
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
    }
    // PROPERTEIS:END

    // INTERNAL UTILS
    boolean matchOneOfThese(String target, String... compare) {
        for (String c : compare) {
            if (c.equals(target)) {
                return true;
            }
        }
        return false;
    }

    QName createHtmlQName(String tagName) {
        return new QName(null, tagName, tagName, NS_URI_HTML);
    }
    // INTERNAL UTILS:END

    /**
     * Initially, the stack of open elements is empty.
     * https://html.spec.whatwg.org/multipage/parsing.html#the-stack-of-open-elements
     */
    Stack<QName> unclosedElementStack = new Stack<>();
    /*
     * Document Handler
     */
    enum InsertionMode {
        Initial(TokenHandlerInitial.class),
        BeforeHtml(TokenHandlerBeforeHtml.class),
        BeforeHead(TokenHandlerBeforeHead.class),
        InHead(TokenHandlerInHead.class),
        InHeadNoScript(TokenHandlerBase.class),
        AfterHead(TokenHandlerAfterHead.class),
        InBody(TokenHandlerInBody.class),
        Text(TokenHandlerBase.class),
        InTable(TokenHandlerBase.class),
        InTableText(TokenHandlerBase.class),
        InCaption(TokenHandlerBase.class),
        InColumnGroup(TokenHandlerBase.class),
        InTableBody(TokenHandlerBase.class),
        InRow(TokenHandlerBase.class),
        InCell(TokenHandlerBase.class),
        InSelect(TokenHandlerBase.class),
        InSelectInTable(TokenHandlerBase.class),
        InTemplate(TokenHandlerBase.class),
        AfterBody(TokenHandlerAfterBody.class),
        InFrameset(TokenHandlerBase.class),
        AfterFrameset(TokenHandlerBase.class),
        AfterAfterBody(TokenHandlerBase.class),
        AfterAfterFrameset(TokenHandlerBase.class);

        Class<? extends TokenHandler> handlerClass;
        TokenHandler handler;

        InsertionMode(Class<? extends TokenHandler> handlerClass) {
            this.handlerClass = handlerClass;
        }
    }

    InsertionMode insertionMode = InsertionMode.Initial;

    class TokenHandlerInitial extends TokenHandlerBase {
        @Override
        public void emitText(HtmlTokenizer tokenizer, HtmlLocation location, String text) {
            // ignore whitespaces before doctype, otherwise change insertion mode to "before html"
            if (REGEX_WHITESPACE_ONLY.matcher(text).matches()) {
                // Ignore whitespace
                super.emitText(tokenizer, location, text);
            } else {
                insertionMode = InsertionMode.BeforeHtml;
                insertionMode.handler.emitText(tokenizer, location, text);
            }
        }

        @Override
        public void emitDoctype(HtmlTokenizer tokenizer, HtmlLocation location, String doctypeName, String publicId, String systemId) {
            if (documentHandler != null) {
                documentHandler.doctypeDecl(doctypeName, publicId, systemId, null);
            }
            insertionMode = InsertionMode.BeforeHtml;
        }

        @Override
        public void emitXmlDecl(HtmlTokenizer tokenizer, HtmlLocation location, String version, String encoding, String standalone) {
            documentHandler.xmlDecl(version, encoding, standalone, null);
        }

        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, Attributes attributes) {
            insertionMode = InsertionMode.BeforeHtml;
            insertionMode.handler.emitTag(tokenizer, location, tagToken, attributes);
        }
    }

    /**
     * Handle tokens in "before html" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#the-before-html-insertion-mode
     */
    class TokenHandlerBeforeHtml extends TokenHandlerBase {

        @Override
        public void emitText(HtmlTokenizer tokenizer, HtmlLocation location, String text) {
            // ignore whitespaces before doctype, otherwise change insertion mode to "before html"
            if (REGEX_WHITESPACE_ONLY.matcher(text).matches()) {
                // Ignore whitespace
                super.emitText(tokenizer, location, text);
            } else {
                if (!featureDocumentFragment) {
                    unclosedElementStack.push(QN_HTML);
                    documentHandler.startElement(QN_HTML, EMPTY_ATTRIBUTES, null);
                }
                insertionMode = InsertionMode.BeforeHead;
            }
        }

        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, Attributes attributes) {
            final String tagName = tagToken.nameBuilder.toString();
            if (tagToken.isEndTag && !matchOneOfThese(tagName, "head", "body", "html", "br")) {
                reportError("parse-error", new Object[]{ tagName });
                // AND IGNORE THIS TOKEN
            } else if (!tagToken.isEndTag && tagName.equals("html")) {
                unclosedElementStack.push(QN_HTML);
                documentHandler.startElement(QN_HTML, attributes, null);
                insertionMode = InsertionMode.BeforeHead;
            } else {
                if (!featureDocumentFragment) {
                    unclosedElementStack.push(QN_HTML);
                    documentHandler.startElement(QN_HTML, EMPTY_ATTRIBUTES, null);
                }
                insertionMode = InsertionMode.BeforeHead;

                insertionMode.handler.emitTag(tokenizer, location, tagToken, attributes);
            }
        }
    }

    /**
     * Handle tokens in "before head" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#the-before-head-insertion-mode
     */
    class TokenHandlerBeforeHead extends TokenHandlerBase {
        @Override
        public void emitText(HtmlTokenizer tokenizer, HtmlLocation location, String text) {
            super.emitText(tokenizer, location, text);
        }

        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, Attributes attributes) {
            final String tagName = tagToken.nameBuilder.toString();
            if (tagToken.isEndTag && !matchOneOfThese(tagName, "head", "body", "html", "br")) {
                reportError("parse-error", new Object[]{ tagName });
                // AND IGNORE THIS TOKEN
            } else if (!tagToken.isEndTag && tagName.equals("html")) {
                reportError("parse-error", new Object[]{ tagName });
                // Parse error.
                if (unclosedElementStack.contains(QN_TEMPLATE)) {
                    // If there is a template element on the stack of open elements, then ignore the token.
                    // IGNORE THIS TOKEN
                } else {
                    // Otherwise, for each attribute on the token, check to see if the attribute is already 
                    // present on the top element of the stack of open elements. If it is not, add the attribute
                    // and its corresponding value to that element.
                }
                documentHandler.startElement(QN_HEAD, attributes, null);
                insertionMode = InsertionMode.InHead;
            } else if (!tagToken.isEndTag && tagName.equals("head")) {
                unclosedElementStack.push(QN_HEAD);
                documentHandler.startElement(QN_HEAD, EMPTY_ATTRIBUTES, null);
                insertionMode = InsertionMode.InHead;
            } else {
                if (!featureDocumentFragment) {
                    unclosedElementStack.push(QN_HEAD);
                    documentHandler.startElement(QN_HEAD, EMPTY_ATTRIBUTES, null);
                }
                insertionMode = InsertionMode.InHead;

                insertionMode.handler.emitTag(tokenizer, location, tagToken, attributes);
            }
        }
    }

    /**
     * Handle tokens in "in head" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#the-in-head-insertion-mode
     */
    class TokenHandlerInHead extends TokenHandlerBase {
        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, Attributes attributes) {
            final String tagName = tagToken.nameBuilder.toString();
            if (!tagToken.isEndTag && matchOneOfThese(tagName, "meta", "base", "basefont", "bgsound", "link")) {
                if (tagToken.isSelfClosingTag) {
                    reportError("non-void-html-element-start-tag-with-trailing-solidus", new Object[]{ tagName });
                }
                documentHandler.emptyElement(createHtmlQName(tagName), attributes, null);
            } else if (tagToken.isEndTag && tagName.equals("head")) {
                QName n = unclosedElementStack.pop();
                if (!n.localpart.equals("head")) {
                    // should be "head"
                    reportError("parse-error", new Object[]{ tagName });
                }
                documentHandler.endElement(QN_HEAD, null);
                insertionMode = InsertionMode.AfterHead;
            } else if (!tagToken.isEndTag && !matchOneOfThese(tagName, "body")) {
                QName n = createHtmlQName(tagName);
                unclosedElementStack.push(n);
                documentHandler.startElement(n, attributes, null);
            } else if (tagToken.isEndTag) {
                unclosedElementStack.pop();
                documentHandler.endElement(createHtmlQName(tagName), null);
            } else if (featureDeleteUnexpectedElement && !tagToken.isEndTag && matchOneOfThese(tagName, "head", "noscript")) {
                // INGNORE
            } else {
                QName n = unclosedElementStack.peek();
                if (n.localpart.equals("head")) {
                    documentHandler.endElement(n, null);
                    unclosedElementStack.pop();
                } else {
                    // should be "head"
                    reportError("parse-error", new Object[]{ tagName });
                }

                insertionMode = InsertionMode.AfterHead;
                insertionMode.handler.emitTag(tokenizer, location, tagToken, attributes);
            }
        }
    }

    /**
     * Handle tokens in "after head" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#the-after-head-insertion-mode
     */
    class TokenHandlerAfterHead extends TokenHandlerBase {
        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, Attributes attributes) {
            final String tagName = tagToken.nameBuilder.toString();
            if ("html".equals(tagName) && !tagToken.isEndTag) {
                reportError("parse-error", new Object[]{ tagName });
                // Parse error.
                if (unclosedElementStack.contains(QN_TEMPLATE)) {
                    // If there is a template element on the stack of open elements, then ignore the token.
                    // IGNORE THIS TOKEN
                } else {
                    // Otherwise, for each attribute on the token, check to see if the attribute is already 
                    // present on the top element of the stack of open elements. If it is not, add the attribute
                    // and its corresponding value to that element.
                }
            } else if ("body".equals(tagName) && !tagToken.isEndTag) {
                unclosedElementStack.push(QN_BODY);
                documentHandler.startElement(QN_BODY, attributes, null);
                insertionMode = InsertionMode.InBody;
            } else {
                if (featureDeleteUnexpectedElement) {
                    if (!tagToken.isEndTag && matchOneOfThese(tagName, "head", "noscript")) {
                        // INGNORE
                        reportError("info-ignore-tag", new Object[]{ tagName });
                    }
                }
                if (!featureDocumentFragment) {
                    unclosedElementStack.push(QN_BODY);
                    documentHandler.startElement(QN_BODY, EMPTY_ATTRIBUTES, null);    
                    insertionMode = InsertionMode.InBody;
                }
                super.emitTag(tokenizer, location, tagToken, attributes);
            }
        }
    }

    /**
     * Handle tokens in "in body" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inbody
     */
    class TokenHandlerInBody extends TokenHandlerBase {
        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, Attributes attributes) {
            final String tagName = tagToken.nameBuilder.toString();
            if ("html".equals(tagName) && !tagToken.isEndTag) {
                reportError("parse-error", new Object[]{ tagName });
                // Parse error.
                if (unclosedElementStack.contains(QN_TEMPLATE)) {
                    // If there is a template element on the stack of open elements, then ignore the token.
                    // IGNORE THIS TOKEN
                } else {
                    // Otherwise, for each attribute on the token, check to see if the attribute is already 
                    // present on the top element of the stack of open elements. If it is not, add the attribute
                    // and its corresponding value to that element.
                }
            } else if ("body".equals(tagName) && !tagToken.isEndTag) {
                reportError("parse-error", new Object[]{ tagName });
            } else if ("body".equals(tagName) && tagToken.isEndTag) {
                // close body tag
                unclosedElementStack.pop();
                documentHandler.endElement(QN_BODY, null);
                insertionMode = InsertionMode.AfterBody;
            } else if (featureDeleteUnexpectedElement && "head".equals(tagName) && !tagToken.isEndTag) {
                // IGNORE
            } else if (!tagToken.isEndTag) {
                QName n = createHtmlQName(tagName);
                unclosedElementStack.push(n);
                documentHandler.startElement(n, attributes, null);
            } else if (tagToken.isEndTag) {
                unclosedElementStack.pop();
                documentHandler.endElement(createHtmlQName(tagName), null);
            } else if (featureDeleteUnexpectedElement && !tagToken.isEndTag && matchOneOfThese(tagName, "head", "noscript")) {
                // INGNORE
            } else {
                super.emitTag(tokenizer, location, tagToken, attributes);
            }
        }
    }

    /**
     * Handle tokens in "after body" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-afterbody
     */
    class TokenHandlerAfterBody extends TokenHandlerBase {
    }

    class TokenHandlerBase implements TokenHandler {

        @Override
        public void emitXmlDecl(HtmlTokenizer tokenizer, HtmlLocation location, String version, String encoding, String standalone) {
            reportError("parse-error", new Object[]{ "xmlDecl", version, encoding, standalone });
            // IGNORE THIS TOKEN
        }

        @Override
        public void emitDoctype(HtmlTokenizer tokenizer, HtmlLocation location, String doctypeName, String publicId, String systemId) {
            reportError("parse-error", new Object[]{ "doctype", doctypeName, publicId, systemId });
            // AND IGNORE THIS TOKEN
        }
    
        @Override
        public void emitComment(HtmlTokenizer tokenizer, HtmlLocation location, String comment) {
            char[] ch = comment.toCharArray();
            documentHandler.comment(new XMLString(ch, 0, ch.length), null);
        }
    
        @Override
        public void emitText(HtmlTokenizer tokenizer, HtmlLocation location, String text) {
            char[] ch = text.toCharArray();
            documentHandler.characters(new XMLString(ch, 0, ch.length), null);
        }
    
        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, Attributes attributes) {        
            final String tagName = tagToken.nameBuilder.toString();
            QName lastOpendTagName = null;
            try {
                lastOpendTagName = unclosedElementStack.peek();
            } catch (EmptyStackException e) {
                // NOP
            }

            if (tagToken.isSelfClosingTag) {
                documentHandler.emptyElement(createHtmlQName(tagName), attributes, null);
            } else if (tagToken.isEndTag) {
                QName qName = createHtmlQName(tagName);
                documentHandler.endElement(qName, null);
                if (lastOpendTagName != null && lastOpendTagName.rawname.equals(qName.rawname)) {
                    unclosedElementStack.pop();
                }
            } else {
                QName qName = createHtmlQName(tagName);
                documentHandler.startElement(qName, attributes, null);
                unclosedElementStack.push(qName);
            }
        }

        @Override
        public void reportError(String msgId, Object[] args) {
            reportFatalError(msgId, args);
        }

        @Override
        public TokenHandler getNextHandler() {
            // System.err.println(insertionMode);
            return insertionMode.handler;
        }
    }

    XMLErrorReporter errorReporter; 

    @Override
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        for (InsertionMode m: InsertionMode.values()) {
            try {
                m.handler = m.handlerClass.getDeclaredConstructor(this.getClass()).newInstance(this);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                 | SecurityException | InvocationTargetException | NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        errorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);

        tokenizer = new HtmlTokenizer();
        tokenizer.setInputSource(inputSource);
        tokenizer.reset();

        insertionMode = InsertionMode.Initial;
        unclosedElementStack = new Stack<>();
    }

    /**
     * Convenience function used in all XML scanners.
     */
    private void reportFatalError(String msgId, Object[] args)
        throws XNIException {
            System.err.println("ERROR:" + msgId);
        // errorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
        //                            msgId, args,
        //                            XMLErrorReporter.SEVERITY_FATAL_ERROR);
    }

    @Override
    public boolean scanDocument(boolean complete) throws IOException, XNIException {
        try {
            XMLLocator locator = tokenizer.getLocator();
            documentHandler.startDocument(locator, inputSource.getEncoding(), null, null);

            tokenizer.runTokenizer(insertionMode.handler);
            // return success
            return true;    
        } catch (BufferUnderflowException e) {
            return false;
        }
    }


    @Override
    public void startEntity(String name, XMLResourceIdentifier identifier, String encoding, Augmentations augs)
            throws XNIException {
        // if (documentHandler != null && name.equals("[xml]")) {
        //     documentHandler.startDocument(encoding, new NamespaceSupport(), null);
        // }        
    }

    @Override
    public void endEntity(String name, Augmentations augs) throws XNIException {
        // TODO Auto-generated method stub

    }

} // class ElementStack



class ScanningInterruptedExeption extends Exception {
    public ScanningInterruptedExeption() {
        super();
    }

    public ScanningInterruptedExeption(Throwable throwable) {
        super(throwable);
    }
}

class HtmlLocation implements Locator, XMLLocator, Cloneable {
    int line = 1;
    int column = 1;
    int offset = 0;
    String publicId = null;
    String systemId = null;

    @Override
    public String toString() {
        return String.format("(%06d)l%d:c%d", offset, line, column);
    }

    @Override
    public HtmlLocation clone() {
        HtmlLocation copy = new HtmlLocation();
        copy.column = column;
        copy.line = line;
        copy.offset = offset;
        copy.publicId = publicId;
        copy.systemId = systemId;
        return copy;
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

    @Override
    public int getCharacterOffset() {
        return offset;
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public String getLiteralSystemId() {
        return getSystemId();
    }

    @Override
    public String getBaseSystemId() {
        return getSystemId();
    }

    @Override
    public String getExpandedSystemId() {
        return getSystemId();
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public String getXMLVersion() {
        return null;
    }
}

class Attributes implements XMLAttributes {
    class Attribute {
        QName attrName;
        String attrType;
        String attrValue;
        Attribute(QName attrName, String attrType, String attrValue) {
            this.attrName = attrName;
            this.attrType = attrType;
            this.attrValue = attrValue;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + Objects.hash(attrName, attrType, attrValue);
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Attribute))
                return false;
            Attribute other = (Attribute) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            return Objects.equals(attrName, other.attrName) && Objects.equals(attrType, other.attrType)
                    && Objects.equals(attrValue, other.attrValue);
        }
        

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        
        @Override
        public String toString() {
            return attrName.rawname + "=\"" + attrValue.toString() + "\"";
        }

        private Attributes getEnclosingInstance() {
            return Attributes.this;
        }
        
    }
    ArrayList<Attribute> attributes = new ArrayList<>();

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString() {
        return attributes.toString();
    }

    @Override
    public int addAttribute(QName attrName, String attrType, String attrValue) {
        attributes.add(new Attribute(attrName, attrType, attrValue));
        return attributes.size() - 1;
    }

    @Override
    public void removeAllAttributes() {
        attributes.clear();
    }

    @Override
    public void removeAttributeAt(int attrIndex) {
        attributes.remove(attrIndex);
    }

    @Override
    public int getLength() {
        return attributes.size();
    }

    @Override
    public int getIndex(String qName) {
        int idx = qName.indexOf(':');
        QName nm = null;
        if (idx == -1) {
            nm = new QName(null, qName, qName, null);
        } else {
            nm = new QName(qName.substring(0, idx), qName.substring(idx + 1), qName, null);
        }
        return attributes.indexOf(new Attribute(nm, null, null));
    }

    @Override
    public int getIndex(String uri, String localPart) {
        QName nm = new QName(null, localPart, localPart, uri);
        return attributes.indexOf(new Attribute(nm, null, null));
    }

    @Override
    public void setName(int attrIndex, QName attrName) {
        Attribute a = attributes.get(attrIndex);
        if (a != null) {
            a.attrName = attrName;
        }
    }

    @Override
    public void getName(int attrIndex, QName attrName) {
        Attribute a = attributes.get(attrIndex);
        if (a != null) {
            attrName.setValues(a.attrName);
        }
    }

    @Override
    public String getPrefix(int index) {
        Attribute a = attributes.get(index);
        if (a != null) {
            return a.attrName.prefix;
        }
        return null;
    }

    @Override
    public String getURI(int index) {
        Attribute a = attributes.get(index);
        if (a != null) {
            return a.attrName.uri;
        }
        return null;
    }

    @Override
    public String getLocalName(int index) {
        Attribute a = attributes.get(index);
        if (a != null) {
            return a.attrName.localpart;
        }
        return null;
    }

    @Override
    public String getQName(int index) {
        Attribute a = attributes.get(index);
        if (a != null) {
            return a.attrName.rawname;
        }
        return null;
    }

    @Override
    public void setType(int attrIndex, String attrType) {
        Attribute a = attributes.get(attrIndex);
        if (a != null) {
            a.attrType = attrType;
        }
    }

    @Override
    public String getType(int index) {
        Attribute a = attributes.get(index);
        if (a != null) {
            return a.attrType;
        }
        return null;
    }

    @Override
    public String getType(String qName) {
        int index = getIndex(qName);
        if (index != -1) {
            return attributes.get(index).attrType;
        }
        return null;
    }

    @Override
    public String getType(String uri, String localName) {
        int index = getIndex(uri, localName);
        if (index != -1) {
            return attributes.get(index).attrType;
        }
        return null;
    }

    @Override
    public void setValue(int attrIndex, String attrValue) {
        Attribute a = attributes.get(attrIndex);
        if (a != null) {
            a.attrValue = attrValue;
        }
    }

    @Override
    public String getValue(int index) {
        Attribute a = attributes.get(index);
        if (a != null) {
            return a.attrValue;
        }
        return null;
    }

    @Override
    public String getValue(String qName) {
        int attrIndex = getIndex(qName);
        Attribute a = attributes.get(attrIndex);
        if (a != null) {
            return a.attrValue;
        }
        return null;
    }

    @Override
    public String getValue(String uri, String localName) {
        int attrIndex = getIndex(uri, localName);
        Attribute a = attributes.get(attrIndex);
        if (a != null) {
            return a.attrValue;
        }
        return null;
    }

    @Override
    public void setNonNormalizedValue(int attrIndex, String attrValue) {
        Attribute a = attributes.get(attrIndex);
        if (a != null) {
            a.attrValue = attrValue;
        }
    }

    @Override
    public String getNonNormalizedValue(int attrIndex) {
        Attribute a = attributes.get(attrIndex);
        if (a != null) {
            return a.attrValue;
        }
        return null;
    }

    @Override
    public void setSpecified(int attrIndex, boolean specified) {
    }

    @Override
    public boolean isSpecified(int attrIndex) {
        return false;
    }

    @Override
    public Augmentations getAugmentations(int attributeIndex) {
        return null;
    }

    @Override
    public Augmentations getAugmentations(String uri, String localPart) {
        return null;
    }

    @Override
    public Augmentations getAugmentations(String qName) {
        return null;
    }

    @Override
    public void setAugmentations(int attrIndex, Augmentations augs) {
    }
}

interface TokenHandler {
    void reportError(String msgId, Object[] args);

    void emitText(HtmlTokenizer tokenizer, HtmlLocation location, String text);

    void emitXmlDecl(HtmlTokenizer tokenizer, HtmlLocation location, String version, String encoding, String standalone);

    void emitDoctype(HtmlTokenizer tokenizer, HtmlLocation location, String doctypeName, String publicId, String systemId);

    void emitComment(HtmlTokenizer tokenizer, HtmlLocation location, String comment);

    void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, Attributes attributes);

    TokenHandler getNextHandler();

}

class HtmlTokenizer {

    private static final char CHAR_SUB = 0x1A;
    private boolean traceTokenizeStateChanged = false;
    private boolean traceInsertionModeChanged = false;
    private boolean traceEmittion = false;

    private XMLInputSource inputSource;
    // private HTMLEntityScanner entityScanner;
    CharBuffer cbuf;

    private int pushedBack = CHAR_SUB;
    private int lastChar = CHAR_SUB;
    private StringBuilder characterBuilder = new StringBuilder();
    private Attributes attributes = new Attributes();
    private HtmlLocation location = new HtmlLocation();
    private HtmlLocation currentLocation = new HtmlLocation();

    private TagToken lastStartTagToken = null;
    TokenizeState tokenizeState = TokenizeState.Data;

    enum TokenizeState {
        Data,
        RcData,
        RawText,
        ScriptData,
        PlainText,
        TagOpen,
        EndTagOpen,
        TagName,
        RcDataLessThanSign,
        RcDataEndTagOpen,
        RcDataEndTagName,
        RawTextLessThanSign,
        RawTextEndTagOpen,
        RawTextEndTagName,
        ScriptDataLessThanSign,
        ScriptDataEndTagOpen,
        ScriptDataEndTagName,
        ScriptDataEscapeStart,
        ScriptDataEscapeStartDash,
        ScriptDataEscaped,
        ScriptDataEscapedDash,
        ScriptDataEscapedLessThanSign,
        ScriptDataEscapedEndTagOpen,
        ScriptDataEscapedEndTagName,
        ScriptDataDoubleEscapeStart,
        ScriptDataDoubleEscaped,
        ScriptDataDoubleEscapedDash,
        ScriptDataDoubleEscapedDashDash,
        ScriptDataDoubleEscapedLessThanSign,
        ScriptDataDoubleEscapeEnd,
        BeforeAttributeName,
        AttributeName,
        AfterAttributeName,
        BeforeAttributeValue,
        AttributeValueDoubleQuoted,
        AttributeValueSingleQuoted,
        AttributeValueUnquoted,
        AfterAttributeValueQuoted,
        SelfClosingStartTag,
        BogusComment,
        MarkupDeclarationOpen,
        CommentStart,
        CommentStartDash,
        Comment,
        CommentLessThanSign,
        CommentLessThanSignBang,
        CommentLessThanSignBangDash,
        CommentLessThanSignBangDashDash,
        CommentEndDash,
        CommentEnd,
        CommentEndBang,
        Doctype,
        BeforeDoctypeName,
        DoctypeName,
        AfterDoctypeName,
        AfterDoctypePublicKeyword,
        BeforeDoctypePublicIdentifier,
        DoctypePublicIdentifierDoubleQuoted,
        DoctypePublicIdentifierSingleQuoted,
        AfterDoctypePublicIdentifier,
        BetweenDoctypePublicAndSystemIdentifiers,
        AfterDoctypeSystemKeyword,
        BeforeDoctypeSystemIdentifier,
        DoctypeSystemIdentifierDoubleQuoted,
        DoctypeSystemIdentifierSingleQuoted,
        AfterDoctypeSystemIdentifier,
        BogusDoctype,
        CDataSection,
        CDataSectionBracket,
        CDataSectionEnd,
        CharacterReference,
        NamedCharacterReference,
        AmbiguousAmpersand,
        NumericCharacterReference,
        HexadecimalCharacterReferenceStart,
        DecimalCharacterReferenceStart,
        HexadecimalCharacterReference,
        DecimalCharacterReference,
        NumericCharacterReferenceEnd,
        ScriptDataEscapedDashDash,
    }

    class Doctype {
        StringBuilder nameBuilder;
        boolean forceQuirkFlag = false;
        StringBuilder publicIdBuilder;
        StringBuilder systemIdBuilder;
    }

    class TagToken {
        StringBuilder nameBuilder = new StringBuilder();
        boolean isEndTag;
        boolean isSelfClosingTag;
    }

    public void setTokenizerState(TokenizeState state) {
        this.tokenizeState = state;
    }

    public XMLLocator getLocator() {
        return location;
    }

    public void reset() {
        // RESET PARSING STATES
        attributes.removeAllAttributes();
        lastStartTagToken = null;
        location.column = 1;
        location.line = 1;
        location.offset = 1;
        if (inputSource != null) {
            location.publicId = inputSource.getPublicId();
            location.systemId = inputSource.getSystemId();
        } else {
            location.publicId = null;
            location.systemId = null;
        }
        cbuf = CharBuffer.allocate(4096);
        cbuf.flip();
    }

    public void setInputSource(XMLInputSource inputSource) {
        this.inputSource = inputSource;
        location.column = 1;
        location.line = 1;
        location.offset = 1;
        if (inputSource != null) {
            location.publicId = inputSource.getPublicId();
            location.systemId = inputSource.getSystemId();    
        } else {
            location.publicId = null;
            location.systemId = null;
        }
    }

    private char getChar() throws ScanningInterruptedExeption, IOException {
        if (cbuf.remaining() == 0) {
            cbuf.flip();
            cbuf.clear();
            if (inputSource.getCharacterStream().read(cbuf) == 0) {
                cbuf.flip();
                return CHAR_SUB;
            };
            cbuf.flip();
        }
        if (pushedBack != CHAR_SUB) {
            final char c = (char) pushedBack;
            pushedBack = CHAR_SUB;
            return c;
        }
        try {
            int c = cbuf.get();
            while (c == '\r') {
                c = cbuf.get();
            }

            lastChar = c;
            currentLocation.offset++;
            if (c == '\n') {
                currentLocation.line++;
                currentLocation.column = 1;
            } else {
                currentLocation.column++;
            }
            return (char) c;
        } catch (BufferUnderflowException e) {
            return CHAR_SUB;
        }
    }

    private void pushBack() {
        pushedBack = lastChar;
        // pushbackされた文字は次回すぐに読み出されるためcurrentLocationは戻さない
    }

    private boolean skipStringIgnoreCase(String string) throws IOException {
        final char[] array = cbuf.array();
        final int start = cbuf.position() + cbuf.arrayOffset();
        final int end = start + string.length();
        for (int i = start, j = 0; i < end; ++i, ++j) {
            final char b = string.charAt(j);
            if (Character.toUpperCase(array[i]) != Character.toUpperCase(b)) {
                return false;
            }
        }
        cbuf.position(cbuf.position() + string.length());
        currentLocation.offset += string.length();
        currentLocation.column += string.length();
        return true;
    }

    private boolean skipString(String string) throws IOException {
        final char[] array = cbuf.array();
        final int start = cbuf.position() + cbuf.arrayOffset();
        final int end = start + string.length();
        for (int i = start, j = 0; i < end; ++i, ++j) {
            if (array[i] != string.charAt(j)) {
                return false;
            }
        }
        cbuf.position(cbuf.position() + string.length());
        currentLocation.offset += string.length();
        currentLocation.column += string.length();
        return true;
    }

    private boolean isAppropriateEndTagToken(TagToken tagToken) {
        if (lastStartTagToken == null) {
            System.err.printf("INFO: not appropriate end tag token\n");
            return false;
        }
        if (lastStartTagToken.nameBuilder.toString().equals(tagToken.nameBuilder.toString())) {
            return true;
        }
        System.err.printf("INFO: not appropriate end tag token\n");
        return false;
    }

    private boolean containSameAttributeName(String name) {
        if (attributes == null) {
            return false;
        }
        return attributes.getIndex(name) != -1;
    }

    private void appendTextNode(int c) {
        characterBuilder.append((char) c);
    }

    private void appendTextNode(char c) {
        characterBuilder.append(c);
    }

    private void appendTextNode(String string) {
        characterBuilder.append(string);
    }

    private void emitAttribute(String prefix, String name, String value) {
        // System.out.printf("%s: ATTR %s:%s=%s\n", location.toString(), prefix, name, value);

        // add to current attribute list.
        attributes.addAttribute(new QName(prefix, name, name, null), name, value);
    }

    private void emitEof() throws ScanningInterruptedExeption {
        throw new ScanningInterruptedExeption();
    }

    private void emitDoctype(TokenHandler handler, Doctype doctype) {
        String doctypeName = doctype.nameBuilder == null ? null: doctype.nameBuilder.toString();
        String publicId = doctype.publicIdBuilder == null ? null: doctype.publicIdBuilder.toString();
        String systemId = doctype.systemIdBuilder == null ? null: doctype.systemIdBuilder.toString();

        if (traceEmittion) {
            System.out.printf("%s: DOCTYPE %s %s %s\n", location.toString(), doctypeName, publicId, systemId);
        }
        handler.emitDoctype(null, location, doctypeName, publicId, systemId);
        location = currentLocation.clone();
    }

    private Map<String, String> extractAttributes(String str) {
        char quoteChar = 0;
        HashMap<String, String> map = new HashMap<>();
        StringBuilder key = null;
        StringBuilder value = null;
        final int BEFORE_KEY = 1;
        final int KEY = 2;
        final int AFTER_KEY = 3;
        final int BEFORE_VALUE = 4;
        final int VALUE = 5;
        int state = BEFORE_KEY;

        for (char c: str.toCharArray()) {
            switch (state) {
                case BEFORE_KEY:
                    if (Character.isWhitespace(c)) {
                        // SKIP WHITE SPACE
                    } else {
                        key = new StringBuilder();
                        key.append(c);
                        state = KEY;
                    }
                    break;
                case KEY:
                    if (Character.isWhitespace(c)) {
                        state = AFTER_KEY;
                    } else if (c == '=') {
                        state = BEFORE_VALUE;
                    } else {
                        key.append(c);    
                    }
                    break;
                case AFTER_KEY:
                    if (Character.isAlphabetic(c)) {
                        map.put(key.toString(), "");
                        key = new StringBuilder();
                        key.append(c);
                    } else if (Character.isWhitespace(c)) {
                        // SKIP WHITE SPACE
                    } else if (c == '=') {
                        state = BEFORE_VALUE;
                    }
                    break;
                case BEFORE_VALUE:
                    if (Character.isAlphabetic(c)) {
                        quoteChar = ' ';
                        value = new StringBuilder();
                        value.append(c);
                        state = VALUE;
                    } else if (Character.isWhitespace(c)) {
                        // SKIP WHITE SPACE
                    } else if (c == '"' || c == '\'') {
                        quoteChar = c;
                        value = new StringBuilder();
                        state = VALUE;
                    }
                    break;
                case VALUE:
                    if (quoteChar == ' ' && Character.isWhitespace(c)) {
                        map.put(key.toString(), value.toString());
                        state = BEFORE_KEY;
                    } else if (c == quoteChar) {
                        map.put(key.toString(), value.toString());
                        state = BEFORE_KEY;
                    } else {
                        value.append(c);
                    }
                    break;
            }
        }
        return map;
    }
    private void emitComment(TokenHandler handler, String comment, boolean mayXmlDecl) {
        if (mayXmlDecl && comment.startsWith("?xml ")) {
            if (traceEmittion) {
                System.out.printf("%s: XML DECL <%s>\n", location.toString(), comment);
            }
            Map<String, String> map = extractAttributes(comment.substring(5));
            handler.emitXmlDecl(this, location, map.get("version"), map.get("encoding"), map.get("standalone"));
        } else {
            if (traceEmittion) {
                System.out.printf("%s: COMMENT <!-- %s -->\n", location.toString(), comment);
            }
            handler.emitComment(this, location, comment);
        }
        location = currentLocation.clone();
    }

    private void emitTag(TokenHandler handler, TagToken tagToken) {
        final String tagName = tagToken.nameBuilder.toString();
        if (tagToken.isSelfClosingTag) {
            if (traceEmittion) {
                System.out.printf("%s: ELEM(EMPTY) %s %s\n", location.toString(), tagName, attributes);
            }
        } else if (tagToken.isEndTag) {
            if (traceEmittion) {
                System.out.printf("%s: ELEM END /%s %s\n", location.toString(), tagName, attributes);
            }
        } else {
            if (traceEmittion) {
                System.out.printf("%s: ELEM START %s %s\n", location.toString(), tagName, attributes);
            }
        }

        handler.emitTag(this, location, tagToken, attributes);

        if (tagToken.isEndTag) {
            lastStartTagToken = null;
        } else {
            lastStartTagToken = tagToken;

            // https://html.spec.whatwg.org/multipage/parsing.html#parsing-html-fragments
            // 4. Set the state of the HTML parser's tokenization stage as follows, switching on the context element:
            switch (tagName) {
                case "title":
                case "textarea":
                    setTokenizerState(TokenizeState.RcData);
                    break;
                case "style":
                case "xmp":
                case "iframe":
                case "noembed":
                case "noframes":
                    setTokenizerState(TokenizeState.RawText);
                    break;
                case "script":
                    setTokenizerState(TokenizeState.ScriptData);
                    break;
                case "noscript":
                    setTokenizerState(TokenizeState.RawText);
                    break;
                case "plaintext":
                    setTokenizerState(TokenizeState.PlainText);
                    break;
                default:
                    break;
            }
            attributes.removeAllAttributes();
        }
        location = currentLocation.clone();
    }

    private void emitSelfClosingTag(TokenHandler handler, TagToken tagToken) {
        tagToken.isSelfClosingTag = true;
        if (traceEmittion) {
        final String tagName = tagToken.nameBuilder.toString();
        System.out.printf("%s: ELEM(EMPTY) %s %s\n", location.toString(), tagName, attributes);
        }

        if (tagToken.isEndTag) {
            lastStartTagToken = null;
        } else {
            lastStartTagToken = null;
        }
        handler.emitTag(this, location, tagToken, attributes);
        attributes.removeAllAttributes();
        location = currentLocation.clone();
    }

    private void emitTextIfAvailable(TokenHandler handler) {
        if (characterBuilder.length() > 0) {
            if (traceEmittion) {
                System.out.printf("%s: TEXT \"%s\"\n", location, characterBuilder.toString());
            }
            handler.emitText(this, location, characterBuilder.toString());
            characterBuilder = new StringBuilder();
            location = currentLocation.clone();
        }
    }

    void runTokenizer(TokenHandler handler) {
        TagToken tagToken = null;
        StringBuilder attrNameBuilder = new StringBuilder();
        StringBuilder attrValueBuilder = new StringBuilder();
        StringBuilder commentBuilder = new StringBuilder();
        Doctype doctype = new Doctype();
        StringBuilder temporaryBuffer = new StringBuilder();
        boolean mayXmlDeclAsBogusComment = false;


        TokenHandler lastTokenHandler = null;
        TokenizeState lastTokenizeState = null;
        try {
            char c = 0xFFFF;
            do {
                if (traceInsertionModeChanged) {
                    if (lastTokenHandler != handler) {
                        System.err.println(handler.getClass().getSimpleName());
                        lastTokenHandler = handler;
                    }
                }
                if (traceTokenizeStateChanged) {
                    if (lastTokenizeState != tokenizeState) {
                        System.err.println(tokenizeState);
                        lastTokenizeState = tokenizeState;
                    }
                }
                switch (tokenizeState) {
                    case Data:
                        c = getChar();
                        if (c == '<') {
                            tokenizeState = TokenizeState.TagOpen;
                        // } else if (c == '&') {
                        //     tokenizeState = TokenizeState.CharacterReference;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            appendTextNode(0);
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            appendTextNode(c);
                        }
                        break;

                    case RcData:
                        // https://html.spec.whatwg.org/multipage/parsing.html#rcdata-state
                        c = getChar();
                        if (c == '<') {
                            tokenizeState = TokenizeState.RcDataLessThanSign;
                        // } else if (c == '&') {
                        //     tokenizeState = TokenizeState.CharacterReference;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            appendTextNode(c);
                        }
                        break;

                    case RawText:
                        // https://html.spec.whatwg.org/multipage/parsing.html#rawtext-state
                        c = getChar();
                        if (c == '<') {
                            tokenizeState = TokenizeState.RawTextLessThanSign;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            appendTextNode(c);
                        }
                        break;

                    case ScriptData:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-state
                        c = getChar();
                        if (c == '<') {
                            tokenizeState = TokenizeState.ScriptDataLessThanSign;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            appendTextNode(c);
                        }
                        break;

                    case PlainText:
                        // https://html.spec.whatwg.org/multipage/parsing.html#plaintext-state
                        c = getChar();
                        if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            appendTextNode(c);
                        }
                        break;

                    case TagOpen:
                        emitTextIfAvailable(handler);
                        c = getChar();
                        if (c == '!') {
                            tokenizeState = TokenizeState.MarkupDeclarationOpen;
                        } else if (c == '/') {
                            tokenizeState = TokenizeState.EndTagOpen;
                        } else if (Character.isAlphabetic(c)) {
                            tagToken = new TagToken();
                            tagToken.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                            tokenizeState = TokenizeState.TagName;
                        } else if (c == '?') {
                            handler.reportError("unexpected-question-mark-instead-of-tag-name", null);
                            commentBuilder = new StringBuilder();
                            commentBuilder.append(c);
                            mayXmlDeclAsBogusComment = true;
                            tokenizeState = TokenizeState.BogusComment;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-before-tag-name", null);
                            appendTextNode('<');
                        } else {
                            handler.reportError("invalid-first-character-of-tag-name", null);
                            tokenizeState = TokenizeState.Data;
                            appendTextNode('<');
                        }
                        break;

                    case EndTagOpen:
                        c = getChar();
                        if (Character.isAlphabetic(c)) {
                            tagToken = new TagToken();
                            tagToken.isEndTag = true;
                            tagToken.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                            tokenizeState = TokenizeState.TagName;
                        } else if (c == '>') {
                            handler.reportError("missing-end-tag-name", null);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-before-tag-name", null);
                            appendTextNode('<');
                            appendTextNode(0x002F);
                            emitEof();
                        } else {
                            handler.reportError("invalid-first-character-of-tag-name", null);
                            tokenizeState = TokenizeState.BogusComment;
                            appendTextNode('<');
                        }
                        break;

                    case TagName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#tag-name-state
                        c = getChar();
                        if (Character.isAlphabetic(c)) {
                            tagToken.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                        } else if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            tokenizeState = TokenizeState.BeforeAttributeName;
                        } else if (c == '/') {
                            tokenizeState = TokenizeState.SelfClosingStartTag;
                        } else if (c == '>') {
                            tokenizeState = TokenizeState.Data;
                            emitTextIfAvailable(handler);
                            emitTag(handler, tagToken);
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            tagToken.nameBuilder.append((char) 0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-before-tag-name", null);
                            appendTextNode('<');
                            appendTextNode(0x002F);
                            emitEof();
                        } else {
                            tagToken.nameBuilder.append(c);
                        }
                        break;
                    case RcDataLessThanSign:
                        // https://html.spec.whatwg.org/multipage/parsing.html#rcdata-less-than-sign-state
                        c = getChar();
                        if (c == '/') {
                            temporaryBuffer = new StringBuilder();
                            tokenizeState = TokenizeState.RcDataEndTagOpen;
                        } else {
                            appendTextNode('<');
                            pushBack();
                            tokenizeState = TokenizeState.RcData;
                        }
                        break;

                    case RcDataEndTagOpen:
                        // https://html.spec.whatwg.org/multipage/parsing.html#rcdata-less-than-sign-state
                        c = getChar();
                        if (Character.isAlphabetic(c)) {
                            tagToken = new TagToken();
                            tagToken.isEndTag = true;
                            pushBack();
                            tokenizeState = TokenizeState.RcDataEndTagName;
                        } else {
                            appendTextNode('<');
                            appendTextNode('/');
                            pushBack();
                            tokenizeState = TokenizeState.RcData;
                        }
                        break;

                    case RcDataEndTagName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#rcdata-end-tag-name-state
                        c = getChar();
                        boolean doElseClause = true;
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.BeforeAttributeName;
                                doElseClause = false;
                            }
                        } else if (c == '/') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.SelfClosingStartTag;
                                doElseClause = false;
                            }
                        } else if (c == '>') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.Data;
                                emitTextIfAvailable(handler);
                                emitTag(handler, tagToken);
                                doElseClause = false;
                            }
                        } else if (Character.isAlphabetic(c)) {
                            tagToken.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                            temporaryBuffer.append((char) c);
                            doElseClause = false;
                        }
                        if (doElseClause) {
                            appendTextNode('<');
                            appendTextNode('/');
                            appendTextNode(temporaryBuffer.toString());
                            pushBack();
                            tokenizeState = TokenizeState.RcData;
                        }
                        break;

                    case RawTextLessThanSign:
                        // https://html.spec.whatwg.org/multipage/parsing.html#rawtext-less-than-sign-state
                        c = getChar();
                        if (c == '/') {
                            temporaryBuffer = new StringBuilder();
                            tokenizeState = TokenizeState.RawTextEndTagOpen;
                        } else {
                            appendTextNode('<');
                            pushBack();
                            tokenizeState = TokenizeState.RawText;
                        }
                        break;


                    case RawTextEndTagOpen:
                        // https://html.spec.whatwg.org/multipage/parsing.html#rawtext-end-tag-open-state
                        c = getChar();
                        if (Character.isAlphabetic(c)) {
                            tagToken = new TagToken();
                            tagToken.isEndTag = true;
                            pushBack();
                            tokenizeState = TokenizeState.RawTextEndTagName;
                        } else {
                            appendTextNode('<');
                            appendTextNode('/');
                            pushBack();
                            tokenizeState = TokenizeState.RawText;
                        }
                        break;

                    case RawTextEndTagName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#rawtext-end-tag-name-state
                        c = getChar();
                        doElseClause = true;
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.BeforeAttributeName;
                                doElseClause = false;
                            }
                        } else if (c == '/') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.SelfClosingStartTag;
                                doElseClause = false;
                            }
                        } else if (c == '>') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.Data;
                                emitTextIfAvailable(handler);
                                emitTag(handler, tagToken);
                                doElseClause = false;
                            }
                        } else if (Character.isAlphabetic(c)) {
                            tagToken.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                            temporaryBuffer.append((char) c);
                            doElseClause = false;
                        }
                        if (doElseClause) {
                            appendTextNode('<');
                            appendTextNode('/');
                            appendTextNode(temporaryBuffer.toString());
                            pushBack();
                            tokenizeState = TokenizeState.RawText;
                        }
                        break;

                    case ScriptDataLessThanSign:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-less-than-sign-state
                        c = getChar();
                        if (c == '/') {
                            temporaryBuffer = new StringBuilder();
                            tokenizeState = TokenizeState.ScriptDataEndTagOpen;
                        } else if (c == '!') {
                            temporaryBuffer = new StringBuilder();
                            tokenizeState = TokenizeState.ScriptDataEscapeStart;
                            appendTextNode('<');
                            appendTextNode('!');
                        } else {
                            appendTextNode('<');
                            pushBack();
                            tokenizeState = TokenizeState.ScriptData;
                        }
                        break;

                    case ScriptDataEndTagOpen:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-end-tag-open-state
                        c = getChar();
                        if (Character.isAlphabetic(c)) {
                            tagToken = new TagToken();
                            tagToken.isEndTag = true;
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataEndTagName;
                        } else {
                            appendTextNode('<');
                            appendTextNode('/');
                            pushBack();
                            tokenizeState = TokenizeState.ScriptData;
                        }
                        break;

                    case ScriptDataEndTagName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-end-tag-name-state
                        c = getChar();
                        doElseClause = true;
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.BeforeAttributeName;
                                doElseClause = false;
                            }
                        } else if (c == '/') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.SelfClosingStartTag;
                                doElseClause = false;
                            }
                        } else if (c == '>') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.Data;
                                emitTextIfAvailable(handler);
                                emitTag(handler, tagToken);
                                doElseClause = false;
                            }
                        } else if (Character.isAlphabetic(c)) {
                            tagToken.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                            temporaryBuffer.append((char) c);
                            doElseClause = false;
                        }
                        if (doElseClause) {
                            appendTextNode('<');
                            appendTextNode('/');
                            appendTextNode(temporaryBuffer.toString());
                            pushBack();
                            tokenizeState = TokenizeState.ScriptData;
                        }
                        break;

                    case ScriptDataEscapeStart:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-escape-start-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.ScriptDataEscapedDash;
                            appendTextNode('-');
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.ScriptData;
                        }
                        break;


                    case ScriptDataEscapeStartDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-escape-start-dash-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.ScriptDataEscapedDashDash;
                            appendTextNode('-');
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.ScriptData;
                        }
                        break;

                    case ScriptDataEscaped:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-escaped-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.ScriptDataEscapedDash;
                            appendTextNode('-');
                        } else if (c == '<') {
                            tokenizeState = TokenizeState.ScriptDataEscapedLessThanSign;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-script-html-comment-like-text", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            appendTextNode(c);
                        }
                        break;

                    case ScriptDataEscapedDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-escaped-dash-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.ScriptDataEscapedDashDash;
                            appendTextNode('-');
                        } else if (c == '<') {
                            tokenizeState = TokenizeState.ScriptDataEscapedLessThanSign;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-script-html-comment-like-text", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                            appendTextNode(c);
                        }
                        break;

                    case ScriptDataEscapedDashDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-escaped-dash-state
                        c = getChar();
                        if (c == '-') {
                            appendTextNode('-');
                        } else if (c == '<') {
                            tokenizeState = TokenizeState.ScriptDataEscapedLessThanSign;
                        } else if (c == '>') {
                            tokenizeState = TokenizeState.ScriptData;
                            appendTextNode('>');
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-script-html-comment-like-text", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                            appendTextNode(c);
                        }
                        break;

                    case ScriptDataEscapedLessThanSign:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-less-than-sign-state
                        c = getChar();
                        if (c == '/') {
                            temporaryBuffer = new StringBuilder();
                            tokenizeState = TokenizeState.ScriptDataEscapedEndTagOpen;
                        } else if (Character.isAlphabetic(c)) {
                            temporaryBuffer = new StringBuilder();
                            appendTextNode('<');
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataDoubleEscapeStart;
                        } else {
                            appendTextNode('<');
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                        }
                        break;

                    case ScriptDataEscapedEndTagOpen:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-escaped-end-tag-open-state
                        c = getChar();
                        if (Character.isAlphabetic(c)) {
                            tagToken = new TagToken();
                            tagToken.isEndTag = true;
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataEscapedEndTagName;
                        } else {
                            appendTextNode('<');
                            appendTextNode('/');
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                        }
                        break;

                    case ScriptDataEscapedEndTagName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-end-tag-name-state
                        c = getChar();
                        doElseClause = true;
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.BeforeAttributeName;
                                doElseClause = false;
                            }
                        } else if (c == '/') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.SelfClosingStartTag;
                                doElseClause = false;
                            }
                        } else if (c == '>') {
                            if (isAppropriateEndTagToken(tagToken)) {
                                tokenizeState = TokenizeState.Data;
                                emitTextIfAvailable(handler);
                                emitTag(handler, tagToken);
                                doElseClause = false;
                            }
                        } else if (Character.isAlphabetic(c)) {
                            tagToken.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                            temporaryBuffer.append((char) c);
                            doElseClause = false;
                        }
                        if (doElseClause) {
                            appendTextNode('<');
                            appendTextNode('/');
                            appendTextNode(temporaryBuffer.toString());
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                        }
                        break;


                    case ScriptDataDoubleEscapeStart:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-double-escape-start-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ' || c == '/' || c == '>') {
                            if (temporaryBuffer.length() >= 6) {
                                String firstSixChars = temporaryBuffer.substring(0, 6);
                                if (firstSixChars.equalsIgnoreCase("script")) {
                                    tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                                    appendTextNode(c);
                                } else {
                                    tokenizeState = TokenizeState.ScriptDataEscaped;
                                   appendTextNode(c);
                                }
                            } else {
                                tokenizeState = TokenizeState.ScriptDataEscaped;
                                appendTextNode(c);
                            }
                        } else if (Character.isAlphabetic(c)) {
                            temporaryBuffer.append(c /*Character.toLowerCase(c)*/);
                            appendTextNode(c);
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                        }
                        break;

                    case ScriptDataDoubleEscaped:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-double-escaped-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.ScriptDataDoubleEscapedDash;
                            appendTextNode('-');
                        } else if (c == '<') {
                            tokenizeState = TokenizeState.ScriptDataDoubleEscapedLessThanSign;
                            appendTextNode('<');
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-script-html-comment-like-text", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            appendTextNode(c);
                        }
                        break;


                    case ScriptDataDoubleEscapedDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-double-escaped-dash-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.ScriptDataDoubleEscapedDashDash;
                            appendTextNode('-');
                        } else if (c == '<') {
                            tokenizeState = TokenizeState.ScriptDataDoubleEscapedLessThanSign;
                            appendTextNode('>');
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-script-html-comment-like-text", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                            appendTextNode(c);
                        }
                        break;

                    case ScriptDataDoubleEscapedDashDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-double-escaped-dash-dash-state
                        c = getChar();
                        if (c == '-') {
                            appendTextNode('-');
                        } else if (c == '<') {
                            tokenizeState = TokenizeState.ScriptDataDoubleEscapedLessThanSign;
                        } else if (c == '>') {
                            tokenizeState = TokenizeState.ScriptData;
                            appendTextNode('>');
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-script-html-comment-like-text", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                            appendTextNode(c);
                        }
                        break;

                    case ScriptDataDoubleEscapedLessThanSign:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-double-escaped-less-than-sign-state
                        c = getChar();
                        if (c == '/') {
                            temporaryBuffer = new StringBuilder();
                            tokenizeState = TokenizeState.ScriptDataDoubleEscapeEnd;
                            appendTextNode('/');
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                        }
                        break;


                    case ScriptDataDoubleEscapeEnd:
                        // https://html.spec.whatwg.org/multipage/parsing.html#script-data-double-escape-start-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ' || c == '/' || c == '>') {
                            if (temporaryBuffer.length() >= 6) {
                                String firstSixChars = temporaryBuffer.substring(0, 6);
                                if (firstSixChars.equalsIgnoreCase("script")) {
                                    tokenizeState = TokenizeState.ScriptDataEscaped;
                                   appendTextNode(c);
                                } else {
                                    tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                                   appendTextNode(c);
                                }
                            } else {
                                tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                                appendTextNode(c);
                            }
                        } else if (Character.isAlphabetic(c)) {
                            temporaryBuffer.append(c /*Character.toLowerCase(c)*/);
                            appendTextNode(c);
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                        }
                        break;

                    case BeforeAttributeName:
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // Ignore these.
                        } else if (c == '/' || c == '>' || c == CHAR_SUB) {
                            pushBack();
                            tokenizeState = TokenizeState.AfterAttributeName;
                        } else if (c == '=') {
                            handler.reportError("unexpected-equals-sign-before-attribute-name", null);
                            tokenizeState = TokenizeState.AttributeName;
                            emitTextIfAvailable(handler);
                            emitAttribute(null/* no prefix */, "=", "");
                        } else {
                            pushBack();
                            attrNameBuilder = new StringBuilder();
                            attrValueBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.AttributeName;
                        }
                        break;

                    case AttributeName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#attribute-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' '
                            || c == '/' || c == '>' || c == CHAR_SUB) {
                            pushBack();
                            tokenizeState = TokenizeState.AfterAttributeName;
                        } else if (c == '=') {
                            tokenizeState = TokenizeState.BeforeAttributeValue;
                        } else if (Character.isAlphabetic(c)) {
                            attrNameBuilder.append(c /*Character.toLowerCase(c)*/);
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            attrNameBuilder.append((char) 0xFFFD);
                        } else if (c == '"' || c == '\'' || c == '<') {
                            handler.reportError("unexpected-null-character", null);
                            attrNameBuilder.append((char) c);
                        } else {
                            attrNameBuilder.append((char) c);
                        }

                        if (tokenizeState != TokenizeState.AttributeName) {
                            // check duplication
                            if (containSameAttributeName(attrNameBuilder.toString())) {
                                handler.reportError("duplicate-attribute", null);
                                attrNameBuilder = new StringBuilder(); // reject this attribute 
                            }
                        }
                        break;
                    case AfterAttributeName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#after-attribute-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // Ignore these.
                        } else if (c == '/') {
                            emitAttribute(null, attrNameBuilder.toString(), attrValueBuilder.toString());
                            tokenizeState = TokenizeState.SelfClosingStartTag;
                        } else if (c == '=') {
                            tokenizeState = TokenizeState.BeforeAttributeValue;
                        } else if (c == '>') {
                            emitAttribute(null, attrNameBuilder.toString(), attrValueBuilder.toString());
                            emitTextIfAvailable(handler);
                            tokenizeState = TokenizeState.Data;
                            emitTag(handler, tagToken);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-tag", null);
                            emitAttribute(null, attrNameBuilder.toString(), attrValueBuilder.toString());
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            emitTextIfAvailable(handler);
                            emitAttribute(null, attrNameBuilder.toString(), "");
                            attrNameBuilder = new StringBuilder();
                            attrValueBuilder = new StringBuilder();
                            pushBack();
                            tokenizeState = TokenizeState.AttributeName;
                        }
                        break;

                    case BeforeAttributeValue:
                        // https://html.spec.whatwg.org/multipage/parsing.html#before-attribute-value-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // Ignore these.
                        } else if (c == '"') {
                            tokenizeState = TokenizeState.AttributeValueDoubleQuoted;
                        } else if (c == '\'') {
                            tokenizeState = TokenizeState.AttributeValueSingleQuoted;
                        } else if (c == '>') {
                            handler.reportError("missing-attribute-value", null);
                            emitTextIfAvailable(handler);
                            tokenizeState = TokenizeState.Data;
                            emitTag(handler, tagToken);
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.AttributeValueUnquoted;
                        }
                        break;
                    case AttributeValueDoubleQuoted:
                        c = getChar();
                        if (c == '"') {
                            // emitTextIfAvailable(handler);
                            emitAttribute(null, attrNameBuilder.toString(), attrValueBuilder.toString());
                            tokenizeState = TokenizeState.AfterAttributeValueQuoted;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            attrValueBuilder.append((char) 0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-tag", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            attrValueBuilder.append(c);
                        }
                        break;
                    case AttributeValueSingleQuoted:
                        c = getChar();
                        if (c == '\'') {
                            emitTextIfAvailable(handler);
                            emitAttribute(null, attrNameBuilder.toString(), attrValueBuilder.toString());
                            tokenizeState = TokenizeState.AfterAttributeValueQuoted;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            attrValueBuilder.append((char) 0xFFFD);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-tag", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            attrValueBuilder.append(c);
                        }
                        break;

                    case AttributeValueUnquoted:
                        // https://html.spec.whatwg.org/multipage/parsing.html#attribute-value-unquoted-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            emitTextIfAvailable(handler);
                            emitAttribute(null, attrNameBuilder.toString(), attrValueBuilder.toString());
                            tokenizeState = TokenizeState.BeforeAttributeName;
                        } else if (c == '>') {
                            emitTextIfAvailable(handler);
                            tokenizeState = TokenizeState.Data;
                            emitTag(handler, tagToken);
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            attrValueBuilder.append((char) 0xFFFD);
                        } else if (c == '"' || c == '\'' || c == '<' || c == '=' || c == '`') {
                            handler.reportError("unexpected-character-in-unquoted-attribute-value", null);
                            attrValueBuilder.append(c);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-tag", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            attrValueBuilder.append(c);
                        }
                        break;

                    case AfterAttributeValueQuoted:
                        // https://html.spec.whatwg.org/multipage/parsing.html#after-attribute-value-unquoted-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            tokenizeState = TokenizeState.BeforeAttributeName;
                        } else if (c == '/') {
                            tokenizeState = TokenizeState.SelfClosingStartTag;
                        } else if (c == '>') {
                            emitTextIfAvailable(handler);
                            tokenizeState = TokenizeState.Data;
                            emitTag(handler, tagToken);
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-tag", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            handler.reportError("missing-whitespace-between-attributes", null);
                            pushBack();
                            tokenizeState = TokenizeState.BeforeAttributeName;
                        }
                        break;

                    case SelfClosingStartTag:
                        // https://html.spec.whatwg.org/multipage/parsing.html#self-closing-start-tag-state
                        emitTextIfAvailable(handler);
                        c = getChar();
                        if (c == '>') {
                            emitSelfClosingTag(handler, tagToken);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-tag", null);
                            emitEof();
                        } else {
                            handler.reportError("unexpected-solidus-in-tag", null);
                            pushBack();
                            tokenizeState = TokenizeState.BeforeAttributeName;
                        }
                        break;

                    case BogusComment:
                        // https://html.spec.whatwg.org/multipage/parsing.html#self-closing-start-tag-state
                        c = getChar();
                        if (c == '>') {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), mayXmlDeclAsBogusComment);
                            mayXmlDeclAsBogusComment = false;
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), mayXmlDeclAsBogusComment);
                            mayXmlDeclAsBogusComment = false;
                            handler.reportError("eof-in-comment", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            commentBuilder.append((char) 0xFFFD);
                        } else {
                            commentBuilder.append((char) c);
                        }
                        break;

                    case MarkupDeclarationOpen:
                        // https://html.spec.whatwg.org/multipage/parsing.html#markup-declaration-open-state
                        if (skipString("--")) {
                            commentBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.CommentStart;
                        } else if (skipStringIgnoreCase("DOCTYPE")) {
                            tokenizeState = TokenizeState.Doctype;
                        } else if (skipString("[CDATA[")) {
                            handler.reportError("cdata-in-html-content", null);
                            commentBuilder = new StringBuilder();
                            commentBuilder.append("[CDATA[");
                            tokenizeState = TokenizeState.BogusComment;
                        } else {
                            handler.reportError("incorrectly-opened-comment", null);
                            tokenizeState = TokenizeState.BogusComment;
                        }
                        break;
                    case CommentStart:
                        // https://html.spec.whatwg.org/multipage/parsing.html#comment-start-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.CommentStartDash;
                        } else if (c == '>') {
                            handler.reportError("abrupt-closing-of-empty-comment", null);
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), mayXmlDeclAsBogusComment);
                            mayXmlDeclAsBogusComment = false;
                            tokenizeState = TokenizeState.Data;
                        } else {
                            tokenizeState = TokenizeState.Comment;
                            pushBack();
                        }
                        break;
                    case CommentStartDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html#comment-start-dash-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.CommentEnd;
                        } else if (c == '>') {
                            handler.reportError("abrupt-closing-of-empty-comment", null);
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            handler.reportError("eof-in-comment", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            commentBuilder.append((char) '-');
                            tokenizeState = TokenizeState.Comment;
                            pushBack();
                            commentBuilder.append((char) c);
                        }
                        break;


                    case Comment:
                        // https://html.spec.whatwg.org/multipage/parsing.html
                        c = getChar();
                        if (c == '<') {
                            commentBuilder.append((char) '<');
                            tokenizeState = TokenizeState.CommentLessThanSign;
                        } else if (c == '-') {
                            tokenizeState = TokenizeState.CommentEndDash;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            commentBuilder.append((char) 0xFFFD);
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            handler.reportError("eof-in-comment", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            commentBuilder.append((char) c);
                        }
                        break;

                    case CommentLessThanSign:
                        // https://html.spec.whatwg.org/multipage/parsing.html
                        c = getChar();
                        if (c == '!') {
                            commentBuilder.append((char) '!');
                            tokenizeState = TokenizeState.CommentLessThanSignBang;
                        } else if (c == '<') {
                            commentBuilder.append((char) c);
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.Comment;
                        }
                        break;

                    case CommentLessThanSignBang:
                        // https://html.spec.whatwg.org/multipage/parsing.html
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.CommentLessThanSignBangDash;
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.Comment;
                        }
                        break;
                    
                    case CommentLessThanSignBangDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.CommentLessThanSignBangDashDash;
                        } else {
                            pushBack();
                            tokenizeState = TokenizeState.CommentEndDash;
                        }
                        break;

                    case CommentLessThanSignBangDashDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html
                        c = getChar();
                        if (c == '>' || c == CHAR_SUB) {
                            pushBack();
                            tokenizeState = TokenizeState.CommentEnd;
                        } else {
                            handler.reportError("nested-comment", null);
                            pushBack();
                            tokenizeState = TokenizeState.CommentEnd;
                        }
                        break;

                    case CommentEndDash:
                        // https://html.spec.whatwg.org/multipage/parsing.html
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.CommentEnd;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-comment", null);
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            commentBuilder.append((char) '-');
                            pushBack();
                            tokenizeState = TokenizeState.Comment;
                        }
                        break;

                    case CommentEnd:
                        // https://html.spec.whatwg.org/multipage/parsing.html
                        c = getChar();
                        if (c == '>') {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == '!') {
                            tokenizeState = TokenizeState.CommentEndBang;
                        } else if (c == '-') {
                            commentBuilder.append('-');
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            handler.reportError("eof-in-comment", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            commentBuilder.append((char) '-');
                            commentBuilder.append((char) '-');
                            pushBack();
                            tokenizeState = TokenizeState.Comment;
                        }
                        break;

                    case CommentEndBang:
                        // https://html.spec.whatwg.org/multipage/parsing.html
                        c = getChar();
                        if (c == '-') {
                            commentBuilder.append('-');
                            commentBuilder.append('-');
                            commentBuilder.append('!');
                            tokenizeState = TokenizeState.CommentEndDash;
                        } else if (c == '>') {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            handler.reportError("incorrectly-closed-comment", null);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            handler.reportError("eof-in-comment", null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            commentBuilder.append((char) '-');
                            commentBuilder.append((char) '-');
                            commentBuilder.append((char) '!');
                            pushBack();
                            tokenizeState = TokenizeState.Comment;
                        }
                        break;

                    case Doctype:
                        // https://html.spec.whatwg.org/multipage/parsing.html#doctype-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            tokenizeState = TokenizeState.BeforeDoctypeName;
                        } else if (c == '>') {
                            pushBack();
                            tokenizeState = TokenizeState.BeforeDoctypeName;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            handler.reportError("missing-whitespace-before-doctype-name", null);
                            pushBack();
                            tokenizeState = TokenizeState.BeforeDoctypeName;
                        }
                        break;

                    case BeforeDoctypeName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#before-doctype-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // Ignore these
                        } else if (Character.isAlphabetic(c)) {
                            doctype.nameBuilder = new StringBuilder();
                            doctype.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                            tokenizeState = TokenizeState.DoctypeName;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            doctype.nameBuilder.append((char) 0xFFFD);
                            tokenizeState = TokenizeState.DoctypeName;
                        } else if (c == '>' ) {
                            handler.reportError("missing-doctype-name", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            doctype.nameBuilder = new StringBuilder();
                            doctype.nameBuilder.append(c);
                            tokenizeState = TokenizeState.DoctypeName;
                        }
                        break;

                    case DoctypeName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#before-doctype-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            tokenizeState = TokenizeState.AfterDoctypeName;
                        } else if (Character.isAlphabetic(c)) {
                            doctype.nameBuilder.append(c /*Character.toLowerCase(c)*/);
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            doctype.nameBuilder.append((char) 0xFFFD);
                        } else if (c == '>' ) {
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            doctype.nameBuilder.append(c);
                        }
                        break;

                    case AfterDoctypeName:
                        // https://html.spec.whatwg.org/multipage/parsing.html#after-doctype-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // Ignore these
                        } else if (c == '>' ) {
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            if ((c == 'p' || c == 'P') && skipStringIgnoreCase("UBLIC")) {
                                tokenizeState = TokenizeState.AfterDoctypePublicKeyword;
                            } else if ((c == 's' || c == 'S') && skipStringIgnoreCase("YSTEM")) {
                                tokenizeState = TokenizeState.AfterDoctypeSystemKeyword;
                            } else {
                                handler.reportError("invalid-character-sequence-after-doctype-name", null);
                                pushBack();
                                doctype.forceQuirkFlag = true;
                                tokenizeState = TokenizeState.BogusDoctype;
                            }
                        }
                        break;

                    case AfterDoctypePublicKeyword:
                        // https://html.spec.whatwg.org/multipage/parsing.html#before-doctype-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            tokenizeState = TokenizeState.BeforeDoctypePublicIdentifier;
                        } else if (c == '"') {
                            handler.reportError("missing-whitespace-after-doctype-public-keyword", null);
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypePublicIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            handler.reportError("missing-whitespace-after-doctype-public-keyword", null);
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypePublicIdentifierSingleQuoted;
                        } else if (c == '>') {
                            handler.reportError("missing-doctype-public-identifier", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            handler.reportError("missing-quote-before-doctype-public-identifier", null);
                            doctype.forceQuirkFlag = true;
                            pushBack();
                            tokenizeState = TokenizeState.BogusDoctype;
                        }
                        break;

                    case BeforeDoctypePublicIdentifier:
                        // https://html.spec.whatwg.org/multipage/parsing.html#after-doctype-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // Ignore these
                        } else if (c == '"') {
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypePublicIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypePublicIdentifierSingleQuoted;
                        } else if (c == '>') {
                            handler.reportError("missing-doctype-public-identifier", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            handler.reportError("missing-quote-before-doctype-public-identifier", null);
                            doctype.forceQuirkFlag = true;
                            pushBack();
                            tokenizeState = TokenizeState.BogusDoctype;
                        }
                        break;

                    case DoctypePublicIdentifierDoubleQuoted:
                        // https://html.spec.whatwg.org/multipage/parsing.html#doctype-public-identifier-(double-quoted)-state
                        c = getChar();
                        if (c == '"') {
                            tokenizeState = TokenizeState.AfterDoctypePublicIdentifier;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            doctype.publicIdBuilder.append((char) 0xFFFD);
                        } else if (c == '>') {
                            handler.reportError("abrupt-doctype-public-identifier", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            doctype.publicIdBuilder.append((char) c);
                        }
                        break;

                    case DoctypePublicIdentifierSingleQuoted:
                        // https://html.spec.whatwg.org/multipage/parsing.html#doctype-public-identifier-(single-quoted)-state
                        c = getChar();
                        if (c == '\'') {
                            tokenizeState = TokenizeState.AfterDoctypePublicIdentifier;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            doctype.publicIdBuilder.append((char) 0xFFFD);
                        } else if (c == '>') {
                            handler.reportError("abrupt-doctype-public-identifier", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            doctype.publicIdBuilder.append((char) c);
                        }
                        break;

                    case AfterDoctypePublicIdentifier:
                        // https://html.spec.whatwg.org/multipage/parsing.html#after-doctype-public-identifier-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            tokenizeState = TokenizeState.BetweenDoctypePublicAndSystemIdentifiers;
                        } else if (c == '>') {
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == '"') {
                            handler.reportError("missing-whitespace-between-doctype-public-and-system-identifiers", null);
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            handler.reportError("missing-whitespace-between-doctype-public-and-system-identifiers", null);
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierSingleQuoted;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            handler.reportError("missing-quote-before-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = true;
                            pushBack();
                            tokenizeState = TokenizeState.BogusDoctype;
                        }
                        break;

                    case BetweenDoctypePublicAndSystemIdentifiers:
                        // https://html.spec.whatwg.org/multipage/parsing.html#after-doctype-public-identifier-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // ignore these
                        } else if (c == '>') {
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == '"') {
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierSingleQuoted;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            handler.reportError("missing-quote-before-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = true;
                            pushBack();
                            tokenizeState = TokenizeState.BogusDoctype;
                        }
                        break;

                    case AfterDoctypeSystemKeyword:
                        // https://html.spec.whatwg.org/multipage/parsing.html#before-doctype-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            tokenizeState = TokenizeState.BeforeDoctypePublicIdentifier;
                        } else if (c == '"') {
                            handler.reportError("missing-whitespace-after-doctype-system-keyword", null);
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            handler.reportError("missing-whitespace-after-doctype-system-keyword", null);
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierSingleQuoted;
                        } else if (c == '>') {
                            handler.reportError("missing-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            handler.reportError("missing-quote-before-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = true;
                            pushBack();
                            tokenizeState = TokenizeState.BogusDoctype;
                        }
                        break;

                    case BeforeDoctypeSystemIdentifier:
                        // https://html.spec.whatwg.org/multipage/parsing.html#before-doctype-system-identifier-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // Ignore these
                        } else if (c == '"') {
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierSingleQuoted;
                        } else if (c == '>') {
                            handler.reportError("missing-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            handler.reportError("missing-quote-before-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = true;
                            pushBack();
                            tokenizeState = TokenizeState.BogusDoctype;
                        }
                        break;


                    case DoctypeSystemIdentifierDoubleQuoted:
                        // https://html.spec.whatwg.org/multipage/parsing.html#doctype-system-identifier-(double-quoted)-state
                        c = getChar();
                        if (c == '"') {
                            tokenizeState = TokenizeState.AfterDoctypeSystemIdentifier;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            doctype.systemIdBuilder.append((char) 0xFFFD);
                        } else if (c == '>') {
                            handler.reportError("abrupt-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            doctype.systemIdBuilder.append((char) c);
                        }
                        break;

                    case DoctypeSystemIdentifierSingleQuoted:
                        // https://html.spec.whatwg.org/multipage/parsing.html#doctype-system-identifier-(single-quoted)-state
                        c = getChar();
                        if (c == '\'') {
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.AfterDoctypeSystemIdentifier;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            doctype.systemIdBuilder.append((char) 0xFFFD);
                        } else if (c == '>') {
                            handler.reportError("abrupt-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            doctype.systemIdBuilder.append((char) c);
                        }
                        break;

                    case AfterDoctypeSystemIdentifier:
                        // https://html.spec.whatwg.org/multipage/parsing.html#after-doctype-system-identifier-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // ignore these
                        } else if (c == '>') {
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-doctype", null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            handler.reportError("unexpected-character-after-doctype-system-identifier", null);
                            doctype.forceQuirkFlag = false; // (This does not set the current DOCTYPE token's force-quirks flag to on.) 
                            pushBack();
                            tokenizeState = TokenizeState.BogusDoctype;
                        }
                        break;

                    case BogusDoctype:
                        // https://html.spec.whatwg.org/multipage/parsing.html#bogus-doctype-state
                        c = getChar();
                        if (c == '>') {
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == 0) {
                            handler.reportError("unexpected-null-character", null);
                            // ignore this character
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            // ignore this character
                        }
                        break;

                    case CDataSection:
                        // https://html.spec.whatwg.org/multipage/parsing.html#cdata-section-state
                        c = getChar();
                        if (c == ']') {
                            tokenizeState = TokenizeState.CDataSectionBracket;
                        } else if (c == CHAR_SUB) {
                            handler.reportError("eof-in-cdata", null);
                            emitEof();
                        } else {
                            appendTextNode(c); 
                        }
                        break;

                    case CDataSectionBracket:
                        // https://html.spec.whatwg.org/multipage/parsing.html#cdata-section-bracket-state
                        c = getChar();
                        if (c == ']') {
                            tokenizeState = TokenizeState.CDataSectionEnd;
                        } else {
                            appendTextNode(']'); 
                            pushBack();
                            tokenizeState = TokenizeState.CDataSection;
                        }
                        break;

                    case CDataSectionEnd:
                        // https://html.spec.whatwg.org/multipage/parsing.html#cdata-section-end-state
                        c = getChar();
                        if (c == ']') {
                            appendTextNode(']'); 
                        } else if (c == '>') {
                            tokenizeState = TokenizeState.Data;
                        } else {
                            appendTextNode(']'); 
                            pushBack();
                            tokenizeState = TokenizeState.CDataSection;
                        }
                        break;
                    
                    case CharacterReference:
                        // not supported
                        break;
                    case NamedCharacterReference:
                        // not supported
                        break;
                    case AmbiguousAmpersand:
                        // not supported
                        break;
                    case NumericCharacterReference:
                        // not supported
                        break;
                    case HexadecimalCharacterReferenceStart:
                        // not supported
                        break;
                    case DecimalCharacterReferenceStart:
                        // not supported
                        break;
                    case HexadecimalCharacterReference:
                        // not supported
                        break;
                    case DecimalCharacterReference:
                        // not supported
                        break;
                    case NumericCharacterReferenceEnd:
                        // not supported
                        break;
                }
                handler = handler.getNextHandler();
            } while (c != CHAR_SUB);
        } catch (ScanningInterruptedExeption e) {
            // EOF
            return;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }
    }

}