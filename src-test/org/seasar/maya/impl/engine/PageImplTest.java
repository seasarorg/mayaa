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
package org.seasar.maya.impl.engine;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageImplTest extends TestCase {

    private EngineImpl _engine;
    private PageImpl _page;
    
    public PageImplTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        _engine = new EngineImpl();
        _page = new PageImpl(_engine, "hello", "html");
    }
     
    public void testGetName() {
        assertEquals("hello", _page.getPageName());
    }
    
    public void testGetExtension() {
        assertEquals("html", _page.getExtension());
    }
    
    public void testGetEngine() {
        assertEquals(_engine, _page.getEngine());
    }
    
}
