/*
 * Copyright 2004-2026 the Seasar Foundation and the Others.
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
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
import org.seasar.mayaa.impl.source.DynamicRegisteredSourceHolder;

/**
 * Specification のライフサイクル(更新検知・再解決)を検証するIT。
 */
public class SpecificationLifeCycleIntegrationTest extends EngineTestBase {

    private static final String BASE = "/it-case/specification-life-cycle/";

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void レイアウトinsertで再入レンダリングするコンポーネントは次リクエストで更新を反映する(boolean useNewParser)
            throws IOException, InterruptedException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-insert-reentry-consistency";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String componentHtmlPath = casePath + "component.html";
        String componentMayaaPath = casePath + "component.mayaa";
        String expected1Path = casePath + "expected1.html";
        String expected2Path = casePath + "expected2.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/specification-life-cycle/layout-insert-reentry-consistency/layout.html">
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                <div id="compSlot">layout-comp-default</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" name="contentBody" replace="false" />
                    <m:insert id="compSlot" path="./component.html" replace="false" />
                </m:mayaa>
                """;
        String componentMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="componentRoot" replace="false" />
                </m:mayaa>
                """;
        String componentHtml1 = "<div id=\"componentRoot\">COMP-OLD</div>";
        String componentHtml2 = "<div id=\"componentRoot\">COMP-NEW</div>";
        String expected1 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <div id="compSlot"><div id="componentRoot">COMP-OLD</div></div>
                </body></html>
                """;
        String expected2 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <div id="compSlot"><div id="componentRoot">COMP-NEW</div></div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath, componentHtml1);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath, componentMayaa);
        DynamicRegisteredSourceHolder.registerContents(expected1Path, expected1);

        execAndVerify(targetHtmlPath, expected1Path, new LinkedHashMap<String, Object>());

        Thread.sleep(5L);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath, componentHtml2);
        DynamicRegisteredSourceHolder.registerContents(expected2Path, expected2);

        execAndVerify(targetHtmlPath, expected2Path, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void 共有レイアウトのHTML自体が更新された場合に次リクエストで変更を反映する(boolean useNewParser)
            throws IOException, InterruptedException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-html-update-detection";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expected1Path = casePath + "expected1.html";
        String expected2Path = casePath + "expected2.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/specification-life-cycle/layout-html-update-detection/layout.html">
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String layoutHtml1 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                <footer>FOOTER-OLD</footer>
                </body></html>
                """;
        String layoutHtml2 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                <footer>FOOTER-NEW</footer>
                </body></html>
                """;
        String expected1 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <footer>FOOTER-OLD</footer>
                </body></html>
                """;
        String expected2 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <footer>FOOTER-NEW</footer>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml1);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expected1Path, expected1);

        execAndVerify(targetHtmlPath, expected1Path, new LinkedHashMap<String, Object>());

        Thread.sleep(5L);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml2);
        DynamicRegisteredSourceHolder.registerContents(expected2Path, expected2);

        execAndVerify(targetHtmlPath, expected2Path, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void targetMayaaのextends先変更を次リクエストで反映する(boolean useNewParser)
            throws IOException, InterruptedException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "target-mayaa-extends-switch";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutAHtmlPath = casePath + "layout-a.html";
        String layoutAMayaaPath = casePath + "layout-a.mayaa";
        String layoutBHtmlPath = casePath + "layout-b.html";
        String layoutBMayaaPath = casePath + "layout-b.mayaa";
        String expected1Path = casePath + "expected1.html";
        String expected2Path = casePath + "expected2.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa1 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/specification-life-cycle/target-mayaa-extends-switch/layout-a.html">
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String targetMayaa2 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/specification-life-cycle/target-mayaa-extends-switch/layout-b.html">
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutAHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-a-default</div>
                <footer>LAYOUT-A</footer>
                </body></html>
                """;
        String layoutBHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-b-default</div>
                <footer>LAYOUT-B</footer>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String expected1 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <footer>LAYOUT-A</footer>
                </body></html>
                """;
        String expected2 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <footer>LAYOUT-B</footer>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa1);
        DynamicRegisteredSourceHolder.registerContents(layoutAHtmlPath, layoutAHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutAMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutBHtmlPath, layoutBHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutBMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expected1Path, expected1);

        execAndVerify(targetHtmlPath, expected1Path, new LinkedHashMap<String, Object>());

        Thread.sleep(5L);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa2);
        DynamicRegisteredSourceHolder.registerContents(expected2Path, expected2);

        execAndVerify(targetHtmlPath, expected2Path, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void layoutMayaaのinsert定義変更を次リクエストで反映する(boolean useNewParser)
            throws IOException, InterruptedException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-mayaa-update-detection";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expected1Path = casePath + "expected1.html";
        String expected2Path = casePath + "expected2.html";

        String targetHtml = "<div id=\"content\">PAGE-CONTENT</div><div id=\"alt\">PAGE-ALT</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/specification-life-cycle/layout-mayaa-update-detection/layout.html">
                    <m:doRender id="content" name="contentBody" />
                    <m:doRender id="alt" name="altBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        String layoutMayaa1 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String layoutMayaa2 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" name="altBody" replace="false" />
                </m:mayaa>
                """;
        String expected1 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE-CONTENT</div>
                </body></html>
                """;
        String expected2 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE-ALT</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa1);
        DynamicRegisteredSourceHolder.registerContents(expected1Path, expected1);

        execAndVerify(targetHtmlPath, expected1Path, new LinkedHashMap<String, Object>());

        Thread.sleep(5L);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa2);
        DynamicRegisteredSourceHolder.registerContents(expected2Path, expected2);

        execAndVerify(targetHtmlPath, expected2Path, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void componentMayaaのdoRender定義変更を次リクエストで反映する(boolean useNewParser)
            throws IOException, InterruptedException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "component-mayaa-update-detection";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String componentHtmlPath = casePath + "component.html";
        String componentMayaaPath = casePath + "component.mayaa";
        String expected1Path = casePath + "expected1.html";
        String expected2Path = casePath + "expected2.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/specification-life-cycle/component-mayaa-update-detection/layout.html">
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                <div id="compSlot">layout-comp-default</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" name="contentBody" replace="false" />
                    <m:insert id="compSlot" path="./component.html" replace="false" />
                </m:mayaa>
                """;
        String componentHtml = "<div id=\"root1\">ONE</div><div id=\"root2\">TWO</div>";
        String componentMayaa1 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="root1" replace="false" />
                </m:mayaa>
                """;
        String componentMayaa2 = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="root2" replace="false" />
                </m:mayaa>
                """;
        String expected1 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <div id="compSlot"><div id="root1">ONE</div></div>
                </body></html>
                """;
        String expected2 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <div id="compSlot"><div id="root2">TWO</div></div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath, componentHtml);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath, componentMayaa1);
        DynamicRegisteredSourceHolder.registerContents(expected1Path, expected1);

        execAndVerify(targetHtmlPath, expected1Path, new LinkedHashMap<String, Object>());

        Thread.sleep(5L);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath, componentMayaa2);
        DynamicRegisteredSourceHolder.registerContents(expected2Path, expected2);

        execAndVerify(targetHtmlPath, expected2Path, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void targetMayaaのextends有無の遷移を次リクエストで反映する(boolean useNewParser)
            throws IOException, InterruptedException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "target-mayaa-extends-toggle";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expected1Path = casePath + "expected1.html";
        String expected2Path = casePath + "expected2.html";
        String expected3Path = casePath + "expected3.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaaNoExtends = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                </m:mayaa>
                """;
        String targetMayaaWithExtends = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/specification-life-cycle/target-mayaa-extends-toggle/layout.html">
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                <footer>WITH-LAYOUT</footer>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String expected1 = "<div id=\"content\">PAGE</div>";
        String expected2 = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE</div>
                <footer>WITH-LAYOUT</footer>
                </body></html>
                """;
        String expected3 = "<div id=\"content\">PAGE</div>";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaaNoExtends);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expected1Path, expected1);

        execAndVerify(targetHtmlPath, expected1Path, new LinkedHashMap<String, Object>());

        Thread.sleep(5L);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaaWithExtends);
        DynamicRegisteredSourceHolder.registerContents(expected2Path, expected2);

        execAndVerify(targetHtmlPath, expected2Path, new LinkedHashMap<String, Object>());

        Thread.sleep(5L);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaaNoExtends);
        DynamicRegisteredSourceHolder.registerContents(expected3Path, expected3);

        execAndVerify(targetHtmlPath, expected3Path, new LinkedHashMap<String, Object>());
    }

    void setUseNewParser(boolean useNewParser) {
        getServiceProvider().getTemplateBuilder().setParameter(
                TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));
    }

    void setAutoEscapeEnabled(boolean enabled) {
        getServiceProvider().getScriptEnvironment().setParameter(
                "autoEscapeEnabled", Boolean.toString(enabled));
    }

    @AfterEach
    void cleanup() {
        DynamicRegisteredSourceHolder.unregisterAll();
        DiagnosticEventBuffer.clear();
    }
}
