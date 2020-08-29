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

import static org.junit.Assert.assertTrue;

import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.XPathFunctionContext;
import org.jaxen.pattern.Pattern;
import org.jaxen.pattern.PatternParser;
import org.jaxen.saxpath.SAXPathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNavigatorTest implements CONST_IMPL {

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testHtmlMatches() throws SAXPathException {
        Pattern pattern = null;
        ContextSupport support = new ContextSupport(new SimpleNamespaceContext(), XPathFunctionContext.getInstance(),
                new SimpleVariableContext(), SpecificationNavigator.getInstance());
        Context context = new Context(support);
        pattern = PatternParser.parse("@class='box'");

        SpecificationNode node = SpecificationUtil
                .createSpecificationNode(SpecificationUtil.createQName(URI_HTML, "html"), "", 0, false, 0);
        node.addPrefixMapping("", URI_HTML);
        node.addAttribute(SpecificationUtil.createQName(URI_HTML, "class"), "box");

        assertTrue(pattern.matches(node, context));
    }

}
