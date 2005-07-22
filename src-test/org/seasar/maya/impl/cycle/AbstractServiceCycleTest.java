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

import junit.framework.TestCase;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.impl.cycle.servlet.MockServletContext;
import org.seasar.maya.impl.cycle.web.WebApplication;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class AbstractServiceCycleTest extends TestCase {

    private AbstractServiceCycle _cycle;
    
    public AbstractServiceCycleTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        MockServletContext servletContext = new MockServletContext(this, "context");
        WebApplication application = new WebApplication();
        application.setServletContext(servletContext);
        _cycle = new TestServiceCycle(application);
    }
    
    public void testGetScopeName() {
        assertEquals("page", _cycle.getScopeName());
    }

    public void testGetImplicitScope() {
        AttributeScope scope = _cycle.getAttributeScope("implicit");
        assertEquals("implicit", scope.getScopeName());
    }

    public void testGetPageScope() {
        AttributeScope scope = _cycle.getAttributeScope("page");
        assertEquals("page", scope.getScopeName());
    }
    
    public void testGetAttribute() {
        _cycle.setAttribute("test", "test attr");
        assertEquals("test attr", _cycle.getAttribute("test"));
    }

}
