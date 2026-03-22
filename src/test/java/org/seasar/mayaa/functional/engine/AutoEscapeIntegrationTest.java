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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
import org.seasar.mayaa.impl.source.DynamicRegisteredSourceHolder;

/**
 * 実運用に近いHTMLを使って自動エスケープの主要パターンを検証するIT。
 */
public class AutoEscapeIntegrationTest extends EngineTestBase {

    private static final String BASE = "/it-case/auto-escape/";

    void setUseNewParser(boolean useNewParser) {
        getServiceProvider().getTemplateBuilder().setParameter(
                TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));
    }

    void setAutoEscapeEnabled(boolean enabled) {
        getServiceProvider().getScriptEnvironment().setParameter(
                "autoEscapeEnabled", Boolean.toString(enabled));
    }

    void setEscapeDetectionLevel(String level) {
        getServiceProvider().getScriptEnvironment().setParameter(
                "escapeDetectionLevel", level);
    }

    @AfterEach
    void cleanup() {
        DynamicRegisteredSourceHolder.unregisterAll();
        DiagnosticEventBuffer.clear();
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void autoEscape有効時に主要コンテキストが安全に出力される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true);
        setEscapeDetectionLevel("normal");

        Map<String, Object> vars = createPageScope();

        String target = """
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                <meta charset="UTF-8">
                <title>News Portal</title>
                <style>
                .hero::before { content: "${promo}"; }
                </style>
                </head>
                <body>
                <article class="news-card">
                <h1>${title}</h1>
                <p class="summary">${summary}</p>
                <a class="cta" data-query="${query}" title="${tooltip}">検索</a>
                <script>
                window.profile = "${profile}";
                </script>
                <textarea>${bio}</textarea>
                <div class="preescaped">${alreadyEscaped}</div>
                </article>
                </body>
                </html>\
                """;

        String expected = """
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                <meta charset="UTF-8">
                <title>News Portal</title>
                <style>
                .hero::before { content: "新着\\"商品\\"\\00000A ライン"; }
                </style>
                </head>
                <body>
                <article class="news-card">
                <h1>&lt;緊急&gt; セール &amp; ニュース</h1>
                <p class="summary">本日限定 &lt;50%OFF&gt; &amp; 送料無料</p>
                <a data-query="&quot;tv&quot; &amp; game" title="おすすめ &#39;特価&#39; &quot;本日&quot;" class="cta">検索</a>
                <script>
                window.profile = "A\\"B\\'\\nC";
                </script>
                <textarea>I &lt;3 Mayaa &amp; JavaScript</textarea>
                <div class="preescaped">&lt;safe&gt;</div>
                </article>
                </body>
                </html>\
                """;

        registerAndVerify("enabled-realistic", target, expected, vars);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void autoEscape無効時にHTML本文SCRIPTSTYLEは未エスケープで出力される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);
        setEscapeDetectionLevel("normal");

        Map<String, Object> vars = createPageScope();

        String target = """
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                <meta charset="UTF-8">
                <title>News Portal</title>
                <style>
                .hero::before { content: "${promo}"; }
                </style>
                </head>
                <body>
                <article class="news-card">
                <h1>${title}</h1>
                <p class="summary">${summary}</p>
                <a class="cta" data-query="${query}" title="${tooltip}">検索</a>
                <script>
                window.profile = "${profile}";
                </script>
                <textarea>${bio}</textarea>
                <div class="preescaped">${alreadyEscaped}</div>
                </article>
                </body>
                </html>\
                """;

        String expected = """
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                <meta charset="UTF-8">
                <title>News Portal</title>
                <style>
                .hero::before { content: "新着"商品"
                ライン"; }
                </style>
                </head>
                <body>
                <article class="news-card">
                <h1><緊急> セール & ニュース</h1>
                <p class="summary">本日限定 <50%OFF> & 送料無料</p>
                <a data-query=""tv" & game" title="おすすめ '特価' "本日"" class="cta">検索</a>
                <script>
                window.profile = "A"B'
                C";
                </script>
                <textarea>I <3 Mayaa & JavaScript</textarea>
                <div class="preescaped">&lt;safe&gt;</div>
                </article>
                </body>
                </html>\
                """;

        registerAndVerify("disabled-realistic", target, expected, vars);

        List<DiagnosticEventBuffer.Event> events = DiagnosticEventBuffer.snapshot();
        assertTrue(events.size() >= 3,
                "autoEscape=false で差分が出る箇所の診断イベントが蓄積されること");
        for (DiagnosticEventBuffer.Event event : events) {
                        assertEquals(DiagnosticEventBuffer.Phase.RENDER, event.phase());
                        assertEquals("auto-escape", event.label());
                        assertTrue(event.positionLineNumber() >= DiagnosticEventBuffer.UNKNOWN_POSITION_LINE,
                                        "レンダリング時のイベントにはposition情報フィールドがあること");
        }
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void autoEscape有効でも既エスケープ値は二重エスケープしない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true);
        setEscapeDetectionLevel("normal");

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("safeHtml", "&lt;strong&gt;already escaped&lt;/strong&gt;");

        String target =
                "<!DOCTYPE html>\n"
                + "<html><head></head><body>\n"
                + "<div class=\"value\">${safeHtml}</div>\n"
                + "</body></html>";

        String expected =
                "<!DOCTYPE html>\n"
                + "<html><head></head><body>\n"
                + "<div class=\"value\">&lt;strong&gt;already escaped&lt;/strong&gt;</div>\n"
                + "</body></html>";

        registerAndVerify("already-escaped", target, expected, vars);

        List<DiagnosticEventBuffer.Event> events = DiagnosticEventBuffer.snapshot();
        assertEquals(1, events.size());
        assertEquals("auto-escape", events.get(0).label());
    }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void autoEscape無効時に既エスケープ値では警告を出さない(boolean useNewParser)
                        throws IOException {
                setUseNewParser(useNewParser);
                setAutoEscapeEnabled(false);
                setEscapeDetectionLevel("normal");

                Map<String, Object> vars = new LinkedHashMap<String, Object>();
                vars.put("safeHtml", "&lt;strong&gt;already escaped&lt;/strong&gt;");

                String target = "<!DOCTYPE html>\n"
                                + "<html><head></head><body>\n"
                                + "<div class=\"value\">${safeHtml}</div>\n"
                                + "</body></html>";

                String expected = "<!DOCTYPE html>\n"
                                + "<html><head></head><body>\n"
                                + "<div class=\"value\">&lt;strong&gt;already escaped&lt;/strong&gt;</div>\n"
                                + "</body></html>";

                registerAndVerify("disabled-already-escaped", target, expected, vars);

                List<DiagnosticEventBuffer.Event> events = DiagnosticEventBuffer.snapshot();
                assertEquals(0, events.size());
        }

            @ParameterizedTest(name = "useNewParser {0}")
            @ValueSource(booleans = {false, true})
            public void autoEscape無効時にHTML_ATTRIBUTEの既エスケープ値では警告を出さない(boolean useNewParser)
                    throws IOException {
                setUseNewParser(useNewParser);
                setAutoEscapeEnabled(false);
                setEscapeDetectionLevel("normal");

                Map<String, Object> vars = new LinkedHashMap<String, Object>();
                vars.put("safeAttr", "Tom &amp; Jerry");

                String target = "<!DOCTYPE html>\n"
                        + "<html><head></head><body>\n"
                        + "<a title=\"${safeAttr}\">link</a>\n"
                        + "</body></html>";

                String expected = "<!DOCTYPE html>\n"
                        + "<html><head></head><body>\n"
                        + "<a title=\"Tom &amp; Jerry\">link</a>\n"
                        + "</body></html>";

                registerAndVerify("disabled-already-escaped-attribute", target, expected, vars);

                assertEquals(0, DiagnosticEventBuffer.snapshot().size());
            }

            @ParameterizedTest(name = "useNewParser {0}")
            @ValueSource(booleans = {false, true})
            public void autoEscape無効時にSCRIPTの既エスケープ値では警告を出さない(boolean useNewParser)
                    throws IOException {
                setUseNewParser(useNewParser);
                setAutoEscapeEnabled(false);
                setEscapeDetectionLevel("normal");

                Map<String, Object> vars = new LinkedHashMap<String, Object>();
                vars.put("safeJs", "Tom &amp; Jerry");

                String target = "<!DOCTYPE html>\n"
                        + "<html><head></head><body>\n"
                        + "<script>window.profile = \"${safeJs}\";</script>\n"
                        + "</body></html>";

                String expected = "<!DOCTYPE html>\n"
                        + "<html><head></head><body>\n"
                        + "<script>window.profile = \"Tom &amp; Jerry\";</script>\n"
                        + "</body></html>";

                registerAndVerify("disabled-already-escaped-script", target, expected, vars);

                List<DiagnosticEventBuffer.Event> events = DiagnosticEventBuffer.snapshot();
                for (DiagnosticEventBuffer.Event event : events) {
                    assertTrue(event.scriptText() == null || event.scriptText().contains("safeJs") == false,
                            "safeJs に起因する警告は記録されないこと");
                }
            }

            @ParameterizedTest(name = "useNewParser {0}")
            @ValueSource(booleans = {false, true})
            public void autoEscape無効時にSTYLEの既エスケープ値では警告を出さない(boolean useNewParser)
                    throws IOException {
                setUseNewParser(useNewParser);
                setAutoEscapeEnabled(false);
                setEscapeDetectionLevel("normal");

                Map<String, Object> vars = new LinkedHashMap<String, Object>();
                vars.put("safeCss", "\\\"x\\\"\\00000A ");

                String target = "<!DOCTYPE html>\n"
                        + "<html><head><style>.hero::before { content: \"${safeCss}\"; }</style></head><body></body></html>";

                String expected = "<!DOCTYPE html>\n"
                        + "<html><head><style>.hero::before { content: \"\\\"x\\\"\\00000A \"; }</style></head><body></body></html>";

                registerAndVerify("disabled-already-escaped-style", target, expected, vars);

                assertEquals(0, DiagnosticEventBuffer.snapshot().size());
            }

            @ParameterizedTest(name = "useNewParser {0}")
            @ValueSource(booleans = {false, true})
            public void autoEscape無効時にTEXTAREA_PREの既エスケープ値では警告を出さない(boolean useNewParser)
                    throws IOException {
                setUseNewParser(useNewParser);
                setAutoEscapeEnabled(false);
                setEscapeDetectionLevel("normal");

                Map<String, Object> vars = new LinkedHashMap<String, Object>();
                vars.put("safeText", "&lt;strong&gt;already escaped&lt;/strong&gt;");

                String target = "<!DOCTYPE html>\n"
                        + "<html><head></head><body>\n"
                        + "<textarea>${safeText}</textarea>\n"
                        + "</body></html>";

                String expected = "<!DOCTYPE html>\n"
                        + "<html><head></head><body>\n"
                        + "<textarea>&lt;strong&gt;already escaped&lt;/strong&gt;</textarea>\n"
                        + "</body></html>";

                registerAndVerify("disabled-already-escaped-textarea", target, expected, vars);

                assertEquals(0, DiagnosticEventBuffer.snapshot().size());
            }

        @ParameterizedTest(name = "useNewParser {0}")
        @ValueSource(booleans = {false, true})
        public void RAW出力マーカーはautoEscape有効時でも生出力される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true);
        setEscapeDetectionLevel("normal");

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("trustedHtml", "<em>trusted</em>");

        String target = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div class="plain">${trustedHtml}</div>
                <div class="raw">${=trustedHtml}</div>
                </body></html>\
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div class="plain">&lt;em&gt;trusted&lt;/em&gt;</div>
                <div class="raw"><em>trusted</em></div>
                </body></html>\
                """;

        registerAndVerify("raw-marker", target, expected, vars);
    }

    private Map<String, Object> createPageScope() {
        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("title", "<緊急> セール & ニュース");
        vars.put("summary", "本日限定 <50%OFF> & 送料無料");
        vars.put("query", "\"tv\" & game");
        vars.put("tooltip", "おすすめ '特価' \"本日\"");
        vars.put("profile", "A\"B'\nC");
        vars.put("promo", "新着\"商品\"\nライン");
        vars.put("bio", "I <3 Mayaa & JavaScript");
        vars.put("alreadyEscaped", "&lt;safe&gt;");
        return vars;
    }

    private void registerAndVerify(String caseName,
            String target,
            String expected,
            Map<String, Object> pageScope) throws IOException {
        String casePath = BASE + caseName + "/";
        String targetPath = casePath + "target.html";
        String expectedPath = casePath + "expected.html";
        DynamicRegisteredSourceHolder.registerContents(targetPath, target);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);
        execAndVerify(targetPath, expectedPath, pageScope);
    }
}