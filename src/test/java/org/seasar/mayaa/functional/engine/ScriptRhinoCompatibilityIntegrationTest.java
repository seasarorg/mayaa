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
import org.seasar.mayaa.impl.source.DynamicRegisteredSourceHolder;

/**
 * JSエンジン互換性（Rhino依存APIの可用性）を検証するIT。
 * 別のJSのスクリプトエンジンに切り替えた時に差分が出やすい観点を固定化する。
 */
public class ScriptRhinoCompatibilityIntegrationTest extends EngineTestBase {

    private static final String BASE = "/it-case/script-rhino-compatibility/";

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void javaとPackagesの両方でJava型参照できる(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "java-and-packages-global";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var a = java.lang.Integer.parseInt("7");
                        var b = Packages.java.lang.Integer.parseInt("8");
                        request.message = String(a + b);
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "15";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void importPackageは未提供である(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "import-package-function";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // 現行Mayaaでは importPackage はグローバル公開されない。
                        // 別のJSのスクリプトエンジンへ切り替える場合も、この可用性差分を
                        // 互換仕様として意識できるよう固定化する。
                        request.message = (typeof importPackage === "function") ? "provided" : "missing";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "missing";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void typeofで未解決識別子を安全に判定できる(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "typeof-unresolved-identifier";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        request.message = (typeof maybeMissing === "undefined") ? "safe" : "ng";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "safe";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void Intlオブジェクトは未提供である(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "intl-not-provided";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // Rhino は ECMAScript の Intl (国際化API) を提供しない。
                        // 別のJSのスクリプトエンジンに切り替えるとtypeofの結果が "object" に変わるため
                        // 現行仕様を固定化する。
                        request.message = (typeof Intl === "undefined") ? "no-intl" : "has-intl";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        // 現行Mayaa (Rhino): Intl 未提供 → "no-intl"
        // GraalJS など ES2015+ 準拠エンジンでは "has-intl" になる移行差分点
        String expected = "no-intl";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void 数値の文字列変換はロケール非依存である(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "number-to-string-locale-independent";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        // String() / toString() による数値→文字列変換はロケール非依存。
                        // JVMのデフォルトロケールが ja_JP でも小数点は "." であることを固定化する。
                        // (ロケール依存の toLocaleString() はエンジン実装依存のため対象外)
                        var n = 3.14;
                        request.message = String(n);
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "3.14";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
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
    }
}
