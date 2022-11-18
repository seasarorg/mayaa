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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.PageImpl;
import org.seasar.mayaa.impl.engine.TemplateImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.source.FileSourceDescriptor;
import org.seasar.mayaa.impl.source.PageSourceFactoryImpl;
import org.seasar.mayaa.source.PageSourceFactory;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class DefaultLayoutTemplateBuilderTest {

    private static final String VALID_SYSTEM_ID = "/defaultlayout.html";
    private static final String INVALID_SYSTEM_ID_1 = "defaultlayout.html";
    private static final String INVALID_SYSTEM_ID_2 = "/foo/../defaultlayout.html";
    private static final String SYSTEM_ID_SUFFIX = ".mayaa/(auto)";

    private DefaultLayoutTemplateBuilder _builder;
    
    @BeforeEach
    public void setUp() {
        FactoryFactory.setInstance(new ManualProviderFactory(this) {
            @Override
            protected PageSourceFactory getPageSourceFactory(Object context) {
                // PageSourceFactoryおよびPageSourceDescriptorを差し替える。
                return new MockPageSourceFactory();
            }
        });
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();

        _builder = new DefaultLayoutTemplateBuilder();
        _builder.setParameter("generateMayaaNode", "true");
        _builder.setParameter("defaultLayoutPageName", VALID_SYSTEM_ID);
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.builder.DefaultLayoutTemplateBuilder#setupExtends(org.seasar.mayaa.engine.Template)}.
     */
    @Test
    public void testSetupExtends() {
        MockTemplateImpl template = new MockTemplateImpl();

        PageImpl page = new PageImpl();
        page.initialize("/test");
        template.setPage(page);

        template.initialize(page, "", "html");

        assertNull(SpecificationUtil.getMayaaNode(page));

        _builder.setupExtends(template);

        SpecificationNode mayaaNode = SpecificationUtil.getMayaaNode(page);

        assertNotNull(mayaaNode);
        assertEquals(VALID_SYSTEM_ID,
                SpecificationUtil.getAttributeValue(mayaaNode, CONST_IMPL.QM_EXTENDS));
    }

    // TODO generateMayaaNode が false の場合のテスト
    // TODO mayaaがある場合のテスト
    // TODO defaultlaytout なら追加しないテスト

    /**
     * Test method for {@link org.seasar.mayaa.impl.builder.DefaultLayoutTemplateBuilder#createMayaaNode(org.seasar.mayaa.engine.Page, org.seasar.mayaa.engine.specification.Specification)}.
     */
    @Test
    public void testCreateMayaaNode() {
        PageImpl page = new PageImpl();
        page.initialize("/test");
        SpecificationImpl spec = new SpecificationImpl();

        SpecificationNode mayaaNode = _builder.createMayaaNode(page, spec);

        assertTrue(mayaaNode.getSystemID().endsWith(SYSTEM_ID_SUFFIX), mayaaNode.getSystemID());
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.builder.DefaultLayoutTemplateBuilder#createMayaaNode(org.seasar.mayaa.engine.Page, org.seasar.mayaa.engine.specification.Specification)}.
     */
    @Test
    public void testGetMayaaNode() {
        PageImpl page = new PageImpl();
        page.initialize("/test");
        SpecificationNodeImpl mayaaNode = new SpecificationNodeImpl(CONST_IMPL.QM_MAYAA);
        mayaaNode.setSystemID("/test.mayaa");
        page.addChildNode(mayaaNode);

        assertNotNull(SpecificationUtil.getMayaaNode(page));

        assertNotNull(_builder.getMayaaNode(page));

        assertNotNull(SpecificationUtil.getMayaaNode(page));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.builder.DefaultLayoutTemplateBuilder#createMayaaNode(org.seasar.mayaa.engine.Page, org.seasar.mayaa.engine.specification.Specification)}.
     */
    @Test
    public void testGetMayaaNode_auto() {
        PageImpl page = new PageImpl();
        page.initialize("/test");
        SpecificationImpl spec = new SpecificationImpl();
        page.addChildNode(_builder.createMayaaNode(page, spec));

        assertNotNull(SpecificationUtil.getMayaaNode(page));

        assertNull(_builder.getMayaaNode(page));

        assertNull(SpecificationUtil.getMayaaNode(page));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.builder.DefaultLayoutTemplateBuilder#addExtends(org.seasar.mayaa.engine.Page, org.seasar.mayaa.engine.specification.SpecificationNode)}.
     */
    @Test
    public void testAddExtends() {
        PageImpl page = new PageImpl();
        page.initialize("/test");
        SpecificationNodeImpl mayaaNode = new SpecificationNodeImpl(CONST_IMPL.QM_MAYAA);

        assertNull(SpecificationUtil.getAttributeValue(mayaaNode, CONST_IMPL.QM_EXTENDS));

        _builder.addExtends(page, mayaaNode);

        assertEquals(VALID_SYSTEM_ID,
                SpecificationUtil.getAttributeValue(mayaaNode, CONST_IMPL.QM_EXTENDS));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.builder.DefaultLayoutTemplateBuilder#validatePageName(java.lang.String)}.
     */
    @Test
    public void testValidatePageName() {
        // assertTrue(_builder.validatePageName(VALID_SYSTEM_ID));
        assertFalse(_builder.validatePageName(null));
        assertFalse(_builder.validatePageName(""));
        assertFalse(_builder.validatePageName("/foo/bar.html"), "存在しない");
        assertFalse(_builder.validatePageName(INVALID_SYSTEM_ID_1), "/はじまりでない");
        assertFalse(_builder.validatePageName(INVALID_SYSTEM_ID_2), "/../がある");
    }

    public static class MockPageSourceFactory extends PageSourceFactoryImpl {

        private static final long serialVersionUID = 1L;

        public SourceDescriptor getPageSource(String systemID) {
            return new MockSourceDescriptor(
                    VALID_SYSTEM_ID.equals(systemID) ||
                    INVALID_SYSTEM_ID_1.equals(systemID) ||
                    INVALID_SYSTEM_ID_2.equals(systemID));
        }

    }

    public static class MockSourceDescriptor extends FileSourceDescriptor {

        private boolean _exist;

        public MockSourceDescriptor(boolean exist) {
            _exist = exist;
        }

        public boolean exists() {
            return _exist;
        }

    }

    public static class MockTemplateImpl extends TemplateImpl {

        private static final long serialVersionUID = 1L;
        private Page _page;

        public Page getPage() {
            return _page;
        }

        public void setPage(Page page) {
            _page = page;
        }

    }

}
