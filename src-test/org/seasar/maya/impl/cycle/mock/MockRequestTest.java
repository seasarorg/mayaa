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
package org.seasar.maya.impl.cycle.mock;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockRequestTest extends TestCase {

    public MockRequestTest(String name) {
        super(name);
    }

    public void testGetScopeName() {
        MockRequest request = new MockRequest();
        assertEquals("request", request.getScopeName());
    }
    
    public void testGetAttribute() {
        MockRequest request = new MockRequest();
        request.setAttribute("test", "test attr");
        assertEquals("test attr", request.getAttribute("test"));
        request.setAttribute("test", null);
        assertNull(request.getAttribute("test"));
    }
    
    public void testGetLocale() {
        Locale locale = new Locale("ja_JP");
        MockRequest request = new MockRequest();
        request.setLocale(locale);
        assertEquals(locale, request.getLocale());
    }
    
    public void testGetPath() {
        MockRequest request = new MockRequest();
        request.setPath("/index.html");
        assertEquals("/index.html", request.getPath());
    }

    public void testIterateParameterNames() {
        MockRequest request = new MockRequest();
        request.addParameter("test1", "");
        request.addParameter("test2", "");
        Iterator it = request.iterateParameterNames();
        it.next();
        it.next();
        assertFalse(it.hasNext());
    }
    
    public void testGetParameterMap() {
        MockRequest request = new MockRequest();
        Map params = request.getParameterMap();
        assertNotNull(params);
        assertEquals(0, params.size());
    }
    
    public void testGetParameters() {
        MockRequest request = new MockRequest();
        request.addParameter("test", "test param 0");
        request.addParameter("test", "test param 1");
        String[] params = request.getParameterValues("test");
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals("test param 0", params[0]);
        assertEquals("test param 1", params[1]);
    }
    
    public void testGetParameter() {
        MockRequest request = new MockRequest();
        request.addParameter("test", "test param");
        assertEquals("test param", request.getParameter("test"));
    }
    
}
