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
