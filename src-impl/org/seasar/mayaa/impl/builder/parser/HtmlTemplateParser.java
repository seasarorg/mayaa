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

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.parsers.BasicParserConfiguration;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.xml.AdditionalSAXParser;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Mitsutaka Watanabe
 */
public class HtmlTemplateParser extends AdditionalSAXParser implements CONST_IMPL {
    static class ParserConfiguration extends BasicParserConfiguration {
        protected static final String ERROR_REPORTER = 
            Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

        HTMLScanner scanner = new HTMLScanner();

        ParserConfiguration() {
            addRecognizedProperties(new String[] { ERROR_REPORTER });
            XMLErrorReporter errorReporter = new XMLErrorReporter();
            setProperty(ERROR_REPORTER, errorReporter);
            addComponent(errorReporter);
        }

        @Override
        public void parse(XMLInputSource inputSource) throws XNIException, IOException {
            scanner.reset(this);
            XMLDocumentHandler documentHandler = getDocumentHandler();
            scanner.setDocumentHandler(documentHandler);
            scanner.setInputSource(inputSource);
            scanner.scanDocument(true);
        }
    }

    /**
     * @param balanceTag タグのバランスを修正するか。基本的にtrue。falseにする場合は必ずテンプレートのタグのバランスを取ること。
     */
    public HtmlTemplateParser() {
        super(new ParserConfiguration());
        // super(isHTML ? new TemplateParserConfiguration(): new StandardParserConfiguration());
        try {
            setFeature("http://xml.org/sax/features/namespaces", false);
            setFeature("http://xml.org/sax/features/validation", false);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            System.out.println(e);
        }

    }

    public void setInsertImpliedTag(boolean _insertImpliedTag) {
    }
}
