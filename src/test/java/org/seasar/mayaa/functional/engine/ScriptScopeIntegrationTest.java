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
 * m:insert によるページ間でのスクリプトスコープの引き継ぎ動作を検証するIT。
 *
 * <p>
 * スコープの仕組み：
 * </p>
 * <ul>
 * <li>beforeRender はレンダリング前に {@code startScope} でスコープを積み、
 * Rhino の PageAttributeScope プロトタイプチェーンに変数を登録する。</li>
 * <li>各 renderTemplateProcessor 呼び出しでも {@code startScope/endScope} が対になって呼ばれ、
 * これは常に現在のスコープの子スコープとして作成されるため、祖先スコープへのアクセスは
 * プロトタイプチェーン経由で維持される。</li>
 * <li>insertProcessor が RenderUtil.renderPage を呼ぶ場合も同様に startScope を積むため、
 * 呼び出し元の beforeRender 変数はコンポーネント内から参照できる（スコープ継承）。</li>
 * <li>兄弟コンポーネントは共通祖先を共有するが、互いに子の関係になく独立したブランチに属するため、
 * 一方のコンポーネントで宣言した変数は他方からは参照できない（スコープ分離）。</li>
 * </ul>
 *
 * <p>
 * コンポーネントのレンダリング構造：
 * </p>
 * <ul>
 * <li>コンポーネント HTML は {@code m:doRender id="root"} (replace=true デフォルト) を含む。
 * replace=true の場合、ComponentRenderer はルート要素のタグを出力せず、
 * その子プロセッサのみをレンダリングする。</li>
 * <li>m:insert id="slot" replace="false" により、スロット要素のタグは保持され、
 * コンポーネントの出力がその内部に挿入される。</li>
 * </ul>
 */
public class ScriptScopeIntegrationTest extends EngineTestBase {

    private static final String BASE = "/it-case/script-scope/";

