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
package org.seasar.mayaa.impl.cycle.jsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.jsp.PageContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageContextImplTest {

    private PageContextImpl _pageContext;

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
        ManualProviderFactory.SCRIPT_ENVIRONMENT.startScope( null);
        _pageContext = new PageContextImpl();
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testGetRequest() {
        assertNotNull(_pageContext.getRequest());
    }

    @Test
    public void testGetResponse() {
        assertNotNull(_pageContext.getResponse());
    }

    @Test
    public void testGetServletConfig() {
        assertNotNull(_pageContext.getServletConfig());
    }

    @Test
    public void testGetServletContext() {
        assertNotNull((_pageContext.getServletContext()));
    }

    @Test
    public void testGetAttribute() {
        _pageContext.setAttribute("test1", "test attr1", PageContext.APPLICATION_SCOPE);
        _pageContext.setAttribute("test2", "test attr2", PageContext.SESSION_SCOPE);
        _pageContext.setAttribute("test3", "test attr3", PageContext.REQUEST_SCOPE);
        _pageContext.setAttribute("test4", "test attr4", PageContext.PAGE_SCOPE);
        assertEquals("test attr1", _pageContext.getAttribute("test1", PageContext.APPLICATION_SCOPE));
        assertEquals("test attr2", _pageContext.getAttribute("test2", PageContext.SESSION_SCOPE));
        assertEquals("test attr3", _pageContext.getAttribute("test3", PageContext.REQUEST_SCOPE));
        assertEquals("test attr4", _pageContext.getAttribute("test4", PageContext.PAGE_SCOPE));
    }

    @Test
    public void testFindAttribute() {
        _pageContext.setAttribute("test1", "test attr1", PageContext.APPLICATION_SCOPE);
        _pageContext.setAttribute("test2", "test attr2", PageContext.SESSION_SCOPE);
        _pageContext.setAttribute("test3", "test attr3", PageContext.REQUEST_SCOPE);
        _pageContext.setAttribute("test4", "test attr4", PageContext.PAGE_SCOPE);
        assertEquals("test attr1", _pageContext.findAttribute("test1"));
        assertEquals("test attr2", _pageContext.findAttribute("test2"));
        assertEquals("test attr3", _pageContext.findAttribute("test3"));
        assertEquals("test attr4", _pageContext.findAttribute("test4"));
    }

    @Test
    public void testGetAttributeNamesInScope() {
        _pageContext.setAttribute("test1", "test attr1", PageContext.APPLICATION_SCOPE);
        _pageContext.setAttribute("test2", "test attr2", PageContext.SESSION_SCOPE);
        _pageContext.setAttribute("test3", "test attr3", PageContext.REQUEST_SCOPE);
        _pageContext.setAttribute("test4", "test attr4", PageContext.PAGE_SCOPE);
        assertEquals(PageContext.APPLICATION_SCOPE, _pageContext.getAttributesScope("test1"));
        assertEquals(PageContext.SESSION_SCOPE, _pageContext.getAttributesScope("test2"));
        assertEquals(PageContext.REQUEST_SCOPE, _pageContext.getAttributesScope("test3"));
        assertEquals(PageContext.PAGE_SCOPE, _pageContext.getAttributesScope("test4"));
    }

}
