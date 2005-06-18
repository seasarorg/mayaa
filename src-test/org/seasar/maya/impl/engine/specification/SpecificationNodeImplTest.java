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
package org.seasar.maya.impl.engine.specification;

import java.util.Iterator;

import junit.framework.TestCase;

import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.QName;
import org.xml.sax.helpers.LocatorImpl;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNodeImplTest extends TestCase {
    
    private SpecificationNodeImpl _node;
    
    protected void setUp() {
        LocatorImpl locator = new LocatorImpl();
        locator.setColumnNumber(5);
        locator.setLineNumber(6);
        locator.setPublicId("PUBLIC_ID");
        locator.setSystemId("SYSTEM_ID");
        _node = new SpecificationNodeImpl(new QName("NODE_TEST"), locator);
        _node.addAttribute(new QName("ATTR_TEST1"), "VALUE1");
        _node.addAttribute(new QName("ATTR_TEST2"), "VALUE2");
        _node.addAttribute(new QName("ATTR_TEST2"), "VALUE2");
    }
    
    public void testGetAttribute() {
        NodeAttribute attr = _node.getAttribute(new QName("ATTR_TEST1"));
        assertNotNull(attr);
        assertEquals("VALUE1", attr.getValue());
    }
    
    public void testIterateAttributes() {
        Iterator it = _node.iterateAttribute();
        assertNotNull(it.next());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }
    
}
