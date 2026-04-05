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
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.builder.parser.HtmlTokenizer.TagToken;
import org.xml.sax.InputSource;
import org.seasar.mayaa.impl.knowledge.HTMLKnowledge;
import org.xml.sax.Locator;

/**
 * 
 * 外部の文書宣言は解決しない
 * フォーマル公開識別子については妥当性の検証を行わない。
 * HTML文字参照以外は解決しない
 */
public class HtmlStandardScanner {
    static final Log LOG = LogFactory.getLog(HtmlStandardScanner.class);
    static final Log LOG_TOKENHANDLER = LogFactory.getLog(HtmlStandardScanner.class.getName() + ".TokenHandler");
    static final Log LOG_TOKENIZER = LogFactory.getLog(HtmlStandardScanner.class.getName() + ".Tokenizer");

    static final String NS_URI_HTML = "http://www.w3.org/1999/xhtml";

    static final String NS_URI_MATHML = "http://www.w3.org/1998/Math/MathML";

    static final String NS_URI_SVG = "http://www.w3.org/2000/svg";

    static final String NS_URI_XLINK = "http://www.w3.org/1999/xlink";

    static final String NS_URI_XML = "http://www.w3.org/XML/1998/namespace";

    static final String NS_URI_XMLNS = "http://www.w3.org/2000/xmlns/";

    static final HtmlAttributesImpl EMPTY_ATTRIBUTES = new HtmlAttributesImpl();

    static final Pattern REGEX_WHITESPACE_ONLY = Pattern.compile("\\s+");

    static final ElemName QN_HTML = new ElemName(null, "html", "html", NS_URI_HTML);
    static final ElemName QN_HEAD = new ElemName(null, "head", "head", NS_URI_HTML);
    static final ElemName QN_BODY = new ElemName(null, "body", "body", NS_URI_HTML);
    static final ElemName QN_TEMPLATE = new ElemName(null, "template", "template", NS_URI_HTML);
    /**  */
    HtmlDocumentHandler documentHandler = null;

    HtmlTokenizer tokenizer;

    //ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
    // CharBuffer buffer = CharBuffer.allocate(8 * 1024);
    InputSource inputSource;

    public void setInputSource(InputSource inputSource) throws IOException {
        tokenizer.setInputSource(inputSource);
        this.inputSource = inputSource;
    }

    public void setDocumentHandler(HtmlDocumentHandler handler) {
        this.documentHandler = handler;
    }

