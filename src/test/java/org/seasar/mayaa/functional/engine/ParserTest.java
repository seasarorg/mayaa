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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;
import org.seasar.mayaa.impl.source.DynamicRegisteredSourceHolder;

/**
 * HTMLパーサのテストを行う。
 * 2022年7月時点でHTML Living Standardの仕様を実装することを目指している。
 * 
 * @see https://html.spec.whatwg.org/multipage/
 * @author Mitsutaka WATANABE <mttk.wtnb@gmail.com>
 */
public class ParserTest extends EngineTestBase {

    void setBalanceTagOn() {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.BALANCE_TAG, "true");
    }

    @BeforeEach
    void setBalanceTagOff() {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.BALANCE_TAG, "false");
    }

    void setBalanceTag(boolean tagBalance) {
        if (tagBalance) {
            setBalanceTagOn();
        } else {
            setBalanceTagOff();
        }
    }

    @AfterEach
    void unregisterAllDynamicContents() {
        DynamicRegisteredSourceHolder.unregisterAll();
    }

    void setUseNewParser(boolean useNewParser) {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));
    }

    @Nested
    class StandardHtml {
        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void HTMLをパースして変更がない部分はそのまま出力する(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
    
            final String DIR = "/it-case/parser/through/";
            if (useNewParser) {            
                execAndVerify(DIR + "target.html", DIR + "target.html", null);
            } else {
                setBalanceTagOff();
                execAndVerify(DIR + "target.html", DIR + "expected-old-parser.html", null);
            }
        }
    
        /**
         * HTMLとしてのパースであるためDOCTYPE宣言は大文字小文字を区別しない。
         * なお、XMLとして扱ってしまうと区別するためDOCTYPEは大文字でなければならない。
         */
        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void doctypeの宣言が大文字小文字を区別しない(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);

            final String DIR = "/it-case/parser/case-insensitive-doctype/";
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void CDATA(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
    
            final String DIR = "/it-case/parser/cdata/";
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void 引用符で囲まれない属性値は空白まで解釈する(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
    
            final String DIR = "/it-case/parser/non-quotation-attr/";
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
        }
    
        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void 使用上省略可能な暗黙的なタグが補完される(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<title>A relatively minimal HTML document</title>\n" +
            "<p>Hello World!</p>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html><head><title>A relatively minimal HTML document</title>\n" +
            "</head><body><p>Hello World!</p></body></html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected-neko.html", 
            "<!DOCTYPE html>\n" +
            "<title>A relatively minimal HTML document</title>\n" +
            "<p>Hello World!</p>"
            );
    
            execAndVerify("/target.html", useNewParser ? "/expected.html": "/expected-neko.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void 属性値が空文字列のときは属性名だけを出力する(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
    
            final String DIR = "/it-case/parser/empty-attr/";
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
        }
    
        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void HTMLファイルで文字参照の状態がテンプレート記載のまま維持される(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<html><head></head>\n" +
            "<body><span id=\"message1\">&lt;&amp;amp;&gt;</span></body></html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html><head></head>\n" +
            "<body><span id=\"message1\">&lt;&amp;amp;&gt;</span></body></html>"
            );
    
            execAndVerify("/target.html", "/expected.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void XMLファイルでも文字参照の状態がテンプレート記載のまま維持される(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.xml", 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<html><head></head>\n" +
            "<body><span id=\"message1\">&lt;&amp;amp;&gt;</span></body></html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.xml", 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<html><head></head>\n" +
            "<body><span id=\"message1\">&lt;&amp;amp;&gt;</span></body></html>"
            );
    
            execAndVerify("/target.xml", "/expected.xml", null);
        }
        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void HTMLファイルにXML宣言がある(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<!DOCTYPE html>\n" +
            "<html><head></head>\n" +
            "<body><span id=\"message1\">&lt;&amp;amp;&gt;</span></body></html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<!DOCTYPE html>\n" +
            "<html><head></head>\n" +
            "<body><span id=\"message1\">&lt;&amp;amp;&gt;</span></body></html>"
            );
    
            execAndVerify("/target.html", "/expected.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void noscriptタグのパースができる(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<html><head>\n" + 
            "<noscript><meta keyword=\"キーワード\"></noscript>\n" +
            "</head>\n" +
            "<body>\n" + 
            "<noscript><!-- 外部ファイルにリンクするアンカー --><a href=\"https://example.com/\">外部リンク</a></noscript>\n" +
            "</body></html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html><head>\n" + 
            "<noscript><meta keyword=\"キーワード\"></noscript>\n" +
            "</head>\n" +
            "<body>\n" + 
            "<noscript><!-- 外部ファイルにリンクするアンカー --><a href=\"https://example.com/\">外部リンク</a></noscript>\n" +
            "</body></html>"
            );
    
            execAndVerify("/target.html", "/expected.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void plaintextタグのパースができる(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<html><head>\n" + 
            "</head>\n" +
            "<body>\n" + 
            "<plaintext>plaintextタグはHTML5以降廃止されている。<a href=\"https://developer.mozilla.org/ja/docs/Web/HTML/Element/plaintext\">リンク</a></plaintext>\n" +
            "</body></html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html><head>\n" + 
            "</head>\n" +
            "<body>\n" + 
            "<plaintext>plaintextタグはHTML5以降廃止されている。<a href=\"https://developer.mozilla.org/ja/docs/Web/HTML/Element/plaintext\">リンク</a></plaintext>\n" +
            "</body></html>"
            );
    
            execAndVerify("/target.html", "/expected.html", null);
        }
    }

    @Nested
    class HtmlFragment {
        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void HTMLフラグメントのパースができる(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<div>${var a='no body';}${a.toUpperCase()}</div>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<div>NO BODY</div>"
            );
    
            execAndVerify("/target.html", "/expected.html", null);
        }    

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void DOCTYPEがない時はフラグメント扱いで使用上省略可能な暗黙的なタグは補完されない(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<title>A relatively minimal HTML document</title>\n" +
            "<p>Hello World!</p>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<title>A relatively minimal HTML document</title>\n" +
            "<p>Hello World!</p>"
            );
    
            execAndVerify("/target.html", "/expected.html", null);
        }
    
    }
    @Nested
    class NonStandardHtml {
        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void HTML自体は名前空間をサポートしないがMayaaでは名前空間を定義可能とする(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
    
            final String DIR = "/it-case/parser/namespace-in-html/";
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
            printPageTree();
        }
    
        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void タグの大文字小文字は変換せず開始タグに合わせる(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<IF><div>${var a='no body';}${a.toUpperCase()}</DIV></if>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<IF><div>NO BODY</div></IF>"
            );
    
            execAndVerify("/target.html", "/expected.html", null);
        }
    
        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void headが存在しない時に空のheadタグを付加しない(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<html lang=\"ja\">\n" +
            "<body>\n" + 
            "            headが存在しない時にheadを付加しない。\n" +
            "</body>\n" +
            "</html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html lang=\"ja\">\n" +
            "<body>\n" + 
            "            headが存在しない時にheadを付加しない。\n" +
            "</body>\n" +
            "</html>"
            );
    
            execAndVerify("/target.html", "/expected.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void html以外のエレメントがパースできる(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<html lang=\"ja\">\n" +
            "<body>\n" + 
            "<a href=\"/\"><IF>AAA</IF></a>\n" +
            "</body>\n" +
            "</html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html lang=\"ja\">\n" +
            "<body>\n" + 
            "<a href=\"/\"><IF>AAA</IF></a>\n" +
            "</body>\n" +
            "</html>"
            );
            if (!useNewParser && tagBalance) {
                // NOTICE: NekoHTMLが閉じタグ補完する場合、閉じタグが不適切に補完される
                DynamicRegisteredSourceHolder.registerContents("/expected.html", 
                "<!DOCTYPE html>\n" +
                "<html lang=\"ja\">\n" +
                "<body>\n" + 
                "<a href=\"/\"></a><IF><a href=\"/\">AAA</a></IF>\n" +
                "</body>\n" +
                "</html>"
                );
            }
            execAndVerify("/target.html", "/expected.html", null);
        }

    }

    @Nested
    class NotValidHtml {
    
        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void 閉じ忘れのタグを補完する(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body><section><div><span>Hello</span>a</body> b\n" +
            "</html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body><section><div><span>Hello</span>a</div></section></body> b\n" +
            "</html>"
            );

            if (!useNewParser) {
                // NOTICE: NekoHTMLが閉じタグ補完をする過程で既存のbody閉じhtml閉じの間のテキストが移動される
                DynamicRegisteredSourceHolder.registerContents("/expected.html", 
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body><section><div><span>Hello</span>a b\n</div></section></body></html>"
                );
            }
            execAndVerify("/target.html", "/expected.html", null);

        }

        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void 閉じタグが多い場合は無視される(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.inc", 
            "<body><div>${var a='no body';}${a.toUpperCase()}</div></div>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.inc", 
            "<body><div>NO BODY</div></body>"
            );
    
            execAndVerify("/target.inc", "/expected.inc", null);
        }

        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void body内のheadは無視される(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.inc", 
            "<body><head><title>TITLE</title></head><div>${var a='no body';}${a.toUpperCase()}</div></div><body>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.inc", 
            "<body><head><title>TITLE</title></head><div>NO BODY</div></body>"
            );
    
            if (!useNewParser && !tagBalance) {
                // NOTICE: （既存の不可解挙動）NekoHTMLが閉じタグ補完しない場合、不必要なbodyタグが挿入され、全体が最初のエレメントで囲まれる。
                DynamicRegisteredSourceHolder.registerContents("/expected.inc", 
                "<body><head><title>TITLE</title></head><div>NO BODY</div><body></body></body>"
                );
            }

            execAndVerify("/target.inc", "/expected.inc", null);
        }

        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void 補完時もheadタグとbodyが親子関係にならない(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.inc", 
            "<head><body><div>${var a='no body';}${a.toUpperCase()}</div></div><body>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.inc", 
            "<head></head><body><div>NO BODY</div></body>"
            );
    
            if (!useNewParser && !tagBalance) {
                // NOTICE: （既存の不可解挙動）NekoHTMLが閉じタグ補完しない場合、不必要なbodyタグが挿入され、全体が最初のエレメントで囲まれる。
                DynamicRegisteredSourceHolder.registerContents("/expected.inc", 
                "<head><body><div>NO BODY</div><body></body></body></head>"
                );
            }

            execAndVerify("/target.inc", "/expected.inc", null);
        }

        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void headが重複していても重複したheadタグを削除しない(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>" +
            "<html lang=\"ja\">" +
            "<head>" +
            "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\">" +
            "</head>" +
            "<head>" + 
            "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\">" +
            "</head>" +
            "<body>" + 
            "    headが重複している時に2つ目のheadタグを削除する" +
            "</body>" +
            "</html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>" +
            "<html lang=\"ja\">" +
            "<head>" +
            "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\">" +
            "</head>" +
            "<head>" + 
            "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\">" +
            "</head>" +
            "<body>" + 
            "    headが重複している時に2つ目のheadタグを削除する" +
            "</body>" +
            "</html>"
            );

            if (!useNewParser && tagBalance) {
                // NOTICE: （既存の不可解挙動）NekoHTMLが閉じタグ補完する場合、metaタグがheadからはみ出してしまう
                DynamicRegisteredSourceHolder.registerContents("/expected.html", 
                "<!DOCTYPE html>" +
                "<html lang=\"ja\">" +
                "<head>" +
                "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\">" +
                "</head>" +
                "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf8\">" +
                "<body>" + 
                "    headが重複している時に2つ目のheadタグを削除する" +
                "</body>" +
                "</html>"
                );
            }
            execAndVerify("/target.html", "/expected.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void 不正な属性表記は除外される(boolean useNewParser) throws IOException {
            setUseNewParser(useNewParser);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<div ='val0'>属性名がない</div>\n"
            + "<div 1a='val1'>数字から始まる属性名</div>\n"
            + "<div _=6>記号で始まる(_)</div>\n"
            + "<div :b='1'>記号で始める(:名前空間に見えるが空の文字列)</div>\n"
            + "<div @click=\"handle(e)\">記号から始まる(@)</div>\n"
            + "<div c@lick=\"handle(e)\">記号を含む(@)</div>\n"
            + "<div a$b=1>記号を含む($)</div>\n"
            + "<div a-1-p='2'>ハイフンを含む</div>\n"
            + "<div aあ=\"1\">非ASCII</div>\n"
            + "<div c='${c}'>非nullの変数値の値を持つ属性</div>\n"
            + "<div d='${d}'>null値の変数を参照する属性</div>\n"
            + "<div ${c}>属性名は持たない変数参照</div>\n"
            + "<input type=\"checkbox\" checked selected xxx >\n"
            + "<div class='abc' x 1>値を持たない属性(暗黙的に空文字列の値を持つ)</div>\n"
            );
            DynamicRegisteredSourceHolder.registerContents("/target.mayaa", 
            "<m:mayaa xmlns:m=\"http://mayaa.seasar.org\"><m:beforeRender>var c = 'CCCC', d = null;</m:beforeRender></m:mayaa>"
            );


            DynamicRegisteredSourceHolder.registerContents("/expected-neko.html", 
            "<div>属性名がない</div>\n"
            + "<div 1a=\"val1\">数字から始まる属性名</div>\n"
            + "<div _=\"6\">記号で始まる(_)</div>\n"
            + "<div :b=\"1\">記号で始める(:名前空間に見えるが空の文字列)</div>\n"
            + "<div>記号から始まる(@)</div>\n"
            + "<div c>記号を含む(@)</div>\n"
            + "<div a>記号を含む($)</div>\n"
            + "<div a-1-p=\"2\">ハイフンを含む</div>\n"
            + "<div aあ=\"1\">非ASCII</div>\n"
            + "<div c=\"CCCC\">非nullの変数値の値を持つ属性</div>\n"
            + "<div d=\"\">null値の変数を参照する属性</div>\n"
            + "<div>属性名は持たない変数参照</div>\n"
            + "<input type=\"checkbox\" checked selected xxx>\n"
            + "<div class=\"abc\" x 1>値を持たない属性(暗黙的に空文字列の値を持つ)</div>\n"
            );

            // 新パーサでは英字に加えて一部の記号(_@:)から始まるものを許容する。
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<div ='val0'>属性名がない</div>\n"
            + "<div 1a=\"val1\">数字から始まる属性名</div>\n"
            + "<div _=\"6\">記号で始まる(_)</div>\n"
            + "<div :b=\"1\">記号で始める(:名前空間に見えるが空の文字列)</div>\n"
            + "<div @click=\"handle(e)\">記号から始まる(@)</div>\n"
            + "<div c@lick=\"handle(e)\">記号を含む(@)</div>\n"
            + "<div a$b=\"1\">記号を含む($)</div>\n"
            + "<div a-1-p=\"2\">ハイフンを含む</div>\n"
            + "<div aあ=\"1\">非ASCII</div>\n"
            + "<div c=\"CCCC\">非nullの変数値の値を持つ属性</div>\n"
            + "<div d=\"\">null値の変数を参照する属性</div>\n"
            + "<div>属性名は持たない変数参照</div>\n"
            + "<input type=\"checkbox\" checked selected xxx>\n"
            + "<div class=\"abc\" x 1>値を持たない属性(暗黙的に空文字列の値を持つ)</div>\n"
            );

            execAndVerify("/target.html", useNewParser ? "/expected.html": "/expected-neko.html", null);
        }

        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        public void エレメント名がコロンで終わったら名前空間Prefixをローカルネームとして扱う(boolean useNewParser, boolean tagBalance) throws IOException {
            enableDump();
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body><section: param=a><p>text</p></section:></body>\n" +
            "</html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body><section: param=\"a\"><p>text</p></section:></body>\n" +
            "</html>"
            );
            execAndVerify("/target.html", "/expected.html", null);
        }

        /**
         * エレメント名の先頭文字として許されない文字がある場合は次の開始タグまではテキストノードとして扱われるが
         * 閉じタグの先頭文字として許されない文字の場合はHTMLコメントとして出力する。ブラウザでの解釈も同じ。
         * ただし、NekoHtmlの場合は不正な閉じタグのHTMLコメント出力はしない。
         * 
         * @param useNewParser
         * @param tagBalance
         * @throws IOException
         * @see https://html.spec.whatwg.org/multipage/parsing.html#parse-error-invalid-first-character-of-tag-name
         */
        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        public void エレメント名がコロンで始まるときはエレメントとして評価されずテキストノードとして扱う_閉じタグはコメント化(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            enableDump();
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body><:div param=a><p>text</p></:div></body>\n" +
            "</html>"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected-neko.html", 
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body><:div param=a><p>text</p></body>\n" +
            "</html>"
            );
            // ウェブブラウザの解釈と同じ（エレメント名として不正な閉じタグはコメントとして付加される）
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body><:div param=a><p>text</p><!--:div--></body>\n" +
            "</html>"
            );
            execAndVerify("/target.html", useNewParser ? "/expected.html": "/expected-neko.html", null);

        }

        @CsvSource({"true, true", "true, false", "false, true", "false, false"})
        @ParameterizedTest(name = "useNewParser {0} / TagBalance {1}")
        public void エレメント名が不正な文字で始まるときはテキストノードとして扱う(boolean useNewParser, boolean tagBalance) throws IOException {
            setUseNewParser(useNewParser);
            setBalanceTag(tagBalance);
            enableDump();
            DynamicRegisteredSourceHolder.registerContents("/target.html", 
            "<42></_45>\n"
            );
            DynamicRegisteredSourceHolder.registerContents("/expected.html", 
            "<42><!--_45-->\n"
            );
            // nekoHTMLでタグバランスなしの場合は不正な形式の閉じタグはコメントとしても削除されない。
            DynamicRegisteredSourceHolder.registerContents("/expected-neko.html", 
            "<42>\n"
            );
            // nekoHTMLでタグバランスをした場合は開始タグも削除されてしまう。
            DynamicRegisteredSourceHolder.registerContents("/expected-neko-balanced.html", 
            "\n"
            );
            execAndVerify("/target.html", useNewParser ? "/expected.html": (tagBalance ? "/expected-neko-balanced.html": "/expected-neko.html"), null);

        }


    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void SSS(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        DynamicRegisteredSourceHolder.registerContents("/target.html", 
        "<!doctype html>\n" +
        "<html>" +
        "<DIV>${var a='no body';}${a.toUpperCase()}</DIV>" +
        "</html>"
        );
        DynamicRegisteredSourceHolder.registerContents("/expected.html", 
        "<!DOCTYPE html>\n" +
        "<html>" +
        "<head></head><body><DIV>NO BODY</DIV></body>" +
        "</html>"
        );
        DynamicRegisteredSourceHolder.registerContents("/expected-neko.html", 
        "<!DOCTYPE html>\n" +
        "<html>" +
        "<DIV>NO BODY</DIV>" +
        "</html>"
        );

        execAndVerify("/target.html", useNewParser ? "/expected.html": "/expected-neko.html", null);
    }
}
