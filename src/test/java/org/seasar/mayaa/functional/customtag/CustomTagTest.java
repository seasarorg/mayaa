/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.functional.customtag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
import org.seasar.mayaa.impl.source.DynamicRegisteredSourceHolder;
import org.springframework.mock.web.MockHttpServletResponse;


public class CustomTagTest extends EngineTestBase {

    @Test
    public void dynamicAttribute() throws IOException {
        final String DIR = "/it-case/customtag/dynamic_attribute/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void dynamicAttributeWrong() throws IOException {
        final String DIR = "/it-case/customtag/dynamic_attribute_wrong/";
        try {
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
            fail();
        } catch (java.lang.IllegalArgumentException e) {
            assertEquals("org.seasar.mayaa.test.tag.DynamicAttributeNotSupportTag does not implements jakarta.servlet.jsp.tagext.DynamicAttributes.", e.getMessage());
        }
    }

    @Test
    public void emptybody() throws IOException {
        final String DIR = "/it-case/customtag/emptybody/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void eval_body_buffered() throws IOException {
        final String DIR = "/it-case/customtag/eval_body_buffered/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void replace_injection_attribute() throws IOException {
        final String DIR = "/it-case/customtag/replace_injection_attribute/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void rtexprtest() throws IOException {
        final String DIR = "/it-case/customtag/rtexprtest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void scopetest() throws IOException {
        final String DIR = "/it-case/customtag/scopetest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void simple_dynamic_test() throws IOException {
        final String DIR = "/it-case/customtag/simple_dynamic_test/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void simplebodytest_component() throws IOException {
        final String DIR = "/it-case/customtag/simplebodytest-component/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void simplebodytest() throws IOException {
        final String DIR = "/it-case/customtag/simplebodytest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @AfterEach
    public void cleanup() {
        DynamicRegisteredSourceHolder.unregisterAll();
    }

    // SimpleTag の基本動作: page スコープから取得した値と simpleName を出力する
    @Test
    public void simpletest_hello1_customSimpleTag() throws IOException {
        final String DIR = "/it-case/customtag/simpletest_h1/";

        String targetHtml = """
                <html><body>
                <span id="hello1" class="c&lt;l&amp;a&quot;s&gt;s" style="display:inline">dummy</span>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        xmlns:mt="http://mayaa.seasar.org/test/mayaa-test">
                    <m:beforeRender><![CDATA[
                        page['SimpleTestTag'] = "hello ";
                    ]]></m:beforeRender>
                    <m:echo m:id="hello1">
                        <mt:simpleTest simpleName="${ 'TestTag' }" />
                    </m:echo>
                </m:mayaa>
                """;

        DynamicRegisteredSourceHolder.registerContents(DIR + "target.html", targetHtml);
        DynamicRegisteredSourceHolder.registerContents(DIR + "target.mayaa", targetMayaa);

        MockHttpServletResponse response = exec(createRequest(DIR + "target.html"), null);
        assertTrue(response.getContentAsString().contains("hello TestTag"),
                "SimpleTestTag should render 'hello TestTag'");
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → 成功 (__parent__ は利用可能)
     * org.mozilla:rhino:1.9.1 → 失敗 __parent__ は Rhino 1.7R3〜R4 で廃止。
     *                           レンダリング時に例外が発生し DiagnosticEventBuffer にエラーが記録される。
     */
    @Test
    public void simpletest_hello2_pageParentDeprecated() throws IOException {
        final String DIR = "/it-case/customtag/simpletest_h2/";

        String targetHtml = """
                <html><body>
                <span id="hello2">dummy</span>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:echo m:id="hello2">
                        [<m:write value="${ page.__parent__['class']; }"/>]
                    </m:echo>
                </m:mayaa>
                """;

        DynamicRegisteredSourceHolder.registerContents(DIR + "target.html", targetHtml);
        DynamicRegisteredSourceHolder.registerContents(DIR + "target.mayaa", targetMayaa);

        try {
            exec(createRequest(DIR + "target.html"), null);
            fail("Expected RuntimeException due to deprecated __parent__ property");
        } catch (RuntimeException e) {
            // expected
        }

        List<DiagnosticEventBuffer.Event> events = capture.snapshot();
        assertTrue(
                events.stream().anyMatch(e -> e.level() == DiagnosticEventBuffer.Level.ERROR),
                "DiagnosticEventBuffer should record an error from deprecated page.__parent__");
    }

    /*
     * [エンジン別動作履歴]
     * rhino:js:1.7R2          → 成功 (__parent__ は利用可能)
     * org.mozilla:rhino:1.9.1 → 失敗 __parent__ は Rhino 1.7R3〜R4 で廃止。
     *                           レンダリング時に例外が発生し DiagnosticEventBuffer にエラーが記録される。
     */
    @Test
    public void simpletest_hello3_thisParentDeprecated() throws IOException {
        final String DIR = "/it-case/customtag/simpletest_h3/";

        String targetHtml = """
                <html><body>
                <span id="hello3">dummy</span>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:echo m:id="hello3">
                        <m:with>
                            [<m:write value="${ this.__parent__.__parent__['class']; }"/>]
                        </m:with>
                    </m:echo>
                </m:mayaa>
                """;

        DynamicRegisteredSourceHolder.registerContents(DIR + "target.html", targetHtml);
        DynamicRegisteredSourceHolder.registerContents(DIR + "target.mayaa", targetMayaa);

        try {
            exec(createRequest(DIR + "target.html"), null);
            fail("Expected RuntimeException due to deprecated __parent__ property");
        } catch (RuntimeException e) {
            // expected
        }

        List<DiagnosticEventBuffer.Event> events = capture.snapshot();
        assertTrue(
                events.stream().anyMatch(e -> e.level() == DiagnosticEventBuffer.Level.ERROR),
                "DiagnosticEventBuffer should record an error from deprecated this.__parent__");
    }

    // _ (WalkStandardScope) でカレントノードの class 属性を取得する
    @Test
    public void simpletest_hello4_scopeWalk() throws IOException {
        final String DIR = "/it-case/customtag/simpletest_h4/";

        String targetHtml = """
                <html><body>
                <span id="hello4" class="c&lt;l&amp;a&quot;s&gt;s" style="display:inline">dummy</span>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:echo m:id="hello4">
                        <m:with>
                            [<m:write value="${ _['class']; }"/>]
                        </m:with>
                    </m:echo>
                </m:mayaa>
                """;

        DynamicRegisteredSourceHolder.registerContents(DIR + "target.html", targetHtml);
        DynamicRegisteredSourceHolder.registerContents(DIR + "target.mayaa", targetMayaa);

        MockHttpServletResponse response = exec(createRequest(DIR + "target.html"), null);
        assertTrue(response.getContentAsString().contains("[c&lt;l&amp;a\"s&gt;s]"),
                "Scope walk _['class'] should return the span's class attribute value");
    }

    // SimpleTag が JSTL c:if の内側に配置されたとき、親タグとして IfTag を検出できる
    @Test
    public void simpletest_hello5_simpleTagInsideJstlIf() throws IOException {
        final String DIR = "/it-case/customtag/simpletest_h5/";

        String targetHtml = """
                <html><body>
                <span id="hello5"><span id="hello6">dummy</span></span>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        xmlns:mt="http://mayaa.seasar.org/test/mayaa-test"
                        xmlns:c="http://java.sun.com/jstl/core_rt">
                    <m:beforeRender><![CDATA[
                        page['SimpleTestTag'] = "hello ";
                    ]]></m:beforeRender>
                    <m:echo m:id="hello5">
                        <c:if test="${ true }"><m:doBody /></c:if>
                    </m:echo>
                    <mt:simpleTest m:id="hello6" simpleName="${ 'TestTag' }" />
                </m:mayaa>
                """;

        DynamicRegisteredSourceHolder.registerContents(DIR + "target.html", targetHtml);
        DynamicRegisteredSourceHolder.registerContents(DIR + "target.mayaa", targetMayaa);

        MockHttpServletResponse response = exec(createRequest(DIR + "target.html"), null);
        assertTrue(response.getContentAsString().contains("hello TestTag"),
                "SimpleTag inside JSTL c:if should render 'hello TestTag'");
        assertTrue(response.getContentAsString().contains("org.apache.taglibs.standard.tag.rt.core.IfTag"),
                "SimpleTag should detect IfTag as ancestor via parent chain");
    }

    @Test
    public void vinulltest() throws IOException {
        final String DIR = "/it-case/customtag/vinulltest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

}
