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

import junit.framework.TestCase;

import org.seasar.maya.impl.cycle.servlet.MockHttpServletRequest;
import org.seasar.maya.impl.cycle.servlet.MockServletContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebSessionTest extends TestCase {

    private MockServletContext _servletContext;
    private MockHttpServletRequest _httpServletRequest;
    private WebSession _session;
    
    public WebSessionTest(String name) {
        super(name);
    }

    protected void setUp() {
        _servletContext = new MockServletContext(this, "context");
        _httpServletRequest = new MockHttpServletRequest(_servletContext);
        _session = new WebSession();
        _session.setHttpServletRequest(_httpServletRequest);
    }
    
    public void testGetScopeName() {
        assertEquals("session", _session.getScopeName());
    }
    
    public void testGetAttribute() {
        _session.setAttribute("test", "test attr");
        assertEquals("test attr", _session.getAttribute("test"));
        _session.setAttribute("test", null);
        assertNull(_session.getAttribute("test"));
    }
    
}
