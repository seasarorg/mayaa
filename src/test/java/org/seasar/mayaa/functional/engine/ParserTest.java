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
package org.seasar.mayaa.functional.engine;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;

/**
 * HTMLパーサのテストを行う。
 * 2022年7月時点でHTML Living Standardの仕様を実装することを目指している。
 * なお、構文エラーについては下記の方針で修復するものとする。
 * 
 * @see https://html.spec.whatwg.org/multipage/
 * @author Mitsutaka WATANABE <mttk.wtnb@gmail.com>
 */
 public class ParserTest extends EngineTestBase {

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void HTMLをパースして変更がない部分はそのまま出力する(boolean useNewParser) throws IOException {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));

        final String DIR = "/it-case/parser/through/";
        if (useNewParser) {            
            execAndVerify(DIR + "target.html", DIR + "target.html", null);
        } else {
            getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.BALANCE_TAG, "false");
            execAndVerify(DIR + "target.html", DIR + "expected-old-parser.html", null);
            getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.BALANCE_TAG, "true");
        }
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void HTML自体は名前空間をサポートしないがMayaaでは名前空間を定義可能とする(boolean useNewParser) throws IOException {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));

        enableDump();
        final String DIR = "/it-case/parser/namespace-in-html/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }   

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void CDATA(boolean useNewParser) throws IOException {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));

        enableDump();
        final String DIR = "/it-case/parser/cdata/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void headが存在しない時に空のheadタグを付加しない(boolean useNewParser) throws IOException {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));

        if (!useNewParser) {
            /* <html>や<body>が無い場合もそのままにするNekoHTMLオプション。これが無いと勝手に付与されてしまう。 
             * http://cyberneko.org/html/features/balance-tags/document-fragment
            */
            getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.BALANCE_TAG, "true");
        }

        enableDump();
        final String DIR = "/it-case/parser/head-missing/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);

        if (!useNewParser) {
            getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.BALANCE_TAG, "false");
        }

    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void headが重複していても重複したheadタグを削除しない(boolean useNewParser) throws IOException {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));

        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.BALANCE_TAG, "false");

        enableDump();
        final String DIR = "/it-case/parser/head-duplicated/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);

        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.BALANCE_TAG, "true");
    }

    }

    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void 引用符で囲まれない属性値は空白まで解釈する(boolean useNewParser) throws IOException {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));

        final String DIR = "/it-case/parser/non-quotation-attr/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void 属性値が空文字列のときは属性名だけを出力する(boolean useNewParser) throws IOException {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));

        final String DIR = "/it-case/parser/empty-attr/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    /**
     * HTMLとしてのパースであるためDOCTYPE宣言は大文字小文字を区別しない。
     * なお、XMLとして扱ってしまうと区別するためDOCTYPEは大文字でなければならない。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void doctypeの宣言が大文字小文字を区別しない(boolean useNewParser) throws IOException {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));

        final String DIR = "/it-case/parser/case-insensitive-doctype/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }
}
