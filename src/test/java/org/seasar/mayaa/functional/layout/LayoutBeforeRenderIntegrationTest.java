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
package org.seasar.mayaa.functional.layout;

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
 * レイアウト共有ページにおける beforeRender スクリプトの動作を検証するIT。
 */
public class LayoutBeforeRenderIntegrationTest extends EngineTestBase {

    private static final String BASE = "/it-case/layout-integration/";

    /**
     * レイアウトページの mayaa に定義した beforeRender スクリプトが
     * レンダリング前に実行され、スクリプト内で設定した変数が
     * レイアウトテンプレート内の m:write から参照できることを確認する。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void レイアウトページのbeforeRenderスクリプトが実行される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);
        enableDump();

        String caseName = "layout-before-render-basic";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/layout-integration/layout-before-render-basic/layout.html">
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <span id="message">dummy</span>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        request.layoutMessage = "from-layout-beforeRender";
                    ]]></m:beforeRender>
                    <m:write id="message" value="${request.layoutMessage}" />
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                from-layout-beforeRender
                <div id="slot">PAGE</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * ターゲットページの mayaa と レイアウトページの mayaa の両方に
     * beforeRender スクリプトが定義されている場合に、どちらも実行されることを確認する。
     * ターゲットの beforeRender で設定した変数はレイアウトの m:write から参照できる。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ターゲットとレイアウト両方のbeforeRenderが実行される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);
        enableDump();

        String caseName = "layout-and-target-before-render";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/layout-integration/layout-and-target-before-render/layout.html">
                    <m:beforeRender><![CDATA[
                        request.targetMessage = "from-target";
                    ]]></m:beforeRender>
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <span id="layoutMsg">dummy</span>
                <span id="targetMsg">dummy</span>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        request.layoutMessage = "from-layout";
                    ]]></m:beforeRender>
                    <m:write id="layoutMsg" value="${request.layoutMessage}" />
                    <m:write id="targetMsg" value="${request.targetMessage}" />
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                from-layout
                from-target
                <div id="slot">PAGE</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * target/layout の beforeRender 実行順序が target → layout であることを確認する。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void beforeRenderの実行順序がtarget先layout後である(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);
        enableDump();

        String caseName = "layout-before-render-order";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/layout-integration/layout-before-render-order/layout.html">
                    <m:beforeRender><![CDATA[
                        if (typeof request.renderOrder === 'undefined' || request.renderOrder == null) {
                            request.renderOrder = "";
                        }
                        request.renderOrder = request.renderOrder + "T";
                    ]]></m:beforeRender>
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <span id="order">dummy</span>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        if (typeof request.renderOrder === 'undefined' || request.renderOrder == null) {
                            request.renderOrder = "";
                        }
                        request.renderOrder = request.renderOrder + "L";
                    ]]></m:beforeRender>
                    <m:write id="order" value="${request.renderOrder}" />
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                TL
                <div id="slot">PAGE</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * target/layout が同じ request キーへ代入した場合、
     * 後から実行される layout 側の値で上書きされることを確認する。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void targetとlayoutで同名requestキーに代入した場合layoutが優先される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);
        enableDump();

        String caseName = "layout-before-render-request-shadow";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/layout-integration/layout-before-render-request-shadow/layout.html">
                    <m:beforeRender><![CDATA[
                        request.sharedMessage = "from-target";
                    ]]></m:beforeRender>
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <span id="message">dummy</span>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        request.sharedMessage = "from-layout";
                    ]]></m:beforeRender>
                    <m:write id="message" value="${request.sharedMessage}" />
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                from-layout
                <div id="slot">PAGE</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * request 以外のスコープ（target の var 変数）も、
     * レイアウトテンプレートから参照できることを確認する。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void targetのvar変数をlayoutから参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);
        enableDump();

        String caseName = "layout-before-render-non-request-scope";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"content\">PAGE</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/layout-integration/layout-before-render-non-request-scope/layout.html">
                    <m:beforeRender><![CDATA[
                        var localMessage = "from-target-local";
                    ]]></m:beforeRender>
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <span id="message">dummy</span>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:write id="message" value="${localMessage}" />
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                from-target-local
                <div id="slot">PAGE</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
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
