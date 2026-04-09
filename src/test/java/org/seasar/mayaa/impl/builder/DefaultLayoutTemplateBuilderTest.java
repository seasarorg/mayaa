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
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.PageImpl;
import org.seasar.mayaa.impl.engine.TemplateImpl;
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
     * .mayaa ファイルなし（mayaaNode=null）かつ generateMayaaNode=true の場合、
     * Page はミューテートされず、template の dynamicSuperPagePath にデフォルトレイアウトが設定される。
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

        // Page はミューテートされない
        assertNull(SpecificationUtil.getMayaaNode(page));
        // Template に dynamicSuperPagePath が設定される
        assertEquals(VALID_SYSTEM_ID, template.getDynamicSuperPagePath());
    }

    /**
     * generateMayaaNode=false の場合、.mayaa ファイルがなくても dynamicSuperPagePath は設定されない。
     */
    @Test
    public void testSetupExtends_generateMayaaNodeFalse() {
        DefaultLayoutTemplateBuilder builder = new DefaultLayoutTemplateBuilder();
        builder.setParameter("generateMayaaNode", "false");
        builder.setParameter("defaultLayoutPageName", VALID_SYSTEM_ID);

        MockTemplateImpl template = new MockTemplateImpl();
        PageImpl page = new PageImpl();
        page.initialize("/test");
        template.setPage(page);
        template.initialize(page, "", "html");

        builder.setupExtends(template);

        assertNull(template.getDynamicSuperPagePath());
    }

    /**
     * .mayaa ファイルがあり m:extends が設定済みの場合、
     * dynamicSuperPagePath は設定されない（.mayaa 定義が優先される）。
     */
    @Test
    public void testSetupExtends_mayaaNodeWithExtends() {
        MockTemplateImpl template = new MockTemplateImpl();
        PageImpl page = new PageImpl();
        page.initialize("/test");
        template.setPage(page);
        template.initialize(page, "", "html");

        SpecificationNodeImpl mayaaNode = new SpecificationNodeImpl(CONST_IMPL.QM_MAYAA);
        mayaaNode.setSystemID("/test.mayaa");
        mayaaNode.addAttribute(CONST_IMPL.QM_EXTENDS, "/other-layout.html");
        page.addChildNode(mayaaNode);

        _builder.setupExtends(template);

        assertNull(template.getDynamicSuperPagePath());
    }

    /**
     * .mayaa ファイルはあるが m:extends が未設定の場合、デフォルトレイアウトが dynamicSuperPagePath に設定される。
     */
    @Test
    public void testSetupExtends_mayaaNodeWithoutExtends() {
        MockTemplateImpl template = new MockTemplateImpl();
        PageImpl page = new PageImpl();
        page.initialize("/test");
        template.setPage(page);
        template.initialize(page, "", "html");

        SpecificationNodeImpl mayaaNode = new SpecificationNodeImpl(CONST_IMPL.QM_MAYAA);
        mayaaNode.setSystemID("/test.mayaa");
        page.addChildNode(mayaaNode);

        _builder.setupExtends(template);

        assertEquals(VALID_SYSTEM_ID, template.getDynamicSuperPagePath());
    }

    /**
     * createMayaaNode は Mayaa 2.0 で廃止。{@link UnsupportedOperationException} がスローされる。
     */
    @Test
    public void testCreateMayaaNode_throwsUnsupported() {
        PageImpl page = new PageImpl();
        page.initialize("/test");
        assertThrows(UnsupportedOperationException.class,
                () -> _builder.createMayaaNode(page, null));
    }

    /**
     * {@link DefaultLayoutTemplateBuilder#getMayaaNode(Page)} は .mayaa ファイルに定義された
     * mayaaNode を返す。
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

     * addExtends は Mayaa 2.0 で廃止。{@link UnsupportedOperationException} がスローされる。
     */
    @Test
    public void testAddExtends_throwsUnsupported() {
        PageImpl page = new PageImpl();
        page.initialize("/test");
        SpecificationNodeImpl mayaaNode = new SpecificationNodeImpl(CONST_IMPL.QM_MAYAA);
        assertThrows(UnsupportedOperationException.class,
                () -> _builder.addExtends(page, mayaaNode));
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
