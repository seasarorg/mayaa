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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.seasar.maya.impl.cycle.servlet.MockHttpServletRequest;
import org.seasar.maya.impl.cycle.servlet.MockHttpServletResponse;
import org.seasar.maya.impl.cycle.servlet.MockServletContext;
import org.seasar.maya.impl.provider.factory.SimpleServiceProviderFactory;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ImplicitScopeTest extends TestCase {

    private MockHttpServletRequest _httpServletRequest;
    private ImplicitScope _scope;
    
    public ImplicitScopeTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        MockServletContext servletContext = new MockServletContext(this, "context");
        _httpServletRequest = new MockHttpServletRequest(servletContext);
        ServiceProviderFactory.setDefaultFactory(new SimpleServiceProviderFactory());
        ServiceProviderFactory.setServletContext(servletContext);
        ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
        provider.initialize(_httpServletRequest, new MockHttpServletResponse());
        _scope = new ImplicitScope();
    }
    
    public void testGetScopeName() {
        assertEquals("implicit", _scope.getScopeName());
    }
    
    public void testIterateAttributeNames() {
        Set set = new HashSet();
        for(Iterator it = _scope.iterateAttributeNames(); it.hasNext(); ) {
            set.add(it.next());
        }
        assertTrue(set.contains("serviceCycle"));
        assertTrue(set.contains("param"));
        assertTrue(set.contains("paramValues"));
        assertTrue(set.contains("header"));
        assertTrue(set.contains("headerValues"));
    }
    
    public void testSetAttribute() {
        try {
            _scope.setAttribute("test", "error");
            fail();
        } catch(ScopeNotWritableException e) {
        }
    }
    
    public void testGetAttribute1() {
        assertNotNull(_scope.getAttribute("serviceCycle"));
    }
    
    public void testGetAttribute2() {
        _httpServletRequest.addParameter("test", "test value");
        Map param = (Map)_scope.getAttribute("param");
        assertEquals("test value", param.get("test"));
    }

}