    public HtmlDocumentHandler getDocumentHandler() {
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
    
    public String[] getRecognizedFeatures() {
        return new String[] {
            FEATURE_DELETE_UNEXPECTED_ELEMENT,
            FEATURE_INSERT_IMPLIED_ELEMENT,
            FEATURE_DOCUMENT_FRAGMENT,
        };
    }

    public Boolean getFeatureDefault(String featureId) {
        switch (featureId) {
            case FEATURE_DELETE_UNEXPECTED_ELEMENT: return Boolean.FALSE;
            case FEATURE_INSERT_IMPLIED_ELEMENT: return Boolean.FALSE;
            case FEATURE_DOCUMENT_FRAGMENT: return Boolean.TRUE;
            default: return Boolean.FALSE;
        }
    }

    public void setFeature(String featureId, boolean state) {
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
    public static final String PROPERTY_PREFIX = "http://mayaa.seasar.org/parser/property/";

    // PROPERTEIS:END

    // INTERNAL UTILS
    boolean matchOneOfThese(String target, String... compare) {
        for (String c : compare) {
            if (c.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    ElemName createHtmlElemName(String tagName) {
        return new ElemName(null, tagName, tagName, NS_URI_HTML);
    }

    void insertStartTag(TagToken tagToken, HtmlAttributesImpl attributes) {
        ElemName elemName = createHtmlElemName(tagToken.nameBuilder.toString());
        insertStartTag(elemName, attributes);
    }

    void insertStartTag(ElemName elemName, HtmlAttributesImpl attributes) {
        if (HTMLKnowledge.isVoidElementLocalPart(elemName.localName())) {
            if (LOG_TOKENHANDLER.isTraceEnabled()) {
                LOG_TOKENHANDLER.trace(String.format("%s: Insert void tag: <%s>", insertionMode, elemName.localName()));
            }
            documentHandler.emptyElement(elemName, attributes);
        } else {
            if (LOG_TOKENHANDLER.isTraceEnabled()) {
                LOG_TOKENHANDLER.trace(String.format("%s: Insert start tag: <%s>", insertionMode, elemName.localName()));
            }
            unclosedElementStack.push(elemName);
            documentHandler.startElement(elemName, attributes);
        }
    }

    void insertEndTag(TagToken tagToken) {
        ElemName elemName = createHtmlElemName(tagToken.nameBuilder.toString());
        insertEndTag(elemName);
    }

    void insertEndTag(final ElemName elemName) {
        try {
            do {
                ElemName top = unclosedElementStack.peek();
                if (elemName.localName().equals(top.localName())) {
                    break;
                }
                if (LOG_TOKENHANDLER.isTraceEnabled()) {
                    LOG_TOKENHANDLER.trace(String.format("%s: Insert implied end tag: </%s>", insertionMode, top.localName()));
                }
                unclosedElementStack.pop();
                documentHandler.endElement(top);
            } while(true);

            if (LOG_TOKENHANDLER.isTraceEnabled()) {
                LOG_TOKENHANDLER.trace(String.format("%s: Insert end tag: </%s>", insertionMode, elemName.localName()));
            }
            unclosedElementStack.pop();
            documentHandler.endElement(elemName);
        } catch (EmptyStackException e) {
            // An appropriate end tag token is an end tag token whose tag name matches the tag name of the last start tag 
            // to have been emitted from this tokenizer, if any. If no start tag has been emitted from this tokenizer,
            // then no end tag token is appropriate.
        }
    }
    void insertImpliedStartTag(ElemName elemName) {
        if (HTMLKnowledge.isVoidElementLocalPart(elemName.localName())) {
            if (LOG_TOKENHANDLER.isTraceEnabled()) {
                LOG_TOKENHANDLER.trace(String.format("%s: Insert implied void tag: <%s>", insertionMode, elemName.localName()));
            }
            documentHandler.emptyElement(elemName, EMPTY_ATTRIBUTES);
        } else {
            if (LOG_TOKENHANDLER.isTraceEnabled()) {
                LOG_TOKENHANDLER.trace(String.format("%s: Insert implied start tag: <%s>", insertionMode, elemName.localName()));
            }
            unclosedElementStack.push(elemName);
            documentHandler.startElement(elemName, EMPTY_ATTRIBUTES);
        }
    }
    void insertImpliedEndTag(ElemName elemName) {
        if (LOG_TOKENHANDLER.isTraceEnabled()) {
            LOG_TOKENHANDLER.trace(String.format("%s: Insert implied end tag: </%s>", insertionMode, elemName.localName()));
        }
        unclosedElementStack.pop();
        documentHandler.endElement(elemName);
    }

    void insertImpliedEndTagIfOpened(final ElemName elemName) {
        for (ElemName en : unclosedElementStack) {
            if (en.localName().equalsIgnoreCase(elemName.localName())) {
                insertEndTag(elemName);
                break;
            }
        }
    }

    boolean isStartTagOf(TagToken tagToken, String ... tagName) {
        if (tagToken.isEndTag) {
            return false;
        }
        final String rawname = tagToken.nameBuilder.toString();
        if (tagName.length == 1) {
            return tagName[0].equalsIgnoreCase(rawname);
        } else if (tagName.length > 1 && matchOneOfThese(rawname, tagName)) {
            return true;
        }
        return false;
    }

    boolean isEndTagOf(TagToken tagToken, String ... tagName) {
        if (!tagToken.isEndTag) {
            return false;
        }
        final String rawname = tagToken.nameBuilder.toString();
        if (tagName.length == 1) {
            return tagName[0].equalsIgnoreCase(rawname);
        } else if (tagName.length > 1 && matchOneOfThese(rawname, tagName)) {
            return true;
        }
        return false;
    }

    // INTERNAL UTILS:END

    /**
     * Initially, the stack of open elements is empty.
     * https://html.spec.whatwg.org/multipage/parsing.html#the-stack-of-open-elements
     */
    Stack<ElemName> unclosedElementStack = new Stack<>();
    /*
     * Document Handler
     */
    enum InsertionMode {
        Initial,
        BeforeHtml,
        BeforeHead,
        InHead,
        InHeadNoScript,
        AfterHead,
        InBody,
        Text,
        InTable,
        InTableText,
        InCaption,
        InColumnGroup,
        InTableBody,
        InRow,
        InCell,
        InSelect,
        InSelectInTable,
        InTemplate,
        AfterBody,
        InFrameset,
        AfterFrameset,
        AfterAfterBody,
        AfterAfterFrameset;
    }

    private final EnumMap<InsertionMode, TokenHandler> handlers = new EnumMap<>(InsertionMode.class);

    HtmlStandardScanner() {
        TokenHandlerBase fallback = new TokenHandlerBase();
        for (InsertionMode m : InsertionMode.values()) {
            handlers.put(m, fallback);
        }
        handlers.put(InsertionMode.Initial,     new TokenHandlerInitial());
        handlers.put(InsertionMode.BeforeHtml,  new TokenHandlerBeforeHtml());
        handlers.put(InsertionMode.BeforeHead,  new TokenHandlerBeforeHead());
        handlers.put(InsertionMode.InHead,      new TokenHandlerInHead());
        handlers.put(InsertionMode.AfterHead,   new TokenHandlerAfterHead());
        handlers.put(InsertionMode.InBody,      new TokenHandlerInBody());
        handlers.put(InsertionMode.AfterBody,   new TokenHandlerAfterBody());
    }

    public boolean fragmentCase = false;

    InsertionMode insertionMode = InsertionMode.Initial;

    private void setInsertionMode(InsertionMode insertionMode) {
        if (HtmlStandardScanner.LOG_TOKENHANDLER.isTraceEnabled()) {
            HtmlStandardScanner.LOG_TOKENHANDLER.trace("Insertion mode:" + this.insertionMode + " -> " + insertionMode);
        }
        this.insertionMode = insertionMode;
    }
    
    class TokenHandlerInitial extends TokenHandlerBase {
        @Override
        public void emitText(HtmlTokenizer tokenizer, HtmlLocation location, String text) {
            // ignore whitespaces before doctype, otherwise change insertion mode to "before html"
            if (REGEX_WHITESPACE_ONLY.matcher(text).matches()) {
                // Ignore whitespace originally, but to retain template as it.
                super.emitText(tokenizer, location, text);
            } else {
                setInsertionMode(InsertionMode.BeforeHtml);
                handlers.get(insertionMode).emitText(tokenizer, location, text);
            }
        }

        @Override
        public void emitDoctype(HtmlTokenizer tokenizer, HtmlLocation location, String doctypeName, String publicId, String systemId) {
            if (documentHandler != null) {
                documentHandler.doctypeDecl(doctypeName, publicId, systemId);
            }
            fragmentCase = false;
            setInsertionMode(InsertionMode.BeforeHtml);
        }

        @Override
        public void emitXmlDecl(HtmlTokenizer tokenizer, HtmlLocation location, String version, String encoding, String standalone) {
            documentHandler.xmlDecl(version, encoding, standalone);
        }

        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, HtmlAttributesImpl attributes) {
            setInsertionMode(InsertionMode.BeforeHtml);
            handlers.get(insertionMode).emitTag(tokenizer, location, tagToken, attributes);
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
                // Ignore whitespace originally, but to retain template as it.
                super.emitText(tokenizer, location, text);
            } else {
                super.emitText(tokenizer, location, text);
                if (!fragmentCase) {
                    insertImpliedStartTag(QN_HTML);
                }
                setInsertionMode(InsertionMode.BeforeHead);
            }
        }

        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, HtmlAttributesImpl attributes) {
            final String tagName = tagToken.nameBuilder.toString();
            if (tagToken.isEndTag && !matchOneOfThese(tagName, "head", "body", "html", "br")) {
                // AND IGNORE THIS TOKEN
            } else if (isStartTagOf(tagToken, "html")) {
                insertStartTag(QN_HTML, attributes);
                setInsertionMode(InsertionMode.BeforeHead);
            } else {
                if (!fragmentCase) {
                    insertImpliedStartTag(QN_HTML);
                }
                setInsertionMode(InsertionMode.BeforeHead);

                handlers.get(insertionMode).emitTag(tokenizer, location, tagToken, attributes);
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
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, HtmlAttributesImpl attributes) {
            final String tagName = tagToken.nameBuilder.toString();
            if (tagToken.isEndTag && !matchOneOfThese(tagName, "head", "body", "html", "br")) {
                // AND IGNORE THIS TOKEN
            } else if (isStartTagOf(tagToken, "html")) {
                if (unclosedElementStack.contains(QN_TEMPLATE)) {
                    // If there is a template element on the stack of open elements, then ignore the token.
                    // IGNORE THIS TOKEN
                } else {
                    // Otherwise, for each attribute on the token, check to see if the attribute is already 
                    // present on the top element of the stack of open elements. If it is not, add the attribute
                    // and its corresponding value to that element.
                }
                documentHandler.startElement(QN_HEAD, EMPTY_ATTRIBUTES);
                setInsertionMode(InsertionMode.InHead);
            } else if (isStartTagOf(tagToken, "head")) {
                insertStartTag(tagToken, attributes);
                setInsertionMode(InsertionMode.InHead);
            } else {
                if (fragmentCase) {
                    setInsertionMode(InsertionMode.AfterHead);
                } else if (isStartTagOf(tagToken, "body") && !featureInsertImpliedElement) {
                    setInsertionMode(InsertionMode.AfterHead);
                } else {
                    insertImpliedStartTag(QN_HEAD);
                    setInsertionMode(InsertionMode.InHead);
                }

                handlers.get(insertionMode).emitTag(tokenizer, location, tagToken, attributes);
            }
        }
    }

    /**
     * Handle tokens in "in head" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#the-in-head-insertion-mode
     */
    class TokenHandlerInHead extends TokenHandlerBase {
        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, HtmlAttributesImpl attributes) {
            if (isStartTagOf(tagToken, "meta", "base", "basefont", "bgsound", "link", "title", "template", "script", "noscript", "noframe", "style")) {
                insertStartTag(tagToken, attributes);
            } else if (isEndTagOf(tagToken, "template")) {
                insertEndTag(tagToken);
            } else if (isEndTagOf(tagToken, "head")) {
                insertEndTag(tagToken);
                setInsertionMode(InsertionMode.AfterHead);
            } else if (tagToken.isEndTag) {
                insertEndTag(tagToken);
            } else {
                // headがすでに閉じている状態でbody以外のタグが検出されたとき
                insertImpliedEndTagIfOpened(QN_HEAD);
                setInsertionMode(InsertionMode.AfterHead);
                handlers.get(insertionMode).emitTag(tokenizer, location, tagToken, attributes);
            }
        }
    }

    /**
     * Handle tokens in "after head" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inhead
     */
    class TokenHandlerAfterHead extends TokenHandlerBase {
        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, HtmlAttributesImpl attributes) {
            if (isStartTagOf(tagToken, "head", "meta", "base", "basefont", "bgsound", "link", "title", "template", "script", "noscript", "noframe", "style")) {
                insertStartTag(tagToken, attributes);
            } else if (isStartTagOf(tagToken, "body")) {
                insertStartTag(QN_BODY, attributes);
                setInsertionMode(InsertionMode.InBody);
            } else if (tagToken.isEndTag) {
                insertEndTag(tagToken);
            } else {
                // headがすでに閉じている状態でbody以外のタグが検出されたとき
                if (!fragmentCase) {
                    insertImpliedStartTag(QN_BODY);
                }
                setInsertionMode(InsertionMode.InBody);
                handlers.get(insertionMode).emitTag(tokenizer, location, tagToken, attributes);
            }
        }
    }

    /**
     * Handle tokens in "in body" insertion mode.
     * https://html.spec.whatwg.org/multipage/parsing.html#parsing-main-inbody
     */
    class TokenHandlerInBody extends TokenHandlerBase {
        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, HtmlAttributesImpl attributes) {
            if (isStartTagOf(tagToken, "html")) {
                // Parse error.
                if (unclosedElementStack.contains(QN_TEMPLATE)) {
                    // If there is a template element on the stack of open elements, then ignore the token.
                    // IGNORE THIS TOKEN
                } else {
                    // Otherwise, for each attribute on the token, check to see if the attribute is already 
                    // present on the top element of the stack of open elements. If it is not, add the attribute
                    // and its corresponding value to that element.
                }
            } else if (isStartTagOf(tagToken, "body")) {
                // AND IGNORE THIS TOKEN
            } else if (isEndTagOf(tagToken, "body")) {
                insertEndTag(tagToken);
                setInsertionMode(InsertionMode.AfterBody);
            } else if (featureDeleteUnexpectedElement && isStartTagOf(tagToken, "head", "noscript")) {
                //     // IGNORE
            } else if (!tagToken.isEndTag) {
                insertStartTag(tagToken, attributes);
            } else if (tagToken.isEndTag) {
                insertEndTag(tagToken);
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
            // IGNORE THIS TOKEN
        }

        @Override
        public void emitDoctype(HtmlTokenizer tokenizer, HtmlLocation location, String doctypeName, String publicId, String systemId) {
            // AND IGNORE THIS TOKEN
        }
    
        @Override
        public void emitComment(HtmlTokenizer tokenizer, HtmlLocation location, String comment) {
            documentHandler.comment(comment);
        }
    
        @Override
        public void emitText(HtmlTokenizer tokenizer, HtmlLocation location, String text) {
            documentHandler.characters(text);
        }
    
        @Override
        public void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, HtmlAttributesImpl attributes) {        
            final String tagName = tagToken.nameBuilder.toString();
            if (tagToken.isSelfClosingTag) {
                documentHandler.emptyElement(createHtmlElemName(tagName), attributes);
            } else if (tagToken.isEndTag) {
                insertEndTag(createHtmlElemName(tagName));
            } else {
                insertStartTag(createHtmlElemName(tagName), attributes);
            }
        }

        @Override
        public TokenHandler getNextHandler() {
            return handlers.get(insertionMode);
        }
    }