    /**
     * ターゲットページの beforeRender で宣言した変数が、
     * m:insert path で挿入されたコンポーネント内の m:write から参照できることを確認する。
     *
     * <p>
     * スコープチェーン（期待動作）:
     * </p>
     * 
     * <pre>
     *   global
     *     → beforeRenderScope (scriptVar ここに存在)
     *       → 各 renderTemplateProcessor が積む子スコープ
     *         → component の renderPage で積む scope
     *           → component の各 renderTemplateProcessor が積む子スコープ
     *             ※ m:write が ${scriptVar} を評価する際、プロトタイプチェーンを遡って
     *                beforeRenderScope の scriptVar を参照できる
     * </pre>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void beforeRenderで宣言した変数がm_insertコンポーネント内で参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-propagate-to-insert";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String componentHtmlPath = casePath + "component.html";
        String componentMayaaPath = casePath + "component.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">slot-default</div>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var scriptVar = "from-beforeRender";
                    ]]></m:beforeRender>
                    <m:insert id="slot" path="./component.html" replace="false" />
                </m:mayaa>
                """;
        // m:doRender id="root" (replace=true デフォルト):
        // - ComponentRenderer が insertRoot として DoRenderProcessor を使用
        // - root 要素タグは出力されず、root の子要素のみがレンダリングされる
        // m:write id="val" value="${scriptVar}":
        // - val span 要素をスコープチェーンから取得した scriptVar の値で置き換える
        String componentHtml = "<html><body><div id=\"root\"><span id=\"val\">dummy</span></div></body></html>";
        String componentMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="root" />
                    <m:write id="val" value="${scriptVar}" />
                </m:mayaa>
                """;
        // beforeRender のスコープが m:insert コンポーネントに引き継がれるなら
        // SlotDiv 内に "from-beforeRender" が出力される。
        // スコープが引き継がれていない（バグ）場合は val span の出力が空になり、
        // SlotDiv の内部も空になる。
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">from-beforeRender</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath, componentHtml);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath, componentMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * ターゲットページの beforeRender で宣言した変数が、
     * 複数の兄弟 m:insert コンポーネントからそれぞれ参照できることを確認する。
     *
     * <p>
     * 兄弟コンポーネントは互いに独立したスコープブランチを持つが、
     * 共通祖先スコープ（beforeRender で積まれたスコープ）の変数は
     * プロトタイプチェーンをたどって両方から参照できる。
     * </p>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void beforeRenderで宣言した変数が複数の兄弟m_insertコンポーネントからも参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-propagate-to-sibling-inserts";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String componentHtmlPath = casePath + "component.html";
        String componentMayaaPath = casePath + "component.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot1">slot1-default</div>
                <div id="slot2">slot2-default</div>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var sharedVar = "shared-value";
                    ]]></m:beforeRender>
                    <m:insert id="slot1" path="./component.html" replace="false" />
                    <m:insert id="slot2" path="./component.html" replace="false" />
                </m:mayaa>
                """;
        String componentHtml = "<html><body><div id=\"root\"><span id=\"val\">dummy</span></div></body></html>";
        String componentMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="root" />
                    <m:write id="val" value="${sharedVar}" />
                </m:mayaa>
                """;
        // 兄弟の両コンポーネントがそれぞれ beforeRender の sharedVar を参照できる
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot1">shared-value</div>
                <div id="slot2">shared-value</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath, componentHtml);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath, componentMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * m:insert コンポーネント A の beforeRender で宣言した変数は、
     * 同一ターゲットページに配置されたコンポーネント B からは参照できないことを確認する。
     *
     * <p>
     * コンポーネント A・B はそれぞれ独立した renderPage スコープブランチを持ち、
     * A → B の順でレンダリングされる場合、A の endScope によって A のスコープは破棄される。
     * B のレンダリング開始時、スコープチェーンのヒントは A の beforeRender 前の状態に戻っており、
     * B の scope chain に compAVar は存在しない。
     * </p>
     *
     * <p>
     * JavaScript の {@code typeof} 演算子は未宣言変数に対して例外を投げず
     * {@code "undefined"} 文字列を返すため、表明として安全に使用できる。
     * </p>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void コンポーネントAのbeforeRenderで宣言した変数はコンポーネントBから参照できない(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-isolation-between-sibling-inserts";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String compAHtmlPath = casePath + "comp-a.html";
        String compAMayaaPath = casePath + "comp-a.mayaa";
        String compBHtmlPath = casePath + "comp-b.html";
        String compBMayaaPath = casePath + "comp-b.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slotA">slotA-default</div>
                <div id="slotB">slotB-default</div>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slotA" path="./comp-a.html" replace="false" />
                    <m:insert id="slotB" path="./comp-b.html" replace="false" />
                </m:mayaa>
                """;
        // comp-a: beforeRender で privateVar を宣言して出力する
        String compAHtml = "<html><body><div id=\"rootA\"><span id=\"val\">dummy</span></div></body></html>";
        String compAMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var privateVar = "secret";
                    ]]></m:beforeRender>
                    <m:doRender id="rootA" />
                    <m:write id="val" value="${privateVar}" />
                </m:mayaa>
                """;
        // comp-b: comp-a の privateVar を参照しようとする。
        // スコープが分離されているなら typeof privateVar は "undefined" を返し、
        // 三項演算子によって "invisible" が出力される。
        // スコープが誤って共有されている(バグ)なら "secret" が出力される。
        String compBHtml = "<html><body><div id=\"rootB\"><span id=\"val\">dummy</span></div></body></html>";
        String compBMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="rootB" />
                    <m:write id="val" value="${typeof privateVar !== 'undefined' ? privateVar : 'invisible'}" />
                </m:mayaa>
                """;
        // comp-a は "secret"、comp-b はスコープ分離により "invisible" を出力する
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slotA">secret</div>
                <div id="slotB">invisible</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(compAHtmlPath, compAHtml);
        DynamicRegisteredSourceHolder.registerContents(compAMayaaPath, compAMayaa);
        DynamicRegisteredSourceHolder.registerContents(compBHtmlPath, compBHtml);
        DynamicRegisteredSourceHolder.registerContents(compBMayaaPath, compBMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // ===================================================================
    // m:extends レイアウト共有でのスコープ継承テスト
    // ===================================================================

    /**
     * m:extends によるレイアウト共有において、
     * レイアウトページの beforeRender で宣言した変数が
     * コンテンツ（ターゲット）テンプレート内から参照できることを確認する。
     *
     * <p>
     * RenderUtil.renderPage の do-while ループは target → layout の順に実行し、
     * 各ページの beforeRender で startScope を呼び出す。
     * </p>
     * 
     * <pre>
     *   ループ1 (target)   : startScope → S1 (targetVar がない場合はスコープのみ)
     *   ループ2 (layout)   : startScope → S2 (layoutVar 登録), S2.parentScope = S1
     *   レンダリング時カレント = S2
     * </pre>
     * <p>
     * layout→target の順でテンプレートがスタックされ（LIFO）、layoutTemplate が先にレンダリングされる。
     * layout の m:insert name="contentArea"（パスなし）は fireEvent=false で renderPage
     * を呼ぶため
     * startScope は追加されず、target テンプレートのプロセッサも S2 スコープで実行される。
     * S2 に layoutVar が存在するため参照できる。
     * </p>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void m_extendsレイアウトのbeforeRender変数がコンテンツテンプレートから参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-extends-layout-var-to-content";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expectedPath = casePath + "expected.html";

        // ターゲットHTML: コンテンツとして使われる div。内部の span に layoutVar を表示する
        String targetHtml = "<div id=\"content\"><span id=\"msg\">dummy</span></div>";
        // ターゲットmayaa: m:extends でレイアウトを指定
        // m:doRender id="content" → content div を contentArea という名前でコンテンツ登録
        // m:write id="msg" → layoutVar を参照（layout の beforeRender で宣言）
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="%slayout.html">
                    <m:doRender id="content" name="contentArea" />
                    <m:write id="msg" value="${layoutVar}" />
                </m:mayaa>
                """.formatted(casePath);
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        // レイアウト: beforeRender で layoutVar を宣言し、contentArea を挿入する
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var layoutVar = "from-layout";
                    ]]></m:beforeRender>
                    <m:insert id="slot" name="contentArea" replace="false" />
                </m:mayaa>
                """;
        // content div (replace=true デフォルト) は子要素のみ出力される。
        // m:write id="msg" → layoutVar = "from-layout" → span タグごと "from-layout"
        // テキストに置換
        // slot div 内に "from-layout" が展開される
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">from-layout</div>
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
     * m:extends によるレイアウト共有において、
     * ターゲットページの beforeRender で宣言した変数が
     * レイアウトテンプレート内から参照できることを確認する。
     *
     * <p>
     * スコープチェーン（m:extends の場合）:
     * </p>
     * 
     * <pre>
     *   ループ1 (target)  : startScope → S1 (targetVar 登録)
     *   ループ2 (layout)  : startScope → S2 (S2.parentScope = S1)
     *   レンダリング時カレント = S2
     *   layout テンプレートで ${targetVar} → S2 になし → S1 で発見 → "from-target"
     * </pre>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void m_extendsターゲットのbeforeRender変数がレイアウトテンプレートから参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-extends-target-var-to-layout";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"content\">PAGE CONTENT</div>";
        // ターゲット: beforeRender で targetVar を宣言し、contentArea としてコンテンツを登録
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="%slayout.html">
                    <m:beforeRender><![CDATA[
                        var targetVar = "from-target";
                    ]]></m:beforeRender>
                    <m:doRender id="content" name="contentArea" />
                </m:mayaa>
                """.formatted(casePath);
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <span id="msg">dummy</span>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        // レイアウト: msg に targetVar を出力し、slot に contentArea を挿入
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:write id="msg" value="${targetVar}" />
                    <m:insert id="slot" name="contentArea" replace="false" />
                </m:mayaa>
                """;
        // m:write id="msg" で span タグごと "from-target" に置換
        // content div (replace=true) → 子テキスト "PAGE CONTENT" のみ出力
        // slot div 内に "PAGE CONTENT" が展開
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                from-target
                <div id="slot">PAGE CONTENT</div>
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
     * beforeRender 内で load 関数を使って別の JavaScript ファイルを読み込むと、
     * そのファイルで定義された変数・関数がカレントスコープ（beforeRender スコープ）に展開され、
     * 同ページのテンプレートから参照できることを確認する。
     *
     * <p>
     * 仕組み:
     * </p>
     * <ul>
     * <li>{@code ServiceCycle.load(systemID)} は
     * {@code SourceCompiledScriptImpl.execute()} を呼び出す。</li>
     * <li>{@code execute()} 内では {@code RhinoUtil.getScope()}（カレント
     * PageAttributeScope）で実行される。</li>
     * <li>よって lib.js の変数宣言や関数定義は beforeRender のスコープに直接登録される。</li>
     * <li>スクリプトから {@code load("path")} と直接呼べるのは、{@code ServiceCycle} が Rhino の
     * スコープ親として {@code wrapAsJavaObject} でラップされているため。</li>
     * </ul>
     *
     * <p>
     * m:extends レイアウト共有と組み合わせたケース: ターゲットの beforeRender で lib.js を load し、
     * layout テンプレート・content テンプレート双方から lib.js の変数を参照できることも確認する。
     * </p>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void m_extendsのbeforeRender内でload関数で読み込んだJSが同スコープに展開される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-extends-with-load";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String libJsPath = casePath + "lib.js";
        String expectedPath = casePath + "expected.html";

        // lib.js: 変数と関数を定義する（load 後に同スコープで参照できることを確認）
        String libJs = """
                var libMessage = "from-lib";
                function libHelper() { return "helper-result"; }
                """;
        // target HTML: コンテンツエリア。lib.js の変数と関数を使った出力用 span を含む
        String targetHtml = """
                <div id="content">
                <span id="libMsg">dummy</span>
                <span id="libFunc">dummy</span>
                </div>
                """;
        // target mayaa: beforeRender で lib.js を load し、layoutも load した変数を使える
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="%slayout.html">
                    <m:beforeRender><![CDATA[
                        load("%s");
                        var libMsgCopy = libMessage;
                    ]]></m:beforeRender>
                    <m:doRender id="content" name="contentArea" />
                    <m:write id="libMsg" value="${libMessage}" />
                    <m:write id="libFunc" value="${libHelper()}" />
                </m:mayaa>
                """.formatted(casePath, libJsPath);
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <span id="header">dummy</span>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        // layout mayaa: target の beforeRender でセットした libMsgCopy を header で出力
        // これにより target の beforeRender→load が layout テンプレートからも参照できることを確認
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:write id="header" value="${libMsgCopy}" />
                    <m:insert id="slot" name="contentArea" replace="false" />
                </m:mayaa>
                """;
        // header span: libMsgCopy = "from-lib"（target の beforeRender で lib.js load
        // 後にコピー）
        // content (replace=true) の中:
        // libMsg span: libMessage = "from-lib"（lib.js の変数）
        // libFunc span: libHelper() = "helper-result"（lib.js の関数）
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                from-lib
                <div id="slot">
                from-lib
                helper-result
                </div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(libJsPath, libJs);
        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // ===================================================================
    // 変数シャドウイングテスト
    // ===================================================================

    /**
     * target と layout の両方で同名変数を beforeRender で宣言した場合、
     * より後に積まれる layout の beforeRender スコープ（S2）の値が優先されることを確認する。
     *
     * <p>
     * スコープチェーン:
     * </p>
     * 
     * <pre>
     *   S1 (target beforeRender)  : myVar = "from-target"
     *   S2 (layout beforeRender)  : myVar = "from-layout", S2.parentScope = S1
     *   レンダリング時カレント = S2 → ${myVar} は S2 で発見 → "from-layout" が優先
     * </pre>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void targetとlayoutで同名変数を定義した場合layoutの値が優先される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-shadow-target-vs-layout";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layoutHtmlPath = casePath + "layout.html";
        String layoutMayaaPath = casePath + "layout.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"content\"><span id=\"msg\">dummy</span></div>";
        // target: 同名変数 myVar を "from-target" で宣言
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="%slayout.html">
                    <m:beforeRender><![CDATA[
                        var myVar = "from-target";
                    ]]></m:beforeRender>
                    <m:doRender id="content" name="contentArea" />
                    <m:write id="msg" value="${myVar}" />
                </m:mayaa>
                """.formatted(casePath);
        String layoutHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">layout-default</div>
                </body></html>
                """;
        // layout: 同名変数 myVar を "from-layout" で宣言
        // layout の S2 は target の S1 の子であり、S2 での myVar が優先される
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var myVar = "from-layout";
                    ]]></m:beforeRender>
                    <m:insert id="slot" name="contentArea" replace="false" />
                </m:mayaa>
                """;
        // content div 内の msg span: layout の S2 を先に参照するため "from-layout"
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">from-layout</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath, layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath, layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // ===================================================================
    // 深いネストでのスコープ継承テスト
    // ===================================================================

    /**
     * target → comp-a → comp-b の 3 段ネストにおいて、
     * target の beforeRender で宣言した変数が末端の comp-b からも参照できることを確認する。
     *
     * <p>
     * スコープチェーン:
     * </p>
     * 
     * <pre>
     *   S0 (target beforeRender)  : rootVar = "from-root"
     *     → S1 (comp-a renderPage scope)
     *       → S2 (comp-b renderPage scope)
     *         ${rootVar} → S2 → S1 → S0 で発見 → "from-root"
     * </pre>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void targetのbeforeRender変数が3段ネストコンポーネントから参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-propagate-3-level-nest";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String compAHtmlPath = casePath + "comp-a.html";
        String compAMayaaPath = casePath + "comp-a.mayaa";
        String compBHtmlPath = casePath + "comp-b.html";
        String compBMayaaPath = casePath + "comp-b.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slotA">slotA-default</div>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var rootVar = "from-root";
                    ]]></m:beforeRender>
                    <m:insert id="slotA" path="./comp-a.html" replace="false" />
                </m:mayaa>
                """;
        // comp-a は slotB を持ち、そこに comp-b を挿入する中継コンポーネント
        String compAHtml = "<html><body><div id=\"rootA\"><div id=\"slotB\">slotB-default</div></div></body></html>";
        String compAMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="rootA" />
                    <m:insert id="slotB" path="./comp-b.html" replace="false" />
                </m:mayaa>
                """;
        // comp-b は rootVar を参照する末端コンポーネント
        String compBHtml = "<html><body><div id=\"rootB\"><span id=\"val\">dummy</span></div></body></html>";
        String compBMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="rootB" />
                    <m:write id="val" value="${rootVar}" />
                </m:mayaa>
                """;
        // 3 段ネストを経由しても rootVar がプロトタイプチェーンで参照できる
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slotA"><div id="slotB">from-root</div></div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(compAHtmlPath, compAHtml);
        DynamicRegisteredSourceHolder.registerContents(compAMayaaPath, compAMayaa);
        DynamicRegisteredSourceHolder.registerContents(compBHtmlPath, compBHtml);
        DynamicRegisteredSourceHolder.registerContents(compBMayaaPath, compBMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // ===================================================================
    // 祖先スコープ変数の変更（Mutation）テスト
    // ===================================================================

    /**
     * コンポーネント A が {@code var} なしで祖先スコープの変数を書き換えた場合、
     * 兄弟コンポーネント B にもその変更が見えることを確認する。
     *
     * <p>
     * JavaScript のスコープチェーンにおいて {@code var} なしの代入は
     * プロトタイプチェーンを遡って変数が宣言されたスコープを書き換える（{@code ScriptRuntime.setName}）。
     * comp-a が target の S0 の {@code sharedVar} を書き換えた後、
     * comp-b も S0 を共有しているため変更後の値を参照する。
     * </p>
     *
     * <pre>
     *   S0 (target beforeRender) : sharedVar = "original"
     *   comp-a beforeRender : sharedVar = "mutated-by-a"  ← var なし → S0 を書き換え
     *   comp-b : ${sharedVar} → S0 → "mutated-by-a" (変更が見える)
     * </pre>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void コンポーネントAがvar無しで祖先変数を書き換えると兄弟Bにも変更が見える(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-mutation-by-component";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String compAHtmlPath = casePath + "comp-a.html";
        String compAMayaaPath = casePath + "comp-a.mayaa";
        String compBHtmlPath = casePath + "comp-b.html";
        String compBMayaaPath = casePath + "comp-b.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slotA">slotA-default</div>
                <div id="slotB">slotB-default</div>
                </body></html>
                """;
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var sharedVar = "original";
                    ]]></m:beforeRender>
                    <m:insert id="slotA" path="./comp-a.html" replace="false" />
                    <m:insert id="slotB" path="./comp-b.html" replace="false" />
                </m:mayaa>
                """;
        // comp-a: var なしで祖先スコープ（S0）の sharedVar を書き換える
        String compAHtml = "<html><body><div id=\"rootA\"><span id=\"val\">dummy</span></div></body></html>";
        String compAMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        sharedVar = "mutated-by-a";
                    ]]></m:beforeRender>
                    <m:doRender id="rootA" />
                    <m:write id="val" value="${sharedVar}" />
                </m:mayaa>
                """;
        // comp-b: sharedVar を参照する。S0 が書き換えられているため "mutated-by-a" が見える
        String compBHtml = "<html><body><div id=\"rootB\"><span id=\"val\">dummy</span></div></body></html>";
        String compBMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="rootB" />
                    <m:write id="val" value="${sharedVar}" />
                </m:mayaa>
                """;
        // comp-a は "mutated-by-a"、comp-b も同じ祖先スコープ経由で "mutated-by-a" を参照する
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slotA">mutated-by-a</div>
                <div id="slotB">mutated-by-a</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(compAHtmlPath, compAHtml);
        DynamicRegisteredSourceHolder.registerContents(compAMayaaPath, compAMayaa);
        DynamicRegisteredSourceHolder.registerContents(compBHtmlPath, compBHtml);
        DynamicRegisteredSourceHolder.registerContents(compBMayaaPath, compBMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // ===================================================================
    // m:insert インフォーマルプロパティテスト
    // ===================================================================

    /**
     * m:insert に追加したカスタム属性（インフォーマルプロパティ）が、
     * コンポーネント内で {@code binding} スコープ経由で参照できることを確認する。
     *
     * <p>
     * InsertProcessor はインフォーマルプロパティを収集して BindingScope に渡す。
     * コンポーネント内では {@code ${binding.propName}} で値を取得できる。
     * </p>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void m_insertのインフォーマルプロパティがbindingスコープで参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-insert-informal-property";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String componentHtmlPath = casePath + "component.html";
        String componentMayaaPath = casePath + "component.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">slot-default</div>
                </body></html>
                """;
        // m:insert に myParam="hello-binding" を追加（インフォーマルプロパティ）
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="slot" path="./component.html" replace="false" myParam="hello-binding" />
                </m:mayaa>
                """;
        String componentHtml = "<html><body><div id=\"root\"><span id=\"val\">dummy</span></div></body></html>";
        // コンポーネント内で binding.myParam としてインフォーマルプロパティを参照する
        String componentMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:doRender id="root" />
                    <m:write id="val" value="${binding.myParam}" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">hello-binding</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath, componentHtml);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath, componentMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * m:insert のインフォーマルプロパティで未定義キーを参照した場合、
     * 例外を起こさずフォールバック値を使えることを確認する。
     *
     * <p>
     * 既存キーの参照だけでなく、未定義キー参照時の安全な扱いを検証することで
     * binding スコープ利用時の境界条件をカバーする。
     * </p>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void m_insertの未定義インフォーマルプロパティ参照でフォールバックできる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-insert-informal-property-missing";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String componentHtmlPath = casePath + "component.html";
        String componentMayaaPath = casePath + "component.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">slot-default</div>
                </body></html>
                """;
        // myParam は渡すが、コンポーネント側では notProvided を参照してフォールバックを確認する
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                        <m:insert id="slot" path="./component.html" replace="false" myParam="hello-binding" />
                </m:mayaa>
                """;
        String componentHtml = "<html><body><div id=\"root\"><span id=\"val\">dummy</span></div></body></html>";
        // 未定義キー binding.notProvided は null/undefined 相当として扱い、missing を返す
        String componentMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                        <m:doRender id="root" />
                        <m:write id="val" value="${binding.notProvided != null ? binding.notProvided : 'missing'}" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">missing</div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath, componentHtml);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath, componentMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // ===================================================================
    // 多段 m:extends（3 段）のスコープテスト
    // ===================================================================

    /**
     * target → layout1 → layout2 の 3 段 m:extends チェーンにおいて、
     * すべての階層の beforeRender 変数がターゲットテンプレートのプロセッサから参照できることを確認する。
     *
     * <p>
     * スコープチェーン:
     * </p>
     * 
     * <pre>
     *   S1 (target beforeRender)  : tVar = "T"
     *   S2 (layout1 beforeRender) : l1Var = "L1", S2.parentScope = S1
     *   S3 (layout2 beforeRender) : l2Var = "L2", S3.parentScope = S2
     *   レンダリング時カレント = S3
     *   ${tVar}  → S3 → S2 → S1 → "T"
     *   ${l1Var} → S3 → S2 → "L1"
     *   ${l2Var} → S3 → "L2"
     * </pre>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void _3段m_extendsチェーンで全階層のbeforeRender変数が参照できる(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-extends-3level-chain";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layout1HtmlPath = casePath + "layout1.html";
        String layout1MayaaPath = casePath + "layout1.mayaa";
        String layout2HtmlPath = casePath + "layout2.html";
        String layout2MayaaPath = casePath + "layout2.mayaa";
        String expectedPath = casePath + "expected.html";

        // target: 全 3 階層の変数を連結して出力する span を含むコンテンツ
        String targetHtml = "<div id=\"tContent\"><span id=\"vars\">dummy</span></div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="%slayout1.html">
                    <m:beforeRender><![CDATA[
                        var tVar = "T";
                    ]]></m:beforeRender>
                    <m:doRender id="tContent" name="contentArea" />
                    <m:write id="vars" value="${tVar}+${l1Var}+${l2Var}" />
                </m:mayaa>
                """.formatted(casePath);
        // layout1: layout2 を継承し、contentArea を中継する
        String layout1Html = "<div id=\"l1Root\"><div id=\"innerSlot\">inner-default</div></div>";
        String layout1Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                        m:extends="%slayout2.html">
                    <m:beforeRender><![CDATA[
                        var l1Var = "L1";
                    ]]></m:beforeRender>
                    <m:doRender id="l1Root" name="outerContent" />
                    <m:insert id="innerSlot" name="contentArea" replace="false" />
                </m:mayaa>
                """.formatted(casePath);
        // layout2: 最外殻 HTML。outerContent スロットに layout1 のコンテンツを挿入する
        String layout2Html = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="outerSlot">outer-default</div>
                </body></html>
                """;
        String layout2Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var l2Var = "L2";
                    ]]></m:beforeRender>
                    <m:insert id="outerSlot" name="outerContent" replace="false" />
                </m:mayaa>
                """;
        // outerSlot → layout1 の l1Root 子要素 → innerSlot → target の tContent 子要素 →
        // "T+L1+L2"
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="outerSlot"><div id="innerSlot">T+L1+L2</div></div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layout1HtmlPath, layout1Html);
        DynamicRegisteredSourceHolder.registerContents(layout1MayaaPath, layout1Mayaa);
        DynamicRegisteredSourceHolder.registerContents(layout2HtmlPath, layout2Html);
        DynamicRegisteredSourceHolder.registerContents(layout2MayaaPath, layout2Mayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * 3 段 m:extends チェーンで layout1 の beforeRender 変数が未定義でも、
     * target/layout2 の変数は参照でき、未定義変数だけフォールバックできることを確認する。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void _3段m_extendsチェーンで中間階層変数が未定義でも他階層は参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-extends-3level-chain-missing-l1";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layout1HtmlPath = casePath + "layout1.html";
        String layout1MayaaPath = casePath + "layout1.mayaa";
        String layout2HtmlPath = casePath + "layout2.html";
        String layout2MayaaPath = casePath + "layout2.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"tContent\"><span id=\"vars\">dummy</span></div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                                m:extends="%slayout1.html">
                        <m:beforeRender><![CDATA[
                                var tVar = "T";
                        ]]></m:beforeRender>
                        <m:doRender id="tContent" name="contentArea" />
                        <m:write id="vars" value="${tVar}+${typeof l1Var !== 'undefined' ? l1Var : 'NA'}+${l2Var}" />
                </m:mayaa>
                """.formatted(casePath);
        String layout1Html = "<div id=\"l1Root\"><div id=\"innerSlot\">inner-default</div></div>";
        // layout1 は l1Var を定義しない
        String layout1Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                                m:extends="%slayout2.html">
                        <m:doRender id="l1Root" name="outerContent" />
                        <m:insert id="innerSlot" name="contentArea" replace="false" />
                </m:mayaa>
                """.formatted(casePath);
        String layout2Html = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="outerSlot">outer-default</div>
                </body></html>
                """;
        String layout2Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                        <m:beforeRender><![CDATA[
                                var l2Var = "L2";
                        ]]></m:beforeRender>
                        <m:insert id="outerSlot" name="outerContent" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="outerSlot"><div id="innerSlot">T+NA+L2</div></div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layout1HtmlPath, layout1Html);
        DynamicRegisteredSourceHolder.registerContents(layout1MayaaPath, layout1Mayaa);
        DynamicRegisteredSourceHolder.registerContents(layout2HtmlPath, layout2Html);
        DynamicRegisteredSourceHolder.registerContents(layout2MayaaPath, layout2Mayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    /**
     * 3 段 m:extends チェーンで layout2 の beforeRender 変数が未定義でも、
     * target/layout1 の変数は参照でき、未定義変数だけフォールバックできることを確認する。
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void _3段m_extendsチェーンで最上位階層変数が未定義でも他階層は参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "layout-extends-3level-chain-missing-l2";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String layout1HtmlPath = casePath + "layout1.html";
        String layout1MayaaPath = casePath + "layout1.mayaa";
        String layout2HtmlPath = casePath + "layout2.html";
        String layout2MayaaPath = casePath + "layout2.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = "<div id=\"tContent\"><span id=\"vars\">dummy</span></div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                                m:extends="%slayout1.html">
                        <m:beforeRender><![CDATA[
                                var tVar = "T";
                        ]]></m:beforeRender>
                        <m:doRender id="tContent" name="contentArea" />
                        <m:write id="vars" value="${tVar}+${l1Var}+${typeof l2Var !== 'undefined' ? l2Var : 'NA'}" />
                </m:mayaa>
                """.formatted(casePath);
        String layout1Html = "<div id=\"l1Root\"><div id=\"innerSlot\">inner-default</div></div>";
        String layout1Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                                m:extends="%slayout2.html">
                        <m:beforeRender><![CDATA[
                                var l1Var = "L1";
                        ]]></m:beforeRender>
                        <m:doRender id="l1Root" name="outerContent" />
                        <m:insert id="innerSlot" name="contentArea" replace="false" />
                </m:mayaa>
                """.formatted(casePath);
        String layout2Html = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="outerSlot">outer-default</div>
                </body></html>
                """;
        // layout2 は l2Var を定義しない
        String layout2Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                        <m:insert id="outerSlot" name="outerContent" replace="false" />
                </m:mayaa>
                """;
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="outerSlot"><div id="innerSlot">T+L1+NA</div></div>
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layout1HtmlPath, layout1Html);
        DynamicRegisteredSourceHolder.registerContents(layout1MayaaPath, layout1Mayaa);
        DynamicRegisteredSourceHolder.registerContents(layout2HtmlPath, layout2Html);
        DynamicRegisteredSourceHolder.registerContents(layout2MayaaPath, layout2Mayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());
    }

    // ===================================================================
    // コンポーネントのローカルスコープ分離テスト
    // ===================================================================

    /**
     * コンポーネントの beforeRender で {@code var} を使って同名変数を宣言した場合、
     * 祖先スコープの同名変数をシャドウする（局所的に上書き）が、
     * コンポーネント終了後は祖先スコープの変数が元の値を保持していることを確認する。
     *
     * <p>
     * {@code var} による宣言はコンポーネントのローカルスコープに新しい束縛を作成するため、
     * 祖先スコープ（target の S0）の {@code myVar} は変更されない。
     * </p>
     *
     * <pre>
     *   S0 (target beforeRender) : myVar = "from-target"
     *   S2 (comp beforeRender)   : myVar = "from-component" (var → S2 に新規束縛)
     *   comp 内 ${myVar} → S2 で発見 → "from-component"
     *   comp 終了後 target の ${myVar} → S0 で発見 → "from-target" (不変)
     * </pre>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void コンポーネントがvar宣言で同名変数をシャドウしても親スコープは不変(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseName = "scope-component-local-shadow";
        String casePath = BASE + caseName + "/";
        String targetHtmlPath = casePath + "target.html";
        String targetMayaaPath = casePath + "target.mayaa";
        String componentHtmlPath = casePath + "component.html";
        String componentMayaaPath = casePath + "component.mayaa";
        String expectedPath = casePath + "expected.html";

        String targetHtml = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">slot-default</div>
                <span id="targetVal">dummy</span>
                </body></html>
                """;
        // target: beforeRender で myVar = "from-target" にセット
        // slot にコンポーネントを挿入し、その後 targetVal で target 側の myVar を出力する
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var myVar = "from-target";
                    ]]></m:beforeRender>
                    <m:insert id="slot" path="./component.html" replace="false" />
                    <m:write id="targetVal" value="${myVar}" />
                </m:mayaa>
                """;
        String componentHtml = "<html><body><div id=\"root\"><span id=\"compVal\">dummy</span></div></body></html>";
        // component: var でローカルスコープに myVar = "from-component" を宣言
        // → 祖先スコープの myVar は変更されない
        String componentMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var myVar = "from-component";
                    ]]></m:beforeRender>
                    <m:doRender id="root" />
                    <m:write id="compVal" value="${myVar}" />
                </m:mayaa>
                """;
        // slot: コンポーネントは "from-component" を出力（ローカルスコープから参照）
        // targetVal: target の myVar は "from-target" のまま（コンポーネントのシャドウは局所的）
        String expected = """
                <!DOCTYPE html>
                <html><head></head><body>
                <div id="slot">from-component</div>
                from-target
                </body></html>
                """;

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath, componentHtml);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath, componentMayaa);
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
