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

import org.seasar.mayaa.impl.CONST_IMPL;
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
import org.xml.sax.ext.LexicalHandler;

/**
 * HTMLテンプレートのSAXパーサ実装。
 * Xerces BasicParserConfiguration / AbstractSAXParser を使わず、
 * HtmlStandardScanner を直接駆動する軽量 XMLReader 実装。
 *
 * @author Mitsutaka Watanabe
 */
public class HtmlTemplateParser implements XMLReader, CONST_IMPL {

    public static final String FEATURE_DELETE_UNEXPECTED_ELEMENT = HtmlStandardScanner.FEATURE_DELETE_UNEXPECTED_ELEMENT;
    public static final String FEATURE_INSERT_IMPLIED_ELEMENT    = HtmlStandardScanner.FEATURE_INSERT_IMPLIED_ELEMENT;

    private static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";

    private final HtmlStandardScanner scanner = new HtmlStandardScanner();

    private ContentHandler contentHandler;
    private ErrorHandler   errorHandler;
    private EntityResolver entityResolver;
    private DTDHandler     dtdHandler;
    private LexicalHandler lexicalHandler;
    private AdditionalHandler additionalHandler;

    // ---- XMLReader: features -----------------------------------------------

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        Boolean v = scanner.getFeatureDefault(name);
        return v != null ? v : false;
    }

    @Override
    public void setFeature(String name, boolean value) {
        scanner.setFeature(name, value);
    }

    // ---- XMLReader: properties ---------------------------------------------

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (LEXICAL_HANDLER_PROPERTY.equals(name)) {
            return lexicalHandler;
        }
        if (AdditionalHandler.ADDITIONAL_HANDLER.equals(name)) {
            return additionalHandler;
        }
        return null;
    }

    @Override
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (LEXICAL_HANDLER_PROPERTY.equals(name)) {
            lexicalHandler = (LexicalHandler) value;
        } else if (AdditionalHandler.ADDITIONAL_HANDLER.equals(name)) {
            additionalHandler = (AdditionalHandler) value;
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
        scanner.reset(errorHandler);
        scanner.setDocumentHandler(new SaxBridgeHandler());
        scanner.setInputSource(input);
        try {
            scanner.scanDocument(true);
        } finally {
            scanner.setInputSource(null);
        }
    }

    // ---- HtmlDocumentHandler bridge to SAX ContentHandler -----------------

    private class SaxBridgeHandler implements HtmlDocumentHandler {

        @Override
        public void startDocument(Locator locator, String encoding) throws IOException {
            if (contentHandler != null) {
                contentHandler.setDocumentLocator(locator);
                try {
                    contentHandler.startDocument();
                } catch (SAXException e) {
                    throw new IOException(e);
                }
            }
        }

        @Override
        public void xmlDecl(String version, String encoding, String standalone) {
            if (additionalHandler != null) {
                additionalHandler.xmlDecl(version, encoding, standalone);
            }
        }

        @Override
        public void doctypeDecl(String name, String publicId, String systemId) {
            if (lexicalHandler != null) {
                try {
                    lexicalHandler.startDTD(name, publicId, systemId);
                    lexicalHandler.endDTD();
                } catch (SAXException e) {
                    // ignore
                }
            }
        }

        @Override
        public void startElement(ElemName elemName, Attributes attributes) {
            if (contentHandler != null) {
                try {
                    contentHandler.startElement(
                            elemName.uri() != null ? elemName.uri() : "",
                            elemName.localName(),
                            elemName.rawName(),
                            attributes);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void endElement(ElemName elemName) {
            if (contentHandler != null) {
                try {
                    contentHandler.endElement(
                            elemName.uri() != null ? elemName.uri() : "",
                            elemName.localName(),
                            elemName.rawName());
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void emptyElement(ElemName elemName, Attributes attributes) {
            startElement(elemName, attributes);
            endElement(elemName);
        }

        @Override
        public void characters(String text) {
            if (contentHandler != null) {
                try {
                    char[] ch = text.toCharArray();
                    contentHandler.characters(ch, 0, ch.length);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void comment(String text) {
            if (lexicalHandler != null) {
                try {
                    char[] ch = text.toCharArray();
                    lexicalHandler.comment(ch, 0, ch.length);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
