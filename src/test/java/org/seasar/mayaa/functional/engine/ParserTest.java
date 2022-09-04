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

import org.junit.Test;
import org.seasar.mayaa.functional.EngineTestBase;

/**
 * HTMLパーサのテストを行う。
 * 2022年7月時点でHTML Living Standardの仕様を実装することを目指している。
 * なお、構文エラーについては下記の方針で修復するものとする。
 * 
 * @see https://html.spec.whatwg.org/multipage/
 * @author Mitsutaka WATANABE <mttk.wtnb@gmail.com>
 */
public class ParserTest extends EngineTestBase {

    @Test
    public void HTMLをパースして変更がない部分はそのまま出力する() throws IOException {
        final String DIR = "/it-case/parser/through/";
        execAndVerify(DIR + "target.html", DIR + "target.html", null);
    }

    @Test
    public void HTML自体は名前空間をサポートしないがMayaaでは名前空間を定義可能とする() throws IOException {
        enableDump();
        final String DIR = "/it-case/parser/namespace-in-html/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
        printPageTree();
    }

    @Test
    public void CDATA() throws IOException {
        enableDump();
        final String DIR = "/it-case/parser/cdata/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
        printPageTree();
    }

    @Test
    public void 引用符で囲まれない属性値は空白まで解釈する() throws IOException {
        final String DIR = "/it-case/parser/non-quotation-attr/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void 属性値が空文字列のときは属性名だけを出力する() throws IOException {
        final String DIR = "/it-case/parser/empty-attr/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    /**
     * HTMLとしてのパースであるためDOCTYPE宣言は大文字小文字を区別しない。
     * なお、XMLとして扱ってしまうと区別するためDOCTYPEは大文字でなければならない。
     */
    @Test
    public void doctypeの宣言が大文字小文字を区別しない() throws IOException {
        final String DIR = "/it-case/parser/case-insensitive-doctype/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }
}
