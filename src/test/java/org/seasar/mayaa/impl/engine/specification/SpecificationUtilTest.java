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
package org.seasar.mayaa.impl.engine.specification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.engine.PageImpl;
import org.seasar.mayaa.impl.engine.TemplateImpl;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.test.util.ManualProviderFactory;

public class SpecificationUtilTest {

    private PageImpl _page;
    private TemplateImpl _template;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);

        _page = new PageImpl();

        _page.setSource(SourceUtil.getSourceDescriptor("/tests/page.mayaa"));
        _page.initialize("/page");
        _page.build();

        _template = new TemplateImpl();
        _template.setSource(SourceUtil.getSourceDescriptor("/tests/page.html"));
        _template.initialize(_page, "ja", "html");
        _template.build();

    }

    /*
     * Test method for
     * 'org.seasar.mayaa.impl.engine.specification.SpecificationUtil.parseQName(
     * String)'
     */
    @Test
    public void testParseQName() {
        QName qName1 = SpecificationUtil.parseQName("{http://mayaa.seasar.org}id");
        assertEquals("http://mayaa.seasar.org", qName1.getNamespaceURI().getValue());
        assertEquals("id", qName1.getLocalName());

        QName qName2 = SpecificationUtil.parseQName("{ http://mayaa.seasar.org } id ");
        assertEquals("http://mayaa.seasar.org", qName2.getNamespaceURI().getValue());
        assertEquals("id", qName2.getLocalName());

        try {
            SpecificationUtil.parseQName("");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("", e.getMessage());
        }

        try {
            SpecificationUtil.parseQName("{foobar");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("{foobar", e.getMessage());
        }

        try {
            SpecificationUtil.parseQName("{foo}  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("{foo}  ", e.getMessage());
        }

        try {
            SpecificationUtil.parseQName("bar}id");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("bar}id", e.getMessage());
        }
    }

    @Test
    public void testSerializeTemplate() throws URISyntaxException {
        File cacheDir = new File(getClass().getResource("WEB-INF").toURI());
        File expectedFile = new File(cacheDir, "tests#page.html.ser");
        assertFalse(_template.isDeprecated());

        SpecificationUtil.serialize(_template, cacheDir);

        // シリアル化したファイルが存在する。
        assertTrue(expectedFile.exists());

        Specification actual = SpecificationUtil.deserialize(_template.getSystemID(), cacheDir);

        assertTrue("Instance must be of Page", actual instanceof Template);
        assertEquals(_template, actual);

        assertFalse(actual.isDeprecated());

        expectedFile.delete();
    }

    @Test
    public void testSerializePage() throws URISyntaxException {
        File cacheDir = new File(getClass().getResource("WEB-INF").toURI());
        File expectedFile = new File(cacheDir, "tests#page.mayaa.ser");
        assertFalse(_page.isDeprecated());

        SpecificationUtil.serialize(_page, cacheDir);

        // シリアル化したファイルが存在する。
        assertTrue(expectedFile.exists());

        Specification actual = SpecificationUtil.deserialize(_page.getSystemID(), cacheDir);

        assertTrue("Instance must be of Page", actual instanceof Page);
        assertEquals(_page, actual);

        assertFalse(actual.isDeprecated());

        expectedFile.delete();
    }

}
