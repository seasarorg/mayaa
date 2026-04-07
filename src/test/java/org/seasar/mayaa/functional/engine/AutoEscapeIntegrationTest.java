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
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ページ単位でautoEscape有効_グローバルOFFページON(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false); // グローバルOFF

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("msg", "<b>bold</b> & 'quote'");

        String target = """
                <!-- m:autoEscape="true" -->
                <!DOCTYPE html>
                <html><head></head><body>
                <!-- non directive comment -->
                <div>${msg}</div>
                </body></html>
                """;

        String expected = """
                <!-- m:autoEscape="true" -->
                <!DOCTYPE html>
                <html><head></head><body>
                <!-- non directive comment -->
                <div>&lt;b&gt;bold&lt;/b&gt; &amp; 'quote'</div>
                </body></html>
                """;

        registerAndVerify("page-override-on", target, expected, vars);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ページ単位でautoEscape無効_グローバルONページOFF(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true); // グローバルON

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("msg", "<b>bold</b> & 'quote'");

        String target = """
                <!-- m:autoEscape="false" -->
                <!DOCTYPE html>
                <html><head></head><body>
                <!-- non directive comment -->
                <div>${msg}</div>
                </body></html>
                """;

        String expected = """
                <!-- m:autoEscape="false" -->
                <!DOCTYPE html>
                <html><head></head><body>
                <!-- non directive comment -->
                <div><b>bold</b> & 'quote'</div>
                </body></html>
                """;

        registerAndVerify("page-override-off", target, expected, vars);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ページ単位でautoEscape有効_引用符なし_グローバルOFFページON(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false); // グローバルOFF

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("msg", "<b>bold</b> & 'quote'");

        String target = """
                <!-- m:autoEscape=true -->
                <!DOCTYPE html>
                <html><head></head><body>
                <div>${msg}</div>
                </body></html>
                """;

        String expected = """
                <!-- m:autoEscape=true -->
                <!DOCTYPE html>
                <html><head></head><body>
                <div>&lt;b&gt;bold&lt;/b&gt; &amp; 'quote'</div>
                </body></html>
                """;

        registerAndVerify("page-override-on-unquoted", target, expected, vars);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void ページ単位でautoEscape無効_引用符なし_グローバルONページOFF(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true); // グローバルON

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("msg", "<b>bold</b> & 'quote'");

        String target = """
                <!-- m:autoEscape=false -->
                <!DOCTYPE html>
                <html><head></head><body>
                <div>${msg}</div>
                </body></html>
                """;

        String expected = """
                <!-- m:autoEscape=false -->
                <!DOCTYPE html>
                <html><head></head><body>
                <div><b>bold</b> & 'quote'</div>
                </body></html>
                """;

        registerAndVerify("page-override-off-unquoted", target, expected, vars);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
        public void 共有レイアウト利用時にページ単位autoEscape有効はページ描画部分へ適用される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false); // グローバルOFF

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("msg", "<b>bold</b> & 'quote'");

        String targetHtml = """
                <div>
                    <div id="content0"><!-- m:autoEscape="true" --><div class="page">1. ${msg}</div></div>
                    <div id="content1"><div class="page">2. ${msg}</div></div>
                    <div id="content2"><!-- m:autoEscape="false" --><div class="page">3. ${msg}</div></div>
                    <div id="content3"><div class="page">4. ${msg}</div></div>
                </div>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/auto-escape/layout-page-override-on/layout.html">
                    <m:doRender id="content0" name="contentBody0" />
                    <m:doRender id="content1" name="contentBody1" />
                    <m:doRender id="content2" name="contentBody2" />
                    <m:doRender id="content3" name="contentBody3" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot0">layout-default</div>
                <div id="slot1">layout-default</div>
                <div id="slot2">layout-default</div>
                <div id="slot3">layout-default</div>
                <div class="layout-tail">${msg}</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot0" name="contentBody0" replace="false" />
                    <m:insert id="slot1" name="contentBody1" replace="false" />
                    <m:insert id="slot2" name="contentBody2" replace="false" />
                    <m:insert id="slot3" name="contentBody3" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot0"><!-- m:autoEscape="true" --><div class="page">1. &lt;b&gt;bold&lt;/b&gt; &amp; 'quote'</div></div>
                <div id="slot1"><div class="page">2. <b>bold</b> & 'quote'</div></div>
                <div id="slot2"><!-- m:autoEscape="false" --><div class="page">3. <b>bold</b> & 'quote'</div></div>
                <div id="slot3"><div class="page">4. <b>bold</b> & 'quote'</div></div>
                <div class="layout-tail"><b>bold</b> & 'quote'</div>
                </body></html>
                """;

        registerLayoutAndVerify(
                "layout-page-override-on",
                targetHtml,
                targetMayaa,
                layoutHtml,
                layoutMayaa,
                expected,
                vars);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
        public void 共有レイアウト利用時にページ単位autoEscape無効はページ描画部分へ適用される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true); // グローバルON

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("msg", "<b>bold</b> & 'quote'");

        String targetHtml = """
                <div>
                    <div id="content0"><!-- m:autoEscape="true" --><div class="page">1. ${msg}</div></div>
                    <div id="content1"><div class="page">2. ${msg}</div></div>
                    <div id="content2"><!-- m:autoEscape="false" --><div class="page">3. ${msg}</div></div>
                    <div id="content3"><div class="page">4. ${msg}</div></div>
                </div>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="/it-case/auto-escape/layout-page-override-off/layout.html">
                    <m:doRender id="content0" name="contentBody0" />
                    <m:doRender id="content1" name="contentBody1" />
                    <m:doRender id="content2" name="contentBody2" />
                    <m:doRender id="content3" name="contentBody3" />
                </m:mayaa>
                """;
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot0">layout-default</div>
                <div id="slot1">layout-default</div>
                <div id="slot2">layout-default</div>
                <div id="slot3">layout-default</div>
                <div class="layout-tail">${msg}</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot0" name="contentBody0" replace="false" />
                    <m:insert id="slot1" name="contentBody1" replace="false" />
                    <m:insert id="slot2" name="contentBody2" replace="false" />
                    <m:insert id="slot3" name="contentBody3" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot0"><!-- m:autoEscape="true" --><div class="page">1. &lt;b&gt;bold&lt;/b&gt; &amp; 'quote'</div></div>
                <div id="slot1"><div class="page">2. &lt;b&gt;bold&lt;/b&gt; &amp; 'quote'</div></div>
                <div id="slot2"><!-- m:autoEscape="false" --><div class="page">3. <b>bold</b> & 'quote'</div></div>
                <div id="slot3"><div class="page">4. &lt;b&gt;bold&lt;/b&gt; &amp; 'quote'</div></div>
                <div class="layout-tail">&lt;b&gt;bold&lt;/b&gt; &amp; 'quote'</div>
                </body></html>
                """;

        registerLayoutAndVerify(
                "layout-page-override-off",
                targetHtml,
                targetMayaa,
                layoutHtml,
                layoutMayaa,
                expected,
                vars);
    }

    private static final String BASE = "/it-case/auto-escape/";

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

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void autoEscape有効時に主要コンテキストが安全に出力される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true);

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
    @ValueSource(booleans = { false, true })
    public void autoEscape無効時にHTML本文SCRIPTSTYLEは未エスケープで出力される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

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

        List<DiagnosticEventBuffer.Event> events = capture.snapshot();
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
    @ValueSource(booleans = { false, true })
    public void autoEscape有効でも既エスケープ値は二重エスケープしない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true);

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("safeHtml", "&lt;strong&gt;already escaped&lt;/strong&gt;");

        String target = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div class="value">${safeHtml}</div>
                </body></html>
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div class="value">&lt;strong&gt;already escaped&lt;/strong&gt;</div>
                </body></html>
                """;

        registerAndVerify("already-escaped", target, expected, vars);

        List<DiagnosticEventBuffer.Event> events = capture.snapshot();
        assertEquals(1, events.size());
        assertEquals("auto-escape", events.get(0).label());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void autoEscape無効時に既エスケープ値では警告を出さない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("safeHtml", "&lt;strong&gt;already escaped&lt;/strong&gt;");

        String target = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div class="value">${safeHtml}</div>
                </body></html>
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div class="value">&lt;strong&gt;already escaped&lt;/strong&gt;</div>
                </body></html>
                """;

        registerAndVerify("disabled-already-escaped", target, expected, vars);

        List<DiagnosticEventBuffer.Event> events = capture.snapshot();
        assertEquals(0, events.size());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void autoEscape無効時にHTML_ATTRIBUTEの既エスケープ値では警告を出さない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("safeAttr", "Tom &amp; Jerry");

        String target = """
                <!DOCTYPE html>
                <html><head></head><body>
                <a title="${safeAttr}">link</a>
                </body></html>
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <a title="Tom &amp; Jerry">link</a>
                </body></html>
                """;

        registerAndVerify("disabled-already-escaped-attribute", target, expected, vars);

        assertEquals(0, capture.size());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void autoEscape無効時にSCRIPTの既エスケープ値では警告を出さない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("safeJs", "Tom &amp; Jerry");

        String target = """
                <!DOCTYPE html>
                <html><head></head><body>
                <script>window.profile = "${safeJs}";</script>
                </body></html>
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <script>window.profile = "Tom &amp; Jerry";</script>
                </body></html>
                """;

        registerAndVerify("disabled-already-escaped-script", target, expected, vars);

        List<DiagnosticEventBuffer.Event> events = capture.snapshot();
        for (DiagnosticEventBuffer.Event event : events) {
            assertTrue(event.scriptText() == null || event.scriptText().contains("safeJs") == false,
                    "safeJs に起因する警告は記録されないこと");
        }
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void autoEscape無効時にSTYLEの既エスケープ値では警告を出さない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("safeCss", "\\\"x\\\"\\00000A ");

        String target = """
                <!DOCTYPE html>
                <html><head><style>.hero::before { content: "${safeCss}"; }</style></head><body></body></html>
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head><style>.hero::before { content: "\\"x\\"\\00000A "; }</style></head><body></body></html>
                """;

        registerAndVerify("disabled-already-escaped-style", target, expected, vars);

        assertEquals(0, capture.size());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void autoEscape無効時にTEXTAREA_PREの既エスケープ値では警告を出さない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("safeText", "&lt;strong&gt;already escaped&lt;/strong&gt;");

        String target = """
                <!DOCTYPE html>
                <html><head></head><body>
                <textarea>${safeText}</textarea>
                </body></html>
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <textarea>&lt;strong&gt;already escaped&lt;/strong&gt;</textarea>
                </body></html>
                """;

        registerAndVerify("disabled-already-escaped-textarea", target, expected, vars);

        assertEquals(0, capture.size());
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void RAW出力マーカーはautoEscape有効時でも生出力される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true);

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

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void MAYAA_SCOPEマクロはscriptコンテキストで_ドル括弧なしでもJSリテラル展開される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true);

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("name", "Tom \"The Cat\"");
        vars.put("title", "Mayaa");

        String target = """
                <!DOCTYPE html>
                <html><head></head><body>
                <script>
                const userName = MAYAA_SCOPE(name);
                const title = MAYAA_SCOPE_AS_STRING(title);
                if (userName === "Tom \\"The Cat\\"" && title === "Mayaa") {
                    document.getElementById("marker").textContent = "ok";
                }
                </script>
                <div id="marker">ok</div>
                </body></html>\
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <script>
                const userName = "Tom \\\"The Cat\\\"";
                const title = "Mayaa";
                if (userName === "Tom \\"The Cat\\"" && title === "Mayaa") {
                    document.getElementById("marker").textContent = "ok";
                }
                </script>
                <div id="marker">ok</div>
                </body></html>\
                """;

        registerAndVerify("scope-macro-script", target, expected, vars);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void MAYAA_SCOPE_WITH_STRINGIFYはscriptコンテキストで_ドル括弧なしでもJSON展開される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(true);

        Map<String, Object> profile = new LinkedHashMap<String, Object>();
        profile.put("name", "Alice");
        profile.put("count", Integer.valueOf(2));

        Map<String, Object> vars = new LinkedHashMap<String, Object>();
        vars.put("user", profile);

        String target = """
                <!DOCTYPE html>
                <html><head></head><body>
                <script>
                const payload = MAYAA_SCOPE_WITH_STRINGIFY(user);
                if (payload.name === "Alice" && payload.count === 2) {
                    document.getElementById("marker").textContent = "ok";
                }
                </script>
                </body></html>\
                """;

        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <script>
                const payload = {"name":"Alice","count":2};
                if (payload.name === "Alice" && payload.count === 2) {
                    document.getElementById("marker").textContent = "ok";
                }
                </script>
                </body></html>\
                """;

        registerAndVerify("scope-macro-stringify", target, expected, vars);
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

        private void registerLayoutAndVerify(String caseName,
                        String targetHtml,
                        String targetMayaa,
                        String layoutHtml,
                        String layoutMayaa,
                        String expected,
                        Map<String, Object> pageScope) throws IOException {
                String casePath = BASE + caseName + "/";
                String targetHtmlPath = casePath + "target.html";
                String targetMayaaPath = casePath + "target.mayaa";
                String layoutHtmlPath = casePath + "layout.html";
                String layoutMayaaPath = casePath + "layout.mayaa";
                String expectedPath = casePath + "expected.html";

                DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
                DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
                DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
                DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
                DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

                execAndVerify(targetHtmlPath, expectedPath, pageScope);
        }
}