    public void reset(org.xml.sax.ErrorHandler errorHandler) {
        tokenizer = new HtmlTokenizer();
        tokenizer.setErrorReporter(new DefaultHTMLErrorHandler(errorHandler));
        tokenizer.setInputSource(inputSource);
        tokenizer.reset();

        setInsertionMode(InsertionMode.Initial);
        unclosedElementStack = new Stack<>();
        fragmentCase = featureDocumentFragment;
    }

    public boolean scanDocument(boolean complete) throws IOException {
        try {
            Locator locator = tokenizer.getLocator();
            documentHandler.startDocument(locator, inputSource.getEncoding());

            tokenizer.runTokenizer(handlers.get(insertionMode));
            // return success
            return true;
        } catch (BufferUnderflowException e) {
            return false;
        }
    }

}

/**
 * Represents an XML/HTML element name (prefix, localName, rawName, uri).
 * Replaces Xerces QName within the HTML parser pipeline.
 */
record ElemName(String prefix, String localName, String rawName, String uri) {}

/**
 * Document event receiver for the HTML parser pipeline.
 * Replaces Xerces XMLDocumentHandler with a SAX-aligned interface.
 */
interface HtmlDocumentHandler {
    void startDocument(org.xml.sax.Locator locator, String encoding) throws java.io.IOException;
    void xmlDecl(String version, String encoding, String standalone);
    void doctypeDecl(String name, String publicId, String systemId);
    void startElement(ElemName elemName, org.xml.sax.Attributes attributes);
    void endElement(ElemName elemName);
    void emptyElement(ElemName elemName, org.xml.sax.Attributes attributes);
    void characters(String text);
    void comment(String text);
}

