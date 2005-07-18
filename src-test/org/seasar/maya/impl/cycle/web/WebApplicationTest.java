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

import org.seasar.maya.impl.cycle.servlet.MockServletContext;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebApplicationTest extends TestCase {

    private MockServletContext _servletContext;
    private WebApplication _application;
    
    public WebApplicationTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        _servletContext = new MockServletContext(this, "context");
        _application = new WebApplication();
        _application.setServletContext(_servletContext);
    }
    
    public void testGetScopeName() {
        assertEquals("application", _application.getScopeName());
    }
    
    public void testGetAttribute() {
        _application.setAttribute("test", "test attr");
        assertEquals("test attr", _application.getAttribute("test"));
        _application.setAttribute("test", null);
        assertNull(_application.getAttribute("test"));
    }
    
}
