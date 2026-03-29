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
 * ScriptエンジンのJava連携（パッケージ参照・Javaオブジェクト参照）を検証するIT。
 */
public class ScriptJavaInteropIntegrationTest extends EngineTestBase {

    private static final String BASE = "/it-case/script-java-interop/";

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void パッケージ参照でJava静的メソッドを呼び出せる(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "java-package-static-call";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var parsed = Packages.java.lang.Integer.parseInt("123");
                        request.message = "num=" + parsed;
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "num=123";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void Javaオブジェクトを生成してインスタンスメソッドを呼び出せる(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "java-object-instance-call";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var list = new Packages.java.util.ArrayList();
                        list.add("A");
                        list.add("B");
                        request.summary = list.get(0) + ":" + list.size();
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.summary}" />
                </m:mayaa>
                """;
        String expected = "A:2";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void requestオブジェクト参照でJavaメソッドを呼び出せる(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "java-request-object-reference";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<span id=\"msg\">dummy</span>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        request.setAttribute("seed", "x");
                        var v = request.getAttribute("seed");
                        request.message = v + "-ok";
                    ]]></m:beforeRender>
                    <m:write id="msg" value="${request.message}" />
                </m:mayaa>
                """;
        String expected = "x-ok";

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
        DiagnosticEventBuffer.clear();
    }
}
