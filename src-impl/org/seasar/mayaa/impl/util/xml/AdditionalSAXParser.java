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
package org.seasar.mayaa.impl.util.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.seasar.mayaa.impl.builder.parser.AdditionalHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * JDK 標準 SAXParserFactory を使った XMLReader 実装。
 * Xerces の AbstractSAXParser を使わず、xmlDecl イベントは
 * 入力ストリーム先頭のスキャンで代替する。
 * xmlDecl は SAX startDocument の直後（_current 初期化済み）に発火する。
 *
 * @author Koji Suga (Gluegent, Inc.)
 */
public class AdditionalSAXParser implements XMLReader {

    /** バイト数ではなく文字数としての先読みバッファサイズ */
    private static final int SNIFF_CHARS = 200;

    private final XMLReader delegate;

    private ContentHandler contentHandler;
    private ErrorHandler   errorHandler;
    private EntityResolver entityResolver;
    private DTDHandler     dtdHandler;
    private AdditionalHandler additionalHandler;

    protected AdditionalSAXParser() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            delegate = factory.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    // ---- XMLReader: features -----------------------------------------------

    @Override
    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        return delegate.getFeature(name);
    }

    @Override
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        delegate.setFeature(name, value);
    }

    // ---- XMLReader: properties ---------------------------------------------

    @Override
    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (AdditionalHandler.ADDITIONAL_HANDLER.equals(name)) {
            return additionalHandler;
        }
        return delegate.getProperty(name);
    }

    @Override
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (AdditionalHandler.ADDITIONAL_HANDLER.equals(name)) {
            additionalHandler = (AdditionalHandler) value;
        } else {
            try {
                delegate.setProperty(name, value);
            } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
                // ignore Xerces-specific properties
            }
        }
    }

    // ---- XMLReader: handlers -----------------------------------------------

    @Override public ContentHandler getContentHandler() { return contentHandler; }
    @Override public void setContentHandler(ContentHandler h) { contentHandler = h; }

    @Override public ErrorHandler getErrorHandler() { return errorHandler; }
    @Override public void setErrorHandler(ErrorHandler h) { errorHandler = h; }

    @Override public EntityResolver getEntityResolver() { return entityResolver; }
    @Override public void setEntityResolver(EntityResolver r) { entityResolver = r; }

    @Override public DTDHandler getDTDHandler() { return dtdHandler; }
    @Override public void setDTDHandler(DTDHandler h) { dtdHandler = h; }

    // ---- XMLReader: parse --------------------------------------------------

    @Override
    public void parse(String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }

    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        XmlDeclInfo declInfo = null;
        if (additionalHandler != null) {
            declInfo = sniffXmlDecl(input);
        }

        ContentHandler ch = contentHandler;
        if (additionalHandler != null && ch != null && declInfo != null) {
            ch = new XmlDeclFireHandler(ch, additionalHandler, declInfo);
        }
        delegate.setContentHandler(ch);
        delegate.setErrorHandler(errorHandler);
        delegate.setEntityResolver(entityResolver);
        delegate.setDTDHandler(dtdHandler);
        delegate.parse(input);
    }

    // ---- XML declaration sniffing ------------------------------------------

    /**
     * 文字ストリームの先頭を mark/reset してXML宣言をスキャンする。
     * XML宣言が見つかれば {@link XmlDeclInfo} を返す。
     * ストリームは巻き戻されるため後続のSAXパースに影響しない。
     */
    private static XmlDeclInfo sniffXmlDecl(InputSource input) throws IOException {
        Reader reader = input.getCharacterStream();
        if (reader == null) {
            // バイトストリームの場合: Locator2 から取得できないため対象外
            return null;
        }

        if (!reader.markSupported()) {
            reader = new BufferedReader(reader);
            input.setCharacterStream(reader);
        }

        reader.mark(SNIFF_CHARS);
        char[] buf = new char[SNIFF_CHARS];
        int total = 0;
        while (total < SNIFF_CHARS) {
            int n = reader.read(buf, total, SNIFF_CHARS - total);
            if (n == -1) break;
            total += n;
        }
        reader.reset();

        String head = new String(buf, 0, total);

        // BOM等を除いた <?xml までの位置を探す（先頭3文字以内）
        int start = head.indexOf("<?xml");
        if (start < 0 || start > 3) {
            return null;
        }
        int end = head.indexOf("?>", start + 5);
        if (end < 0) {
            return null;
        }

        String attrs = head.substring(start + 5, end);
        return parseXmlDeclAttrs(attrs);
    }

    private static XmlDeclInfo parseXmlDeclAttrs(String attrs) {
        // version, encoding, standalone を順に取り出す
        Matcher m = Pattern.compile(
                "version\\s*=\\s*[\"']([^\"']*)[\"']").matcher(attrs);
        String version = m.find() ? m.group(1) : null;

        m = Pattern.compile(
                "encoding\\s*=\\s*[\"']([^\"']*)[\"']").matcher(attrs);
        String encoding = m.find() ? m.group(1) : null;

        m = Pattern.compile(
                "standalone\\s*=\\s*[\"']([^\"']*)[\"']").matcher(attrs);
        String standalone = m.find() ? m.group(1) : null;

        if (version == null && encoding == null) {
            return null;
        }
        return new XmlDeclInfo(version, encoding, standalone);
    }

    // ---- XmlDeclInfo -------------------------------------------------------

    private static final class XmlDeclInfo {
        final String version;
        final String encoding;
        final String standalone;

        XmlDeclInfo(String version, String encoding, String standalone) {
            this.version    = version;
            this.encoding   = encoding;
            this.standalone = standalone;
        }
    }

    // ---- ContentHandler wrapper to fire xmlDecl after startDocument --------

    /**
     * SAX startDocument の直後（_current 初期化後）に xmlDecl を発火する
     * ContentHandler ラッパー。
     */
    private static final class XmlDeclFireHandler implements ContentHandler {

        private final ContentHandler delegate;
        private final AdditionalHandler additionalHandler;
        private final XmlDeclInfo declInfo;

        XmlDeclFireHandler(ContentHandler delegate, AdditionalHandler additionalHandler, XmlDeclInfo declInfo) {
            this.delegate          = delegate;
            this.additionalHandler = additionalHandler;
            this.declInfo          = declInfo;
        }

        @Override
        public void startDocument() throws SAXException {
            delegate.startDocument();
            // xmlDecl は startDocument の直後に発火する（Xerces の動作に準じる）
            additionalHandler.xmlDecl(
                    declInfo.version    != null ? declInfo.version    : "1.0",
                    declInfo.encoding   != null ? declInfo.encoding   : "",
                    declInfo.standalone);
        }

        @Override public void setDocumentLocator(Locator locator) { delegate.setDocumentLocator(locator); }
        @Override public void endDocument() throws SAXException { delegate.endDocument(); }
        @Override public void startPrefixMapping(String prefix, String uri) throws SAXException { delegate.startPrefixMapping(prefix, uri); }
        @Override public void endPrefixMapping(String prefix) throws SAXException { delegate.endPrefixMapping(prefix); }
        @Override public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException { delegate.startElement(uri, localName, qName, atts); }
        @Override public void endElement(String uri, String localName, String qName) throws SAXException { delegate.endElement(uri, localName, qName); }
        @Override public void characters(char[] ch, int start, int length) throws SAXException { delegate.characters(ch, start, length); }
        @Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { delegate.ignorableWhitespace(ch, start, length); }
        @Override public void processingInstruction(String target, String data) throws SAXException { delegate.processingInstruction(target, data); }
        @Override public void skippedEntity(String name) throws SAXException { delegate.skippedEntity(name); }
    }
}

