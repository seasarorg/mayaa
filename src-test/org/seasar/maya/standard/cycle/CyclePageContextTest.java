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
package org.seasar.maya.standard.cycle;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.impl.cycle.ServiceCycleImpl;
import org.seasar.maya.impl.cycle.local.LocalApplication;
import org.seasar.maya.impl.cycle.local.LocalRequest;
import org.seasar.maya.impl.cycle.local.LocalResponse;
import org.seasar.maya.impl.cycle.local.LocalSession;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CyclePageContextTest extends TestCase {

    private ServiceCycleImpl _cycle;
    private CyclePageContext _pageContext;
    
    public CyclePageContextTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        _cycle = new ServiceCycleImpl();
        _cycle.setApplication(new LocalApplication());
        _cycle.setSession(new LocalSession());
        _cycle.setRequest(new LocalRequest());
        _cycle.setResponse(new LocalResponse());
        _pageContext = new CyclePageContext(_cycle, null);
    }
    
    public void testGetRequest() {
        assertNotNull(_pageContext.getRequest());
    }
    
    public void testGetResponse() {
        assertNotNull(_pageContext.getResponse());
    }
    
    public void testGetServletConfig() {
        assertNotNull(_pageContext.getServletConfig());
    }
    
    public void testGetServletContext() {
        assertNotNull((_pageContext.getServletContext()));
    }
    
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
