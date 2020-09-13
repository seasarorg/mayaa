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
package org.seasar.mayaa.impl.cycle.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.test.mock.MockHttpServletRequest;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class RequestScopeImplTest {

    private MockHttpServletRequest _httpServletRequest;
    private RequestScopeImpl _request;

    @Before
    public void setUp() {
        ManualProviderFactory.setUp(this);
        _httpServletRequest = ManualProviderFactory.HTTP_SERVLET_REQUEST;
        _request = (RequestScopeImpl) CycleUtil.getRequestScope();
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testParsePath() {
        // よくあるパターン
        _request.parsePath("/index.html");
        assertEquals("/index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("html", _request.getExtension());

        // 接尾辞を強制的に指定されているパターン
        _request.parsePath("/index$ja.html");
        assertEquals("/index", _request.getPageName());
        assertEquals("ja", _request.getRequestedSuffix());
        assertEquals("html", _request.getExtension());

        // フォルダ階層があるパターン
        _request.parsePath("/folder/index.html");
        assertEquals("/folder/index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("html", _request.getExtension());

        // 拡張子がないパターン
        _request.parsePath("/index");
        assertEquals("/index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("", _request.getExtension());

        // ファイル名の先頭がドットのパターン
        _request.parsePath("/.index");
        assertEquals("/.index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("", _request.getExtension());

        // ファイル名の先頭がアンダースコアのパターン
        _request.parsePath("/_index.html");
        assertEquals("/_index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("html", _request.getExtension());
    }

    @Test
    public void testGetScopeName() {
        assertEquals("request", _request.getScopeName());
    }

    @Test
    public void testGetAttribute() {
        _request.setAttribute("test", "test attr");
        assertEquals("test attr", _request.getAttribute("test"));
        _request.setAttribute("test", null);
        assertNull(_request.getAttribute("test"));
    }

    @Test
    public void testGetLocale() {
        Locale locale = new Locale("ja_JP");
        _httpServletRequest.addLocale(locale);
        assertEquals(locale, _request.getLocales()[0]);
    }

    @Test
    public void testGetPath() {
        _httpServletRequest.setPathInfo("/index.html");
        assertEquals("/index.html", _request.getRequestedPath());
    }

    @Test
    public void testGetParaValues() {
        _httpServletRequest.addParameter("test", "test param 0");
        _httpServletRequest.addParameter("test", "test param 1");
        String[] params = (String[])_request.getParamValues().getAttribute("test");
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals("test param 0", params[0]);
        assertEquals("test param 1", params[1]);
    }

}
