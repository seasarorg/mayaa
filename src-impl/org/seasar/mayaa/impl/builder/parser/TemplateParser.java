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

import org.apache.xerces.parsers.AbstractSAXParser;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.HTMLScanner;
import org.seasar.mayaa.impl.CONST_IMPL;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateParser extends AbstractSAXParser implements CONST_IMPL {

	/**
	 * @param scanner
	 * @param templateDefaultCharset テンプレートの文字コードが不明な場合に使用する文字コード。
	 * @param balanceTag タグのバランスを修正するか。基本的にtrue。falseにする場合は必ずテンプレートのタグのバランスを取ること。
	 */
	public TemplateParser(HTMLScanner scanner, String templateDefaultCharset, boolean balanceTag) {
        super(new TemplateParserConfiguration(scanner, templateDefaultCharset, balanceTag));
    }

    private static class TemplateParserConfiguration extends HTMLConfiguration {

        /** Ignore outside content. copy from org.cyberneko.html.HTMLTagBalancer */
        protected static final String IGNORE_OUTSIDE_CONTENT =
            "http://cyberneko.org/html/features/balance-tags/ignore-outside-content";
        /** Document fragment balancing only. copy from org.cyberneko.html.HTMLTagBalancer */
        protected static final String DOCUMENT_FRAGMENT =
            "http://cyberneko.org/html/features/balance-tags/document-fragment";
        protected static final String BALANCE_TAGS =
            "http://cyberneko.org/html/features/balance-tags";

        public TemplateParserConfiguration(HTMLScanner scanner, String templateDefaultCharset, boolean balanceTag) {
            AdditionalHandlerFilter starter = new AdditionalHandlerFilter();
            addComponent(starter);
            setProperty(TemplateScanner.HTML_NAMES_ELEMS, "match");
            setProperty(TemplateScanner.HTML_NAMES_ATTRS, "no-change");
            /* テンプレート上にエンコーディング指定がなければUTF-8と見なす */
            setProperty(TemplateScanner.HTML_DEFAULT_ENCODING, templateDefaultCharset);
            setProperty(TemplateScanner.FILTERS, new XMLDocumentFilter[] { starter });
            /* 元のテンプレート内容を忠実に再現させるオプション。
             * ただし、</html>の後ろは無視される。false(デフォルト)の場合は、
             * </body>と</html>の後につづくものをnekoがむりやり前に持ってくる */
            setFeature(IGNORE_OUTSIDE_CONTENT, true);
            /* <html>や<body>が無い場合もそのままにするオプション。
             * これが無いと勝手に付与されてしまう。 */
            setFeature(DOCUMENT_FRAGMENT, true);
            /* HTMLの省略可能な閉じタグなどを自動的に付与するオプション。
             * これをfalseにするべきではないが、HTML5の場合にはaタグがblock要素になっているが
             * NekoHTMLはinlineとして見てしまうため意図しない動きをするため、HTMLのバランスを
             * 作成者側で保証することとしてfalseにする。 */
            setFeature(BALANCE_TAGS, balanceTag);
            fDocumentScanner = scanner;
            fDocumentScanner.reset(this);
        }

    }

}
