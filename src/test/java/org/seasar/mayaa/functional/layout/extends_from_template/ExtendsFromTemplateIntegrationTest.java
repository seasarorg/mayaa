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
package org.seasar.mayaa.functional.layout.extends_from_template;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;
import org.seasar.mayaa.impl.engine.EngineImpl;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.DynamicRegisteredSourceHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * HTML テンプレートのノードに書かれた {@code m:extends} 属性を元に
 * レイアウト共有を行うケースの統合テスト。
 *
 * <p>{@link HtmlExtendsTemplateBuilder} が {@code setupExtends} を
 * オーバーライドして HTML エレメントの {@code m:extends} 属性を読み取り、
 * ページの mayaaNode へ転写することでレイアウトを適用する。</p>
 *
 * <p>問題の背景：{@code RenderUtil.renderPage} 内で
 * {@code resolveSuperPageInfo(page)} が {@code getTemplate(...)} より先に
 * 呼ばれていたため、テンプレートビルド中に {@code setupExtends} が
 * mayaaNode へ設定した {@code m:extends} が初回リクエスト時に参照されなかった。</p>
 */
public class ExtendsFromTemplateIntegrationTest extends EngineTestBase {

    private static final String BASE = "/it-case/extends-from-template/";

    /**
     * HTML テンプレートのルートノードに {@code m:extends} を書いた場合に
     * 初回リクエストでレイアウトが適用されることを確認する。
     *
     * <p>{@code generateMayaaNode=true} のため対応する .mayaa ファイルが
     * 存在しなくても mayaaNode を自動生成し、HTML から読み取った {@code m:extends}
     * が初回リクエスト時に正しく参照されることを検証する。</p>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    void htmlノードのm_extends属性で初回リクエストでレイアウトが適用される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "html-extends-first-request";
        String casePath = BASE + caseName + "/";
        String layoutHtmlPath = casePath + "layout.html";
        String targetHtmlPath = casePath + "target.html";
        String expectedPath = casePath + "expected.html";

        String layoutHtml = "<html><head></head><body><p id=\"marker\">LAYOUT-CONTENT</p></body></html>";
        // ターゲット HTML に xmlns:m を宣言して m:extends でレイアウトを指定する
        String targetHtml = "<html xmlns:m=\"http://mayaa.seasar.org\" m:extends=\"" + layoutHtmlPath + "\">"
                + "<head></head><body><p id=\"content\">TARGET-CONTENT</p></body></html>";
        // レイアウトに m:insert がないのでターゲットコンテンツは出力されない
        String expected = "<html><head></head><body><p id=\"marker\">LAYOUT-CONTENT</p></body></html>";

        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<>());
    }

    /**
     * HTML テンプレートのルートノードに {@code m:extends} を書いた場合に
     * 2 回目のリクエストでもレイアウトが適用されることを確認する。
     * （初回後にテンプレートキャッシュが効いている状態）
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    void htmlノードのm_extends属性で2回目のリクエストでもレイアウトが適用される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "html-extends-second-request";
        String casePath = BASE + caseName + "/";
        String layoutHtmlPath = casePath + "layout.html";
        String targetHtmlPath = casePath + "target.html";
        String expectedPath = casePath + "expected.html";

        String layoutHtml = "<html><head></head><body><p id=\"marker\">LAYOUT-CONTENT</p></body></html>";
        String targetHtml = "<html xmlns:m=\"http://mayaa.seasar.org\" m:extends=\"" + layoutHtmlPath + "\">"
                + "<head></head><body><p id=\"content\">TARGET-CONTENT</p></body></html>";
        String expected = "<html><head></head><body><p id=\"marker\">LAYOUT-CONTENT</p></body></html>";

        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        // 1 回目
        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<>());
        // 2 回目（テンプレートがキャッシュされている状態）
        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<>());
    }

    /**
     * HTML の {@code m:extends} と .mayaa ファイルの {@code m:doRender} を組み合わせて、
     * コンテンツがレイアウトのスロットへ正しく挿入されることを確認する。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    void htmlのm_extendsとmayaaのm_doRenderでコンテンツをレイアウトへ挿入できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);
        enableDump();

        String caseName = "html-extends-with-dorender";
        String casePath = BASE + caseName + "/";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String expectedPath = casePath + "expected.html";

        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" name="contentBody" replace="false" />
                </m:mayaa>
                """;
        // ターゲット HTML ルートノードで m:extends によりレイアウトを指定
        String targetHtml = "<html xmlns:m=\"http://mayaa.seasar.org\" m:extends=\"" + layoutHtmlPath + "\">"
                + "<head></head><body><div id=\"content\">PAGE-CONTENT</div></body></html>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="content" name="contentBody" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">PAGE-CONTENT</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<>());
    }

    /**
     * レイアウトページ自身へのリクエストには {@code m:extends} が適用されず
     * そのままレンダリングされることを確認する。（自己参照防止）
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    void レイアウトページ自身へのリクエストにはレイアウトが適用されない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "html-extends-layout-self";
        String casePath = BASE + caseName + "/";
        String layoutHtmlPath = casePath + "layout.html";
        String targetHtmlPath = casePath + "target.html";
        String expectedPath = casePath + "expected.html";

        String layoutHtml = "<html><head></head><body><p id=\"marker\">LAYOUT-CONTENT</p></body></html>";
        String targetHtml = "<html xmlns:m=\"http://mayaa.seasar.org\" m:extends=\"" + layoutHtmlPath + "\">"
                + "<head></head><body><p id=\"content\">TARGET-CONTENT</p></body></html>";
        // レイアウトページ自身は m:extends が付かないのでそのままレンダリングされる
        String expected = "<html><head></head><body><p id=\"marker\">LAYOUT-CONTENT</p></body></html>";

        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(layoutHtmlPath, expectedPath, new LinkedHashMap<>());
    }

    void setUseNewParser(boolean useNewParser) {
        getServiceProvider().getTemplateBuilder().setParameter(
                TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));
    }

    void setAutoEscapeEnabled(boolean enabled) {
        getServiceProvider().getScriptEnvironment().setParameter(
                "autoEscapeEnabled", Boolean.toString(enabled));
    }

    /**
     * .mayaa ファイルが存在しないページで Caffeine の TTL エビクションにより
     * Page がキャッシュから消えた後も、2回目以降のリクエストでレイアウトが
     * 正しく適用されることを確認する。
     *
     * <p>デフォルトレイアウト情報（m:extends パス）は Template 自身が
     * {@code _dynamicSuperPagePath} フィールドで保持するため、Page がエビクション
     * されても Template がキャッシュに残っている限りレイアウトは維持される。
     * Template の再ビルドは不要。
     *
     * <p>再現手順：
     * <ol>
     *   <li>1回目リクエスト: Page と Template の両方をビルド・キャッシュ</li>
     *   <li>Page のみを deprecate（Caffeine TTL エビクション相当）</li>
     *   <li>2回目リクエスト: Page は再生成されるが Template はキャッシュのまま再利用。
     *       Template が持つ {@code _dynamicSuperPagePath} によりレイアウトが適用される。</li>
     * </ol>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    void Pageエビクション後も既存Templateのdynamic設定でレイアウトが適用される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "html-extends-page-eviction";
        String casePath = BASE + caseName + "/";
        String layoutHtmlPath = casePath + "layout.html";
        String targetHtmlPath = casePath + "target.html";
        String expectedPath = casePath + "expected.html";

        String layoutHtml = "<html><head></head><body><p id=\"marker\">LAYOUT-CONTENT</p></body></html>";
        String targetHtml = "<html xmlns:m=\"http://mayaa.seasar.org\" m:extends=\"" + layoutHtmlPath + "\">"
                + "<head></head><body><p id=\"content\">TARGET-CONTENT</p></body></html>";
        // レイアウトに m:insert がないのでターゲットコンテンツは出力されない
        String expected = "<html><head></head><body><p id=\"marker\">LAYOUT-CONTENT</p></body></html>";

        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        // 1回目: Page（target.mayaa）と Template（target.html）をビルド・キャッシュ
        MockHttpServletRequest request = createRequest(targetHtmlPath);
        MockHttpServletResponse response1 = exec(request, new LinkedHashMap<>());
        verifyResponse(response1, expectedPath, "1回目リクエスト");

        // Caffeine TTL エビクション相当: Pageのみを無効化（Template はキャッシュに残す）。
        // PageのsystemIDはリクエストされたパスから拡張子と ".mayaa" で構成。
        // /it-case/.../target.html → pageName = /it-case/.../target → pageSystemID = /it-case/.../target.mayaa
        String pageSystemID = targetHtmlPath.substring(0, targetHtmlPath.lastIndexOf('.')) + ".mayaa";
        ((EngineImpl) ProviderUtil.getEngine()).deprecateSpecification(pageSystemID, false);

        // 2回目: Page が再生成される。この際、関連 Template も deprecate されて
        // 再ビルドが起きることで mayaaNode が正しく付与され、レイアウトが適用されるはず。
        MockHttpServletRequest request2 = createRequest(targetHtmlPath);
        MockHttpServletResponse response2 = exec(request2, new LinkedHashMap<>());
        verifyResponse(response2, expectedPath, "Pageエビクション後の2回目リクエスト");
    }

    @AfterEach
    void cleanup() {
        DynamicRegisteredSourceHolder.unregisterAll();
    }
}