class ScanningInterruptedExeption extends Exception {
    private static final long serialVersionUID = -592372281502837246L;

    public ScanningInterruptedExeption() {
        super();
    }

    public ScanningInterruptedExeption(Throwable throwable) {
        super(throwable);
    }
}

class HtmlLocation implements Locator, Cloneable {
    int line = 1;
    int column = 1;
    int offset = 0;
    String publicId = null;
    String systemId = null;
    CharBuffer cbuf;

    void setCharBuffer(CharBuffer cbuf) {
        this.cbuf = cbuf;
    }

    @Override
    public String toString() {
        return String.format("(%06d)l%d:c%d", offset, line, column);
    }

    public void copyPositionTo(HtmlLocation copy) {
        copy.column = column;
        copy.line = line;
        copy.offset = offset;
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

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

    public String getLiteralSystemId() {
        return getSystemId();
    }

    public String getBaseSystemId() {
        return getSystemId();
    }

    public String getExpandedSystemId() {
        return getSystemId();
    }

    public String getEncoding() {
        return null;
    }

    public String getXMLVersion() {
        return null;
    }

    public String getTextAroundCurrent() {
        final int range = 10; // 取得する文字数の範囲

        // 現在の位置を取得
        int currentPosition = cbuf.position();

        // 開始位置 (0未満にならないようにする)
        int start = Math.max(currentPosition - range, 0);

        // 終了位置 (バッファの終端を超えないようにする)
        int end = Math.min(currentPosition + range, cbuf.limit());

        // 現在の位置を復元するためにバックアップ
        int originalPosition = cbuf.position();

        // 指定範囲のサブシーケンスを取得
        cbuf.position(start);
        char[] result = new char[end - start];
        cbuf.get(result);

        // 位置を元に戻す
        cbuf.position(originalPosition);

        return new String(result).replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r");
    }
}

/**
 * Mutable SAX Attributes implementation used internally by the HTML parser.
 */
class HtmlAttributesImpl implements org.xml.sax.Attributes {
    private static final String ATTR_TYPE_CDATA = "CDATA";

