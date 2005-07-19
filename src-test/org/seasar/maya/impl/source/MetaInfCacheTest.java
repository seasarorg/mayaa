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
package org.seasar.maya.impl.source;

import java.util.Iterator;

import junit.framework.TestCase;

import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MetaInfCacheTest extends TestCase {

    public MetaInfCacheTest(String name) {
        super(name);
    }
    
    public void testReadLinesFromSource() {
        SourceDescriptor source = new JavaSourceDescriptor(
                "Test.lst", MetaInfCacheTest.class);
        MetaInfCache cache = new MetaInfCache();
        Iterator it = cache.readLinesFromSource(source);
        assertEquals("aaa", it.next());
        assertEquals("bbb", it.next());
        assertEquals("#ddd", it.next());
        assertFalse(it.hasNext());
    }

}
