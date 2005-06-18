/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.engine.specification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

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
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.xml.NullLocator;
import org.xml.sax.Locator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationXPathTest extends TestCase implements CONST_IMPL {
    
    private QName _qName1 = new QName("testName1");
    private QName _qName2 = new QName("testName2");
    private TestSpecification _specification;
    private SpecificationNodeImpl _node1;
    private SpecificationNodeImpl _node2;
    private SpecificationNodeImpl _node3;
    private SpecificationNodeImpl _node4;
    
    protected void setUp() {
        Locator locator = NullLocator.getInstance();
        _specification = new TestSpecification(null);
        // 1: qName1 & testID1
        _node1 = new SpecificationNodeImpl(_qName1, locator);
        _node1.addAttribute(QM_ID, "testID1");
        _specification.addChildNode(_node1);
        _node1.addNamespace("m", URI_MAYA);
        // 2: qName2 & testID1
        _node2 = new SpecificationNodeImpl(_qName2, locator);
        _node2.addAttribute(QM_ID, "testID1");
        _specification.addChildNode(_node2);
        _node2.addNamespace("m", URI_MAYA);
        // 3: qName1 & testID2
        _node3 = new SpecificationNodeImpl(_qName1, locator);
        _node3.addAttribute(QM_ID, "testID2");
        _specification.addChildNode(_node3);
        _node3.addNamespace("m", URI_MAYA);
        // 4: qName2 & testID2
        _node4 = new SpecificationNodeImpl(_qName2, locator);
        _node4.addAttribute(QM_ID, "testID2");
        _specification.addChildNode(_node4);
        _specification.addNamespace("m", URI_MAYA);
        _node4.addNamespace("m", URI_MAYA);
    }

    public void testByNodeName() throws JaxenException {
        XPath xpath = null;
        List list = null;
        Map namespaces = new HashMap();
        namespaces.put("m", URI_MAYA);
        // testName1Ç≈åüçı
        xpath = SpecificationXPath.createXPath("/m:testName1", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals(_qName1, ((SpecificationNode)list.get(0)).getQName());
        assertEquals(_qName1, ((SpecificationNode)list.get(1)).getQName());
        
        // testName2Ç≈åüçı
        xpath = SpecificationXPath.createXPath("/m:testName2", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals(_qName2, ((SpecificationNode)list.get(0)).getQName());
        assertEquals(_qName2, ((SpecificationNode)list.get(1)).getQName());
        
        // testName3Ç≈åüçı
        xpath = SpecificationXPath.createXPath("/m:testName3", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(0, list.size());
    }
    
    public void testByAttribute() throws JaxenException {
        XPath xpath = null;
        List list = null;
        Map namespaces = new HashMap();
        namespaces.put("m", URI_MAYA);
        // testID1Ç≈åüçı
        xpath = SpecificationXPath.createXPath("/*[@m:id='testID1']", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals("testID1", SpecificationUtil.getAttributeValue(
                (SpecificationNode)list.get(0), QM_ID));
        assertEquals("testID1", SpecificationUtil.getAttributeValue(
                (SpecificationNode)list.get(1), QM_ID));
        
        // testID2Ç≈åüçı
        xpath = SpecificationXPath.createXPath("/*[@m:id='testID2']", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals("testID2", SpecificationUtil.getAttributeValue(
                (SpecificationNode)list.get(0), QM_ID));
        assertEquals("testID2", SpecificationUtil.getAttributeValue(
                (SpecificationNode)list.get(1), QM_ID));
        
        // testID3Ç≈åüçı
        xpath = SpecificationXPath.createXPath("/*[@m:id='testID3']", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(0, list.size());
    }
    
    public void testOr() throws JaxenException {
        XPath xpath = null;
        List list = null;
        Map namespaces = new HashMap();
        namespaces.put("m", URI_MAYA);
        // testName1&testID1 | testName2&testID2
        xpath = SpecificationXPath.createXPath(
                "/m:testName1[@m:id='testID1'] | /m:testName2[@m:id='testID2']", namespaces);
        list = xpath.selectNodes(_specification);
        assertEquals(2, list.size());
        assertEquals(_qName1, ((SpecificationNode)list.get(0)).getQName());
        assertEquals("testID1", SpecificationUtil.getAttributeValue(
                (SpecificationNode)list.get(0), QM_ID));
        assertEquals(_qName2, ((SpecificationNode)list.get(1)).getQName());
        assertEquals("testID2", SpecificationUtil.getAttributeValue(
                (SpecificationNode)list.get(1), QM_ID));
    }
    
    public void testMatches() throws SAXPathException {
        Pattern pattern = null;
        SimpleNamespaceContext namespace = new SimpleNamespaceContext();
        namespace.addNamespace("m", URI_MAYA);
        ContextSupport support = new ContextSupport(
                namespace,
                XPathFunctionContext.getInstance(),
                new SimpleVariableContext(),
                SpecificationNavigator.getInstance());
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
