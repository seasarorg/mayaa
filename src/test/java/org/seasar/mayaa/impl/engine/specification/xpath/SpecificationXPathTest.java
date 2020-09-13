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
package org.seasar.mayaa.impl.engine.specification.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.XPath;
import org.jaxen.XPathFunctionContext;
import org.jaxen.pattern.Pattern;
import org.jaxen.pattern.PatternParser;
import org.jaxen.saxpath.SAXPathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationXPathTest implements CONST_IMPL {

    private QName _qName1;
    private QName _qName2;
    private SpecificationImpl _specification;
    private SpecificationNode _node1;
    private SpecificationNode _node2;
    private SpecificationNode _node3;
    private SpecificationNode _node4;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();

        _qName1 = SpecificationUtil.createQName("testName1");
        _qName2 = SpecificationUtil.createQName("testName2");

        _specification = new SpecificationImpl();

        // 1: qName1 & testID1
        _node1 = SpecificationUtil.createSpecificationNode(_qName1, "", 0, false, 0);
        _node1.addAttribute(QM_ID, "testID1");
        _specification.addChildNode(_node1);
        _node1.addPrefixMapping("m", URI_MAYAA);
        // 2: qName2 & testID1
        _node2 = SpecificationUtil.createSpecificationNode(_qName2, "", 0, false, 1);
        _node2.addAttribute(QM_ID, "testID1");
        _specification.addChildNode(_node2);
        _node2.addPrefixMapping("m", URI_MAYAA);
        // 3: qName1 & testID2
        _node3 = SpecificationUtil.createSpecificationNode(_qName1, "", 0, false, 2);
        _node3.addAttribute(QM_ID, "testID2");
        _specification.addChildNode(_node3);
        _node3.addPrefixMapping("m", URI_MAYAA);
        // 4: qName2 & testID2
        _node4 = SpecificationUtil.createSpecificationNode(_qName2, "", 0, false, 3);
        _node4.addAttribute(QM_ID, "testID2");
        _specification.addChildNode(_node4);
        _node4.addPrefixMapping("m", URI_MAYAA);
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testByNodeName() throws JaxenException {
        XPath xpath = null;
        List<?> list = null;
        Namespace namespaces = SpecificationUtil.createNamespace();
        namespaces.addPrefixMapping("m", URI_MAYAA);
        // testName1で検索
        xpath = SpecificationXPath.createXPath("/m:testName1", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals(_qName1, ((SpecificationNode) list.get(0)).getQName());
        assertEquals(_qName1, ((SpecificationNode) list.get(1)).getQName());

        // testName2で検索
        xpath = SpecificationXPath.createXPath("/m:testName2", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals(_qName2, ((SpecificationNode) list.get(0)).getQName());
        assertEquals(_qName2, ((SpecificationNode) list.get(1)).getQName());

        // testName3で検索
        xpath = SpecificationXPath.createXPath("/m:testName3", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(0, list.size());
    }

    @Test
    public void testByAttribute() throws JaxenException {
        XPath xpath = null;
        List<?> list = null;
        Namespace namespaces = SpecificationUtil.createNamespace();
        namespaces.addPrefixMapping("m", URI_MAYAA);
        // testID1で検索
        xpath = SpecificationXPath.createXPath("/*[@m:id='testID1']", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals("testID1", SpecificationUtil.getAttributeValue((SpecificationNode) list.get(0), QM_ID));
        assertEquals("testID1", SpecificationUtil.getAttributeValue((SpecificationNode) list.get(1), QM_ID));

        // testID2で検索
        xpath = SpecificationXPath.createXPath("/*[@m:id='testID2']", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals("testID2", SpecificationUtil.getAttributeValue((SpecificationNode) list.get(0), QM_ID));
        assertEquals("testID2", SpecificationUtil.getAttributeValue((SpecificationNode) list.get(1), QM_ID));

        // testID3で検索
        xpath = SpecificationXPath.createXPath("/*[@m:id='testID3']", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(0, list.size());
    }

    @Test
    public void testOr() throws JaxenException {
        XPath xpath = null;
        List<?> list = null;
        Namespace namespaces = SpecificationUtil.createNamespace();
        namespaces.addPrefixMapping("m", URI_MAYAA);
        // testName1&testID1 | testName2&testID2
        xpath = SpecificationXPath.createXPath("/m:testName1[@m:id='testID1'] | /m:testName2[@m:id='testID2']",
                namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals(_qName1, ((SpecificationNode) list.get(0)).getQName());
        assertEquals("testID1", SpecificationUtil.getAttributeValue((SpecificationNode) list.get(0), QM_ID));
        assertEquals(_qName2, ((SpecificationNode) list.get(1)).getQName());
        assertEquals("testID2", SpecificationUtil.getAttributeValue((SpecificationNode) list.get(1), QM_ID));
    }

    @Test
    public void testMatches() throws SAXPathException {
        Pattern pattern = null;
        SimpleNamespaceContext namespace = new SimpleNamespaceContext();
        namespace.addNamespace("m", URI_MAYAA.getValue());
        ContextSupport support = new ContextSupport(namespace, XPathFunctionContext.getInstance(),
                new SimpleVariableContext(), SpecificationNavigator.getInstance());
        Context context = new Context(support);
        pattern = PatternParser.parse("/m:testName1[@m:id='testID1']");
        assertTrue(pattern.matches(_node1, context));
        assertFalse(pattern.matches(_node2, context));
        assertFalse(pattern.matches(_node3, context));
        assertFalse(pattern.matches(_node4, context));

        pattern = PatternParser.parse("/m:testName2[@m:id='testID1']");
        assertFalse(pattern.matches(_node1, context));
        assertTrue(pattern.matches(_node2, context));
        assertFalse(pattern.matches(_node3, context));
        assertFalse(pattern.matches(_node4, context));

        pattern = PatternParser.parse("/m:testName1[@m:id='testID2']");
        assertFalse(pattern.matches(_node1, context));
        assertFalse(pattern.matches(_node2, context));
        assertTrue(pattern.matches(_node3, context));
        assertFalse(pattern.matches(_node4, context));

        pattern = PatternParser.parse("/m:testName2[@m:id='testID2']");
        assertFalse(pattern.matches(_node1, context));
        assertFalse(pattern.matches(_node2, context));
        assertFalse(pattern.matches(_node3, context));
        assertTrue(pattern.matches(_node4, context));

        pattern = PatternParser.parse("/*[@m:id='testID2']");
        assertFalse(pattern.matches(_node1, context));
        assertFalse(pattern.matches(_node2, context));
        assertTrue(pattern.matches(_node3, context));
        assertTrue(pattern.matches(_node4, context));
    }

}
