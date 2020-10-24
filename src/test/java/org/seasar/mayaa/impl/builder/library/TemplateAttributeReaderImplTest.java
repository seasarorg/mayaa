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
package org.seasar.mayaa.impl.builder.library;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TemplateAttributeReaderImplTest {

    @Before
    public void setUp() throws Exception {
        _reader = new TemplateAttributeReaderImpl();
        _tagName = QNameImpl.getInstance(
                SpecificationUtil.createURI("http://mayaa.seasar.org"), "test");
        _attributeName = "value";
    }

    private TemplateAttributeReaderImpl _reader;
    private QName _tagName;
    private String _attributeName;

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.
     * TemplateAttributeReaderImpl.addIgnoreAttribute(String, String)'
     */
    @Test
    public void testAddIgnoreAttribute() {
        try {
            _reader.addIgnoreAttribute(_tagName.toString(), _attributeName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            fail("1");
        }

        try {
            _reader.addIgnoreAttribute("test", _attributeName);
            fail("2");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addIgnoreAttribute("{test", _attributeName);
            fail("3");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addIgnoreAttribute("}test", _attributeName);
            fail("4");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addIgnoreAttribute("t{est}test", _attributeName);
            fail("5");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addIgnoreAttribute("t{est}}test", _attributeName);
            fail("6");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addIgnoreAttribute("t{{est}test", _attributeName);
            fail("7");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.
     * TemplateAttributeReaderImpl.addAliasAttribute(String, String, String)'
     */
    @Test
    public void testAddAliasAttribute1() {
        try {
            _reader.addAliasAttribute(
                    _tagName.toString(), _attributeName, "test");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            fail("1");
        }

        try {
            _reader.addAliasAttribute("test", _attributeName, "test");
            fail("2");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute("{test", _attributeName, "test");
            fail("3");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute("}test", _attributeName, "test");
            fail("4");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute("t{est}test", _attributeName, "test");
            fail("5");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute("t{est}}test", _attributeName, "test");
            fail("6");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute("t{{est}test", _attributeName, "test");
            fail("7");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.
     * TemplateAttributeReaderImpl.addAliasAttribute(String, String, String)'
     */
    @Test
    public void testAddAliasAttribute2() {
        try {
            _reader.addAliasAttribute(_tagName.toString(), _attributeName,
                    "{http://mayaa.seasar.org}test");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            fail("1");
        }

        try {
            _reader.addAliasAttribute(_tagName.toString(), _attributeName,
                    "{test");
            fail("2");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute(_tagName.toString(), _attributeName,
                    "}test");
            fail("3");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute(_tagName.toString(), _attributeName,
                    "t{es}t");
            fail("4");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute(_tagName.toString(), _attributeName,
                    "{tes}}t");
            fail("5");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            _reader.addAliasAttribute(_tagName.toString(), _attributeName,
                    "{{tes}t");
            fail("6");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

}
