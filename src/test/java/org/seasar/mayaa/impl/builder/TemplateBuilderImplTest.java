/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.builder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.impl.engine.TemplateImpl;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * Tests for {@link org.seasar.mayaa.impl.builder.TemplateBuilderImpl}
 * @author Koji SUGA (Gluegent, Inc.)
 */
public class TemplateBuilderImplTest {

    @BeforeEach
    public void setUp() throws Exception {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    /**
     * ContentHandler生成時のパラメータ渡しのテスト。
     */
    @Test
    public void testCreateContentHandler() {
        TemplateBuilderImpl builder = new TemplateBuilderImpl();
        builder.setParameter(TemplateBuilderImpl.OUTPUT_TEMPLATE_WHITESPACE, "false");
        builder.setParameter(TemplateBuilderImpl.REPLACE_SSI_INCLUDE, "true");
        NekoHtmlNodeHandler handler =
            (NekoHtmlNodeHandler)builder.createContentHandler(new TemplateImpl(), "UTF-8");
        assertTrue(handler.isRemoveWhitespace());
        assertTrue(handler.isSSIIncludeReplacementEnabled());

        builder.setParameter(TemplateBuilderImpl.OUTPUT_TEMPLATE_WHITESPACE, "true");
        builder.setParameter(TemplateBuilderImpl.REPLACE_SSI_INCLUDE, "false");
        NekoHtmlNodeHandler handler2 =
            (NekoHtmlNodeHandler)builder.createContentHandler(new TemplateImpl(), "UTF-8");
        assertFalse(handler2.isRemoveWhitespace());
        assertFalse(handler2.isSSIIncludeReplacementEnabled());
    }

}
