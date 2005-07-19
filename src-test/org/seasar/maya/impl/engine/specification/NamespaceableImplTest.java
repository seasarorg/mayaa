/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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

import org.seasar.maya.engine.specification.NodeNamespace;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NamespaceableImplTest extends TestCase {
    
    private NamespaceableImpl _nsable;
    
    protected void setUp() {
        _nsable = new NamespaceableImpl();
        _nsable.addNamespace("PREFIX_TEST1", "URI_TEST1");
        _nsable.addNamespace("PREFIX_TEST2", "URI_TEST2");
        _nsable.addNamespace("PREFIX_TEST3", "URI_TEST2");
    }

    public void testGetNamespace() {
        NodeNamespace ns = _nsable.getNamespace("PREFIX_TEST1");
        assertNotNull(ns);
        assertEquals("PREFIX_TEST1", ns.getPrefix());
        assertEquals("URI_TEST1", ns.getNamespaceURI());
    }
    
    public void testIterateNamespace() {
        Iterator it = _nsable.iterateNamespace();
        assertNotNull(it.next());
        assertNotNull(it.next());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }
    
    public void testIterateFilteredNamespace() {
        Iterator it1 = _nsable.iterateNamespace("URI_TEST1"); 
        assertNotNull(it1.next());
        assertFalse(it1.hasNext());
        
        Iterator it2 = _nsable.iterateNamespace("URI_TEST2");
        assertNotNull(it2.next());
        assertNotNull(it2.next());
        assertFalse(it2.hasNext());
    }
    
}