    static class Attr {
        final String prefix;
        final String localName;
        final String rawName;
        final String uri;
        final String type;
        String value;

        Attr(String prefix, String localName, String rawName, String uri, String type, String value) {
            this.prefix = prefix;
            this.localName = localName;
            this.rawName = rawName != null ? rawName : localName;
            this.uri = uri != null ? uri : "";
            this.type = type != null ? type : ATTR_TYPE_CDATA;
            this.value = value != null ? value : "";
        }

        @Override
        public String toString() {
            return rawName + "=\"" + value + "\"";
        }
    }

    private final ArrayList<Attr> attrs = new ArrayList<>();

    void addAttribute(String prefix, String localName, String value) {
        attrs.add(new Attr(prefix, localName, localName, null, ATTR_TYPE_CDATA, value));
    }

    void clear() {
        attrs.clear();
    }

    @Override
    public String toString() {
        return attrs.toString();
    }

    @Override
    public int getLength() {
        return attrs.size();
    }

    @Override
    public String getURI(int index) {
        if (index < 0 || index >= attrs.size()) return null;
        return attrs.get(index).uri;
    }

    @Override
    public String getLocalName(int index) {
        if (index < 0 || index >= attrs.size()) return null;
        return attrs.get(index).localName;
    }

    @Override
    public String getQName(int index) {
        if (index < 0 || index >= attrs.size()) return null;
        return attrs.get(index).rawName;
    }

    @Override
    public String getType(int index) {
        if (index < 0 || index >= attrs.size()) return null;
        return attrs.get(index).type;
    }

    @Override
    public String getValue(int index) {
        if (index < 0 || index >= attrs.size()) return null;
        return attrs.get(index).value;
    }

    @Override
    public int getIndex(String uri, String localName) {
        for (int i = 0; i < attrs.size(); i++) {
            Attr a = attrs.get(i);
            if (a.localName.equals(localName) && a.uri.equals(uri)) return i;
        }
        return -1;
    }

    @Override
    public int getIndex(String qName) {
        for (int i = 0; i < attrs.size(); i++) {
            if (attrs.get(i).rawName.equals(qName)) return i;
        }
        return -1;
    }

    @Override
    public String getType(String uri, String localName) {
        int i = getIndex(uri, localName);
        return i >= 0 ? attrs.get(i).type : null;
    }

    @Override
    public String getType(String qName) {
        int i = getIndex(qName);
        return i >= 0 ? attrs.get(i).type : null;
    }

    @Override
    public String getValue(String uri, String localName) {
        int i = getIndex(uri, localName);
        return i >= 0 ? attrs.get(i).value : null;
    }

    @Override
    public String getValue(String qName) {
        int i = getIndex(qName);
        return i >= 0 ? attrs.get(i).value : null;
    }
}

interface TokenHandler {
    void emitText(HtmlTokenizer tokenizer, HtmlLocation location, String text);

    void emitXmlDecl(HtmlTokenizer tokenizer, HtmlLocation location, String version, String encoding, String standalone);

    void emitDoctype(HtmlTokenizer tokenizer, HtmlLocation location, String doctypeName, String publicId, String systemId);

    void emitComment(HtmlTokenizer tokenizer, HtmlLocation location, String comment);

    void emitTag(HtmlTokenizer tokenizer, HtmlLocation location, TagToken tagToken, HtmlAttributesImpl attributes);

    TokenHandler getNextHandler();

}

class HtmlTokenizer {

    // Represent EOF
    private static final char CHAR_SUB = 0x1A;

    private InputSource inputSource;
    private HTMLErrorReporter errorReporter = new DefaultHTMLErrorHandler(null);

    public void setErrorReporter(HTMLErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    void reportError(HtmlLocation location, ParseError error, Object[] args) {
        if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
            HtmlStandardScanner.LOG_TOKENIZER.trace("ParseError:" + error
                + (args == null ? "" : (" " + Arrays.toString(args))));
        }
        errorReporter.reportError(location, error, args);
    }

    CharBuffer cbuf;

    private int pushedBack = CHAR_SUB;
    private int lastChar = CHAR_SUB;
    private StringBuilder characterBuilder = new StringBuilder();
    private HtmlAttributesImpl attributes = new HtmlAttributesImpl();
    private HtmlLocation location = new HtmlLocation();
    private HtmlLocation currentLocation = new HtmlLocation();

    private TagToken lastStartTagToken = null;
    TokenizeState tokenizeState = TokenizeState.Data;

    enum TokenizeState {
        Data,
        RcData,
        RawText,
        ScriptData,
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

    public Locator getLocator() {
        return location;
    }

    public void reset() {
        // RESET PARSING STATES
        attributes.clear();
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
        location.copyPositionTo(currentLocation);
        cbuf = CharBuffer.allocate(4096);
        cbuf.flip();

        location.setCharBuffer(cbuf);
        currentLocation.setCharBuffer(cbuf);

    }

