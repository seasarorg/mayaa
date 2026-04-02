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
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
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
        String expected = "<div id=\"root\">D&gt;T&gt;P1&gt;P2</div>";

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

        assertEquals("|B-D|B-T|B-P1|B-P2|A-P2|A-P1|A-T|A-D",
            CycleUtil.getServiceCycle().getApplicationScope().getAttribute("parentChainTrace"));
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
        DiagnosticEventBuffer.clear();
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

}
