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

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.impl.cycle.servlet.MockHttpServletRequest;
import org.seasar.maya.impl.cycle.servlet.MockHttpServletResponse;
import org.seasar.maya.impl.cycle.servlet.MockServletContext;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebServiceCycleTest extends TestCase {

    private WebServiceCycle _cycle;

    public WebServiceCycleTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        MockServletContext servletContext = new MockServletContext(this, "context");
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest(servletContext);
        WebApplication application = new WebApplication();
        application.setServletContext(servletContext);
        WebRequest request = new WebRequest("$");
        request.setHttpServletRequest(httpServletRequest);
        WebResponse response = new WebResponse();
        response.setHttpServletResponse(new MockHttpServletResponse());
        _cycle = new WebServiceCycle(application);
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
    
    public void testForward() {
        // TODO テスト
    }
    
    public void testRedirect() {
        // TODO テスト
    }
    
}
