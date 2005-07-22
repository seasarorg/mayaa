/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.cycle.web;

import java.util.Iterator;
import java.util.Locale;

import junit.framework.TestCase;

import org.seasar.maya.impl.cycle.servlet.MockHttpServletRequest;
import org.seasar.maya.impl.cycle.servlet.MockServletContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebRequestTest extends TestCase {

    private MockServletContext _servletContext;
    private MockHttpServletRequest _httpServletRequest;
    private WebRequest _request;
     
    public WebRequestTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        _servletContext = new MockServletContext(this, "context");
        _httpServletRequest = new MockHttpServletRequest(_servletContext);
        _request = new WebRequest("$");
        _request.setHttpServletRequest(_httpServletRequest);
    }
    public void testGetRequestedPageInfo() {
        // よくあるパターン
        _request.setRequestedPageInfo("/index.html", "$");
        assertEquals("/index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("html", _request.getExtension());
        
        // 接尾辞を強制的に指定されているパターン
        _request.setRequestedPageInfo("/index$ja.html", "$");
        assertEquals("/index", _request.getPageName());
        assertEquals("ja", _request.getRequestedSuffix());
        assertEquals("html", _request.getExtension());

        // フォルダ階層があるパターン
        _request.setRequestedPageInfo("/folder/index.html", "$");
        assertEquals("/folder/index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("html", _request.getExtension());
        
        // 拡張子がないパターン
        _request.setRequestedPageInfo("/index", "$");
        assertEquals("/index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("", _request.getExtension());
        
        // ファイル名の先頭がドットのパターン
        _request.setRequestedPageInfo("/.index", "$");
        assertEquals("/.index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("", _request.getExtension());

        // ファイル名の先頭がアンダースコアのパターン
        _request.setRequestedPageInfo("/_index.html", "$");
        assertEquals("/_index", _request.getPageName());
        assertEquals("", _request.getRequestedSuffix());
        assertEquals("html", _request.getExtension());
    }

    public void testGetScopeName() {
        assertEquals("request", _request.getScopeName());
    }
    
    public void testGetSession() {
        assertNotNull(_request.getSession());
    }
    
    public void testGetAttribute() {
        _request.setAttribute("test", "test attr");
        assertEquals("test attr", _request.getAttribute("test"));
        _request.setAttribute("test", null);
        assertNull(_request.getAttribute("test"));
    }
    
    public void testGetLocale() {
        Locale locale = new Locale("ja_JP");
        _httpServletRequest.addLocale(locale);
        assertEquals(locale, _request.getLocales()[0]);
    }
    
    public void testGetPath() {
        _httpServletRequest.setPathInfo("/index.html");
        assertEquals("/index.html", _request.getHttpRequestPath());
    }

    public void testIterateParameterNames() {
        _httpServletRequest.addParameter("test1", "");
        _httpServletRequest.addParameter("test2", "");
        Iterator it = _request.iterateParameterNames();
        it.next();
        it.next();
        assertFalse(it.hasNext());
    }

    public void testGetParameters() {
        _httpServletRequest.addParameter("test", "test param 0");
        _httpServletRequest.addParameter("test", "test param 1");
        String[] params = _request.getParameterValues("test");
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals("test param 0", params[0]);
        assertEquals("test param 1", params[1]);
    }

}
