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
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.ParentSpecificationResolver;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.DynamicRegisteredSourceHolder;

/**
 * ParentSpecificationResolver を差し替えた場合の親連鎖解決と
 * beforeRender/afterRender 実行順を検証するIT。
 */
public class ParentSpecificationResolverIntegrationTest extends EngineTestBase {

    private ParentSpecificationResolver originalResolver;

    @Test
    public void デフォルトParentSpecificationResolverはPageに対してdefaultSpecificationを返す() {
        String caseRoot = "/it-case/parent-specification-resolver/default-resolver";
        String targetHtmlPath = caseRoot + "/target.html";
        String targetMayaaPath = caseRoot + "/target.mayaa";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, "<div id=\"root\">dummy</div>");
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<m:mayaa xmlns:m=\"http://mayaa.seasar.org\"></m:mayaa>");

        exec(createRequest(targetHtmlPath), new LinkedHashMap<String, Object>());

        ParentSpecificationResolver resolver = getServiceProvider().getParentSpecificationResolver();
        Page page = getPage();

        assertSame(SpecificationUtil.getDefaultSpecification(), resolver.getParentSpecification(page));
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void 親仕様を複数段返し最後にケース専用defaultページを返す場合にbeforeAfterRenderが親連鎖順で実行される(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseRoot = "/it-case/parent-specification-resolver/multi-parent-chain";
        String targetHtmlPath = caseRoot + "/target.html";
        String targetMayaaPath = caseRoot + "/target.mayaa";
        String parent1HtmlPath = caseRoot + "/parent1.html";
        String parent1MayaaPath = caseRoot + "/parent1.mayaa";
        String parent2HtmlPath = caseRoot + "/parent2.html";
        String parent2MayaaPath = caseRoot + "/parent2.mayaa";
        String defaultMayaaPath = caseRoot + "/default.mayaa";
        String expectedPath = caseRoot + "/expected.html";

        String targetHtml = "<div id=\"root\"><span id=\"chain\">dummy</span></div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        appendBefore("T");
                    ]]></m:beforeRender>
                    <m:write id="chain" value="${chain.join('>')}" />
                    <m:afterRender><![CDATA[
                        application.parentChainTrace = application.parentChainTrace + "|A-T";
                    ]]></m:afterRender>
                </m:mayaa>
                """;
        String parent1Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        appendBefore("P1");
                    ]]></m:beforeRender>
                    <m:afterRender><![CDATA[
                        application.parentChainTrace = application.parentChainTrace + "|A-P1";
                    ]]></m:afterRender>
                </m:mayaa>
                """;
        String parent2Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        appendBefore("P2");
                    ]]></m:beforeRender>
                    <m:afterRender><![CDATA[
                        application.parentChainTrace = application.parentChainTrace + "|A-P2";
                    ]]></m:afterRender>
                </m:mayaa>
                """;
        String defaultMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        application.parentChainTrace = "";
                        var chain = [];
                        function appendBefore(marker) {
                            chain.push(marker);
                            application.parentChainTrace = application.parentChainTrace + "|B-" + marker;
                        }
                        appendBefore("D");
                    ]]></m:beforeRender>
                    <m:afterRender><![CDATA[
                        application.parentChainTrace = application.parentChainTrace + "|A-D";
                    ]]></m:afterRender>
                </m:mayaa>
                """;
        String expected = "<div id=\"root\">D&gt;P2&gt;P1&gt;T</div>";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath, targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath, targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(parent1HtmlPath, "<div id=\"parent1\"></div>");
        DynamicRegisteredSourceHolder.registerContents(parent1MayaaPath, parent1Mayaa);
        DynamicRegisteredSourceHolder.registerContents(parent2HtmlPath, "<div id=\"parent2\"></div>");
        DynamicRegisteredSourceHolder.registerContents(parent2MayaaPath, parent2Mayaa);
        DynamicRegisteredSourceHolder.registerContents(defaultMayaaPath, defaultMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath, expected);

        getServiceProvider().getEngine().setParameter("defaultSpecification", defaultMayaaPath);

        originalResolver = getServiceProvider().getParentSpecificationResolver();
        getServiceProvider().setParentSpecificationResolver(
                new ChainParentSpecificationResolver(originalResolver, caseRoot));

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<String, Object>());

        assertEquals("|B-D|B-P2|B-P1|B-T|A-T|A-P1|A-P2|A-D",
            CycleUtil.getServiceCycle().getApplicationScope().getAttribute("parentChainTrace"));
    }

    /**
     * ParentSpecificationResolver を差し替えて複数の親仕様が存在する状態で、
     * target が m:extends によるレイアウト継承も使用している場合に、
     * 各親仕様の beforeRender で宣言した変数がより具体的な仕様（target）の
     * beforeRender / テンプレート内 m:write から参照できることを確認する。
     *
     * シナリオ:
     *   ParentSpec chain: target → parentSpec1 → parentSpec2 → default
     *   レイアウト継承 (m:extends): target → layout
     *
     * 期待するスコープ可視性:
     *   parentSpec2.beforeRender が宣言した変数 → parentSpec1.beforeRender から参照可能
     *   parentSpec1.beforeRender が宣言した変数 → target.beforeRender から参照可能
     *   target.beforeRender が宣言した変数    → テンプレート内 m:write から参照可能
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void m_extendsとParentSpecResolver共存時に各beforeRenderで宣言した変数が子仕様から参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseRoot = "/it-case/parent-specification-resolver/extends-with-parent-chain";

        String targetHtmlPath  = caseRoot + "/target.html";
        String targetMayaaPath = caseRoot + "/target.mayaa";
        String layoutHtmlPath  = caseRoot + "/layout.html";
        String layoutMayaaPath = caseRoot + "/layout.mayaa";
        String parentSpec1MayaaPath = caseRoot + "/parent-spec1.mayaa";
        String parentSpec2MayaaPath = caseRoot + "/parent-spec2.mayaa";
        String defaultMayaaPath     = caseRoot + "/default.mayaa";
        String expectedPath         = caseRoot + "/expected.html";

        // target.html: m:write で各スコープの変数を出力する
        String targetHtml = "<div id=\"root\">"
                + "<span id=\"p2var\">?</span>"
                + "<span id=\"p1var\">?</span>"
                + "<span id=\"tvar\">?</span>"
                + "</div>";

        // target.mayaa: m:extends でレイアウトを継承しつつ、beforeRender でも変数を宣言する。
        //   - p2var / p1var は親仕様の beforeRender で宣言されるはずの変数を参照する
        //   - tvar は自身の beforeRender で宣言する
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                         m:extends="%s">
                    <m:beforeRender><![CDATA[
                        var tvar = "from-target";
                        application.scopeTrace = application.scopeTrace + ",B-target";
                    ]]></m:beforeRender>
                    <m:write id="p2var" value="${p2var}" />
                    <m:write id="p1var" value="${p1var}" />
                    <m:write id="tvar"  value="${tvar}" />
                    <m:doRender id="root" name="rootContent" />
                </m:mayaa>
                """.formatted(layoutHtmlPath);

        // layout.html: レイアウトのHTMLテンプレート（スロット役）
        String layoutHtml = "<div id=\"layout\"><div id=\"root\">layout-slot</div></div>";

        // layout.mayaa: レイアウト自体は beforeRender なし、rootContent を insert で受け取る
        // replace="false" にすることで layout の div#root タグを保持しつつ中身を置換する
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:insert id="root" name="rootContent" replace="false" />
                </m:mayaa>
                """;

        // parent-spec1.mayaa: target の Parent として Resolver が返す親仕様。変数 p1var を宣言。
        //   p2var を参照して連鎖を確認（p2var は parent-spec2 が宣言する変数）
        String parentSpec1Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var p1var = "from-parent1(p2var=" + p2var + ")";
                        application.scopeTrace = application.scopeTrace + ",B-p1";
                    ]]></m:beforeRender>
                </m:mayaa>
                """;

        // parent-spec2.mayaa: parent-spec1 の Parent として Resolver が返す仕様。変数 p2var を宣言。
        String parentSpec2Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var p2var = "from-parent2";
                        application.scopeTrace = application.scopeTrace + ",B-p2";
                    ]]></m:beforeRender>
                </m:mayaa>
                """;

        // default.mayaa: チェーンの末端 Specification（defaultSpecification として使用）。
        //   scopeTrace の初期化も担う。
        String defaultMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        application.scopeTrace = "B-default";
                    ]]></m:beforeRender>
                </m:mayaa>
                """;

        // 期待する出力: 各 beforeRender で宣言した変数がテンプレートから参照できていること
        // m:write はデフォルト replace="true" なので span タグごと値に置換される
        String expected = "<div id=\"layout\"><div id=\"root\">"
                + "from-parent2"
                + "from-parent1(p2var=from-parent2)"
                + "from-target"
                + "</div></div>";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath,      targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath,     targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath,      layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath,     layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(parentSpec1MayaaPath, parentSpec1Mayaa);
        DynamicRegisteredSourceHolder.registerContents(parentSpec2MayaaPath, parentSpec2Mayaa);
        DynamicRegisteredSourceHolder.registerContents(defaultMayaaPath,    defaultMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath,        expected);

        getServiceProvider().getEngine().setParameter("defaultSpecification", defaultMayaaPath);

        // target → parent-spec1 → parent-spec2 → default という親仕様チェーンを構築するリゾルバ
        originalResolver = getServiceProvider().getParentSpecificationResolver();
        getServiceProvider().setParentSpecificationResolver(
                new ExtendsWithChainParentSpecificationResolver(originalResolver, caseRoot));

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<>());

        // beforeRender 実行順序のトレース検証
        //   期待順: default → p2 → p1 → target（外側スコープが先に実行される）
        assertEquals("B-default,B-p2,B-p1,B-target",
            CycleUtil.getServiceCycle().getApplicationScope().getAttribute("scopeTrace"));
    }

    /**
     * SuperPage (m:extends によって指定されるレイアウトページ) の親仕様チェーンで宣言した
     * 変数が、より具体的な仕様 (SuperPage 自身の beforeRender) から参照できることを確認する。
     *
     * <p>用語</p>
     * <ul>
     *   <li>Target    : アクセスの直接の対象ページ</li>
     *   <li>SuperPage : Target に m:extends で指定されるレイアウトページ</li>
     *   <li>Parent    : Target/SuperPage それぞれに対する親ページ (Resolver が返す)</li>
     * </ul>
     *
     * <p>Parent chain (SuperPage 側):</p>
     * <pre>layout → layout-parent1 → layout-parent2 → default</pre>
     *
     * <p>期待するスコープ可視性:</p>
     * <ul>
     *   <li>layout-parent2.beforeRender 宣言 lp2var → layout-parent1 から参照可能</li>
     *   <li>layout-parent1.beforeRender 宣言 lp1var → layout から参照可能</li>
     * </ul>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void SuperPageの親仕様チェーンの各beforeRenderで宣言した変数がSuperPage自身のbeforeRenderから参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseRoot = "/it-case/parent-specification-resolver/super-page-parent-chain";
        String targetHtmlPath      = caseRoot + "/target.html";
        String targetMayaaPath     = caseRoot + "/target.mayaa";
        String layoutHtmlPath      = caseRoot + "/layout.html";
        String layoutMayaaPath     = caseRoot + "/layout.mayaa";
        String layoutParent1MayaaPath = caseRoot + "/layout-parent1.mayaa";
        String layoutParent2MayaaPath = caseRoot + "/layout-parent2.mayaa";
        String defaultMayaaPath    = caseRoot + "/default.mayaa";
        String expectedPath        = caseRoot + "/expected.html";

        // Target: m:extends でレイアウトを指定するだけ。コンテンツ挿入は行わない。
        String targetHtml = "<div>TARGET</div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org"
                         m:extends="%s">
                    <m:beforeRender><![CDATA[
                        application.scopeTrace = application.scopeTrace + ",B-T";
                    ]]></m:beforeRender>
                </m:mayaa>
                """.formatted(layoutHtmlPath);

        // SuperPage (layout): layout-parent{1,2} が宣言した変数を参照して lvar を構築し出力する
        String layoutHtml = "<div id=\"layout\"><span id=\"lvar\">dummy</span></div>";
        String layoutMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var lvar = "from-layout(lp1var=" + lp1var + ")";
                        application.scopeTrace = application.scopeTrace + ",B-L";
                    ]]></m:beforeRender>
                    <m:write id="lvar" value="${lvar}" />
                </m:mayaa>
                """;

        // layout の Parent chain: layout-parent2 が末端、layout-parent1 が中間
        String layoutParent2Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var lp2var = "from-lp2";
                        application.scopeTrace = application.scopeTrace + ",B-LP2";
                    ]]></m:beforeRender>
                </m:mayaa>
                """;
        String layoutParent1Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var lp1var = "from-lp1(lp2var=" + lp2var + ")";
                        application.scopeTrace = application.scopeTrace + ",B-LP1";
                    ]]></m:beforeRender>
                </m:mayaa>
                """;

        // default: アプリスコープの scopeTrace を初期化
        String defaultMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        application.scopeTrace = "B-D";
                    ]]></m:beforeRender>
                </m:mayaa>
                """;

        // 期待する出力: layout の lvar が lp1var/lp2var を参照して構築された値になること
        // m:write は replace="true" (デフォルト) なので span タグごと値に置換される
        String expected = "<div id=\"layout\">from-layout(lp1var=from-lp1(lp2var=from-lp2))</div>";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath,       targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath,      targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutHtmlPath,       layoutHtml);
        DynamicRegisteredSourceHolder.registerContents(layoutMayaaPath,      layoutMayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutParent1MayaaPath, layoutParent1Mayaa);
        DynamicRegisteredSourceHolder.registerContents(layoutParent2MayaaPath, layoutParent2Mayaa);
        DynamicRegisteredSourceHolder.registerContents(defaultMayaaPath,     defaultMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath,         expected);

        getServiceProvider().getEngine().setParameter("defaultSpecification", defaultMayaaPath);

        // layout → layout-parent1 → layout-parent2 → default という親仕様チェーンを構築するリゾルバ
        Map<String, String> parentMap = new LinkedHashMap<>();
        parentMap.put(caseRoot + "/layout",         caseRoot + "/layout-parent1");
        parentMap.put(caseRoot + "/layout-parent1", caseRoot + "/layout-parent2");
        parentMap.put(caseRoot + "/layout-parent2", null); // null = defaultSpec で終端

        originalResolver = getServiceProvider().getParentSpecificationResolver();
        getServiceProvider().setParentSpecificationResolver(
                new MappedParentSpecificationResolver(originalResolver, parentMap));

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<>());

        // beforeRender 実行順序のトレース検証
        //   期待順: D → T (Target, 親仕様なし) → LP2 → LP1 → L (SuperPage の親チェーンは外→内)
        assertEquals("B-D,B-T,B-LP2,B-LP1,B-L",
            CycleUtil.getServiceCycle().getApplicationScope().getAttribute("scopeTrace"));
    }

    /**
     * Component (m:insert で差し込まれるページ) の親仕様チェーンで宣言した変数が、
     * Component 自身の beforeRender から参照できることを確認する。
     *
     * <p>用語</p>
     * <ul>
     *   <li>Component : Target や SuperPage 内から m:insert によって差し込まれる断片ページ</li>
     *   <li>Parent    : Component に対する親ページ (Resolver が返す)</li>
     * </ul>
     *
     * <p>Parent chain (Component 側):</p>
     * <pre>component → comp-parent1 → default</pre>
     *
     * <p>期待するスコープ可視性:</p>
     * <ul>
     *   <li>comp-parent1.beforeRender 宣言 cp1var → component から参照可能</li>
     * </ul>
     *
     * <p>Component の beforeRender は m:insert の処理タイミングでインラインに実行される。</p>
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = { false, true })
    public void Componentの親仕様チェーンの各beforeRenderで宣言した変数がComponent自身のbeforeRenderから参照できる(boolean useNewParser)
            throws IOException {
        setUseNewParser(useNewParser);
        setAutoEscapeEnabled(false);

        String caseRoot = "/it-case/parent-specification-resolver/component-parent-chain";
        String targetHtmlPath     = caseRoot + "/target.html";
        String targetMayaaPath    = caseRoot + "/target.mayaa";
        String componentHtmlPath  = caseRoot + "/component.html";
        String componentMayaaPath = caseRoot + "/component.mayaa";
        String compParent1MayaaPath = caseRoot + "/comp-parent1.mayaa";
        String defaultMayaaPath   = caseRoot + "/default.mayaa";
        String expectedPath       = caseRoot + "/expected.html";

        // Target: component を m:insert (path 指定) で差し込む
        String targetHtml = "<div id=\"root\"><div id=\"comp-slot\">dummy</div></div>";
        String targetMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        application.scopeTrace = application.scopeTrace + ",B-T";
                    ]]></m:beforeRender>
                    <m:insert id="comp-slot" path="%s" replace="false" />
                </m:mayaa>
                """.formatted(componentHtmlPath);

        // Component: comp-parent1 が宣言した cp1var を参照して cvar を構築し出力する
        // div id="root" は m:doRender で Component のルートコンテンツとしてマークする
        String componentHtml = "<div id=\"root\"><span id=\"cvar\">dummy</span></div>";
        String componentMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var cvar = "from-comp(cp1var=" + cp1var + ")";
                        application.scopeTrace = application.scopeTrace + ",B-C";
                    ]]></m:beforeRender>
                    <m:doRender id="root" />
                    <m:write id="cvar" value="${cvar}" />
                </m:mayaa>
                """;

        // Component の Parent: comp-parent1
        String compParent1Mayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        var cp1var = "from-cp1";
                        application.scopeTrace = application.scopeTrace + ",B-CP1";
                    ]]></m:beforeRender>
                </m:mayaa>
                """;

        // default: アプリスコープの scopeTrace を初期化
        String defaultMayaa = """
                <?xml version="1.0" encoding="UTF-8"?>
                <m:mayaa xmlns:m="http://mayaa.seasar.org">
                    <m:beforeRender><![CDATA[
                        application.scopeTrace = "B-D";
                    ]]></m:beforeRender>
                </m:mayaa>
                """;

        // 期待する出力: comp-slot の中に component が挿入され、cvar が cp1var を参照して構築された値になること
        // m:write は replace="true" (デフォルト) なので span タグごと値に置換される
        // m:insert replace="false" なので comp-slot の div タグは保持される
        String expected = "<div id=\"root\"><div id=\"comp-slot\">from-comp(cp1var=from-cp1)</div></div>";

        DynamicRegisteredSourceHolder.registerContents(targetHtmlPath,      targetHtml);
        DynamicRegisteredSourceHolder.registerContents(targetMayaaPath,     targetMayaa);
        DynamicRegisteredSourceHolder.registerContents(componentHtmlPath,   componentHtml);
        DynamicRegisteredSourceHolder.registerContents(componentMayaaPath,  componentMayaa);
        DynamicRegisteredSourceHolder.registerContents(compParent1MayaaPath, compParent1Mayaa);
        DynamicRegisteredSourceHolder.registerContents(defaultMayaaPath,    defaultMayaa);
        DynamicRegisteredSourceHolder.registerContents(expectedPath,        expected);

        getServiceProvider().getEngine().setParameter("defaultSpecification", defaultMayaaPath);

        // component → comp-parent1 → default という親仕様チェーンを構築するリゾルバ
        Map<String, String> parentMap = new LinkedHashMap<>();
        parentMap.put(caseRoot + "/component", caseRoot + "/comp-parent1");
        parentMap.put(caseRoot + "/comp-parent1", null); // null = defaultSpec で終端

        originalResolver = getServiceProvider().getParentSpecificationResolver();
        getServiceProvider().setParentSpecificationResolver(
                new MappedParentSpecificationResolver(originalResolver, parentMap));

        execAndVerify(targetHtmlPath, expectedPath, new LinkedHashMap<>());

        // beforeRender 実行順序のトレース検証
        //   期待順: D → T → CP1 → C（Component の親チェーンは m:insert 処理タイミングでインラインに実行）
        assertEquals("B-D,B-T,B-CP1,B-C",
            CycleUtil.getServiceCycle().getApplicationScope().getAttribute("scopeTrace"));
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
        if (originalResolver != null) {
            getServiceProvider().setParentSpecificationResolver(originalResolver);
            originalResolver = null;
        }
        getServiceProvider().getEngine().setParameter("defaultSpecification", "/default.mayaa");
        // Ensure next tests rebuild global default from regular resources.
        SpecificationUtil.getDefaultSpecification().deprecate();
        DynamicRegisteredSourceHolder.unregisterAll();
    }

    private static final class ChainParentSpecificationResolver extends ParameterAwareImpl
            implements ParentSpecificationResolver {

        private static final long serialVersionUID = 1L;

        private final ParentSpecificationResolver fallback;
        private final String caseRoot;

        private ChainParentSpecificationResolver(ParentSpecificationResolver fallback, String caseRoot) {
            this.fallback = fallback;
            this.caseRoot = caseRoot;
        }

        @Override
        public Specification getParentSpecification(Specification spec) {
            if (spec instanceof Template) {
                return ((Template) spec).getPage();
            }
            if (!(spec instanceof Page)) {
                return null;
            }

            String pageName = ((Page) spec).getPageName();
            Engine engine = ProviderUtil.getEngine();
            if ((caseRoot + "/target").equals(pageName)) {
                return engine.getPage(caseRoot + "/parent1");
            }
            if ((caseRoot + "/parent1").equals(pageName)) {
                return engine.getPage(caseRoot + "/parent2");
            }
            if ((caseRoot + "/parent2").equals(pageName)) {
                return SpecificationUtil.getDefaultSpecification();
            }
            if (fallback != null) {
                return fallback.getParentSpecification(spec);
            }
            return null;
        }
    }

    /**
     * m:extends によるレイアウト継承と ParentSpecificationResolver による親仕様連鎖を
     * 組み合わせるテスト用リゾルバ。
     *   target          → parent-spec1 (親仕様チェーン)
     *   parent-spec1    → parent-spec2 (親仕様チェーン)
     *   parent-spec2    → defaultSpec  (終端)
     *   layout / others → fallback
     */
    private static final class ExtendsWithChainParentSpecificationResolver extends ParameterAwareImpl
            implements ParentSpecificationResolver {

        private static final long serialVersionUID = 1L;

        private final ParentSpecificationResolver fallback;
        private final String caseRoot;

        private ExtendsWithChainParentSpecificationResolver(
                ParentSpecificationResolver fallback, String caseRoot) {
            this.fallback = fallback;
            this.caseRoot = caseRoot;
        }

        @Override
        public Specification getParentSpecification(Specification spec) {
            if (spec instanceof Template) {
                return ((Template) spec).getPage();
            }
            if (!(spec instanceof Page)) {
                return null;
            }

            String pageName = ((Page) spec).getPageName();
            Engine engine = ProviderUtil.getEngine();
            if ((caseRoot + "/target").equals(pageName)) {
                return engine.getPage(caseRoot + "/parent-spec1");
            }
            if ((caseRoot + "/parent-spec1").equals(pageName)) {
                return engine.getPage(caseRoot + "/parent-spec2");
            }
            if ((caseRoot + "/parent-spec2").equals(pageName)) {
                return SpecificationUtil.getDefaultSpecification();
            }
            if (fallback != null) {
                return fallback.getParentSpecification(spec);
            }
            return null;
        }
    }

    /**
     * ページ名と親ページ名のマッピングによって親仕様を解決する汎用リゾルバ。
     * マップの値が null の場合は defaultSpecification を返す。
     * マップに含まれないページは fallback リゾルバに委譲する。
     */
    private static final class MappedParentSpecificationResolver extends ParameterAwareImpl
            implements ParentSpecificationResolver {

        private static final long serialVersionUID = 1L;

        private final ParentSpecificationResolver fallback;
        private final Map<String, String> parentMap;

        private MappedParentSpecificationResolver(
                ParentSpecificationResolver fallback, Map<String, String> parentMap) {
            this.fallback = fallback;
            this.parentMap = parentMap;
        }

        @Override
        public Specification getParentSpecification(Specification spec) {
            if (spec instanceof Template) {
                return ((Template) spec).getPage();
            }
            if (!(spec instanceof Page)) {
                return null;
            }
            String pageName = ((Page) spec).getPageName();
            if (parentMap.containsKey(pageName)) {
                String parentName = parentMap.get(pageName);
                if (parentName == null) {
                    return SpecificationUtil.getDefaultSpecification();
                }
                return ProviderUtil.getEngine().getPage(parentName);
            }
            if (fallback != null) {
                return fallback.getParentSpecification(spec);
            }
            return null;
        }
    }

}
