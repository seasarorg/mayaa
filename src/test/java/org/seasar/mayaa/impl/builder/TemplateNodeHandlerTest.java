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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.TemplateImpl;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.mayaa.impl.engine.specification.URIImpl;
import org.seasar.mayaa.test.util.ManualProviderFactory;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Tests for {@link org.seasar.mayaa.impl.builder.TemplateNodeHandler}
 * @author Koji SUGA (Gluegent, Inc.)
 */
public class TemplateNodeHandlerTest {

    @BeforeEach
    public void setUp() throws Exception {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    protected TemplateNodeHandler createHandler(String path) {
        TemplateImpl template = new TemplateImpl();
        TemplateNodeHandler handler = new TemplateNodeHandler(template);
        handler.setSSIIncludeReplacementEnabled(true);

        LocatorImpl locator = new LocatorImpl();
        locator.setSystemId(path);
        locator.setPublicId(path);
        locator.setLineNumber(10);
        locator.setColumnNumber(20);
        handler.setDocumentLocator(locator);

        handler.startDocument();

        SpecificationNodeImpl parent = new SpecificationNodeImpl(QNameImpl.getInstance(CONST_IMPL.URI_HTML, "div"));
        parent.setDefaultNamespaceURI(CONST_IMPL.URI_HTML);
        template.addChildNode(parent);

        handler.setCurrentNode(parent);
        return handler;
    }

    @Test
    public void testIncludeToInsert() throws Throwable {
        String path = "/common/include.html";
        String comment = "#include virtual=\"" + path + "\"";
        TemplateNodeHandler handler = createHandler(path);

        handler.comment(comment.toCharArray(), 0, comment.length());

        NodeTreeWalker node = handler.getCurrentNode().getChildNode(0);
        assertTrue(node instanceof SpecificationNodeImpl);
        SpecificationNodeImpl spec = (SpecificationNodeImpl) node;

        assertEquals(CONST_IMPL.URI_HTML, spec.getQName().getNamespaceURI());
        assertEquals("span", spec.getQName().getLocalName());
        assertEquals("/common/include.html", spec.getAttribute(QNameImpl.getInstance("path")).getValue());

        assertNull(spec.getAttribute(QNameImpl.getInstance("name")));
    }

    @Test
    public void testIncludeToInsert_file() throws Throwable {
        String path = "include.html";
        String comment = "#include file=\"" + path + "\"";
        TemplateNodeHandler handler = createHandler(path);

        handler.comment(comment.toCharArray(), 0, comment.length());

        NodeTreeWalker node = handler.getCurrentNode().getChildNode(0);
        assertTrue(node instanceof SpecificationNodeImpl);
        SpecificationNodeImpl spec = (SpecificationNodeImpl) node;

        assertEquals(CONST_IMPL.URI_HTML, spec.getQName().getNamespaceURI());
        assertEquals("span", spec.getQName().getLocalName());
        assertEquals("include.html", spec.getAttribute(QNameImpl.getInstance("path")).getValue());

        assertNull(spec.getAttribute(QNameImpl.getInstance("name")));
    }

    @Test
    public void testIncludeToInsert_query_and_fragment() throws Throwable {
        String path = "/common/include.html";
        String comment = "#include virtual=\"" + path + "?foo=bar&amp;bar=baz#fragment\"";
        TemplateNodeHandler handler = createHandler(path);

        handler.comment(comment.toCharArray(), 0, comment.length());

        NodeTreeWalker node = handler.getCurrentNode().getChildNode(0);
        assertTrue(node instanceof SpecificationNodeImpl);
        SpecificationNodeImpl spec = (SpecificationNodeImpl) node;

        assertEquals(CONST_IMPL.URI_HTML, spec.getQName().getNamespaceURI());
        assertEquals("span", spec.getQName().getLocalName());
        assertEquals("/common/include.html", spec.getAttribute(QNameImpl.getInstance("path")).getValue());
        assertEquals("fragment", spec.getAttribute(QNameImpl.getInstance("name")).getValue());

        URI parameterURI = URIImpl.getInstance(TemplateNodeHandler.AUTO_INSERT_NAMESPACE);
        assertEquals("bar", spec.getAttribute(QNameImpl.getInstance(parameterURI, "foo")).getValue());
        assertEquals("baz", spec.getAttribute(QNameImpl.getInstance(parameterURI, "bar")).getValue());
    }

    @Test
    public void testIncludeToInsert_not_include() throws Throwable {
        String path = "/common/include.html";
        String comment = "#includ virtual=\"" + path + "\"";
        TemplateNodeHandler handler = createHandler(path);

        handler.comment(comment.toCharArray(), 0, comment.length());

        NodeTreeWalker node = handler.getCurrentNode().getChildNode(0);
        assertTrue(node instanceof SpecificationNodeImpl);
        SpecificationNodeImpl spec = (SpecificationNodeImpl) node;

        assertEquals(CONST_IMPL.URI_MAYAA, spec.getQName().getNamespaceURI());
        assertEquals("comment", spec.getQName().getLocalName());
    }

    @Test
    public void testIncludeToInsert_disabled() throws Throwable {
        String path = "/common/include.html";
        String comment = "#include virtual=\"" + path + "\"";
        TemplateNodeHandler handler = createHandler(path);
        handler.setSSIIncludeReplacementEnabled(false);

        handler.comment(comment.toCharArray(), 0, comment.length());

        NodeTreeWalker node = handler.getCurrentNode().getChildNode(0);
        assertTrue(node instanceof SpecificationNodeImpl);
        SpecificationNodeImpl spec = (SpecificationNodeImpl) node;

        assertEquals(CONST_IMPL.URI_MAYAA, spec.getQName().getNamespaceURI());
        assertEquals("comment", spec.getQName().getLocalName());
    }

}
