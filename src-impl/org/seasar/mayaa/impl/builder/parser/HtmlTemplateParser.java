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

import org.apache.xerces.parsers.BasicParserConfiguration;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.xml.AdditionalSAXParser;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Mitsutaka Watanabe
 */
public class HtmlTemplateParser extends AdditionalSAXParser implements CONST_IMPL {
    public static String FEATURE_DELETE_UNEXPECTED_ELEMENT = HtmlStandardScanner.FEATURE_DELETE_UNEXPECTED_ELEMENT;
    public static String FEATURE_INSERT_IMPLIED_ELEMENT = HtmlStandardScanner.FEATURE_INSERT_IMPLIED_ELEMENT;

    static class ParserConfiguration extends BasicParserConfiguration {

        HtmlStandardScanner scanner = new HtmlStandardScanner();

        ParserConfiguration() {
            addComponent(scanner);
        }

        @Override
        public void parse(XMLInputSource inputSource) throws IOException {
            try {
                scanner.reset(this);
                XMLDocumentHandler documentHandler = getDocumentHandler();
                scanner.setDocumentHandler(documentHandler);
                scanner.setInputSource(inputSource);
                scanner.scanDocument(true);    
            } catch (IOException | RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw e;
            } finally {
                scanner.setInputSource(null);
            }
        }
    }

    public HtmlTemplateParser() {
        super(new ParserConfiguration());
        // super(isHTML ? new TemplateParserConfiguration(): new StandardParserConfiguration());
        setFeature("http://xml.org/sax/features/namespaces", false);
        setFeature("http://xml.org/sax/features/validation", false);
    }

    public void setFeature(String featureId, boolean state) {
        try {
            super.setFeature(featureId, state);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
        }
    }
}
