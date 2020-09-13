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
package org.seasar.mayaa.impl.engine;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.processor.ElementProcessor;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author maruo_syunsuke
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateImplTest implements CONST_IMPL {

    private PageImpl _page;
    private TemplateImpl _template;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);

        _page = new PageImpl();
        _page.initialize("hello");
        _template = new TemplateImpl();
        _template.initialize(_page, "ja", "html");
        ElementProcessor html = new ElementProcessor();
        html.setName(SpecificationUtil.createPrefixAwareName(SpecificationUtil.createQName(URI_HTML, "html")));
        _template.addChildProcessor(html);
        ElementProcessor body = new ElementProcessor();
        body.setName(SpecificationUtil.createPrefixAwareName(SpecificationUtil.createQName(URI_HTML, "body")));
        html.addChildProcessor(body);

        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testGetSuffix() {
        assertEquals("ja", _template.getSuffix());
    }

    @Test
    public void testGetExtension() {
        assertEquals("html", _template.getExtension());
    }

    @Test
    public void testGetPage() {
        assertEquals(_page.getPageName(), _template.getPage().getPageName());
    }

}
