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
package org.seasar.maya.impl.cycle;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.impl.cycle.servlet.MockHttpServletRequest;
import org.seasar.maya.impl.cycle.servlet.MockHttpServletResponse;
import org.seasar.maya.impl.cycle.servlet.MockServletContext;
import org.seasar.maya.impl.cycle.web.WebApplication;
import org.seasar.maya.impl.cycle.web.WebRequest;
import org.seasar.maya.impl.cycle.web.WebResponse;
import org.seasar.maya.impl.cycle.web.WebSession;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServiceCycleImplTest extends TestCase {

    private ServiceCycleImpl _cycle;
    
    public ServiceCycleImplTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        MockServletContext servletContext = new MockServletContext(this, "context");
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest(servletContext);
        WebApplication application = new WebApplication();
        application.setServletContext(servletContext);
        WebSession session = new WebSession();
        session.setHttpServletRequest(httpServletRequest);
        WebRequest request = new WebRequest();
        request.setHttpServletRequest(httpServletRequest);
        WebResponse response = new WebResponse();
        response.setHttpServletResponse(new MockHttpServletResponse());
        _cycle = new ServiceCycleImpl();
        _cycle.setApplication(application);
        _cycle.setRequest(request);
        _cycle.setResponse(response);
    }
    
    public void testGetApplication() {
        assertNotNull(_cycle.getApplication());
    }
    
    public void testGetRequest() {
        assertNotNull(_cycle.getRequest());
    }
    
    public void testGetResponse() {
        assertNotNull(_cycle.getResponse());
    }
    
    public void testGetScopeName() {
        assertEquals("page", _cycle.getScopeName());
    }

    public void testGetImplicitScope() {
        AttributeScope scope = _cycle.getAttributeScope("implicit");
        assertEquals("implicit", scope.getScopeName());
    }
    
    public void testGetApplicationScope() {
        AttributeScope scope = _cycle.getAttributeScope("application");
        assertEquals("application", scope.getScopeName());
    }

    public void testGetSessionScope() {
        AttributeScope scope = _cycle.getAttributeScope("session");
        assertEquals("session", scope.getScopeName());
    }
    
    public void testGetRequestScope() {
        AttributeScope scope = _cycle.getAttributeScope("request");
        assertEquals("request", scope.getScopeName());
    }

    public void testGetPageScope() {
        AttributeScope scope = _cycle.getAttributeScope("page");
        assertEquals("page", scope.getScopeName());
    }
    
    public void testGetApplicationAttribute() {
        _cycle.setAttribute("test", "test attr", "application");
        assertEquals("test attr", _cycle.getAttribute("test", "application"));
    }
    
    public void testGetSessionAttribute() {
        _cycle.setAttribute("test", "test attr", "session");
        assertEquals("test attr", _cycle.getAttribute("test", "session"));
    }
    
    public void testGetRequestAttribute() {
        _cycle.setAttribute("test", "test attr", "request");
        assertEquals("test attr", _cycle.getAttribute("test", "request"));
    }
    
    public void testGetPageAttribute() {
        _cycle.setAttribute("test", "test attr", "page");
        assertEquals("test attr", _cycle.getAttribute("test", "page"));
        assertEquals("test attr", _cycle.getAttribute("test"));
    }
    
    public void testGetAttribute() {
        _cycle.setAttribute("test", "test attr");
        assertEquals("test attr", _cycle.getAttribute("test"));
        assertEquals("test attr", _cycle.getAttribute("test", "page"));
    }

}