    public void setInputSource(InputSource inputSource) {
        this.inputSource = inputSource;
        reset();
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

    private boolean isApplicableAttributeName(final String token) {
        if (token == null || token.length() == 0) {
            return false;
        }
        if (token.charAt(0) == '$' && token.length() > 2 && token.charAt(1) == '{' && token.charAt(token.length() - 1) == '}') {
            // 属性名としての変数参照は無視する
            return false;
        }
        return true;
        // return ATTR_NAME_PATTERN.matcher(token).matches();
    }

    private boolean isAppropriateEndTagToken(TagToken tagToken) {
        if (lastStartTagToken == null) {
            return false;
        }
        if (lastStartTagToken.nameBuilder.toString().equalsIgnoreCase(tagToken.nameBuilder.toString())) {
            return true;
        }
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
        if (!isApplicableAttributeName(name)) {
            return;
        }
        // add to current attribute list.
        attributes.addAttribute(prefix, name, value);
    }

    private void emitEof() throws ScanningInterruptedExeption {
        throw new ScanningInterruptedExeption();
    }

    private void emitDoctype(TokenHandler handler, Doctype doctype) {
        String doctypeName = doctype.nameBuilder == null ? null: doctype.nameBuilder.toString();
        String publicId = doctype.publicIdBuilder == null ? null: doctype.publicIdBuilder.toString();
        String systemId = doctype.systemIdBuilder == null ? null: doctype.systemIdBuilder.toString();

        if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
            HtmlStandardScanner.LOG_TOKENIZER.trace(String.format("%s: DOCTYPE %s %s %s", location.toString(), doctypeName, publicId, systemId));
        }
        handler.emitDoctype(null, location, doctypeName, publicId, systemId);
        currentLocation.copyPositionTo(location);
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
            if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
                HtmlStandardScanner.LOG_TOKENIZER.trace(String.format("%s: XML DECL <%s>", location.toString(), comment));
            }
            Map<String, String> map = extractAttributes(comment.substring(5));
            handler.emitXmlDecl(this, location, map.get("version"), map.get("encoding"), map.get("standalone"));
        } else {
            if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
                HtmlStandardScanner.LOG_TOKENIZER.trace(String.format("%s: COMMENT <!-- %s -->", location.toString(), comment));
            }
            handler.emitComment(this, location, comment);
        }
        currentLocation.copyPositionTo(location);
    }

    private void emitTag(TokenHandler handler, TagToken tagToken) {
        final String tagName = tagToken.nameBuilder.toString();
        if (tagToken.isSelfClosingTag) {
            if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
                HtmlStandardScanner.LOG_TOKENIZER.trace(String.format("%s: ELEM(EMPTY) %s %s", location.toString(), tagName, attributes));
            }
        } else if (tagToken.isEndTag) {
            if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
                HtmlStandardScanner.LOG_TOKENIZER.trace(String.format("%s: ELEM END /%s %s", location.toString(), tagName, attributes));
            }
        } else {
            if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
                HtmlStandardScanner.LOG_TOKENIZER.trace(String.format("%s: ELEM START %s %s", location.toString(), tagName, attributes));
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
                default:
                    break;
            }
            attributes.clear();
        }
        currentLocation.copyPositionTo(location);
    }

    private void emitSelfClosingTag(TokenHandler handler, TagToken tagToken) {
        tagToken.isSelfClosingTag = true;
        if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
            final String tagName = tagToken.nameBuilder.toString();
            HtmlStandardScanner.LOG_TOKENIZER.trace(String.format("%s: ELEM(EMPTY) %s %s", location.toString(), tagName, attributes));
        }

        if (tagToken.isEndTag) {
            lastStartTagToken = null;
        } else {
            lastStartTagToken = null;
        }
        handler.emitTag(this, location, tagToken, attributes);
        attributes.clear();
        currentLocation.copyPositionTo(location);
    }

    private void emitTextIfAvailable(TokenHandler handler) {
        if (characterBuilder.length() > 0) {
            if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
                String text = characterBuilder.toString();
                text = text.replace("\n", "\\n");
                text = text.replace("\t", "\\t");
                final int LIMIT_LANGTH = 40;
                if (text.length() > LIMIT_LANGTH) {
                    text = text.substring(0, LIMIT_LANGTH/2-1) + "..."
                         + text.substring(text.length() - LIMIT_LANGTH/2-2, text.length());
                }
                HtmlStandardScanner.LOG_TOKENIZER.trace(String.format("%s: TEXT \"%s\"", location, text));
            }
            handler.emitText(this, location, characterBuilder.toString()); 
            characterBuilder = new StringBuilder();
            currentLocation.copyPositionTo(location);
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


        TokenizeState lastTokenizeState = null;
        try {
            char c = 0xFFFF;
            do {
                if (HtmlStandardScanner.LOG_TOKENIZER.isTraceEnabled()) {
                    if (lastTokenizeState != tokenizeState) {                        
                        HtmlStandardScanner.LOG_TOKENIZER.trace("Tokenize state:" + lastTokenizeState + " -> " + tokenizeState);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
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
                            reportError(location, ParseError.UNEXPECTED_QUESTION_MARK_INSTEAD_OF_TAG_NAME, null);
                            commentBuilder = new StringBuilder();
                            commentBuilder.append(c);
                            mayXmlDeclAsBogusComment = true;
                            tokenizeState = TokenizeState.BogusComment;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_BEFORE_TAG_NAME, null);
                            appendTextNode('<');
                        } else {
                            reportError(location, ParseError.INVALID_FIRST_CHARACTER_OF_TAG_NAME, null);
                            tokenizeState = TokenizeState.Data;
                            appendTextNode('<');
                            pushBack();
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
                            reportError(location, ParseError.MISSING_END_TAG_NAME, null);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_BEFORE_TAG_NAME, null);
                            appendTextNode('<');
                            appendTextNode(0x002F);
                            emitEof();
                        } else {
                            reportError(location, ParseError.INVALID_FIRST_CHARACTER_OF_TAG_NAME, null);
                            tokenizeState = TokenizeState.BogusComment;
                            pushBack();
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            tagToken.nameBuilder.append((char) 0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_BEFORE_TAG_NAME, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_SCRIPT_HTML_COMMENT_LIKE_TEXT, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_SCRIPT_HTML_COMMENT_LIKE_TEXT, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            tokenizeState = TokenizeState.ScriptDataEscaped;
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_SCRIPT_HTML_COMMENT_LIKE_TEXT, null);
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
                                if ("script".equalsIgnoreCase(firstSixChars)) {
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_SCRIPT_HTML_COMMENT_LIKE_TEXT, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_SCRIPT_HTML_COMMENT_LIKE_TEXT, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            tokenizeState = TokenizeState.ScriptDataDoubleEscaped;
                            appendTextNode(0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_SCRIPT_HTML_COMMENT_LIKE_TEXT, null);
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
                                if ("script".equalsIgnoreCase(firstSixChars)) {
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
                        // https://html.spec.whatwg.org/multipage/parsing.html#before-attribute-name-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            // Ignore these.
                        } else if (c == '/' || c == '>' || c == CHAR_SUB) {
                            pushBack();
                            attrNameBuilder = new StringBuilder();
                            attrValueBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.AfterAttributeName;
                        } else if (c == '=') {
                            // https://html.spec.whatwg.org/multipage/parsing.html#parse-error-unexpected-equals-sign-before-attribute-name
                            reportError(location, ParseError.UNEXPECTED_EQUALS_SIGN_BEFORE_ATTRIBUTE_NAME, null);
                            attrNameBuilder = new StringBuilder();
                            attrValueBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.AttributeName;
                            attrNameBuilder.append("=");
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
                        } else if (c == 0) {
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            attrNameBuilder.append((char) 0xFFFD);
                        } else if (c == '"' || c == '\'' || c == '<') {
                            reportError(location, ParseError.UNEXPECTED_CHARACTER_IN_ATTRIBUTE_NAME, null);
                            attrNameBuilder.append((char) c);
                        } else {
                            attrNameBuilder.append(c);
                        }

                        if (tokenizeState != TokenizeState.AttributeName) {
                            // check duplication
                            if (containSameAttributeName(attrNameBuilder.toString())) {
                                reportError(location, ParseError.DUPLICATE_ATTRIBUTE, null);
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
                            reportError(location, ParseError.EOF_IN_TAG, null);
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
                            reportError(location, ParseError.MISSING_ATTRIBUTE_VALUE, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            attrValueBuilder.append((char) 0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_TAG, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            attrValueBuilder.append((char) 0xFFFD);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_TAG, null);
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
                            emitAttribute(null, attrNameBuilder.toString(), attrValueBuilder.toString());
                            tokenizeState = TokenizeState.Data;
                            emitTag(handler, tagToken);
                        } else if (c == 0) {
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            attrValueBuilder.append((char) 0xFFFD);
                        } else if (c == '"' || c == '\'' || c == '<' || c == '=' || c == '`') {
                            reportError(location, ParseError.UNEXPECTED_CHARACTER_IN_UNQUOTED_ATTRIBUTE_VALUE, null);
                            attrValueBuilder.append(c);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_TAG, null);
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
                            emitAttribute(null, attrNameBuilder.toString(), attrValueBuilder.toString());
                            tokenizeState = TokenizeState.Data;
                            emitTag(handler, tagToken);
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_TAG, null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            reportError(location, ParseError.MISSING_WHITESPACE_BETWEEN_ATTRIBUTES, null);
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
                            reportError(location, ParseError.EOF_IN_TAG, null);
                            emitEof();
                        } else {
                            reportError(location, ParseError.UNEXPECTED_SOLIDUS_IN_TAG, null);
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
                            reportError(location, ParseError.EOF_IN_COMMENT, null);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else if (c == 0) {
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
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
                            reportError(location, ParseError.CDATA_IN_HTML_CONTENT, null);
                            commentBuilder = new StringBuilder();
                            commentBuilder.append("[CDATA[");
                            tokenizeState = TokenizeState.BogusComment;
                        } else {
                            reportError(location, ParseError.INCORRECTLY_OPENED_COMMENT, null);
                            tokenizeState = TokenizeState.BogusComment;
                        }
                        break;
                    case CommentStart:
                        // https://html.spec.whatwg.org/multipage/parsing.html#comment-start-state
                        c = getChar();
                        if (c == '-') {
                            tokenizeState = TokenizeState.CommentStartDash;
                        } else if (c == '>') {
                            reportError(location, ParseError.ABRUPT_CLOSING_OF_EMPTY_COMMENT, null);
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
                            reportError(location, ParseError.ABRUPT_CLOSING_OF_EMPTY_COMMENT, null);
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            reportError(location, ParseError.EOF_IN_COMMENT, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            commentBuilder.append((char) 0xFFFD);
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            reportError(location, ParseError.EOF_IN_COMMENT, null);
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
                            reportError(location, ParseError.NESTED_COMMENT, null);
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
                            reportError(location, ParseError.EOF_IN_COMMENT, null);
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
                            reportError(location, ParseError.EOF_IN_COMMENT, null);
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
                            reportError(location, ParseError.INCORRECTLY_CLOSED_COMMENT, null);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            emitTextIfAvailable(handler);
                            emitComment(handler, commentBuilder.toString(), false);
                            reportError(location, ParseError.EOF_IN_COMMENT, null);
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
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitTextIfAvailable(handler);
                            emitEof();
                        } else {
                            reportError(location, ParseError.MISSING_WHITESPACE_BEFORE_DOCTYPE_NAME, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            doctype.nameBuilder.append((char) 0xFFFD);
                            tokenizeState = TokenizeState.DoctypeName;
                        } else if (c == '>' ) {
                            reportError(location, ParseError.MISSING_DOCTYPE_NAME, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            doctype.nameBuilder.append((char) 0xFFFD);
                        } else if (c == '>' ) {
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
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
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
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
                                reportError(location, ParseError.INVALID_CHARACTER_SEQUENCE_AFTER_DOCTYPE_NAME, null);
                                pushBack();
                                doctype.forceQuirkFlag = true;
                                tokenizeState = TokenizeState.BogusDoctype;
                            }
                        }
                        break;

                    case AfterDoctypePublicKeyword:
                        // https://html.spec.whatwg.org/multipage/parsing.html#after-doctype-public-keyword-state
                        c = getChar();
                        if (c == '\t'/*TAB*/ || c == '\n'/*LINEFEED*/ || c == 0x0C/*FORMFEED*/ || c == ' ') {
                            tokenizeState = TokenizeState.BeforeDoctypePublicIdentifier;
                        } else if (c == '"') {
                            reportError(location, ParseError.MISSING_WHITESPACE_AFTER_DOCTYPE_PUBLIC_KEYWORD, null);
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypePublicIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            reportError(location, ParseError.MISSING_WHITESPACE_AFTER_DOCTYPE_PUBLIC_KEYWORD, null);
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypePublicIdentifierSingleQuoted;
                        } else if (c == '>') {
                            reportError(location, ParseError.MISSING_DOCTYPE_PUBLIC_IDENTIFIER, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            reportError(location, ParseError.MISSING_QUOTE_BEFORE_DOCTYPE_PUBLIC_IDENTIFIER, null);
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
                            reportError(location, ParseError.MISSING_DOCTYPE_PUBLIC_IDENTIFIER, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            reportError(location, ParseError.MISSING_QUOTE_BEFORE_DOCTYPE_PUBLIC_IDENTIFIER, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            doctype.publicIdBuilder.append((char) 0xFFFD);
                        } else if (c == '>') {
                            reportError(location, ParseError.ABRUPT_DOCTYPE_PUBLIC_IDENTIFIER, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            doctype.publicIdBuilder.append((char) 0xFFFD);
                        } else if (c == '>') {
                            reportError(location, ParseError.ABRUPT_DOCTYPE_PUBLIC_IDENTIFIER, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
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
                            reportError(location, ParseError.MISSING_WHITESPACE_BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS, null);
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            reportError(location, ParseError.MISSING_WHITESPACE_BETWEEN_DOCTYPE_PUBLIC_AND_SYSTEM_IDENTIFIERS, null);
                            doctype.systemIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierSingleQuoted;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            reportError(location, ParseError.MISSING_QUOTE_BEFORE_DOCTYPE_SYSTEM_IDENTIFIER, null);
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
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            reportError(location, ParseError.MISSING_QUOTE_BEFORE_DOCTYPE_SYSTEM_IDENTIFIER, null);
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
                            reportError(location, ParseError.MISSING_WHITESPACE_AFTER_DOCTYPE_SYSTEM_KEYWORD, null);
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierDoubleQuoted;
                        } else if (c == '\'') {
                            reportError(location, ParseError.MISSING_WHITESPACE_AFTER_DOCTYPE_SYSTEM_KEYWORD, null);
                            doctype.publicIdBuilder = new StringBuilder();
                            tokenizeState = TokenizeState.DoctypeSystemIdentifierSingleQuoted;
                        } else if (c == '>') {
                            reportError(location, ParseError.MISSING_DOCTYPE_SYSTEM_IDENTIFIER, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            reportError(location, ParseError.MISSING_QUOTE_BEFORE_DOCTYPE_SYSTEM_IDENTIFIER, null);
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
                            reportError(location, ParseError.MISSING_DOCTYPE_SYSTEM_IDENTIFIER, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            reportError(location, ParseError.MISSING_QUOTE_BEFORE_DOCTYPE_SYSTEM_IDENTIFIER, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            doctype.systemIdBuilder.append((char) 0xFFFD);
                        } else if (c == '>') {
                            reportError(location, ParseError.ABRUPT_DOCTYPE_SYSTEM_IDENTIFIER, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
                            doctype.systemIdBuilder.append((char) 0xFFFD);
                        } else if (c == '>') {
                            reportError(location, ParseError.ABRUPT_DOCTYPE_SYSTEM_IDENTIFIER, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            tokenizeState = TokenizeState.Data;
                        } else if (c == CHAR_SUB) {
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
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
                            reportError(location, ParseError.EOF_IN_DOCTYPE, null);
                            doctype.forceQuirkFlag = true;
                            emitTextIfAvailable(handler);
                            emitDoctype(handler, doctype);
                            emitEof();
                        } else {
                            reportError(location, ParseError.UNEXPECTED_CHARACTER_AFTER_DOCTYPE_SYSTEM_IDENTIFIER, null);
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
                            reportError(location, ParseError.UNEXPECTED_NULL_CHARACTER, null);
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
                            reportError(location, ParseError.EOF_IN_CDATA, null);
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
            HtmlStandardScanner.LOG.error("Parser error " + e.getMessage(), e);
            return;
        }
    }

}