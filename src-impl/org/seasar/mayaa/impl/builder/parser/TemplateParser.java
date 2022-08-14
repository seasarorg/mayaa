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

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.xml.AdditionalSAXParser;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateParser extends AdditionalSAXParser implements CONST_IMPL {
    static class TemplateParserConfiguration extends StandardParserConfiguration {
        @Override
        protected XMLDocumentScanner createDocumentScanner() {
            HTMLScanner scanner = new HTMLScanner();
            return scanner;
        }

        @Override
        protected XMLEntityManager createEntityManager() {
            return new HTMLEntityManager();
        } // createEntityManager():XMLEntityManager
    }

    /**
     * @param balanceTag タグのバランスを修正するか。基本的にtrue。falseにする場合は必ずテンプレートのタグのバランスを取ること。
     */
    public TemplateParser(boolean balanceTag) {
        super(new TemplateParserConfiguration());
        // super(isHTML ? new TemplateParserConfiguration(): new StandardParserConfiguration());
        try {
            setFeature("http://xml.org/sax/features/namespaces", false);
            setFeature("http://xml.org/sax/features/validation", false);
            setFeature("http://apache.org/xml/features/validation/schema", false);
            setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            setFeature("http://xml.org/sax/features/use-entity-resolver2", false);

            setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            System.out.println(e);
        }

    }
}
