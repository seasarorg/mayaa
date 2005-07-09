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
package org.seasar.maya.impl.cycle.local;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LocalRequestTest extends TestCase {

    public LocalRequestTest(String name) {
        super(name);
    }

    public void testGetScopeName() {
        LocalRequest request = new LocalRequest();
        assertEquals("request", request.getScopeName());
    }
    
    public void testGetAttribute() {
        LocalRequest request = new LocalRequest();
        request.setAttribute("test", "test attr");
        assertEquals("test attr", request.getAttribute("test"));
        request.setAttribute("test", null);
        assertNull(request.getAttribute("test"));
    }
    
    public void testGetLocale() {
        Locale locale = new Locale("ja_JP");
        LocalRequest request = new LocalRequest();
        request.setLocale(locale);
        assertEquals(locale, request.getLocale());
    }
    
    public void testGetPath() {
        LocalRequest request = new LocalRequest();
        request.setPath("/index.html");
        assertEquals("/index.html", request.getPath());
    }

    public void testIterateParameterNames() {
        LocalRequest request = new LocalRequest();
        request.addParameter("test1", "");
        request.addParameter("test2", "");
        Iterator it = request.iterateParameterNames();
        it.next();
        it.next();
        assertFalse(it.hasNext());
    }
    
    public void testGetParameterMap() {
        LocalRequest request = new LocalRequest();
        Map params = request.getParameterMap();
        assertNotNull(params);
        assertEquals(0, params.size());
    }
    
    public void testGetParameters() {
        LocalRequest request = new LocalRequest();
        request.addParameter("test", "test param 0");
        request.addParameter("test", "test param 1");
        String[] params = request.getParameterValues("test");
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals("test param 0", params[0]);
        assertEquals("test param 1", params[1]);
    }
    
    public void testGetParameter() {
        LocalRequest request = new LocalRequest();
        request.addParameter("test", "test param");
        assertEquals("test param", request.getParameter("test"));
    }
    
}
